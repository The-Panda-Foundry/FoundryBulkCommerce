package com.pandafoundry.bulkcommerce;
import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.storage.*;
import java.util.UUID;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import io.wispforest.accessories.api.AccessoriesCapability;
@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class AccessoriesAliasGameTest {
 @GameTest(template="empty",batch="backpacks-accessories",timeoutTicks=100000)
 public static void accessoriesAliases(GameTestHelper h){
  if(!ModList.get().isLoaded("accessories")||!ModList.get().isLoaded("sophisticatedbackpacks")){h.succeed();return;}
  Installed.run(h);
 }
 // Keep compiler-generated optional API lambda signatures out of GameTest discovery.
 private static final class Installed {
 static void run(GameTestHelper h){
  var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"AccessoryAliases"));p.getInventory().clearContent();
  var box=BackpackHotbarGameTest.backpack(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK.get(),64);p.getInventory().setItem(0,box);
  var cap=AccessoriesCapability.getOptionally(p).orElseThrow();cap.updateContainers();
  FoundryBulkCommerce.LOGGER.info("[BC020 Accessories] registered slots={}",cap.getContainers().keySet());
  var slot=cap.getContainers().values().stream().filter(v->v.getSize()>0).findFirst().orElseThrow();
  slot.getAccessories().setItem(0,box.copy());
  h.assertTrue(BackpackSources.collect(p,new ItemStack(Items.WHEAT)).rejections().getFirst().reason()==SaleRejection.Reason.DUPLICATE_UUID,"Accessories equipment alias blocks hotbar backpack");
  slot.getAccessories().setItem(0,ItemStack.EMPTY);slot.getCosmeticAccessories().setItem(0,box.copy());
  h.assertTrue(BackpackSources.collect(p,new ItemStack(Items.WHEAT)).rejections().getFirst().reason()==SaleRejection.Reason.DUPLICATE_UUID,"Accessories cosmetic alias also blocks");
  p.getInventory().setItem(0,ItemStack.EMPTY);var budget=new ScanBudget(4096,1);
  h.assertTrue(BackpackSources.collect(p,new ItemStack(Items.WHEAT),budget).sources().isEmpty()&&budget.bytes()==0,"Accessories contents not decoded/extracted");
  slot.getCosmeticAccessories().setItem(0,ItemStack.EMPTY);BackpackStorage.get().removeBackpackContents(box.get(ModCoreDataComponents.STORAGE_UUID));h.succeed();
 }
}
}
