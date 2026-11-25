package com.pandafoundry.bulkcommerce;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import com.pandafoundry.bulkcommerce.ledger.LocalLedger;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import dev.yassou.numismaticstreasury.block.entity.ServerShopBlockEntity;
import dev.yassou.numismaticstreasury.block.entity.ShopMode;
import dev.yassou.numismaticstreasury.server.BankService;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

@GameTestHolder("foundrybulkcommerce")
@PrefixGameTestTemplate(false)
public final class NetheriteTransactionsGameTest {
    private static final class State {
        ServerPlayer player, competitor; BankAccountAccessor account, competingAccount; Map<UUID,ServerPlayer> players;
        ServerShopBlockEntity shop; BlockPos pos; List<ItemStack> boxes=new ArrayList<>(); int oldLimit;
        long start; CompletableFuture<List<LocalLedger.Row>> rows;
    }
    @SuppressWarnings("unchecked")
    private static void setup(GameTestHelper h,State s) {
        s.oldLimit=BulkConfig.LIMIT.get();BulkConfig.LIMIT.set(65536);
        s.player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"NetheriteSales"));
        s.competitor=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"ConcurrentSale"));
        try {var f=PlayerList.class.getDeclaredField("playersByUUID");f.setAccessible(true);s.players=(Map<UUID,ServerPlayer>)f.get(h.getLevel().getServer().getPlayerList());}
        catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        s.players.put(s.player.getUUID(),s.player);s.players.put(s.competitor.getUUID(),s.competitor);
        s.account=(BankAccountAccessor)(Object)BankService.account(s.player);
        s.competingAccount=(BankAccountAccessor)(Object)BankService.account(s.competitor);
        s.competingAccount.foundry$setBalance(0);s.competingAccount.foundry$setAdditionalBalance(0);
        s.pos=h.absolutePos(new BlockPos(1,1,1));
        h.getLevel().setBlockAndUpdate(s.pos,dev.yassou.numismaticstreasury.registry.TreasuryContent.NUMIS_SHOP.get().defaultBlockState());
        s.shop=(ServerShopBlockEntity)h.getLevel().getBlockEntity(s.pos);
        s.shop.configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.WHEAT),1,2304);
        for(var p:List.of(s.player,s.competitor))p.setPos(s.pos.getX()+0.5,s.pos.getY(),s.pos.getZ()+0.5);
        s.competitor.getInventory().clearContent();
        for(int i=0;i<36;i++)s.competitor.getInventory().setItem(i,new ItemStack(Items.WHEAT,64));
    }
    static void invoke(ServerPlayer player,BlockPos pos) {
        try {var m=dev.yassou.numismaticstreasury.network.ServerActionHandler.class.getDeclaredMethod("tradeShop",ServerPlayer.class,JsonObject.class,boolean.class);m.setAccessible(true);var data=new JsonObject();data.addProperty("pos",pos.asLong());m.invoke(null,player,data,true);}
        catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    private static void fill(GameTestHelper h,State s,int count) {
        for(var box:s.boxes)ShulkerCompatibilityGameTest.forget(box);s.boxes.clear();s.player.getInventory().clearContent();
        s.account.foundry$setBalance(0);s.account.foundry$setAdditionalBalance(0);BankService.existingAccount(s.player.getUUID()).markDirty();
        for(int i=0;i<count;i++) {
            var box=ShulkerCompatibilityGameTest.box(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get());s.boxes.add(box);s.player.getInventory().setItem(i,box);
            var wrapper=StackStorageWrapper.fromStack(h.getLevel().registryAccess(),box);int slots=wrapper.getInventoryHandler().getSlots();
            h.assertTrue(slots==132,"Default Netherite acceptance requires 132 slots");
            for(int slot=0;slot<slots;slot++)wrapper.getInventoryHandler().setStackInSlot(slot,new ItemStack(Items.WHEAT,64));
            wrapper.getInventoryHandler().saveInventory();
        }
    }
    @GameTest(template="empty",batch="bc001-transactions",timeoutTicks=100000)
    public static void netheriteLots(GameTestHelper h) {
        State s=new State();var sequence=h.startSequence().thenWaitUntil(()->h.assertTrue(LedgerRuntime.admissionOpen(),"Ledger ready"))
            .thenExecute(()->setup(h,s));
        int[] expectedPaid={3,7,11,14};long[] expectedLeft={1536,768,0,1536};
        for(int n=1;n<=4;n++) {
            final int boxes=n;
            sequence.thenExecute(()->{
                fill(h,s,boxes);BulkConfig.LIMIT.set(65536);BulkSales.reset();s.start=System.nanoTime();invoke(s.player,s.pos);
                h.assertTrue(BulkSales.hasActive(),"One accepted asynchronous sale");
                long started=System.nanoTime();invoke(s.competitor,s.pos);
                FoundryBulkCommerce.LOGGER.info("[BC001 performance] concurrent admission rejectionMicros={}",(System.nanoTime()-started)/1000.0);
                h.assertTrue(s.competingAccount.foundry$getBalance()==0&&s.competitor.getInventory().getItem(0).getCount()==64,"Concurrent request safely deferred with stock/funds unchanged");
            }).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Await durable Netherite sale"))
            .thenExecute(()->{
                long left=s.boxes.stream().mapToLong(ShulkerCompatibilityGameTest::wheat).sum();
                h.assertTrue(s.account.foundry$getBalance()==expectedPaid[boxes-1]&&left==expectedLeft[boxes-1],"Netherite acceptance totals: "+boxes);
                h.assertTrue(s.shop.lotSize()==2304,"Treasury shop bundle size unchanged");
                FoundryBulkCommerce.LOGGER.info("[BC001 acceptance] boxes={} starting={} spurs={} remaining={} durableOutcomeMillis={}",boxes,boxes*8448,expectedPaid[boxes-1],left,(System.nanoTime()-s.start)/1000000.0);
            });
        }
        sequence.thenExecute(()->{fill(h,s,1);BulkConfig.LIMIT.set(4608);BulkSales.reset();invoke(s.player,s.pos);})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Await limited sale"))
            .thenExecute(()->{h.assertTrue(s.account.foundry$getBalance()==2&&ShulkerCompatibilityGameTest.wheat(s.boxes.getFirst())==3840,"Stock greater than limit retained");BulkSales.reset();invoke(s.player,s.pos);})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Await repeated sale"))
            .thenExecute(()->{
                h.assertTrue(s.account.foundry$getBalance()==3&&ShulkerCompatibilityGameTest.wheat(s.boxes.getFirst())==1536,"Repeated request sells only next complete bundle");
                BulkSales.reset();invoke(s.player,s.pos);
                h.assertTrue(!BulkSales.hasActive()&&s.account.foundry$getBalance()==3&&ShulkerCompatibilityGameTest.wheat(s.boxes.getFirst())==1536,"Insufficient remainder receives no payment");
                fill(h,s,0);s.shop.configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.WHEAT),2,64);
                int index=0;
                for(var tier:ShulkerCompatibilityGameTest.tiers()) {
                    var box=ShulkerCompatibilityGameTest.box(tier);s.boxes.add(box);s.player.getInventory().setItem(index++,box);
                    var wrapper=StackStorageWrapper.fromStack(h.getLevel().registryAccess(),box);
                    wrapper.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,64));
                    wrapper.getInventoryHandler().setStackInSlot(1,new ItemStack(Items.DIAMOND,7));wrapper.getInventoryHandler().saveInventory();
                }
                var vanilla=new ItemStack(Items.SHULKER_BOX);
                vanilla.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(List.of(new ItemStack(Items.WHEAT,64))));
                s.player.getInventory().setItem(index++,vanilla);s.player.getInventory().setItem(index,new ItemStack(Items.WHEAT,64));
                BulkSales.reset();invoke(s.player,s.pos);
            }).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Await mixed tier, vanilla and loose transaction"))
            .thenExecute(()->{
                h.assertTrue(s.account.foundry$getBalance()==16&&s.boxes.stream().mapToLong(ShulkerCompatibilityGameTest::wheat).sum()==0,"All six tiers plus vanilla and loose stock settle together");
                for(var box:s.boxes)h.assertTrue(StackStorageWrapper.fromStack(h.getLevel().registryAccess(),box).getInventoryHandler().getStackInSlot(1).getCount()==7,"Unrelated mixed-tier contents preserved");
                s.player.getInventory().setItem(7,new ItemStack(Items.WHEAT,2));
                s.shop.configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.WHEAT),Integer.MAX_VALUE,1);
                BulkSales.reset();invoke(s.player,s.pos);
                h.assertTrue(!BulkSales.hasActive()&&s.account.foundry$getBalance()==16&&s.player.getInventory().getItem(7).getCount()==2,"Payout overflow rejected before mutation");
                s.shop.configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.WHEAT),1,1);s.account.foundry$setAdditionalBalance(Long.MAX_VALUE);
                BulkSales.reset();invoke(s.player,s.pos);
                h.assertTrue(!BulkSales.hasActive()&&s.account.foundry$getBalance()==16&&s.account.foundry$getAdditionalBalance()==Long.MAX_VALUE,"Combined bank overflow rejected before mutation");
                s.account.foundry$setAdditionalBalance(0);
                s.rows=LedgerRuntime.ledger().recent(s.player.getUUID(),false,25);
            }).thenWaitUntil(()->h.assertTrue(s.rows.isDone(),"Await ledger evidence"))
            .thenExecute(()->{
                var rows=s.rows.getNow(List.of());
                h.assertTrue(rows.size()==7&&rows.stream().allMatch(r->r.state().equals("APPLIED_MEMORY")),"Exactly seven successful requests recorded; rejected requests add no economic intent");
                h.assertTrue(rows.stream().map(LocalLedger.Row::id).distinct().count()==7,"Unique transaction references");
                h.assertTrue(rows.stream().anyMatch(r->r.quantity()==32256&&r.payment()==14),"Greater-than-2304 outcome persisted");
                BulkConfig.LIMIT.set(s.oldLimit);s.players.remove(s.player.getUUID());s.players.remove(s.competitor.getUUID());
                for(var b:s.boxes)ShulkerCompatibilityGameTest.forget(b);s.player.getInventory().clearContent();s.competitor.getInventory().clearContent();
                s.account.foundry$setBalance(0);s.account.foundry$setAdditionalBalance(0);BankService.existingAccount(s.player.getUUID()).markDirty();
            }).thenSucceed();
    }
}
