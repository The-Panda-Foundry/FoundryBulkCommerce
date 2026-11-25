package com.pandafoundry.bulkcommerce.storage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;
import top.theillusivec4.curios.api.CuriosApi;
/** Optional bridge: inspect active and cosmetic slots, without querying any container's contents. */
final class CuriosIdentities {
    static void collect(ServerPlayer player,ScanBudget budget,Consumer<ItemStack> count){
        CuriosApi.getCuriosInventory(player).ifPresent(inventory->{
            for(var handler:inventory.getCurios().values()){
                for(var slots:java.util.List.of(handler.getStacks(),handler.getCosmeticStacks()))
                    for(int slot=0;slot<slots.getSlots();slot++){budget.slot();count.accept(slots.getStackInSlot(slot));}
            }
        });
    }
}
