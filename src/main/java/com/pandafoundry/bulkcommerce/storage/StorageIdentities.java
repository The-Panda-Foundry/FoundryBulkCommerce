package com.pandafoundry.bulkcommerce.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;

/** No content access: physical inventory/equipment occurrences, keyed by canonical repository. */
public final class StorageIdentities {
    public record Key(String repository,UUID uuid) {}
    private StorageIdentities() {}
    public static Map<Key,Integer> collect(ServerPlayer player,ScanBudget budget) {
        Map<Key,Integer> result=new HashMap<>();
        java.util.function.Consumer<ItemStack> count=stack->{
            UUID id=stack.get(ModCoreDataComponents.STORAGE_UUID);if(id==null)return;
            String ns=BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
            if(!ns.equals("sophisticatedbackpacks"))result.merge(new Key("storage",id),1,Integer::sum);
            if(!ns.equals("sophisticatedstorage"))result.merge(new Key("backpack",id),1,Integer::sum);
        };
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++){budget.slot();count.accept(player.getInventory().getItem(slot));}
        if(player.containerMenu!=null){budget.slot();count.accept(player.containerMenu.getCarried());}
        if(ModList.get().isLoaded("curios"))CuriosIdentities.collect(player,budget,count);
        if(ModList.get().isLoaded("accessories"))AccessoriesIdentities.collect(player,budget,count);
        return result;
    }
    public static boolean unique(Map<Key,Integer> ids,String repo,UUID uuid){return ids.getOrDefault(new Key(repo,uuid),0)==1;}
}
