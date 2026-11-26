package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.ledger.*;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import dev.yassou.numismaticstreasury.block.entity.*;
import dev.yassou.numismaticstreasury.server.BankService;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class UpgradeTransactionsGameTest {
    private static final class State {
        ServerPlayer player;Map<UUID,ServerPlayer> players;BankAccountAccessor bank;BlockPos pos;int limit;
        UpgradeCompatibilityGameTest.Fixture backpack,shulker,protectedBackpack;
        CompoundTag backpackAux,shulkerAux,protectedRoot;CompletableFuture<List<LocalLedger.Row>> rows;
    }
    private static CompoundTag auxiliaryImage(UpgradeCompatibilityGameTest.Fixture f){var root=f.root().copy();(f.backpack()?root:root.getCompound("storageWrapper").getCompound("contents")).remove("inventory");return root;}
    private static long wheat(UpgradeCompatibilityGameTest.Fixture f){
        f.wrapper().onContentsNbtUpdated();long count=0;var inventory=f.wrapper().getInventoryHandler();
        for(int slot=0;slot<inventory.getSlots();slot++)if(inventory.getStackInSlot(slot).is(Items.WHEAT))count=Math.addExact(count,(long)inventory.getStackInSlot(slot).getCount());
        return count;
    }
    @SuppressWarnings("unchecked") private static void setup(GameTestHelper h,State s){
        s.limit=BulkConfig.LIMIT.get();BulkConfig.LIMIT.set(230400);s.player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"UpgradeLedger"));s.player.getInventory().clearContent();
        try{var field=PlayerList.class.getDeclaredField("playersByUUID");field.setAccessible(true);s.players=(Map<UUID,ServerPlayer>)field.get(h.getLevel().getServer().getPlayerList());}
        catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        s.players.put(s.player.getUUID(),s.player);s.bank=(BankAccountAccessor)(Object)BankService.account(s.player);s.bank.foundry$setBalance(0);s.bank.foundry$setAdditionalBalance(0);
        s.pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlockAndUpdate(s.pos,dev.yassou.numismaticstreasury.registry.TreasuryContent.NUMIS_SHOP.get().defaultBlockState());
        ((ServerShopBlockEntity)h.getLevel().getBlockEntity(s.pos)).configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.WHEAT),1,2304);s.player.setPos(s.pos.getX()+0.5,s.pos.getY(),s.pos.getZ()+0.5);
        s.backpack=UpgradeCompatibilityGameTest.create(s.player,true,"stack_upgrade_omega_tier","crafting_upgrade","tank_upgrade","battery_upgrade","pickup_upgrade","filter_upgrade","magnet_upgrade");
        s.shulker=UpgradeCompatibilityGameTest.create(s.player,false,"stack_upgrade_omega_tier","crafting_upgrade","pickup_upgrade","filter_upgrade","magnet_upgrade");
        s.protectedBackpack=UpgradeCompatibilityGameTest.create(s.player,true,"crafting_upgrade","pickup_upgrade");
        for(var f:List.of(s.backpack,s.shulker,s.protectedBackpack)) {
            UpgradeCompatibilityGameTest.auxiliary(f);var inventory=f.wrapper().getInventoryHandler();
            inventory.setStackInSlot(0,new ItemStack(Items.WHEAT,f==s.backpack?300000:f==s.shulker?6912:64));inventory.setStackInSlot(1,new ItemStack(Items.DIAMOND,7));inventory.saveInventory();
        }
        s.backpackAux=auxiliaryImage(s.backpack);s.shulkerAux=auxiliaryImage(s.shulker);s.protectedRoot=s.protectedBackpack.root().copy();
        s.player.getInventory().setItem(0,s.backpack.stack());s.player.getInventory().setItem(1,s.shulker.stack());s.player.getInventory().setItem(20,s.protectedBackpack.stack());
        BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.player,s.pos);
    }
    @GameTest(template="empty",batch="bc004-transactions",timeoutTicks=100000)
    public static void upgradedContainersAndLedger(GameTestHelper h){
        if(!ModList.get().isLoaded("sophisticatedbackpacks")){h.succeed();return;}
        State s=new State();h.startSequence().thenWaitUntil(()->h.assertTrue(LedgerRuntime.admissionOpen(),"Ledger ready"))
            .thenExecute(()->setup(h,s)).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Mixed upgraded transaction durable"))
            .thenExecute(()->{
                h.assertTrue(s.bank.foundry$getBalance()==100&&wheat(s.shulker)==0&&wheat(s.backpack)==76512,"Ceiling removes6912 shulker+223488 backpack wheat; pays100, retains76512");
                h.assertTrue(auxiliaryImage(s.backpack).equals(s.backpackAux)&&auxiliaryImage(s.shulker).equals(s.shulkerAux)&&s.protectedBackpack.root().equals(s.protectedRoot),"All upgrades/settings/fluids/energy/auxiliary wheat and protected storage exact");
                BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.player,s.pos);
            }).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Repeated upgraded transaction durable"))
            .thenExecute(()->{
                h.assertTrue(s.bank.foundry$getBalance()==133&&wheat(s.backpack)==480,"Repeated Sell All removes76032 and pays33, leaving480; auxiliary wheat never counted");
                h.assertTrue(auxiliaryImage(s.backpack).equals(s.backpackAux)&&auxiliaryImage(s.shulker).equals(s.shulkerAux),"Repeat preserves full auxiliary bytes");
                var inventory=s.backpack.wrapper().getInventoryHandler();inventory.setStackInSlot(0,new ItemStack(Items.WHEAT,300000));inventory.saveInventory();
                BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.player,s.pos);
                for(var upgrade:s.backpack.wrapper().getUpgradeHandler().getSlotWrappers().values())if(upgrade instanceof CraftingUpgradeWrapper crafting)crafting.getInventory().setStackInSlot(0,new ItemStack(Items.WHEAT,18));
            }).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Auxiliary change cancels stale intent"))
            .thenExecute(()->{
                h.assertTrue(s.bank.foundry$getBalance()==133&&wheat(s.backpack)==300000,"Changing upgrade-owned contents while intent pending causes no primary removal/payment");
                for(var upgrade:s.backpack.wrapper().getUpgradeHandler().getSlotWrappers().values())if(upgrade instanceof CraftingUpgradeWrapper crafting)h.assertTrue(crafting.getInventory().getStackInSlot(0).getCount()==18,"Stale cancellation never overwrites new auxiliary state");
                s.rows=LedgerRuntime.ledger().recent(s.player.getUUID(),false,25);
            }).thenWaitUntil(()->h.assertTrue(s.rows.isDone(),"Read-only evidence ready"))
            .thenExecute(()->{
                var rows=s.rows.getNow(List.of());h.assertTrue(rows.size()==3&&rows.stream().filter(r->r.state().equals("APPLIED_MEMORY")).count()==2&&rows.stream().filter(r->r.state().equals("CANCELLED")).count()==1&&rows.stream().map(LocalLedger.Row::id).distinct().count()==3,"Unique durable upgraded success/cancellation outcomes");
                h.assertTrue(rows.stream().anyMatch(r->r.quantity()==230400&&r.payment()==100&&r.state().equals("APPLIED_MEMORY"))&&rows.stream().anyMatch(r->r.quantity()==76032&&r.payment()==33),"Exact planned quantities/payments audited");
                s.players.remove(s.player.getUUID());s.player.getInventory().clearContent();for(var f:List.of(s.backpack,s.shulker,s.protectedBackpack))f.discard();s.bank.foundry$setBalance(0);s.bank.foundry$setAdditionalBalance(0);BankService.existingAccount(s.player.getUUID()).markDirty();BulkConfig.LIMIT.set(s.limit);
                FoundryBulkCommerce.LOGGER.info("[BC004 ledger] mixed Omega/non-stack/auxiliary resources: 100+33Spurs,480 remainder, stale auxiliary cancellation;3 durable references passed");
            }).thenSucceed();
    }
}
