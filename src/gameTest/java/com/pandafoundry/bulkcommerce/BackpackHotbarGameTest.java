package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.storage.*;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.*;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;

@GameTestHolder("foundrybulkcommerce")
@PrefixGameTestTemplate(false)
public final class BackpackHotbarGameTest {
    static ItemStack backpack(Item item,int quantity){
        var box=new ItemStack(item);var wrapper=BackpackWrapper.fromStack(box);
        wrapper.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,quantity));wrapper.getInventoryHandler().saveInventory();return box;
    }
    private static Transaction.Wallet wallet(){return new Transaction.Wallet(){int paid;public boolean isBefore(){return paid==0;}public void credit(int p){paid=p;}public boolean isCredited(int p){return paid==p;}public void restore(){paid=0;}};}
    @GameTest(template="empty",batch="backpacks-matrix",timeoutTicks=100000)
    public static void backpackMatrix(GameTestHelper h){
        if(!ModList.get().isLoaded("sophisticatedbackpacks")){FoundryBulkCommerce.LOGGER.info("[BC020] Backpacks absent; optional class path not invoked");h.succeed();return;}
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"BackpackMatrix"));
        var tiers=BuiltInRegistries.ITEM.stream().filter(i->i instanceof BackpackItem&&BuiltInRegistries.ITEM.getKey(i).getNamespace().equals("sophisticatedbackpacks")).toList();
        var upgrades=BuiltInRegistries.ITEM.stream().filter(i->i.getClass()==StackUpgradeItem.class&&BuiltInRegistries.ITEM.getKey(i).getNamespace().equals("sophisticatedbackpacks")).toList();
        for(var upgrade:upgrades)FoundryBulkCommerce.LOGGER.info("[BC020 modifier] item={} multiplier={}",BuiltInRegistries.ITEM.getKey(upgrade),((StackUpgradeItem)upgrade).getStackSizeMultiplier());
        int checks=0;h.assertTrue(tiers.size()==6,"Every backpack tier discovered");
        for(var item:tiers){
            player.getInventory().clearContent();var box=backpack(item,64);player.getInventory().setItem(8,box);
            var wrapper=BackpackWrapper.fromStack(box);
            wrapper.getInventoryHandler().setStackInSlot(0,ItemStack.EMPTY);wrapper.getInventoryHandler().saveInventory();
            h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().isEmpty(),"Empty canonical backpack is safe");checks++;
            wrapper.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,64));
            int slots=wrapper.getInventoryHandler().getSlots();
            for(int slot=1;slot<slots;slot++)wrapper.getInventoryHandler().setStackInSlot(slot,new ItemStack(Items.WHEAT,64));wrapper.getInventoryHandler().saveInventory();
            var uuid=box.get(ModCoreDataComponents.STORAGE_UUID);var before=BackpackStorage.get().getBackpackContents(uuid).orElseThrow().copy();
            var scan=BackpackSources.collect(player,new ItemStack(Items.WHEAT),new ScanBudget(4096,1048576));
            h.assertTrue(scan.sources().size()==1&&scan.sources().getFirst().available()==slots*64L,"Full registered backpack tier counted from nonselected hotbar slot");checks++;
            var edit=scan.sources().getFirst().remove(64);h.assertTrue(Transaction.commit(List.of(edit),wallet(),1).successful()&&wrapper.getInventoryHandler().getStackInSlot(0).isEmpty(),"Actual backpack removal and wrapper invalidation");checks++;
            edit.restore();h.assertTrue(edit.isBefore()&&BackpackStorage.get().getBackpackContents(uuid).orElseThrow().equals(before),"Exact root rollback");checks++;
            player.getInventory().setItem(20,box);player.getInventory().setItem(8,ItemStack.EMPTY);
            var protectedBudget=new ScanBudget(4096,1);h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT),protectedBudget).sources().isEmpty()&&protectedBudget.bytes()==0,"Protected main inventory not decoded");checks++;
            player.getInventory().setItem(8,box.copy());
            h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT)).rejections().getFirst().reason()==SaleRejection.Reason.DUPLICATE_UUID,"Protected inventory alias blocks eligible backpack");checks++;
            player.getInventory().setItem(20,ItemStack.EMPTY);player.getInventory().setItem(8,box);
            wrapper=BackpackWrapper.fromStack(box);for(int slot=0;slot<slots;slot++)wrapper.getInventoryHandler().setStackInSlot(slot,ItemStack.EMPTY);
            for(var upgrade:upgrades){
                wrapper=BackpackWrapper.fromStack(box);
                wrapper.getUpgradeHandler().setStackInSlot(0,new ItemStack(upgrade));
                int amount=(int)Math.min(Integer.MAX_VALUE,((StackUpgradeItem)upgrade).getStackSizeMultiplier()*64);
                wrapper.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,amount));wrapper.getInventoryHandler().setStackInSlot(1,new ItemStack(Items.DIAMOND));wrapper.getInventoryHandler().saveInventory();
                var original=BackpackStorage.get().getBackpackContents(uuid).orElseThrow().copy();
                var source=BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().getFirst();
                h.assertTrue(source.available()==amount,"Every stack upgrade including Omega counted");checks++;
                var part=source.remove(Math.min(2304,amount));h.assertTrue(Transaction.commit(List.of(part),wallet(),1).successful(),"Upgraded partial extraction verified");checks++;
                part.restore();h.assertTrue(BackpackStorage.get().getBackpackContents(uuid).orElseThrow().equals(original),"Upgrades/settings/unrelated root bytes preserved");checks++;
            }
            wrapper=BackpackWrapper.fromStack(box);
            if(wrapper.getUpgradeHandler().getSlots()>1){
                var inv=wrapper.getInventoryHandler();inv.setStackInSlot(0,ItemStack.EMPTY);
                for(int u=0;u<wrapper.getUpgradeHandler().getSlots();u++)wrapper.getUpgradeHandler().setStackInSlot(u,new ItemStack(upgrades.get(u%upgrades.size())));
                inv.setStackInSlot(0,new ItemStack(Items.WHEAT,64));inv.saveInventory();
                h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().getFirst().available()==64,"Mixed passive modifiers supported");checks++;
                for(int u=0;u<wrapper.getUpgradeHandler().getSlots();u++)wrapper.getUpgradeHandler().setStackInSlot(u,new ItemStack(upgrades.getLast()));
                inv.setStackInSlot(0,new ItemStack(Items.WHEAT,Integer.MAX_VALUE));inv.saveInventory();
                h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().getFirst().available()==Integer.MAX_VALUE,"Repeated Omega modifiers supported");checks++;
            }
            wrapper=BackpackWrapper.fromStack(box);
            var safeRoot=BackpackStorage.get().getBackpackContents(uuid).orElseThrow().copy();
            wrapper.getUpgradeHandler().setStackInSlot(0,new ItemStack(BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("sophisticatedbackpacks:inception_upgrade"))));
            h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT)).rejections().getFirst().reason()==SaleRejection.Reason.UNSUPPORTED_UPGRADE,"Nested inventory upgrade remains excluded");checks++;
            BackpackStorage.get().setBackpackContents(uuid,safeRoot);wrapper.onContentsNbtUpdated();
            // Movement and equipped/protected locations invalidate a previously prepared source.
            var planned=BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().getFirst().remove(1);
            player.getInventory().setItem(40,box);player.getInventory().setItem(8,ItemStack.EMPTY);
            h.assertTrue(!planned.isBefore()&&BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().isEmpty(),"Offhand protected; moved plan rejected");checks++;
            player.getInventory().setItem(40,ItemStack.EMPTY);player.getInventory().armor.set(2,box);
            h.assertTrue(BackpackSources.collect(player,new ItemStack(Items.WHEAT)).sources().isEmpty(),"Equipped armor backpack protected");checks++;
            player.getInventory().armor.set(2,ItemStack.EMPTY);
            FoundryBulkCommerce.LOGGER.info("[BC020 matrix] tier={} slots={} upgradeSlots={} modifiers={}",BuiltInRegistries.ITEM.getKey(item),slots,wrapper.getUpgradeHandler().getSlots(),upgrades.size());
            BackpackStorage.get().removeBackpackContents(uuid);
        }
        player.getInventory().clearContent();FoundryBulkCommerce.LOGGER.info("[BC020 matrix] {} assertions passed; tiers={} modifiers={}",checks,tiers.size(),upgrades.size());h.succeed();
    }
}
