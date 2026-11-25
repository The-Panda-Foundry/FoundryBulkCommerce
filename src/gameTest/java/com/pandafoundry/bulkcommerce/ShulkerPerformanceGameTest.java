package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.storage.*;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import java.lang.management.ManagementFactory;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

/** Bounded warm-JVM preparation measurements, not a full-pack TPS benchmark. */
@GameTestHolder("foundrybulkcommerce")
@PrefixGameTestTemplate(false)
public final class ShulkerPerformanceGameTest {
    @FunctionalInterface private interface Collector { List<SaleSource> collect(ServerPlayer p,ScanBudget b) throws Exception; }
    private record Sample(long nanos,long allocated,int slots,int bytes,int evidenceCharacters) {}
    @GameTest(template="empty",batch="bc001-performance",timeoutTicks=100000)
    public static void boundedPreparation(GameTestHelper h) throws Exception {
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"ShulkerBenchmark"));
        Collector current=(p,b)->SophisticatedSources.collect(p,new ItemStack(Items.WHEAT),b).sources();
        String baselinePath=System.getProperty("foundrybulkcommerce.baselineJar","");
        URLClassLoader oldLoader=null;Collector baseline=null;
        if(!baselinePath.isBlank()) {
            // Only the old adapter/nested classes are private to this loader; Minecraft and all live mod classes are shared.
            oldLoader=new URLClassLoader(new java.net.URL[]{Path.of(baselinePath).toUri().toURL()},SophisticatedSources.class.getClassLoader()) {
                protected synchronized Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
                    if(name.startsWith("com.pandafoundry.bulkcommerce.storage.SophisticatedSources")) {
                        Class<?> c=findLoadedClass(name);if(c==null)c=findClass(name);if(resolve)resolveClass(c);return c;
                    }
                    return super.loadClass(name,resolve);
                }
            };
            var type=oldLoader.loadClass(SophisticatedSources.class.getName());
            var collect=type.getMethod("collect",ServerPlayer.class,ItemStack.class,ScanBudget.class);
            var sources=type.getDeclaredClasses(); // Resolve the old record's public accessor without copying dependency classes.
            var resultType=Arrays.stream(sources).filter(c->c.getSimpleName().equals("Collected")).findFirst().orElseThrow();
            var accessor=resultType.getMethod("sources");
            baseline=(p,b)->{ @SuppressWarnings("unchecked") var result=(List<SaleSource>)accessor.invoke(collect.invoke(null,p,new ItemStack(Items.WHEAT),b));return result;};
        }
        try {
            for(int scenario=0;scenario<3;scenario++) {
                player.getInventory().clearContent();List<ItemStack> boxes=new ArrayList<>();int number=scenario==0?1:4;
                for(int i=0;i<number;i++) {
                    var box=ShulkerCompatibilityGameTest.box(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get());boxes.add(box);player.getInventory().setItem(i,box);
                    var wrapper=StackStorageWrapper.fromStack(h.getLevel().registryAccess(),box);
                    if(scenario==2)wrapper.getUpgradeHandler().setStackInSlot(0,new ItemStack(ModItems.STACK_UPGRADE_TIER_5.get()));
                    for(int slot=0;slot<wrapper.getInventoryHandler().getSlots();slot++)wrapper.getInventoryHandler().setStackInSlot(slot,new ItemStack(Items.WHEAT,scenario==2?2048:64));
                    wrapper.getInventoryHandler().saveInventory();
                }
                String name=scenario==0?"1-full-netherite":scenario==1?"4-full-netherite":"4-full-netherite-32x";
                if(baseline!=null)report(name,"v0.1.1",player,baseline);
                report(name,"v0.1.2",player,current);
                // Same canonical snapshots must remain sellable; performance sampling never applies edits or pays.
                h.assertTrue(SophisticatedSources.collect(player,new ItemStack(Items.WHEAT)).sources().size()==number,"Benchmark does not mutate stock");
                try { current.collect(player,new ScanBudget(64,1048576));throw new AssertionError("Scan budget accepted oversized work"); }
                catch(SaleRejection rejected){h.assertTrue(rejected.reason()==SaleRejection.Reason.SCAN_LIMIT,"Distinct scan-limit diagnosis");}
                try { current.collect(player,new ScanBudget(4096,512));throw new AssertionError("Snapshot budget accepted oversized data"); }
                catch(SaleRejection rejected){h.assertTrue(rejected.reason()==SaleRejection.Reason.SNAPSHOT_LIMIT,"Distinct snapshot-limit diagnosis");}
                for(var box:boxes)ShulkerCompatibilityGameTest.forget(box);
            }
        } finally {player.getInventory().clearContent();if(oldLoader!=null)oldLoader.close();}
        h.succeed();
    }
    private static void report(String scenario,String version,ServerPlayer player,Collector collect) throws Exception {
        for(int i=0;i<12;i++)sample(player,collect);
        long[] times=new long[40],allocations=new long[40];Sample last=null;
        for(int i=0;i<40;i++){last=sample(player,collect);times[i]=last.nanos();allocations[i]=last.allocated();}
        Arrays.sort(times);Arrays.sort(allocations);
        FoundryBulkCommerce.LOGGER.info("[BC001 benchmark] scenario={} version={} samples=40 medianMillis={} p95Millis={} medianAllocatedBytes={} slots={} nbtBytes={} evidenceChars={}",
            scenario,version,times[20]/1000000.0,times[37]/1000000.0,allocations[20],last.slots(),last.bytes(),last.evidenceCharacters());
    }
    private static Sample sample(ServerPlayer player,Collector collector) throws Exception {
        var bean=ManagementFactory.getThreadMXBean();var allocation=bean instanceof com.sun.management.ThreadMXBean m&&m.isThreadAllocatedMemorySupported()?m:null;
        if(allocation!=null&&!allocation.isThreadAllocatedMemoryEnabled())allocation.setThreadAllocatedMemoryEnabled(true);
        long thread=Thread.currentThread().threadId(),startBytes=allocation==null?0:allocation.getThreadAllocatedBytes(thread),start=System.nanoTime();
        ScanBudget budget=new ScanBudget(4096,1048576);List<SaleSource> sources=collector.collect(player,budget);
        long count=0;for(var source:sources)count=Math.addExact(count,source.available());
        int remaining=Transaction.quantity(count,2304,65536),evidence=0;
        for(var source:sources) {int take=(int)Math.min(remaining,source.available());if(take>0){var edit=source.remove(take);evidence+=edit.recoveryDetails().length();remaining-=take;}if(remaining==0)break;}
        long elapsed=System.nanoTime()-start,allocated=allocation==null?-1:allocation.getThreadAllocatedBytes(thread)-startBytes;
        return new Sample(elapsed,allocated,budget.slots(),budget.bytes(),evidence);
    }
}
