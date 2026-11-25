package com.pandafoundry.bulkcommerce.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import static com.pandafoundry.bulkcommerce.storage.SaleRejection.Reason.*;


/** Shared finite inventory schema used by the two separate canonical repositories. */
final class CanonicalContents {
    record Match(int entry, int count) {}
    record Parsed(long matching, List<Match> matches) {}
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
    static Parsed parse(Provider registries, CompoundTag contents, ItemStack template, ScanBudget budget, int capacity, int upgradeCapacity, String upgradeNamespace) {
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
                        || !BuiltInRegistries.ITEM.getKey(upgrade.getItem()).getNamespace().equals(upgradeNamespace) || upgrade.getCount() != 1)
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
    static CompoundTag removeMatching(CompoundTag before, List<Match> matches, int amount, boolean shulker) {
        if (amount <= 0) throw reject(VALIDATION_FAILURE, "Invalid removal quantity");
        CompoundTag after = before.copy();
        CompoundTag inv = (shulker ? after.getCompound("storageWrapper").getCompound("contents") : after).getCompound("inventory");
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

}
