package com.pandafoundry.bulkcommerce.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;

public final class ItemPolicy {
    private ItemPolicy() {}
    /** Conservative commodity scope: container/packed block items are never sale candidates. No capability queries. */
    public static boolean saleable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id.getNamespace().startsWith("sophisticated") || id.getPath().contains("backpack")) return false;
        return !(stack.getItem() instanceof BundleItem)
            && stack.canFitInsideContainerItems()
            && !stack.has(DataComponents.CONTAINER)
            && !stack.has(DataComponents.BUNDLE_CONTENTS)
            && !stack.has(DataComponents.BLOCK_ENTITY_DATA)
            && !(stack.getItem() instanceof BlockItem block && block.getBlock() instanceof EntityBlock);
    }
}
