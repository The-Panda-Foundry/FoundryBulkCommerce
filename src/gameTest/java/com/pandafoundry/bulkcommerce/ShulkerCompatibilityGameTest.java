package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.storage.*;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

/** Actual installed registry/API coverage; no test classes enter the addon JAR. */
@GameTestHolder("foundrybulkcommerce")
@PrefixGameTestTemplate(false)
public final class ShulkerCompatibilityGameTest {
    static List<Item> tiers() { return BuiltInRegistries.ITEM.stream().filter(i -> i instanceof ShulkerBoxItem && BuiltInRegistries.ITEM.getKey(i).getNamespace().equals("sophisticatedstorage")).toList(); }
    static List<Item> upgrades() { return BuiltInRegistries.ITEM.stream().filter(i -> i instanceof StackUpgradeItem && BuiltInRegistries.ITEM.getKey(i).getNamespace().equals("sophisticatedstorage")).toList(); }
    static ItemStack box(Item item) { var box=new ItemStack(item);box.set(ModCoreDataComponents.STORAGE_UUID,UUID.randomUUID());return box; }
    static void forget(ItemStack box) { UUID id=box.get(ModCoreDataComponents.STORAGE_UUID);if(id!=null)ItemContentsStorage.get().removeStorageContents(id); }
    static CompoundTag canonical(ItemStack box) { return ItemContentsStorage.get().getStorageContents(box.get(ModCoreDataComponents.STORAGE_UUID)).orElseThrow(); }
    private static CompoundTag contents(CompoundTag root) { return root.getCompound("storageWrapper").getCompound("contents"); }
    static long wheat(ItemStack box) { long sum=0;for(var t:contents(canonical(box)).getCompound("inventory").getList("Items",10)) {var e=(CompoundTag)t;if(e.getString("id").equals("minecraft:wheat"))sum+=e.getInt("count");}return sum; }
    private static Transaction.Wallet wallet() { return new Transaction.Wallet() {
        int value;
        public boolean isBefore(){return value==0;}
        public void credit(int amount){value+=amount;}
        public boolean isCredited(int amount){return value==amount;}
        public void restore(){value=0;}
    }; }
    private static void check(GameTestHelper h,boolean value,String message) { checks++;h.assertTrue(value,message); }
    private static int checks;

