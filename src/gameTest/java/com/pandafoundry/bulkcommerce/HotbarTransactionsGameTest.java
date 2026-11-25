package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.ledger.*;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import dev.yassou.numismaticstreasury.block.entity.*;
import dev.yassou.numismaticstreasury.server.BankService;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.*;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;

@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class HotbarTransactionsGameTest {
 private static final class State {ServerPlayer p;Map<UUID,ServerPlayer> players;BankAccountAccessor bank;BlockPos pos;ItemStack bp,protectedBp,protectedVanilla;CompoundTag protectedRoot;int limit;CompletableFuture<List<LocalLedger.Row>> rows;}
 private static ItemStack vanilla(){var box=new ItemStack(Items.SHULKER_BOX);box.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.WHEAT,64))));return box;}
 @SuppressWarnings("unchecked") private static void setup(GameTestHelper h,State s){
  s.limit=BulkConfig.LIMIT.get();BulkConfig.LIMIT.set(230400);s.p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"HotbarLedger"));s.p.getInventory().clearContent();s.p.getInventory().selected=5;
  try{var f=PlayerList.class.getDeclaredField("playersByUUID");f.setAccessible(true);s.players=(Map<UUID,ServerPlayer>)f.get(h.getLevel().getServer().getPlayerList());}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
  s.players.put(s.p.getUUID(),s.p);s.bank=(BankAccountAccessor)(Object)BankService.account(s.p);s.bank.foundry$setBalance(0);s.bank.foundry$setAdditionalBalance(0);
  s.pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlockAndUpdate(s.pos,dev.yassou.numismaticstreasury.registry.TreasuryContent.NUMIS_SHOP.get().defaultBlockState());
  ((ServerShopBlockEntity)h.getLevel().getBlockEntity(s.pos)).configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.WHEAT),1,64);s.p.setPos(s.pos.getX()+0.5,s.pos.getY(),s.pos.getZ()+0.5);
  s.bp=BackpackHotbarGameTest.backpack(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.NETHERITE_BACKPACK.get(),64);var w=BackpackWrapper.fromStack(s.bp);w.getUpgradeHandler().setStackInSlot(0,new ItemStack(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_OMEGA_TIER.get()));
  w.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,Integer.MAX_VALUE));w.getInventoryHandler().setStackInSlot(1,vanilla());w.getInventoryHandler().saveInventory();s.p.getInventory().setItem(0,s.bp);
  var sh=ShulkerCompatibilityGameTest.box(net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.SHULKER_BOX_ITEM.get());s.p.getInventory().setItem(1,sh);var sw=net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper.fromStack(s.p.registryAccess(),sh);sw.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,64));sw.getInventoryHandler().saveInventory();
  s.p.getInventory().setItem(2,vanilla());s.p.getInventory().setItem(12,new ItemStack(Items.WHEAT,64));s.protectedBp=BackpackHotbarGameTest.backpack(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK.get(),64);s.p.getInventory().setItem(15,s.protectedBp);
  s.protectedRoot=BackpackStorage.get().getBackpackContents(s.protectedBp.get(ModCoreDataComponents.STORAGE_UUID)).orElseThrow().copy();s.protectedVanilla=vanilla();s.p.getInventory().setItem(16,s.protectedVanilla);s.p.getInventory().setItem(40,vanilla());
  BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.p,s.pos);
 }
 @GameTest(template="empty",batch="backpacks-transactions",timeoutTicks=100000)
 public static void mixedHotbarLedger(GameTestHelper h){
  if(!ModList.get().isLoaded("sophisticatedbackpacks")){h.succeed();return;}
  State s=new State();h.startSequence().thenWaitUntil(()->h.assertTrue(LedgerRuntime.admissionOpen(),"Ledger ready"))
  .thenExecute(()->setup(h,s)).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Durable mixed transaction"))
  .thenExecute(()->{
   h.assertTrue(s.bank.foundry$getBalance()==3600&&BackpackWrapper.fromStack(s.bp).getInventoryHandler().getStackInSlot(0).getCount()==2147253439,"230400 ceiling with combined stock exceeding int and nonselected hotbar containers");
   h.assertTrue(BackpackWrapper.fromStack(s.bp).getInventoryHandler().getStackInSlot(1).get(DataComponents.CONTAINER).getStackInSlot(0).getCount()==64,"Nested container untouched");
   h.assertTrue(BackpackStorage.get().getBackpackContents(s.protectedBp.get(ModCoreDataComponents.STORAGE_UUID)).orElseThrow().equals(s.protectedRoot)&&s.p.getInventory().getItem(16)==s.protectedVanilla&&s.p.getInventory().getItem(40).get(DataComponents.CONTAINER).getStackInSlot(0).getCount()==64,"Protected main/offhand storage unchanged");
   BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.p,s.pos);
  }).thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Repeated transaction"))
  .thenExecute(()->{h.assertTrue(s.bank.foundry$getBalance()==7200,"Repeated request paid exactly once");BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.p,s.pos);s.p.getInventory().setItem(18,s.bp);s.p.getInventory().setItem(0,ItemStack.EMPTY);})
  .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Movement cancellation"))
  .thenExecute(()->{h.assertTrue(s.bank.foundry$getBalance()==7200&&BackpackWrapper.fromStack(s.bp).getInventoryHandler().getStackInSlot(0).getCount()==2147023039,"Moved backpack cancelled before mutation");s.p.getInventory().setItem(0,s.bp);s.p.getInventory().setItem(18,ItemStack.EMPTY);BulkSales.reset();NetheriteTransactionsGameTest.invoke(s.p,s.pos);s.p.getInventory().setItem(20,s.bp.copy());})
  .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"New alias cancellation"))
  .thenExecute(()->{h.assertTrue(s.bank.foundry$getBalance()==7200,"Alias introduced during preparation prevents payment");s.rows=LedgerRuntime.ledger().recent(s.p.getUUID(),false,25);})
  .thenWaitUntil(()->h.assertTrue(s.rows.isDone(),"Ledger audit"))
  .thenExecute(()->{var rows=s.rows.getNow(List.of());h.assertTrue(rows.size()==4&&rows.stream().filter(r->r.state().equals("APPLIED_MEMORY")).count()==2&&rows.stream().filter(r->r.state().equals("CANCELLED")).count()==2,"Accurate success and cancellation ledger outcomes");h.assertTrue(rows.stream().filter(r->r.quantity()==230400).count()==4,"Planned ceiling preserved in audit");BulkConfig.LIMIT.set(s.limit);s.players.remove(s.p.getUUID());BackpackStorage.get().removeBackpackContents(s.bp.get(ModCoreDataComponents.STORAGE_UUID));BackpackStorage.get().removeBackpackContents(s.protectedBp.get(ModCoreDataComponents.STORAGE_UUID));ShulkerCompatibilityGameTest.forget(s.p.getInventory().getItem(1));s.p.getInventory().clearContent();s.bank.foundry$setBalance(0);s.bank.foundry$setAdditionalBalance(0);BankService.existingAccount(s.p.getUUID()).markDirty();}).thenSucceed();
 }
}
