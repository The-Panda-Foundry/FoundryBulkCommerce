package com.pandafoundry.bulkcommerce.storage;

import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

public final class VanillaTest {
    private static int checks;
    private static void check(boolean condition, String name) { checks++; if (!condition) throw new AssertionError(name); }
    public static void main(String[] args) {
        net.neoforged.fml.loading.LoadingModList.of(List.of(), List.of(), List.of(), List.of(), java.util.Map.of());
        net.minecraft.SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        Inventory inventory = new Inventory(null);
        ItemStack template = new ItemStack(Items.STONE);
        inventory.setItem(0, new ItemStack(Items.STONE, 12));
        ItemStack box = new ItemStack(Items.BLUE_SHULKER_BOX);
        box.set(DataComponents.CUSTOM_NAME, Component.literal("Keep this box"));
        NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
        contents.set(0, new ItemStack(Items.STONE, 64));
        contents.set(1, new ItemStack(Items.DIAMOND, 9));
        contents.set(2, new ItemStack(Items.STONE, 20));
        ItemStack namedStone = new ItemStack(Items.STONE, 32);
        namedStone.set(DataComponents.CUSTOM_NAME, Component.literal("Different components"));
        contents.set(3, namedStone);
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        inventory.setItem(1, box);
        var sources = VanillaSources.collect(inventory, template, true, registries);
        check(sources.size() == 2 && sources.stream().mapToLong(SaleSource::available).sum() == 96, "loose plus exact component shulker matching");
        var loose = sources.get(0).remove(12);
        var boxed = sources.get(1).remove(70);
        check(loose.isBefore() && boxed.isBefore() && inventory.getItem(0).getCount() == 12, "planning is read-only");
        loose.apply(); boxed.apply();
        check(loose.isAfter() && boxed.isAfter(), "removals verified");
        var after = inventory.getItem(1).get(DataComponents.CONTAINER);
        check(after.getStackInSlot(0).isEmpty() && after.getStackInSlot(2).getCount() == 14, "multiple slots removed exactly");
        check(after.getStackInSlot(1).getCount() == 9 && ItemStack.matches(after.getStackInSlot(3),namedStone), "nonmatching contents preserved");
        check(inventory.getItem(1).get(DataComponents.CUSTOM_NAME).getString().equals("Keep this box"), "box components preserved");
        boxed.restore(); loose.restore();
        check(loose.isBefore() && boxed.isBefore(), "exact rollback");
        inventory.setItem(40, box.copy());
        check(VanillaSources.collect(inventory,template,true,registries).stream().mapToLong(SaleSource::available).sum() == 180, "offhand shulkers supported");
        check(VanillaSources.collect(inventory,template,false,registries).size() == 1, "container configuration switch");
        check(!ItemPolicy.saleable(box) && !ItemPolicy.saleable(new ItemStack(Items.CHEST)), "nested and container template exclusion");
        NonNullList<ItemStack> invalid = NonNullList.withSize(28, ItemStack.EMPTY);
        invalid.set(27, new ItemStack(Items.STONE));
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(invalid));
        inventory.setItem(1, box);
        boolean rejected = false;
        try { VanillaSources.collect(inventory,template,true,registries); } catch (IllegalStateException expected) { rejected = true; }
        check(rejected, "oversized vanilla layout cannot be truncated");
        System.out.println("Vanilla adapter: "+checks+" checks passed");
    }
}
