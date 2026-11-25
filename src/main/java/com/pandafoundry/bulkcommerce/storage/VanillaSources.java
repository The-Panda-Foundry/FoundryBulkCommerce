package com.pandafoundry.bulkcommerce.storage;

import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class VanillaSources {
    private VanillaSources() {}
    public static List<SaleSource> collect(ServerPlayer player, ItemStack template, boolean shulkers) {
        return collect(player, template, shulkers, new ScanBudget(32768, 4194304));
    }
    public static List<SaleSource> collect(ServerPlayer player, ItemStack template, boolean shulkers, ScanBudget budget) {
        return collect(player.getInventory(), template, shulkers, player.registryAccess(), budget);
    }
    public static List<SaleSource> collect(net.minecraft.world.entity.player.Inventory inventory, ItemStack template, boolean shulkers, Provider registries) {
        return collect(inventory, template, shulkers, registries, new ScanBudget(32768, 4194304));
    }
    private static List<SaleSource> collect(net.minecraft.world.entity.player.Inventory inventory, ItemStack template, boolean shulkers, Provider registries, ScanBudget budget) {
        List<SaleSource> sources = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            budget.slot();
            ItemStack stack = inventory.getItem(slot);
            final int index = slot;
            if (slot < inventory.items.size() && ItemStack.isSameItemSameComponents(stack, template)) {
                budget.nbt((net.minecraft.nbt.CompoundTag)stack.saveOptional(registries));
                ItemStack before = stack.copy();
                sources.add(new SaleSource() {
                    public long available() { return before.getCount(); }
                    public Transaction.Edit remove(int amount) {
                        return new StackEdit(inventory, index, before, remainder(before, amount), registries);
                    }
                });
            } else if (shulkers && stack.getCount() == 1
                    && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock
                    && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft")
                    && !stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
                budget.nbt((net.minecraft.nbt.CompoundTag)stack.saveOptional(registries));
                ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                if (contents.getSlots() > 27) throw new IllegalStateException("Invalid vanilla shulker size");
                NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
                for (int inspected = 0; inspected < 27; inspected++) budget.slot();
                contents.copyInto(items);
                long count = items.stream().filter(i -> ItemPolicy.saleable(i) && ItemStack.isSameItemSameComponents(i, template))
                    .mapToLong(ItemStack::getCount).sum();
                if (count == 0) continue;
                ItemStack before = stack.copy();
                sources.add(new SaleSource() {
                    public long available() { return count; }
                    public Transaction.Edit remove(int amount) {
                        NonNullList<ItemStack> updated = NonNullList.withSize(27, ItemStack.EMPTY);
                        contents.copyInto(updated);
                        int remaining = amount;
                        for (int i = 0; i < updated.size() && remaining > 0; i++) {
                            ItemStack item = updated.get(i);
                            if (ItemPolicy.saleable(item) && ItemStack.isSameItemSameComponents(item, template)) {
                                int taken = Math.min(remaining, item.getCount());
                                updated.set(i, remainder(item, taken));
                                remaining -= taken;
                            }
                        }
                        if (remaining != 0) throw new IllegalStateException("Vanilla plan underflow");
                        ItemStack after = before.copy();
                        after.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(updated));
                        return new StackEdit(inventory, index, before, after, registries);
                    }
                });
            }
        }
        return sources;
    }
    private static ItemStack remainder(ItemStack stack, int amount) {
        if (amount <= 0 || amount > stack.getCount()) throw new IllegalArgumentException("Invalid removal");
        return stack.getCount() == amount ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - amount);
    }
}
