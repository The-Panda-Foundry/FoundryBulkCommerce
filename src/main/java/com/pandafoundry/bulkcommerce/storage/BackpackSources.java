package com.pandafoundry.bulkcommerce.storage;

import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModDataComponents;
import static com.pandafoundry.bulkcommerce.storage.SaleRejection.Reason.*;

/** Pinned canonical BackpackStorage schema; hotbar-only, finite and unlinked. */
public final class BackpackSources {
    private BackpackSources() {}
    public static StorageScan collect(ServerPlayer player, ItemStack template) {
        return collect(player, template, new ScanBudget(32768, 4194304));
    }
    public static StorageScan collect(ServerPlayer player, ItemStack template, ScanBudget budget) {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER)
            throw new IllegalStateException("Canonical Sophisticated storage requires the server thread group");
        var inventory = player.getInventory();
        var identities = StorageIdentities.collect(player,budget);
        List<SaleSource> sources = new ArrayList<>();
        List<StorageScan.Skipped> rejected = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            budget.slot();
            ItemStack stack = inventory.getItem(i);
            UUID uuid = stack.get(ModCoreDataComponents.STORAGE_UUID);
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!(stack.getItem() instanceof BackpackItem)) {
                continue;
            }
            try {
                if (stack.getCount() != 1 || !id.getNamespace().equals("sophisticatedbackpacks")
                        || stack.has(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)
                        || stack.has(DataComponents.BLOCK_ENTITY_DATA) || stack.has(DataComponents.CUSTOM_DATA)
                        || stack.has(ModDataComponents.LOOT_TABLE.get()) || stack.has(ModDataComponents.TEMPLATE_NAME.get()))
                    throw new SaleRejection(UNSUPPORTED_STORAGE, "Linked, legacy or unverified storage item");
                if (uuid == null) continue; // Empty new storage: no migration, UUID or wrapper allocation.
                if (!StorageIdentities.unique(identities,"backpack",uuid)) throw new SaleRejection(DUPLICATE_UUID, "Duplicate carried storage identity");
                CompoundTag root = BackpackStorage.get().getBackpackContents(uuid).orElseThrow(() -> new SaleRejection(INVALID_DATA, "Missing canonical contents"));
                budget.nbt(root);
                // The getNumber... APIs mutate outer components. Use the non-mutating defaults plus modern saved components.
                int capacity = Math.max(((BackpackItem)stack.getItem()).getNumberOfSlots(), stack.getOrDefault(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, 0));
                int upgradeSlots = Math.max(((BackpackItem)stack.getItem()).getNumberOfUpgradeSlots(), stack.getOrDefault(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, 0));
                CanonicalContents.Parsed parsed = CanonicalContents.parse(player.registryAccess(), root, template, budget, capacity, upgradeSlots,"sophisticatedbackpacks",stack.getOrDefault(ModDataComponents.COLUMNS_TAKEN.get(),0));
                if (parsed.matching() > 0) sources.add(new CanonicalSource(player, i, stack, uuid, root.copy(), parsed.matching(), parsed.matches()));
            } catch (SaleRejection failure) {
                if (failure.reason() == SCAN_LIMIT || failure.reason() == SNAPSHOT_LIMIT) throw failure;
                rejected.add(new StorageScan.Skipped(i, id.toString(), failure.reason()));
            }
        }
        return new StorageScan(List.copyOf(sources), List.copyOf(rejected));
    }

    private record CanonicalSource(ServerPlayer player, int slot, ItemStack reference, UUID uuid,
            CompoundTag before, long available, List<CanonicalContents.Match> matches) implements SaleSource {
        public Transaction.Edit remove(int amount) {
            CompoundTag after = CanonicalContents.removeMatching(before, matches, amount,false);
            ItemStack outerSnapshot = reference.copy();
            return new Transaction.Edit() {
                private boolean outerUnchanged() {
                    if (player.getInventory().getItem(slot) != reference || !ItemStack.matches(reference, outerSnapshot)) return false;
                    // A carried alias may have appeared while the durable intent was being written.
                    return slot < 9 && StorageIdentities.unique(StorageIdentities.collect(player,new ScanBudget(com.pandafoundry.bulkcommerce.BulkConfig.SCAN_LIMIT.get(),1)),"backpack",uuid);
                }
                private boolean matches(CompoundTag expected) {
                    return outerUnchanged() && BackpackStorage.get().getBackpackContents(uuid).filter(expected::equals).isPresent();
                }
                public boolean isBefore() { return matches(before); }
                public boolean isAfter() { return matches(after); }
                private void write(CompoundTag value) {
                    BackpackStorage.get().setBackpackContents(uuid, value.copy());
                    // Reset only an existing wrapper; native refresh callbacks remain observable in full-root verification.
                    // No added cache entries, global invalidation or extraction/automation hooks.
                    BackpackWrapper.fromExistingData(reference).ifPresent(w -> w.onContentsNbtUpdated());
                    player.getInventory().setChanged();
                }
                public void apply() {
                    if (player.getInventory().getItem(slot) != reference || !isBefore()) throw new IllegalStateException("Storage identity changed");
                    write(after);
                }
                public void restore() { write(before); }
                public String recoveryDetails() { return describe() + " BEFORE " + before + " AFTER " + after; }
                public String describe() { return "Sophisticated backpack slot " + slot + " UUID " + uuid; }
            };
        }
    }
}
