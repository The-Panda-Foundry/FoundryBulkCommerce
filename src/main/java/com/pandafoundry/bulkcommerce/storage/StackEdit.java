package com.pandafoundry.bulkcommerce.storage;

import com.pandafoundry.bulkcommerce.transaction.Transaction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;

/** Whole-slot replacement avoids partial mutation of the immutable vanilla container component. */
public final class StackEdit implements Transaction.Edit {
    private final Inventory inventory;
    private final Provider registries;
    private final int slot;
    private final ItemStack originalReference;
    private final ItemStack before;
    private final ItemStack after;
    private boolean restored;
    public StackEdit(Inventory inventory, int slot, ItemStack before, ItemStack after, Provider registries) {
        this.inventory = inventory;
        this.registries = registries;
        this.slot = slot;
        this.originalReference = inventory.getItem(slot);
        this.before = before.copy();
        this.after = after.copy();
    }
    public boolean isBefore() { return (restored || inventory.getItem(slot) == originalReference) && ItemStack.matches(inventory.getItem(slot), before); }
    public boolean isAfter() { return ItemStack.matches(inventory.getItem(slot), after); }
    public void apply() {
        if (inventory.getItem(slot) != originalReference || !isBefore()) throw new IllegalStateException("Inventory source changed");
        inventory.setItem(slot, after.copy());
        inventory.setChanged();
    }
    public void restore() {
        inventory.setItem(slot, before.copy());
        restored = true;
        inventory.setChanged();
    }
    public String recoveryDetails() { return describe() + " BEFORE " + before.saveOptional(registries) + " AFTER " + after.saveOptional(registries); }
    public String describe() { return "inventory slot " + slot; }
}
