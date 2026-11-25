package com.pandafoundry.bulkcommerce.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

public final class ItemPolicy {
    private ItemPolicy() {}
    /** Conservative commodity scope: container/packed block items are never sale candidates. No capability queries. */
    public static boolean saleable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id.getPath().contains("backpack")) return false;
        // UUID-backed storage is excluded without loading optional Sophisticated classes.
        var storageUuid = BuiltInRegistries.DATA_COMPONENT_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sophisticatedcore", "storage_uuid"));
        if (storageUuid != null && stack.has(storageUuid)) return false;
        return !(stack.getItem() instanceof BundleItem)
            && stack.canFitInsideContainerItems()
            && !stack.has(DataComponents.CONTAINER)
            && !stack.has(DataComponents.BUNDLE_CONTENTS)
            && !stack.has(DataComponents.BLOCK_ENTITY_DATA);
    }
}
