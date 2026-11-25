package com.pandafoundry.bulkcommerce.storage;

import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.StorageWrapperRepository;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import static com.pandafoundry.bulkcommerce.storage.SaleRejection.Reason.*;

/** Pinned canonical schema; eligibility/capacity comes from the actual registered shulker APIs. */
public final class SophisticatedSources {
    private SophisticatedSources() {}
    public record Skipped(int slot, String item, SaleRejection.Reason reason) {}
    public record Collected(List<SaleSource> sources, List<Skipped> rejections) {
        public int skipped() { return rejections.size(); }
    }
    private record Match(int entry, int count) {}
    private record Parsed(long matching, List<Match> matches) {}

    public static Collected collect(ServerPlayer player, ItemStack template) {
        return collect(player, template, new ScanBudget(32768, 4194304));
    }
    public static Collected collect(ServerPlayer player, ItemStack template, ScanBudget budget) {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER)
            throw new IllegalStateException("Canonical Sophisticated storage requires the server thread group");
        var inventory = player.getInventory();
        List<UUID> identities = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            budget.slot();
            UUID uuid = inventory.getItem(i).get(ModCoreDataComponents.STORAGE_UUID);
            if (uuid != null) identities.add(uuid);
        }
        Set<UUID> duplicateIdentities = duplicates(identities);
        List<SaleSource> sources = new ArrayList<>();
        List<Skipped> rejected = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            budget.slot();
            ItemStack stack = inventory.getItem(i);
            UUID uuid = stack.get(ModCoreDataComponents.STORAGE_UUID);
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!(stack.getItem() instanceof ShulkerBoxItem)) {
                if (uuid != null) rejected.add(new Skipped(i, id.toString(), UNSUPPORTED_STORAGE));
                continue;
            }
            try {
                if (stack.getCount() != 1 || !id.getNamespace().equals("sophisticatedstorage")
                        || stack.has(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)
                        || stack.has(DataComponents.BLOCK_ENTITY_DATA) || stack.has(DataComponents.CUSTOM_DATA))
                    throw reject(UNSUPPORTED_STORAGE, "Linked, legacy or unverified storage item");
                if (uuid == null) continue; // Empty new storage: no migration, UUID or wrapper allocation.
                if (duplicateIdentities.contains(uuid)) throw reject(DUPLICATE_UUID, "Duplicate carried storage identity");
                CompoundTag root = ItemContentsStorage.get().getStorageContents(uuid).orElseThrow(() -> reject(INVALID_DATA, "Missing canonical contents"));
                budget.nbt(root);
                // The getNumber... APIs mutate outer components. Use the non-mutating defaults plus modern saved components.
                int capacity = Math.max(StorageBlockItem.getDefaultNumberOfInventorySlots(stack), stack.getOrDefault(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, 0));
                int upgradeSlots = Math.max(StorageBlockItem.getDefaultNumberOfUpgradeSlots(stack), stack.getOrDefault(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, 0));
                Parsed parsed = parse(player.registryAccess(), root, template, budget, capacity, upgradeSlots);
                if (parsed.matching() > 0) sources.add(new CanonicalSource(player, i, stack, uuid, root.copy(), parsed.matching(), parsed.matches()));
            } catch (SaleRejection failure) {
                if (failure.reason() == SCAN_LIMIT || failure.reason() == SNAPSHOT_LIMIT) throw failure;
                rejected.add(new Skipped(i, id.toString(), failure.reason()));
            }
        }
        return new Collected(List.copyOf(sources), List.copyOf(rejected));
    }

    static Set<UUID> duplicates(Iterable<UUID> identities) {
        Set<UUID> seen = new HashSet<>(), duplicates = new HashSet<>();
        for (UUID uuid : identities) if (!seen.add(uuid)) duplicates.add(uuid);
        return duplicates;
    }
    private static SaleRejection reject(SaleRejection.Reason reason, String explanation) { return new SaleRejection(reason, explanation); }
    private static CompoundTag compound(CompoundTag parent, String name) {
        if (!parent.contains(name, Tag.TAG_COMPOUND)) throw reject(INVALID_DATA, "Invalid " + name + " compound");
        return parent.getCompound(name);
    }
    private static ListTag entries(CompoundTag inventory) {
        if (!inventory.contains("Items", Tag.TAG_LIST)) throw reject(INVALID_DATA, "Missing inventory list");
        var raw = (ListTag)inventory.get("Items");
        if (!raw.isEmpty() && raw.getElementType() != Tag.TAG_COMPOUND) throw reject(INVALID_DATA, "Invalid inventory entry type");
        return raw;
    }
    private static ItemStack decode(Provider registries, CompoundTag tag) {
        if (!tag.contains("id", Tag.TAG_STRING) || (tag.contains("count") && !tag.contains("count", Tag.TAG_INT)))
            throw reject(INVALID_DATA, "Invalid stored item fields");
        int count = tag.contains("count") ? tag.getInt("count") : 1;
        if (count <= 0) throw reject(INVALID_DATA, "Invalid stored count");
        CompoundTag single = tag.copy(); single.putInt("count", 1);
        try { return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), single).getOrThrow().copyWithCount(count); }
        catch (RuntimeException malformed) { throw reject(INVALID_DATA, "Stored item cannot be decoded"); }
    }
    private static Parsed parse(Provider registries, CompoundTag root, ItemStack template, ScanBudget budget, int capacity, int upgradeCapacity) {
        CompoundTag contents = compound(compound(root, "storageWrapper"), "contents");
        CompoundTag inventory = compound(contents, "inventory");
        if (!inventory.contains("Size", Tag.TAG_INT)) throw reject(INVALID_DATA, "Invalid inventory size type");
        int size = inventory.getInt("Size");
        if (size <= 0 || size > capacity) throw reject(INVALID_DATA, "Canonical size exceeds storage API capacity");
        budget.container(size);
        ListTag items = entries(inventory);
        if (items.size() > size) throw reject(INVALID_DATA, "Too many serialized entries");
        if (contents.contains("partitioner")) {
            CompoundTag parts = compound(contents, "partitioner");
            int[] base = parts.getIntArray("baseIndexes");
            ListTag names = parts.getList("inventoryPartNames", Tag.TAG_STRING);
            if (base.length != 1 || base[0] != 0 || names.size() != 1 || !names.getString(0).equals("default"))
                throw reject(UNSUPPORTED_STORAGE, "Non-default or infinite partition");
        }
        if (contents.contains("upgradeInventory")) {
            CompoundTag upgrades = compound(contents, "upgradeInventory");
            ListTag upgradeItems = entries(upgrades);
            if (!upgrades.contains("Size", Tag.TAG_INT) || upgrades.getInt("Size") < 0 || upgrades.getInt("Size") > upgradeCapacity)
                throw reject(INVALID_DATA, "Invalid upgrade inventory size");
            if (upgradeItems.size() > upgrades.getInt("Size")) throw reject(INVALID_DATA, "Too many upgrades");
            Set<Integer> upgradeSlots = new HashSet<>();
            for (int n = 0; n < upgradeItems.size(); n++) {
                budget.slot();
                CompoundTag tag = upgradeItems.getCompound(n);
                int slot = tag.getInt("Slot");
                if (!tag.contains("Slot", Tag.TAG_INT) || slot < 0 || slot >= upgrades.getInt("Size") || !upgradeSlots.add(slot))
                    throw reject(INVALID_DATA, "Invalid upgrade slot");
                ItemStack upgrade = decode(registries, tag);
                // Exact class is passive multiplier-only in the pinned Core JAR; subclass/other upgrades may have hooks.
                if (upgrade.getItem().getClass() != StackUpgradeItem.class
                        || !BuiltInRegistries.ITEM.getKey(upgrade.getItem()).getNamespace().equals("sophisticatedstorage") || upgrade.getCount() != 1)
                    throw reject(UNSUPPORTED_UPGRADE, "Only registered passive stack upgrades supported");
                double multiplier = ((StackUpgradeItem)upgrade.getItem()).getStackSizeMultiplier();
                if (!Double.isFinite(multiplier) || multiplier <= 0) throw reject(UNSUPPORTED_UPGRADE, "Invalid stack multiplier");
            }
        }
        Set<Integer> slots = new HashSet<>();
        List<Match> matches = new ArrayList<>();
        long matching = 0;
        for (int n = 0; n < items.size(); n++) {
            budget.slot();
            CompoundTag tag = items.getCompound(n);
            int slot = tag.getInt("Slot");
            if (!tag.contains("Slot", Tag.TAG_INT) || slot < 0 || slot >= size || !slots.add(slot)) throw reject(INVALID_DATA, "Invalid or duplicate inventory slot");
            ItemStack item = decode(registries, tag);
            if (ItemPolicy.saleable(item) && ItemStack.isSameItemSameComponents(item, template)) {
                matching = Math.addExact(matching, (long)item.getCount());
                matches.add(new Match(n, item.getCount()));
            }
        }
        return new Parsed(matching, List.copyOf(matches));
    }
    // Isolated schema tests use explicit capacity bounds without allocating live wrappers.
    static boolean supported(Provider registries, CompoundTag root, int capacity, int upgradeCapacity) {
        try { parse(registries, root, ItemStack.EMPTY, new ScanBudget(32768, 4194304), capacity, upgradeCapacity); return true; }
        catch (SaleRejection invalid) { return false; }
    }
    static CompoundTag removeContents(Provider registries, CompoundTag before, ItemStack template, int amount, int capacity, int upgradeCapacity) {
        Parsed parsed = parse(registries, before, template, new ScanBudget(32768, 4194304), capacity, upgradeCapacity);
        return removeMatching(before, parsed.matches(), amount);
    }
    private static CompoundTag removeMatching(CompoundTag before, List<Match> matches, int amount) {
        if (amount <= 0) throw reject(VALIDATION_FAILURE, "Invalid removal quantity");
        CompoundTag after = before.copy();
        CompoundTag inv = after.getCompound("storageWrapper").getCompound("contents").getCompound("inventory");
        ListTag items = inv.getList("Items", Tag.TAG_COMPOUND), updated = new ListTag();
        int remaining = amount, matchIndex = 0;
        for (int n = 0; n < items.size(); n++) {
            CompoundTag entry = items.getCompound(n);
            if (matchIndex < matches.size() && matches.get(matchIndex).entry() == n) {
                int count = matches.get(matchIndex++).count();
                int removed = Math.min(remaining, count); remaining -= removed;
                if (removed == count) continue;
                if (removed > 0) entry.putInt("count", count - removed);
            }
            updated.add(entry);
        }
        if (remaining != 0) throw reject(VALIDATION_FAILURE, "Sophisticated plan underflow");
        inv.put("Items", updated);
        return after;
    }

    private record CanonicalSource(ServerPlayer player, int slot, ItemStack reference, UUID uuid,
            CompoundTag before, long available, List<Match> matches) implements SaleSource {
        public Transaction.Edit remove(int amount) {
            CompoundTag after = removeMatching(before, matches, amount);
            ItemStack outerSnapshot = reference.copy();
            return new Transaction.Edit() {
                private boolean outerUnchanged() {
                    if (player.getInventory().getItem(slot) != reference || !ItemStack.matches(reference, outerSnapshot)) return false;
                    // A carried alias may have appeared while the durable intent was being written.
                    int occurrences = 0;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++)
                        if (uuid.equals(player.getInventory().getItem(i).get(ModCoreDataComponents.STORAGE_UUID)) && ++occurrences > 1)
                            return false;
                    return occurrences == 1;
                }
                private boolean matches(CompoundTag expected) {
                    return outerUnchanged() && ItemContentsStorage.get().getStorageContents(uuid).filter(expected::equals).isPresent();
                }
                public boolean isBefore() { return matches(before); }
                public boolean isAfter() { return matches(after); }
                private void write(CompoundTag value) {
                    ItemContentsStorage.get().setStorageContents(uuid, value.copy());
                    // Reset only an existing wrapper. No added cache entries, global invalidations or upgrade hooks.
                    StorageWrapperRepository.getExistingStorageWrapper(reference, StackStorageWrapper.class)
                        .ifPresent(StackStorageWrapper::onContentsNbtUpdated);
                    player.getInventory().setChanged();
                }
                public void apply() {
                    if (player.getInventory().getItem(slot) != reference || !isBefore()) throw new IllegalStateException("Storage identity changed");
                    write(after);
                }
                public void restore() { write(before); }
                public String recoveryDetails() { return describe() + " BEFORE " + before + " AFTER " + after; }
                public String describe() { return "Sophisticated shulker slot " + slot + " UUID " + uuid; }
            };
        }
    }
}