    @GameTest(template="empty", batch="bc001-matrix", timeoutTicks=100000)
    public static void registeredShulkers(GameTestHelper h) {
        checks=0;
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"ShulkerMatrix"));
        var template=new ItemStack(Items.WHEAT);
        var tierItems=tiers();var stackUpgrades=upgrades();
        check(h,tierItems.size()==6,"All six registered tiers discovered");
        for(var i:stackUpgrades)FoundryBulkCommerce.LOGGER.info("[BC001 discovery] {} class={}",BuiltInRegistries.ITEM.getKey(i),i.getClass().getName());
        check(h,stackUpgrades.size()==10,"All ten passive stack capacity modifiers discovered; count="+stackUpgrades.size());
        for(var upgrade:stackUpgrades)FoundryBulkCommerce.LOGGER.info("[BC001 matrix] upgrade={} multiplier={}",BuiltInRegistries.ITEM.getKey(upgrade),((StackUpgradeItem)upgrade).getStackSizeMultiplier());
        for(var item:tierItems) {
            player.getInventory().clearContent();
            var empty=new ItemStack(item);player.getInventory().setItem(0,empty);
            check(h,SophisticatedSources.collect(player,template).sources().isEmpty()&&!empty.has(ModCoreDataComponents.STORAGE_UUID),"UUID-less empty box remains unallocated");
            var box=box(item);player.getInventory().setItem(0,box);
            var wrapper=StackStorageWrapper.fromStack(h.getLevel().registryAccess(),box);
            var handler=wrapper.getInventoryHandler();int slots=handler.getSlots();int upgradeSlots=wrapper.getUpgradeHandler().getSlots();
            handler.saveInventory();
            check(h,SophisticatedSources.collect(player,template).sources().isEmpty(),"Canonical empty tier supported");
            for(int s=0;s<slots;s++)handler.setStackInSlot(s,new ItemStack(Items.WHEAT,64));
            handler.saveInventory();
            var outer=box.copy();var before=canonical(box).copy();
            var result=SophisticatedSources.collect(player,template,new ScanBudget(4096,1048576));
            check(h,result.sources().size()==1&&result.sources().getFirst().available()==(long)slots*64,"Full tier counted "+item);
            check(h,ItemStack.matches(outer,box)&&canonical(box).equals(before),"Collection leaves outer/canonical metadata untouched");
            FoundryBulkCommerce.LOGGER.info("[BC001 matrix] tier={} slots={} upgradeSlots={} fullStock={}",BuiltInRegistries.ITEM.getKey(item),slots,upgradeSlots,(long)slots*64);
            var edit=result.sources().getFirst().remove(32);
            check(h,edit.isBefore()&&Transaction.commit(List.of(edit),wallet(),1).successful()&&wheat(box)==(long)slots*64-32,"Partial extraction from full tier");
            edit.restore();check(h,edit.isBefore()&&canonical(box).equals(before),"Exact rollback for full tier");
            player.getInventory().setItem(1,box.copy());
            var duplicates=SophisticatedSources.collect(player,template);
            check(h,duplicates.sources().isEmpty()&&duplicates.rejections().size()==2&&duplicates.rejections().stream().allMatch(r->r.reason()==SaleRejection.Reason.DUPLICATE_UUID),"Both aliases rejected with exact diagnostic");
            player.getInventory().setItem(1,ItemStack.EMPTY);
            player.getInventory().setItem(20,box.copy());
            check(h,SophisticatedSources.collect(player,template).sources().isEmpty(),"Protected main-inventory alias blocks shulker");
            player.getInventory().setItem(20,box);player.getInventory().setItem(0,ItemStack.EMPTY);
            var protectedBudget=new ScanBudget(4096,1);
            check(h,SophisticatedSources.collect(player,template,protectedBudget).sources().isEmpty()&&protectedBudget.bytes()==0,"Protected shulker contents not serialized");
            player.getInventory().setItem(20,ItemStack.EMPTY);player.getInventory().setItem(0,box);

            handler=wrapper.getInventoryHandler();
            for(int s=0;s<slots;s++)handler.setStackInSlot(s,ItemStack.EMPTY);
            for(var upgrade:stackUpgrades) {
                handler=wrapper.getInventoryHandler();
                for(int s=0;s<upgradeSlots;s++)wrapper.getUpgradeHandler().setStackInSlot(s,ItemStack.EMPTY);
                check(h,wrapper.getUpgradeHandler().isItemValid(0,new ItemStack(upgrade)),"Upgrade accepted by dependency handler");
                wrapper.getUpgradeHandler().setStackInSlot(0,new ItemStack(upgrade));
                int count=(int)Math.min(Integer.MAX_VALUE,Math.max(1,((StackUpgradeItem)upgrade).getStackSizeMultiplier()*64));
                int nonmatchingCount=Math.min(7,count);
                handler.setStackInSlot(0,new ItemStack(Items.WHEAT,count));
                handler.setStackInSlot(slots-1,new ItemStack(Items.DIAMOND,nonmatchingCount));handler.saveInventory();
                var root=canonical(box).copy();
                var upgradeData=contents(root).getCompound("upgradeInventory").copy();
                var plan=SophisticatedSources.collect(player,template).sources().getFirst();
                check(h,plan.available()==count,"Upgraded count supported for every tier/upgrade");
                var partial=plan.remove(Math.min(count,2304));
                check(h,Transaction.commit(List.of(partial),wallet(),1).successful(),"Verified upgraded extraction");
                check(h,contents(canonical(box)).getCompound("upgradeInventory").equals(upgradeData),"Installed upgrade bytes preserved");
                check(h,wrapper.getInventoryHandler().getStackInSlot(slots-1).is(Items.DIAMOND)&&wrapper.getInventoryHandler().getStackInSlot(slots-1).getCount()==nonmatchingCount,"Unrelated mixed stack preserved");
                partial.restore();check(h,canonical(box).equals(root),"Full upgraded rollback preserves metadata");
            }
            handler=wrapper.getInventoryHandler();
            if(upgradeSlots>1) {
                double multiplier=1;
                for(int s=0;s<upgradeSlots;s++)wrapper.getUpgradeHandler().setStackInSlot(s,new ItemStack(stackUpgrades.get(s%stackUpgrades.size())));
                for(int s=0;s<upgradeSlots;s++)multiplier*=((StackUpgradeItem)stackUpgrades.get(s%stackUpgrades.size())).getStackSizeMultiplier();
                int combinationCount=(int)Math.min(4096,64*multiplier);
                handler.setStackInSlot(0,new ItemStack(Items.WHEAT,combinationCount));handler.saveInventory();
                check(h,SophisticatedSources.collect(player,template).sources().getFirst().available()==combinationCount,"Mixed stack upgrade combination");
                for(int s=0;s<upgradeSlots;s++)wrapper.getUpgradeHandler().setStackInSlot(s,new ItemStack(stackUpgrades.getLast()));
                handler.setStackInSlot(0,new ItemStack(Items.WHEAT,Integer.MAX_VALUE));handler.saveInventory();
                check(h,SophisticatedSources.collect(player,template).sources().getFirst().available()==Integer.MAX_VALUE,"Repeated maximum stack upgrades and int-max count");
            }
            // Unknown active hooks must never be silently treated as a multiplier.
            var unsafe=BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("sophisticatedstorage:void_upgrade"));
            wrapper.getUpgradeHandler().setStackInSlot(0,new ItemStack(unsafe));
            check(h,SophisticatedSources.collect(player,template).rejections().getFirst().reason()==SaleRejection.Reason.UNSUPPORTED_UPGRADE,"Non-stack processing upgrade rejected");
            for(int s=0;s<upgradeSlots;s++)wrapper.getUpgradeHandler().setStackInSlot(s,ItemStack.EMPTY);
            var invalid=canonical(box).copy();contents(invalid).getCompound("inventory").putInt("Size",slots+1);
            ItemContentsStorage.get().setStorageContents(box.get(ModCoreDataComponents.STORAGE_UUID),invalid);
            check(h,SophisticatedSources.collect(player,template).rejections().getFirst().reason()==SaleRejection.Reason.INVALID_DATA,"Canonical capacity beyond storage API rejected");
            forget(box);
        }
        player.getInventory().clearContent();
        for(int s=0;s<tierItems.size();s++) {
            var box=box(tierItems.get(s));player.getInventory().setItem(s,box);
            var wrapper=StackStorageWrapper.fromStack(h.getLevel().registryAccess(),box);
            wrapper.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,64));wrapper.getInventoryHandler().saveInventory();
        }
        check(h,SophisticatedSources.collect(player,template).sources().size()==6,"Different tiers coexist");
        for(int s=0;s<tierItems.size();s++)forget(player.getInventory().getItem(s));
        player.getInventory().clearContent();
        // Modern saved capacities may legitimately exceed tier defaults (e.g. retained capacity after a config change).
        var expanded=box(tierItems.getLast());expanded.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS,300);
        player.getInventory().setItem(0,expanded);
        var expandedWrapper=StackStorageWrapper.fromStack(h.getLevel().registryAccess(),expanded);
        check(h,expandedWrapper.getInventoryHandler().getSlots()==300,"Dependency API promotes modern saved capacity");
        for(int s=0;s<300;s++)expandedWrapper.getInventoryHandler().setStackInSlot(s,new ItemStack(Items.WHEAT,64));
        expandedWrapper.getInventoryHandler().saveInventory();
        var expandedSource=SophisticatedSources.collect(player,template,new ScanBudget(4096,1048576)).sources().getFirst();
        check(h,expandedSource.available()==19200,"API capacity above old 256 bound supported");
        var expandedEdit=expandedSource.remove(11520);
        check(h,Transaction.commit(List.of(expandedEdit),wallet(),5).successful()&&wheat(expanded)==7680,"Larger capacity partial extraction");
        expandedEdit.restore();check(h,wheat(expanded)==19200,"Larger capacity rollback");
        expanded.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(new CompoundTag()));
        check(h,SophisticatedSources.collect(player,template).rejections().getFirst().reason()==SaleRejection.Reason.UNSUPPORTED_STORAGE,"Legacy/custom-data exclusion diagnosed explicitly");
        expanded.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        player.getInventory().setItem(1,expanded.copy());
        check(h,!expandedEdit.isBefore(),"Alias introduced after plan invalidates expanded source");
        player.getInventory().setItem(1,ItemStack.EMPTY);
        forget(expanded);player.getInventory().clearContent();
        FoundryBulkCommerce.LOGGER.info("[BC001 matrix] {} installed-registry assertions passed",checks);
        h.succeed();
    }
}
