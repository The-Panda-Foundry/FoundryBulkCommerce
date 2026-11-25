package com.pandafoundry.bulkcommerce.storage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;
import io.wispforest.accessories.api.AccessoriesCapability;
/** Optional Accessories bridge: inspect identity components only, never backpack/container contents. */
final class AccessoriesIdentities {
    static void collect(ServerPlayer player,ScanBudget budget,Consumer<ItemStack> count){
        AccessoriesCapability.getOptionally(player).ifPresent(cap->{
            for(var handler:cap.getContainers().values())
                for(var slots:java.util.List.of(handler.getAccessories(),handler.getCosmeticAccessories()))
                    for(int slot=0;slot<slots.getContainerSize();slot++){budget.slot();count.accept(slots.getItem(slot));}
        });
    }
}
