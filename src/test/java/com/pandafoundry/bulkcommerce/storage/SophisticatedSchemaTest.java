package com.pandafoundry.bulkcommerce.storage;

import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Exercises the actual canonical-content planner with real registries; no storage world or live Mixins. */
public final class SophisticatedSchemaTest {
    private static int checks;
    private static void check(boolean value, String name) { checks++; if (!value) throw new AssertionError(name); }
    private static CompoundTag item(int slot, String id, int count) {
        CompoundTag item = new CompoundTag();
        item.putInt("Slot",slot); item.putString("id",id); item.putInt("count",count);
        return item;
    }
    private static CompoundTag inventory(CompoundTag root) { return root.getCompound("storageWrapper").getCompound("contents").getCompound("inventory"); }
    public static void main(String[] args) {
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
        net.minecraft.SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var identity = java.util.UUID.randomUUID();
        var other = java.util.UUID.randomUUID();
        check(SophisticatedSources.duplicates(List.of(identity,other,identity,identity)).equals(java.util.Set.of(identity)), "all aliases of duplicate UUID excluded; unique UUID retained");
        check(SophisticatedSources.duplicates(List.of(identity,other)).isEmpty(), "distinct UUIDs remain eligible");
        CompoundTag root = new CompoundTag(), wrapper = new CompoundTag(), contents = new CompoundTag(), inventory = new CompoundTag();
        ListTag items = new ListTag();
        items.add(item(0,"minecraft:stone",1048576));
        items.add(item(1,"minecraft:diamond",7));
        items.add(item(2,"minecraft:stone",Integer.MAX_VALUE));
        inventory.putInt("Size",27); inventory.put("Items",items);
        contents.put("inventory",inventory); wrapper.put("contents",contents); root.put("storageWrapper",wrapper);
        CompoundTag original = root.copy();
        check(SophisticatedSources.supported(registries,root), "oversized finite stacks accepted");
        CompoundTag after = SophisticatedSources.removeContents(registries,root,new ItemStack(Items.STONE),1048576);
        check(root.equals(original), "planning does not mutate canonical source");
        ListTag updated = inventory(after).getList("Items",Tag.TAG_COMPOUND);
        check(updated.size() == 2 && updated.getCompound(1).getInt("count") == Integer.MAX_VALUE, "exact oversized slot removal; other oversized stack preserved");
        check(updated.getCompound(0).equals(items.getCompound(1)), "nonmatching serialized entry preserved");
        after = SophisticatedSources.removeContents(registries,root,new ItemStack(Items.STONE),1048580);
        check(inventory(after).getList("Items",Tag.TAG_COMPOUND).getCompound(1).getInt("count") == Integer.MAX_VALUE-4, "oversized reduction across slot boundary");
        CompoundTag duplicate = root.copy();
        inventory(duplicate).getList("Items",Tag.TAG_COMPOUND).add(item(0,"minecraft:stone",20));
        check(!SophisticatedSources.supported(registries,duplicate), "duplicate serialized slot rejected");
        CompoundTag outside = root.copy(); inventory(outside).getList("Items",Tag.TAG_COMPOUND).getCompound(0).putInt("Slot",27);
        check(!SophisticatedSources.supported(registries,outside), "out-of-bounds slot rejected");
        CompoundTag partitioned = root.copy(); CompoundTag partition = new CompoundTag();
        partition.putIntArray("baseIndexes",new int[]{0,1});
        partitioned.getCompound("storageWrapper").getCompound("contents").put("partitioner",partition);
        check(!SophisticatedSources.supported(registries,partitioned), "partitioned/infinite storage rejected");
        CompoundTag tooLarge = root.copy(); inventory(tooLarge).putInt("Size",257);
        check(!SophisticatedSources.supported(registries,tooLarge), "scan bound enforced");
        CompoundTag upgradeRoot = root.copy(), upgrades = new CompoundTag(); ListTag upgradeItems = new ListTag();
        upgradeItems.add(item(0,"minecraft:stone",1)); upgrades.put("Items",upgradeItems);
        upgradeRoot.getCompound("storageWrapper").getCompound("contents").put("upgradeInventory",upgrades);
        check(!SophisticatedSources.supported(registries,upgradeRoot), "non-stack upgrade rejected");
        boolean underflow = false;
        try { SophisticatedSources.removeContents(registries,root,new ItemStack(Items.DIAMOND),8); }
        catch (IllegalStateException expected) { underflow = true; }
        check(underflow && root.equals(original), "underflow fails without source mutation");
        System.out.println("Sophisticated canonical schema: "+checks+" checks passed (no live storage world)");
    }
}
