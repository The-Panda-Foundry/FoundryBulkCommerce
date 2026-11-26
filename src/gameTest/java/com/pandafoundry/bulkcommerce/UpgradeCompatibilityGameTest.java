package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.storage.*;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.battery.BatteryUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;

/** All fixtures use actual upgrade insertion, capacity/layout and auxiliary resource APIs. Not shipped. */
@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class UpgradeCompatibilityGameTest {
    public record Fixture(ItemStack stack,IStorageWrapper wrapper,boolean backpack) {
        public UUID uuid(){return stack.get(ModCoreDataComponents.STORAGE_UUID);}
        public CompoundTag root(){return backpack?BackpackStorage.get().getBackpackContents(uuid()).orElseThrow():ItemContentsStorage.get().getStorageContents(uuid()).orElseThrow();}
        public void discard(){if(backpack)BackpackStorage.get().removeBackpackContents(uuid());else ItemContentsStorage.get().removeStorageContents(uuid());}
    }
    public static Fixture create(ServerPlayer player,boolean backpack,String... upgrades) {
        String namespace=backpack?"sophisticatedbackpacks":"sophisticatedstorage";
        Item item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace,backpack?"netherite_backpack":"netherite_shulker_box"));
        return createItem(player,backpack,item,upgrades);
    }
    private static Fixture createItem(ServerPlayer player,boolean backpack,Item item,String... upgrades) {
        String namespace=backpack?"sophisticatedbackpacks":"sophisticatedstorage";
        ItemStack stack=new ItemStack(item);stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("BC004 regression"));
        IStorageWrapper wrapper;
        if(backpack){wrapper=BackpackWrapper.fromStack(stack);wrapper.getInventoryHandler();}
        else{var shulker=StackStorageWrapper.fromStack(player.registryAccess(),stack);shulker.ensureContentsUuid();wrapper=shulker;}
        int columns=0;
        for(int slot=0;slot<upgrades.length;slot++) {
            var upgrade=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace,upgrades[slot])));
            if(!((IUpgradeItem<?>)upgrade.getItem()).canAddUpgradeTo(wrapper,upgrade,true,false).successful()
                ||!wrapper.getUpgradeHandler().isItemValid(slot,upgrade)||!wrapper.getUpgradeHandler().insertItem(slot,upgrade.copy(),true).isEmpty()
                ||!wrapper.getUpgradeHandler().insertItem(slot,upgrade,false).isEmpty())throw new IllegalStateException("Native upgrade insertion refused "+upgrades[slot]);
            columns=Math.addExact(columns,((IUpgradeItem<?>)upgrade.getItem()).getInventoryColumnsTaken());
        }
        wrapper.getUpgradeHandler().saveInventory();
        // Native menus apply this same layout change. Use the real wrapper API, then its fresh actual handler.
        wrapper.setColumnsTaken(columns,false);wrapper.onContentsNbtUpdated();wrapper.getInventoryHandler().saveInventory();
        return new Fixture(stack,wrapper,backpack);
    }
    public static void auxiliary(Fixture fixture) {
        for(var upgrade:fixture.wrapper().getUpgradeHandler().getSlotWrappers().values()) {
            if(upgrade instanceof net.p3pp3rf1y.sophisticatedcore.upgrades.IFilteredUpgrade filtered) {
                var filter=filtered.getFilterLogic();filter.setAllowList(false);filter.setMatchComponents(true);filter.setMatchDurability(true);
                var slots=filter.getFilterHandler();
                if(slots.getSlots()>0) {
                    var prototype=new ItemStack(Items.WHEAT);
                    if(!slots.isItemValid(0,prototype))prototype=new ItemStack(Items.BREAD);
                    if(slots.isItemValid(0,prototype))slots.setStackInSlot(0,prototype);
                }
            }
            if(upgrade instanceof net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoCookingUpgradeWrapper<?,?,?> auto) {
                auto.getInputFilterLogic().setAllowList(false);auto.getFuelFilterLogic().setAllowList(false);
            }
            if(upgrade instanceof net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.ICookingUpgrade<?> cooking) {
                cooking.getCookingLogic().getCookingInventory();cooking.getCookingLogic().setCookInput(new ItemStack(upgrade.getClass().getSimpleName().contains("Smoking")?Items.BEEF:Items.RAW_IRON,3));cooking.getCookingLogic().setFuel(new ItemStack(Items.COAL,4));
            }
            if(upgrade instanceof net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.JukeboxUpgradeWrapper jukebox) {
                if(!jukebox.getDiscInventory().insertItem(0,new ItemStack(Items.MUSIC_DISC_13),false).isEmpty())throw new IllegalStateException("Native music disc insertion refused");
            }
            if(fixture.backpack()&&upgrade instanceof net.p3pp3rf1y.sophisticatedbackpacks.upgrades.anvil.AnvilUpgradeWrapper anvil) {
                anvil.getInventory().setStackInSlot(0,new ItemStack(Items.DIAMOND_PICKAXE));anvil.getInventory().setStackInSlot(1,new ItemStack(Items.DIAMOND,3));anvil.setItemName("BC004 retained rename");
            }
            if(fixture.backpack()&&upgrade instanceof net.p3pp3rf1y.sophisticatedbackpacks.upgrades.smithing.SmithingUpgradeWrapper smithing) {
                smithing.getInventory().setStackInSlot(0,new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));smithing.getInventory().setStackInSlot(1,new ItemStack(Items.DIAMOND_PICKAXE));smithing.getInventory().setStackInSlot(2,new ItemStack(Items.NETHERITE_INGOT));
            }
            if(upgrade instanceof CraftingUpgradeWrapper crafting) {
                crafting.getInventory().setStackInSlot(0,new ItemStack(Items.WHEAT,17));
                crafting.setShiftClickIntoStorage(false);crafting.setRefillCraftingGridNBT(true);
            }
            if(upgrade instanceof TankUpgradeWrapper tank) {
                if(tank.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000),FluidAction.EXECUTE,true)!=1000)throw new IllegalStateException("Native tank fill refused");
            }
            if(upgrade instanceof BatteryUpgradeWrapper battery) {
                if(battery.receiveEnergy(1000,false)<=0)throw new IllegalStateException("Native battery fill refused");
            }
        }
    }
    public static StorageScan collect(ServerPlayer player,boolean backpack){return backpack?BackpackSources.collect(player,new ItemStack(Items.WHEAT)):SophisticatedSources.collect(player,new ItemStack(Items.WHEAT));}
    private static int checks;
    private static void check(GameTestHelper h,boolean value,String message){h.assertTrue(value,message);checks++;}
    private static void exercise(GameTestHelper h,ServerPlayer player,boolean backpack,String... upgrades) {
        exerciseFixture(h,player,create(player,backpack,upgrades),upgrades);
    }
    private static void exerciseFixture(GameTestHelper h,ServerPlayer player,Fixture f,String... upgrades) {
        boolean backpack=f.backpack();
        try {
            var inventory=f.wrapper().getInventoryHandler();
            inventory.setStackInSlot(0,new ItemStack(Items.WHEAT,64));inventory.setStackInSlot(1,new ItemStack(Items.DIAMOND,7));inventory.saveInventory();
            auxiliary(f);player.getInventory().setItem(0,f.stack());
            CompoundTag before=f.root().copy();ItemStack outer=f.stack().copy();
            var found=collect(player,backpack);
            check(h,found.rejections().isEmpty()&&found.sources().size()==1&&found.sources().getFirst().available()==64,"Reviewed upgrade does not disqualify primary contents: "+Arrays.toString(upgrades));
            var edit=found.sources().getFirst().remove(32);edit.apply();
            check(h,edit.isAfter(),"Full canonical after image verified");
            CompoundTag expected=before.copy();var contents=backpack?expected:expected.getCompound("storageWrapper").getCompound("contents");
            contents.getCompound("inventory").getList("Items",10).getCompound(0).putInt("count",32);
            check(h,f.root().equals(expected)&&ItemStack.matches(outer,f.stack()),"Only primary wheat count changed; auxiliary/config/UUID/outer exact");
            f.wrapper().onContentsNbtUpdated();
            check(h,f.wrapper().getInventoryHandler().getStackInSlot(0).getCount()==32&&f.wrapper().getInventoryHandler().getStackInSlot(1).getCount()==7,"Existing native wrapper sees removal; unrelated item unchanged");
            check(h,f.root().equals(expected),"Native wrapper reopen preserves auxiliary/config bytes");
            edit.restore();check(h,edit.isBefore()&&f.root().equals(before),"Exact rollback restores primary count without altering upgrade resources");
            class FailingWallet implements Transaction.Wallet {
                int balance;
                public boolean isBefore(){return balance==0;}
                public void credit(int amount){balance=amount;throw new IllegalStateException("Injected failure after credit");}
                public boolean isCredited(int amount){return balance==amount;}
                public void restore(){balance=0;}
            }
            var wallet=new FailingWallet();var result=Transaction.commit(List.of(collect(player,backpack).sources().getFirst().remove(16)),wallet,3);
            check(h,!result.successful()&&!result.recoveryRequired()&&wallet.balance==0&&f.root().equals(before),"Post-credit compensation restores native source/auxiliary state after wallet verification");
            if(backpack&&Arrays.asList(upgrades).contains("tank_upgrade")) {
                var key=net.p3pp3rf1y.sophisticatedbackpacks.init.ModDataComponents.COLUMNS_TAKEN.get();int original=f.stack().getOrDefault(key,0);
                f.stack().set(key,original+1);var invalid=collect(player,true);
                check(h,invalid.sources().isEmpty()&&!invalid.rejections().isEmpty()&&f.root().equals(before),"Column/layout disagreement refused without mutation");f.stack().set(key,original);
            }
        }finally{player.getInventory().clearContent();f.discard();}
    }
    @GameTest(template="empty",batch="bc004-upgrades",timeoutTicks=100000)
    public static void reviewedUpgradeMatrix(GameTestHelper h) {
        checks=0;var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"UpgradeMatrix"));player.getInventory().clearContent();
        for(boolean backpack:new boolean[]{false,true}) {
            if(backpack&&!ModList.get().isLoaded("sophisticatedbackpacks"))continue;
            String namespace=backpack?"sophisticatedbackpacks":"sophisticatedstorage";int allowed=0,restricted=0;
            exercise(h,player,backpack); // No upgrades.
            int tiers=0;
            for(var tier:BuiltInRegistries.ITEM) {
                if(!BuiltInRegistries.ITEM.getKey(tier).getNamespace().equals(namespace))continue;
                if(backpack?!(tier instanceof net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem):!(tier instanceof net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem))continue;
                exerciseFixture(h,player,createItem(player,backpack,tier,"pickup_upgrade"),"pickup_upgrade");tiers++;
            }
            check(h,tiers==6,"All registered storage tiers support reviewed ordinary upgrade");
            for(var item:BuiltInRegistries.ITEM) {
                if(!(item instanceof IUpgradeItem<?>)||!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(namespace)||item.getClass()==StackUpgradeItem.class)continue;
                String name=BuiltInRegistries.ITEM.getKey(item).getPath();
                boolean reviewed=true;try{UpgradePolicy.columns(new ItemStack(item),namespace);}catch(SaleRejection unsupported){reviewed=false;}
                FoundryBulkCommerce.LOGGER.info("[BC004 upgrade] type={} item={} class={} policy={}",namespace,name,item.getClass().getName(),reviewed?"PRIMARY_ONLY":"RESTRICTED");
                if(reviewed){exercise(h,player,backpack,name);allowed++;}
                else {
                    var f=create(player,backpack);
                    try {
                        f.wrapper().getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,64));f.wrapper().getInventoryHandler().saveInventory();
                        // Intentionally install unsafe test state; the production parser must still refuse it.
                        f.wrapper().getUpgradeHandler().setStackInSlot(0,new ItemStack(item));player.getInventory().setItem(0,f.stack());
                        var scan=collect(player,backpack);check(h,scan.sources().isEmpty()&&!scan.rejections().isEmpty(),"Unsafe/unreviewed upgrade remains rejected: "+name);restricted++;
                    }finally{player.getInventory().clearContent();f.discard();}
                }
            }
            exercise(h,player,backpack,"crafting_upgrade","pickup_upgrade","filter_upgrade");
            if(backpack)exercise(h,player,true,"crafting_upgrade","pickup_upgrade","filter_upgrade","magnet_upgrade","feeding_upgrade","void_upgrade","jukebox_upgrade");
            else exercise(h,player,false,"crafting_upgrade","pickup_upgrade","filter_upgrade","magnet_upgrade","void_upgrade");
            if(backpack)exercise(h,player,true,"stack_upgrade_omega_tier","crafting_upgrade","tank_upgrade","battery_upgrade","pickup_upgrade","filter_upgrade","magnet_upgrade");
            else exercise(h,player,false,"stack_upgrade_omega_tier","crafting_upgrade","pickup_upgrade","filter_upgrade","magnet_upgrade");
            FoundryBulkCommerce.LOGGER.info("[BC004 matrix] type={} individualReviewed={} restricted={} assertions={}",namespace,allowed,restricted,checks);
        }
        FoundryBulkCommerce.LOGGER.info("[BC004 matrix] {} assertions passed",checks);h.succeed();
    }
}
