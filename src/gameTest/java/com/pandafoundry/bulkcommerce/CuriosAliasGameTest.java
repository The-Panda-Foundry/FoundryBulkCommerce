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
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class CuriosAliasGameTest {
 @GameTest(template="empty",batch="backpacks-curios",timeoutTicks=100000)
 public static void equippedAliases(GameTestHelper h){
  if(!ModList.get().isLoaded("sophisticatedbackpacks")||!ModList.get().isLoaded("curios")){h.succeed();return;}
  Installed.run(h);
 }
 // Keep compiler-generated optional API lambda signatures out of GameTest discovery.
 private static final class Installed {
 static void run(GameTestHelper h){
  var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"CuriosAliases"));p.getInventory().clearContent();
  var box=BackpackHotbarGameTest.backpack(ModItems.NETHERITE_BACKPACK.get(),64);var id=box.get(ModCoreDataComponents.STORAGE_UUID);p.getInventory().setItem(0,box);
  var curios=CuriosApi.getCuriosInventory(p).orElseThrow();
  FoundryBulkCommerce.LOGGER.info("[BC020 Curios] registered slots={}",curios.getCurios().keySet());
  var slot=curios.getCurios().values().stream().filter(v->v.getSlots()>0).findFirst().orElseThrow();
  slot.getStacks().setStackInSlot(0,box.copy());
  h.assertTrue(BackpackSources.collect(p,new ItemStack(Items.WHEAT)).rejections().getFirst().reason()==SaleRejection.Reason.DUPLICATE_UUID,"Equipped Curios alias blocks eligible hotbar UUID");
  p.getInventory().setItem(0,ItemStack.EMPTY);var budget=new ScanBudget(4096,1);
  h.assertTrue(BackpackSources.collect(p,new ItemStack(Items.WHEAT),budget).sources().isEmpty()&&budget.bytes()==0,"Equipped backpack contents never decoded");
  p.getInventory().setItem(0,box);slot.getStacks().setStackInSlot(0,ItemStack.EMPTY);slot.getCosmeticStacks().setStackInSlot(0,box.copy());
  h.assertTrue(BackpackSources.collect(p,new ItemStack(Items.WHEAT)).rejections().getFirst().reason()==SaleRejection.Reason.DUPLICATE_UUID,"Cosmetic Curios alias also blocks");
  slot.getCosmeticStacks().setStackInSlot(0,ItemStack.EMPTY);
  var shulker=new ItemStack(net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.SHULKER_BOX_ITEM.get());shulker.set(ModCoreDataComponents.STORAGE_UUID,id);p.getInventory().setItem(1,shulker);
  var wrapper=StackStorageWrapper.fromStack(p.registryAccess(),shulker);wrapper.getInventoryHandler().setStackInSlot(0,new ItemStack(Items.WHEAT,64));wrapper.getInventoryHandler().saveInventory();
  h.assertTrue(SophisticatedSources.collect(p,new ItemStack(Items.WHEAT)).sources().size()==1&&BackpackSources.collect(p,new ItemStack(Items.WHEAT)).sources().size()==1,"Equal UUIDs in separate repositories are independent canonical identities");
  ItemContentsStorage.get().removeStorageContents(id);BackpackStorage.get().removeBackpackContents(id);p.getInventory().clearContent();h.succeed();
 }
}
}
