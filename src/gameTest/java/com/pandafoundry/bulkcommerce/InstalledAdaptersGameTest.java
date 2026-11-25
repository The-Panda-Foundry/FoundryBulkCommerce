package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import com.pandafoundry.bulkcommerce.storage.ScanBudget;
import com.pandafoundry.bulkcommerce.storage.SophisticatedSources;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import dev.yassou.numismaticstreasury.server.BankService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

/** Never packaged in the production addon. Runs with the real mods and Mixin transformer. */
@GameTestHolder("foundrybulkcommerce")
@PrefixGameTestTemplate(false)
public final class InstalledAdaptersGameTest {
    @GameTest(template = "empty", timeoutTicks = 100000)
    public static void installedAdapters(GameTestHelper helper) {
        Flow flow = new Flow();
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(LedgerRuntime.admissionOpen(), "Ledger must become ready: " + (LedgerRuntime.ledger() == null ? "missing" : LedgerRuntime.ledger().health())))
            .thenExecute(() -> exercise(helper))
            .thenExecute(() -> startSale(helper, flow))
            .thenWaitUntil(() -> helper.assertTrue(!BulkSales.hasActive(), "Await durable outcome acknowledgement"))
            .thenExecute(() -> {
                helper.assertTrue(flow.player.getInventory().getItem(0).isEmpty(), "Full Sell All removed loose stock");
                helper.assertTrue(flow.player.getInventory().getItem(1).get(DataComponents.CONTAINER).stream().allMatch(ItemStack::isEmpty), "Full Sell All emptied vanilla contents");
                helper.assertTrue(flow.wrapper.getInventoryHandler().getStackInSlot(0).isEmpty(), "Full Sell All emptied upgraded canonical contents");
                helper.assertTrue(flow.account.foundry$getBalance() == 12, "Full Sell All paid exactly four original-price lots");
                BulkSales.reset(); // No pending operation; avoid waiting out the admission cooldown in a fast-ticking test world.
                flow.player.getInventory().setItem(0, new ItemStack(Items.STONE, 64));
                invokeSellAll(flow);
                helper.assertTrue(BulkSales.hasActive(), "Intent acknowledgement is asynchronous");
                flow.player.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
            })
            .thenWaitUntil(() -> helper.assertTrue(!BulkSales.hasActive(), "Await stale-plan cancellation"))
            .thenExecute(() -> {
                helper.assertTrue(flow.account.foundry$getBalance() == 12 && flow.player.getInventory().getItem(0).is(Items.DIAMOND), "Changed source cancels without item or currency mutation");
                flow.records = LedgerRuntime.ledger().recent(flow.player.getUUID(), false, 25);
            })
            .thenWaitUntil(() -> helper.assertTrue(flow.records.isDone(), "Await read-only journal query"))
            .thenExecute(() -> {
                var rows = flow.records.getNow(List.of());
                helper.assertTrue(rows.size() == 2 && rows.stream().anyMatch(r -> r.state().equals("APPLIED_MEMORY")) && rows.stream().anyMatch(r -> r.state().equals("CANCELLED")), "Full asynchronous flow recorded success and stale cancellation");
                flow.players.remove(flow.player.getUUID());
                flow.player.getInventory().clearContent();
                ItemContentsStorage.get().removeStorageContents(flow.storage);
                flow.account.foundry$setBalance(0); flow.account.foundry$setAdditionalBalance(0);
                BankService.existingAccount(flow.player.getUUID()).markDirty();
            }).thenSucceed();
    }
    private static final class Flow {
        ServerPlayer player;
        BankAccountAccessor account;
        StackStorageWrapper wrapper;
        UUID storage;
        BlockPos pos;
        java.util.Map<UUID, ServerPlayer> players;
        java.util.concurrent.CompletableFuture<List<com.pandafoundry.bulkcommerce.ledger.LocalLedger.Row>> records;
    }
    @SuppressWarnings("unchecked")
    private static void startSale(GameTestHelper h, Flow flow) {
        var level = h.getLevel();
        flow.player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "BulkLedgerTest"));
        // Test fixture only: register an otherwise connection-free FakePlayer for the continuation's identity check.
        try {
            var field = PlayerList.class.getDeclaredField("playersByUUID"); field.setAccessible(true);
            flow.players = (java.util.Map<UUID, ServerPlayer>)field.get(level.getServer().getPlayerList());
            flow.players.put(flow.player.getUUID(), flow.player);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        flow.pos = h.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(flow.pos, dev.yassou.numismaticstreasury.registry.TreasuryContent.NUMIS_SHOP.get().defaultBlockState());
        var shop = (dev.yassou.numismaticstreasury.block.entity.ServerShopBlockEntity)level.getBlockEntity(flow.pos);
        shop.configure(dev.yassou.numismaticstreasury.block.entity.ShopMode.BUY_FROM_PLAYER, new ItemStack(Items.STONE), 3, 64);
        flow.player.setPos(flow.pos.getX() + 0.5, flow.pos.getY(), flow.pos.getZ() + 0.5);
        flow.account = (BankAccountAccessor)(Object)BankService.account(flow.player);
        flow.account.foundry$setBalance(0); flow.account.foundry$setAdditionalBalance(0);
        flow.player.getInventory().clearContent();
        flow.player.getInventory().setItem(0, new ItemStack(Items.STONE, 64));
        var vanilla = new ItemStack(Items.SHULKER_BOX);
        vanilla.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.STONE, 64))));
        flow.player.getInventory().setItem(1, vanilla);
        var upgraded = new ItemStack(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get());
        flow.storage = UUID.randomUUID(); upgraded.set(ModCoreDataComponents.STORAGE_UUID, flow.storage);
        flow.player.getInventory().setItem(2, upgraded);
        flow.wrapper = StackStorageWrapper.fromStack(level.registryAccess(), upgraded);
        flow.wrapper.getUpgradeHandler().setStackInSlot(0, new ItemStack(ModItems.STACK_UPGRADE_TIER_1.get()));
        flow.wrapper.getInventoryHandler().setStackInSlot(0, new ItemStack(Items.STONE, 128));
        flow.wrapper.getInventoryHandler().saveInventory();
        invokeSellAll(flow);
        h.assertTrue(BulkSales.hasActive(), "Actual Treasury Mixin started a durable asynchronous sale");
    }
    private static void invokeSellAll(Flow flow) {
        try {
            var method = dev.yassou.numismaticstreasury.network.ServerActionHandler.class.getDeclaredMethod("tradeShop", ServerPlayer.class, com.google.gson.JsonObject.class, boolean.class);
            method.setAccessible(true);
            var request = new com.google.gson.JsonObject(); request.addProperty("pos", flow.pos.asLong());
            method.invoke(null, flow.player, request, true);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
    }
    private static void exercise(GameTestHelper h) {
        BulkConfig.DEBUG_TRANSACTIONS.set(true); // Test-only: exercise user-configured INFO phase logging.
        var level = h.getLevel();
        var server = level.getServer();
        h.assertTrue(hasHandler(PlayerList.class, "foundry$recoveryGate"), "Admission Mixin applied");
        h.assertTrue(hasHandler(MinecraftServer.class, "foundry$orderlyExit") && hasHandler(MinecraftServer.class, "foundry$saveReturned"), "Shutdown Mixins applied");
        h.assertTrue(hasHandler(dev.yassou.numismaticstreasury.network.ServerActionHandler.class, "foundry$sellAll"), "Treasury Sell All Mixin applied");
        var root = server.getCommands().getDispatcher().getRoot().getChild("bulkcommerce");
        h.assertTrue(root != null && !root.canUse(server.createCommandSourceStack().withPermission(0)) && root.canUse(server.createCommandSourceStack().withPermission(2)), "Audit commands require permission level two");
        h.assertTrue(root.getChildren().size() == 5, "Only status and four read-only audit commands registered");

        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "BulkCommerceTest"));
        var account = BankService.account(player);
        h.assertTrue(account instanceof BankAccountAccessor, "Actual bank accessor Mixin applied");
        var access = (BankAccountAccessor)(Object)account;
        access.foundry$setBalance(1073741821); access.foundry$setAdditionalBalance(0);
        account.deposit(7);
        h.assertTrue(access.foundry$getBalance() == 1073741823 && access.foundry$getAdditionalBalance() == 5, "Actual bank overflow deposit verified");
        h.assertTrue(account.deduct(4, false) && access.foundry$getAdditionalBalance() == 1, "Actual bank reserve deduction verified");
        access.foundry$setBalance(0); access.foundry$setAdditionalBalance(0); account.markDirty();

        player.getInventory().clearContent();
        var box = new ItemStack(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get());
        UUID uuid = UUID.randomUUID(); box.set(ModCoreDataComponents.STORAGE_UUID, uuid);
        player.getInventory().setItem(0, box);
        var wrapper = StackStorageWrapper.fromStack(level.registryAccess(), box);
        wrapper.getUpgradeHandler().setStackInSlot(0, new ItemStack(ModItems.STACK_UPGRADE_TIER_1.get()));
        wrapper.getInventoryHandler().setStackInSlot(0, new ItemStack(Items.STONE, 128));
        wrapper.getInventoryHandler().saveInventory();
        var collected = SophisticatedSources.collect(player, new ItemStack(Items.STONE), new ScanBudget(4096, 1048576));
        h.assertTrue(collected.sources().size() == 1 && collected.sources().getFirst().available() == 128, "Actual upgraded UUID-backed shulker counted");
        player.getInventory().setItem(1, box.copy());
        var aliased = SophisticatedSources.collect(player, new ItemStack(Items.STONE), new ScanBudget(4096, 1048576));
        h.assertTrue(aliased.sources().isEmpty() && aliased.skipped() == 2, "Actual carried duplicate UUID aliases both excluded");
        player.getInventory().setItem(1, ItemStack.EMPTY);
        var edit = collected.sources().getFirst().remove(64);
        player.getInventory().setItem(1, box.copy());
        h.assertTrue(!edit.isBefore(), "Alias introduced after preparation invalidates the planned edit");
        player.getInventory().setItem(1, ItemStack.EMPTY);
        h.assertTrue(edit.isBefore(), "Unchanged unique source revalidates before mutation");
        var result = Transaction.commit(List.of(edit), new Transaction.Wallet() {
            public boolean isBefore() { return access.foundry$getBalance() == 0 && access.foundry$getAdditionalBalance() == 0; }
            public void credit(int amount) { if (!BankService.credit(player.getUUID(), amount)) throw new IllegalStateException("Credit failed"); }
            public boolean isCredited(int amount) { return access.foundry$getBalance() == amount && access.foundry$getAdditionalBalance() == 0; }
            public void restore() { access.foundry$setBalance(0); access.foundry$setAdditionalBalance(0); account.markDirty(); }
        }, 3);
        h.assertTrue(result.successful() && edit.isAfter() && access.foundry$getBalance() == 3, "Actual shulker removal verified before actual bank payment");
        h.assertTrue(wrapper.getInventoryHandler().getStackInSlot(0).getCount() == 64, "Existing wrapper reads changed canonical contents");
        var saved = ItemContentsStorage.get().save(new net.minecraft.nbt.CompoundTag(), level.registryAccess());
        var loaded = ItemContentsStorage.load(saved, level.registryAccess());
        h.assertTrue(loaded.getStorageContents(uuid).orElseThrow().equals(ItemContentsStorage.get().getStorageContents(uuid).orElseThrow()), "Sophisticated SavedData serialization preserves changed contents");
        edit.restore();
        h.assertTrue(edit.isBefore() && wrapper.getInventoryHandler().getStackInSlot(0).getCount() == 128, "Actual canonical rollback and wrapper invalidation verified");
        access.foundry$setBalance(0); access.foundry$setAdditionalBalance(0); account.markDirty();
        player.getInventory().clearContent();
        ItemContentsStorage.get().removeStorageContents(uuid);
    }
    private static boolean hasHandler(Class<?> type, String text) { return Arrays.stream(type.getDeclaredMethods()).anyMatch(method -> method.getName().contains(text)); }
}
