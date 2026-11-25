package com.pandafoundry.bulkcommerce;
import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.storage.*;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.*;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class BackpackPerformanceGameTest {
 @GameTest(template="empty",batch="backpacks-performance",timeoutTicks=100000)
 public static void largeBackpacks(GameTestHelper h){
  if(!ModList.get().isLoaded("sophisticatedbackpacks")){h.succeed();return;}
  var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"BackpackPerformance"));
  for(int number:new int[]{1,4}){
   p.getInventory().clearContent();List<ItemStack> bags=new ArrayList<>();
   for(int n=0;n<number;n++){
    var bag=BackpackHotbarGameTest.backpack(ModItems.NETHERITE_BACKPACK.get(),64);bags.add(bag);p.getInventory().setItem(n,bag);var w=BackpackWrapper.fromStack(bag);w.getUpgradeHandler().setStackInSlot(0,new ItemStack(ModItems.STACK_UPGRADE_OMEGA_TIER.get()));
    for(int slot=0;slot<w.getInventoryHandler().getSlots();slot++)w.getInventoryHandler().setStackInSlot(slot,new ItemStack(Items.WHEAT,Integer.MAX_VALUE));w.getInventoryHandler().saveInventory();
   }
   long[] time=new long[40];int slots=0,bytes=0;
   for(int run=-12;run<40;run++){
    long start=System.nanoTime();var budget=new ScanBudget(4096,1048576);var collected=BackpackSources.collect(p,new ItemStack(Items.WHEAT),budget);long stock=collected.sources().stream().mapToLong(SaleSource::available).sum();
    h.assertTrue(stock>Integer.MAX_VALUE,"Upgraded aggregate uses long");var edit=collected.sources().getFirst().remove(230400);edit.recoveryDetails();
    if(run>=0)time[run]=System.nanoTime()-start;slots=budget.slots();bytes=budget.bytes();
   }
   Arrays.sort(time);FoundryBulkCommerce.LOGGER.info("[BC020 performance] backpacks={} medianMillis={} p95Millis={} inspections={} binaryNbtBytes={} samples=40",number,time[20]/1e6,time[37]/1e6,slots,bytes);
   for(var bag:bags)BackpackStorage.get().removeBackpackContents(bag.get(ModCoreDataComponents.STORAGE_UUID));
  }
  p.getInventory().clearContent();h.succeed();
 }
}
