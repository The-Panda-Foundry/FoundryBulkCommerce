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

/** Version-pinned adapter for canonical, finite, unlinked item storage. Planning never creates wrappers or UUIDs. */
public final class SophisticatedSources {
    private static final int MAX_SLOTS = 256;
    private static final Set<String> STACK_UPGRADES = Set.of("stack_upgrade_tier_1", "stack_upgrade_tier_1_plus",
        "stack_upgrade_tier_2", "stack_upgrade_tier_3", "stack_upgrade_tier_4", "stack_upgrade_tier_5", "stack_upgrade_omega_tier");
    private SophisticatedSources() {}

    public record Collected(List<SaleSource> sources, int skipped) {}
    public static Collected collect(ServerPlayer player, ItemStack template) {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER)
            throw new IllegalStateException("Canonical Sophisticated storage requires the server thread group");
        var inventory = player.getInventory();
        List<UUID> identities = new ArrayList<>();
        // Include all carried identities, even unsupported containers; never count a UUID twice.
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            UUID uuid = inventory.getItem(i).get(ModCoreDataComponents.STORAGE_UUID);
            if (uuid != null) identities.add(uuid);
        }
        Set<UUID> duplicateIdentities = duplicates(identities);
        List<SaleSource> sources = new ArrayList<>();
        int skipped = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!(stack.getItem() instanceof ShulkerBoxItem)) continue;
            UUID uuid = stack.get(ModCoreDataComponents.STORAGE_UUID);
            if (stack.getCount() != 1 || stack.has(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)
                    || stack.has(DataComponents.BLOCK_ENTITY_DATA) || stack.has(DataComponents.CUSTOM_DATA)
                    || !BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("sophisticatedstorage")) {
                skipped++; continue;
            }
            if (uuid == null) continue; // New empty boxes have no canonical contents; do not allocate storage.
            if (duplicateIdentities.contains(uuid)) { skipped++; continue; }
            CompoundTag root = ItemContentsStorage.get().getStorageContents(uuid).map(CompoundTag::copy).orElse(null);
            if (root == null || !supported(player.registryAccess(), root)) { skipped++; continue; }
            CompoundTag storedInventory = root.getCompound("storageWrapper").getCompound("contents").getCompound("inventory");
            ListTag items = storedInventory.getList("Items", Tag.TAG_COMPOUND);
            long matching = 0;
            for (int n = 0; n < items.size(); n++) {
                ItemStack item = decode(player.registryAccess(), items.getCompound(n));
                if (ItemPolicy.saleable(item) && ItemStack.isSameItemSameComponents(item, template)) matching += item.getCount();
            }
            if (matching > 0) sources.add(new CanonicalSource(player, i, stack, uuid, root, matching, template));
        }
        return new Collected(sources, skipped);
    }

    static Set<UUID> duplicates(Iterable<UUID> identities) {
        Set<UUID> seen = new HashSet<>(), duplicates = new HashSet<>();
        for (UUID uuid : identities) if (!seen.add(uuid)) duplicates.add(uuid);
        return duplicates;
    }

    private static ItemStack decode(Provider registries, CompoundTag tag) {
        // The verified Core schema is ordinary id/components plus an unrestricted integer count.
        // Decode components with Minecraft's public codec at count=1, then restore the exact count.
        // This avoids initializing Core's unrelated client/container codecs during server-side planning.
        if (tag.contains("count") && !tag.contains("count", Tag.TAG_INT)) throw new IllegalStateException("Invalid stored count type");
        int count = tag.contains("count") ? tag.getInt("count") : 1;
        if (count <= 0) throw new IllegalStateException("Invalid stored count");
        CompoundTag single = tag.copy();
        single.putInt("count", 1);
        return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), single).getOrThrow().copyWithCount(count);
    }

    static boolean supported(Provider registries, CompoundTag root) {
        if (!root.contains("storageWrapper", Tag.TAG_COMPOUND)) return false;
        CompoundTag contents = root.getCompound("storageWrapper").getCompound("contents");
        if (!contents.contains("inventory", Tag.TAG_COMPOUND)) return false;
        CompoundTag inventory = contents.getCompound("inventory");
        int size = inventory.getInt("Size");
        if (size < 1 || size > MAX_SLOTS || !inventory.contains("Items", Tag.TAG_LIST)) return false;
        ListTag items = inventory.getList("Items", Tag.TAG_COMPOUND);
        if (items.size() != ((ListTag)inventory.get("Items")).size() || items.size() > size) return false;
        Set<Integer> slots = new HashSet<>();
        for (int n = 0; n < items.size(); n++) {
            CompoundTag itemTag = items.getCompound(n);
            if (!itemTag.contains("Slot", Tag.TAG_INT) || !itemTag.contains("id", Tag.TAG_STRING)) return false;
            int slot = itemTag.getInt("Slot");
            if (slot < 0 || slot >= size || !slots.add(slot) || decode(registries, itemTag).getCount() <= 0) return false;
        }
        if (contents.contains("partitioner")) {
            CompoundTag parts = contents.getCompound("partitioner");
            int[] base = parts.getIntArray("baseIndexes");
            ListTag names = parts.getList("inventoryPartNames", Tag.TAG_STRING);
            if (base.length != 1 || base[0] != 0 || names.size() != 1 || !names.getString(0).equals("default")) return false;
        }
        if (contents.contains("upgradeInventory")) {
            CompoundTag upgrades = contents.getCompound("upgradeInventory");
            if (!upgrades.contains("Items", Tag.TAG_LIST)) return false;
            ListTag upgradeItems = upgrades.getList("Items", Tag.TAG_COMPOUND);
            if (upgradeItems.size() != ((ListTag)upgrades.get("Items")).size() || upgradeItems.size() > 16) return false;
            for (int n = 0; n < upgradeItems.size(); n++) {
                ItemStack upgrade = decode(registries, upgradeItems.getCompound(n));
                var id = BuiltInRegistries.ITEM.getKey(upgrade.getItem());
                if (!(upgrade.getItem() instanceof StackUpgradeItem) || !id.getNamespace().equals("sophisticatedstorage")
                        || !STACK_UPGRADES.contains(id.getPath()) || upgrade.getCount() != 1) return false;
            }
        }
        return true;
    }

    static CompoundTag removeContents(Provider registries, CompoundTag before, ItemStack template, int amount) {
        CompoundTag after = before.copy();
        CompoundTag inv = after.getCompound("storageWrapper").getCompound("contents").getCompound("inventory");
        ListTag updated = new ListTag();
        int remaining = amount;
        for (Tag tag : inv.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag entry = ((CompoundTag)tag).copy();
            ItemStack item = decode(registries, entry);
            if (remaining > 0 && ItemPolicy.saleable(item) && ItemStack.isSameItemSameComponents(item, template)) {
                int removed = Math.min(remaining, item.getCount());
                remaining -= removed;
                if (removed == item.getCount()) continue;
                entry.putInt("count", item.getCount() - removed);
            }
            updated.add(entry);
        }
        if (remaining != 0 || amount <= 0) throw new IllegalStateException("Sophisticated plan underflow");
        inv.put("Items", updated);
        return after;
    }

    private record CanonicalSource(ServerPlayer player, int slot, ItemStack reference, UUID uuid,
            CompoundTag before, long available, ItemStack template) implements SaleSource {
        public Transaction.Edit remove(int amount) {
            CompoundTag after = removeContents(player.registryAccess(), before, template, amount);
            ItemStack outerSnapshot = reference.copy();
            return new Transaction.Edit() {
                private boolean outerUnchanged() { return ItemStack.matches(player.getInventory().getItem(slot), outerSnapshot); }
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
