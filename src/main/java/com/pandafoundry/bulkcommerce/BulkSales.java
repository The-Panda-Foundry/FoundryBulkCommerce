package com.pandafoundry.bulkcommerce;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import com.pandafoundry.bulkcommerce.ledger.LocalLedger;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import com.pandafoundry.bulkcommerce.storage.*;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.yassou.numismaticstreasury.block.entity.ServerShopBlockEntity;
import dev.yassou.numismaticstreasury.block.entity.ShopMode;
import dev.yassou.numismaticstreasury.config.TreasuryConfig;
import dev.yassou.numismaticstreasury.network.TreasuryNetwork;
import dev.yassou.numismaticstreasury.registry.TreasuryContent;
import dev.yassou.numismaticstreasury.server.BankService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class BulkSales {
    // One pending transaction globally: no resource-lock framework or player/storage caches.
    private static UUID active;
    private static long nextRequest, nextBusyNotice;
    private BulkSales() {}
    public static boolean hasActive() { return active != null; }
    public static void reset() { active = null; nextRequest = 0; nextBusyNotice = 0; }

    public static void sell(ServerPlayer player, JsonObject data) {
        MinecraftServer server = player.getServer();
        if (server == null || !server.isSameThread()) throw new IllegalStateException("Server thread required");
        long now = System.nanoTime();
        if (!BulkConfig.ENABLED.get() || !LedgerRuntime.admissionOpen() || active != null || now < nextRequest) {
            if (now >= nextBusyNotice) {
                feedback(player, "Bulk selling is disabled, busy or awaiting ledger recovery. Please try again later.", false);
                nextBusyNotice = now + TimeUnit.SECONDS.toNanos(1);
            }
            return;
        }
        nextRequest = now + TimeUnit.MILLISECONDS.toNanos(BulkConfig.REQUEST_INTERVAL.get());
        UUID id = UUID.randomUUID();
        active = id;
        try {
            BlockPos pos = BlockPos.of(data.get("pos").getAsLong());
            ServerShopBlockEntity shop = validShop(player, pos);
            ItemStack template = shop.template().copy();
            if (!ItemPolicy.saleable(template)) throw new IllegalArgumentException("Container or storage-identity templates are excluded");
            int lotSize = shop.lotSize(), price = shop.price();
            if (lotSize <= 0 || price <= 0) throw new IllegalArgumentException("Invalid shop lot or price");
            ScanBudget budget = new ScanBudget(BulkConfig.SCAN_LIMIT.get(), BulkConfig.SNAPSHOT_LIMIT.get());
            boolean vanilla = BulkConfig.VANILLA.get(), sophisticated = BulkConfig.SOPHISTICATED.get();
            List<SaleSource> sources = new ArrayList<>(VanillaSources.collect(player, template, vanilla, budget));
            int skipped = 0;
            if (sophisticated && ModList.get().isLoaded("sophisticatedstorage") && ModList.get().isLoaded("sophisticatedcore")) {
                var collected = SophisticatedSources.collect(player, template, budget);
                sources.addAll(collected.sources()); skipped = collected.skipped();
            }
            long available = 0;
            for (SaleSource source : sources) available = Math.addExact(available, source.available());
            int quantity = Transaction.quantity(available, lotSize, BulkConfig.LIMIT.get());
            if (quantity == 0) throw new IllegalArgumentException("No complete lot fits the stock and transaction limit; skipped boxes: " + skipped);
            int total = Math.multiplyExact(price, quantity / lotSize);
            AccountWallet wallet = new AccountWallet(player, BankService.account(player));
            wallet.checkCapacity(total);
            List<Transaction.Edit> edits = new ArrayList<>();
            int remaining = quantity;
            for (SaleSource source : sources) {
                int take = (int)Math.min((long)remaining, source.available());
                if (take > 0) { edits.add(source.remove(take)); remaining -= take; }
                if (remaining == 0) break;
            }
            if (remaining != 0) throw new IllegalStateException("Sale plan underflow");
            JsonObject intent = new JsonObject();
            intent.addProperty("schemaVersion", 1); intent.addProperty("addonVersion", "0.1.0");
            intent.addProperty("dimension", player.level().dimension().location().toString()); intent.addProperty("shopPosition", pos.asLong());
            intent.addProperty("lotSize", lotSize); intent.addProperty("pricePerLotSpurs", price);
            intent.addProperty("quantity", quantity); intent.addProperty("paymentSpurs", total);
            intent.addProperty("standardBalanceBefore", wallet.beforeBalance); intent.addProperty("additionalBalanceBefore", wallet.beforeReserve);
            intent.addProperty("shopTemplate", template.saveOptional(player.registryAccess()).toString());
            JsonArray evidence = new JsonArray();
            for (var edit : edits) evidence.add(edit.recoveryDetails());
            intent.add("sourceBeforeAfterImages", evidence);
            String json = intent.toString();
            if (json.getBytes(StandardCharsets.UTF_8).length > BulkConfig.SNAPSHOT_LIMIT.get()) throw new IllegalArgumentException("Intent snapshot byte limit exceeded");
            LocalLedger ledger = LedgerRuntime.ledger();
            Plan plan = new Plan(id, player, server, ledger, shop, player.level().dimension().location().toString(), pos,
                template, lotSize, price, quantity, total, List.copyOf(edits), wallet, now, skipped, vanilla, sophisticated);
            var record = new LocalLedger.Intent(id, player.getUUID(), player.getGameProfile().getName(), System.currentTimeMillis(), quantity, total, json);
            feedback(player, "Preparing bulk sale. Reference: " + id, true);
            ledger.prepare(record).whenComplete((unused, error) -> resume(server, () -> {
                if (LedgerRuntime.ledger() != ledger) return;
                if (error != null) {
                    active = null;
                    FoundryBulkCommerce.LOGGER.error("Intent {} was not acknowledged; no economic mutations authorised", id, error);
                    LedgerRuntime.quarantine(server, "Ledger intent failure " + id);
                } else apply(plan);
            }));
        } catch (RuntimeException preparationFailure) {
            active = null;
            FoundryBulkCommerce.LOGGER.debug("Rejected sale preparation {} ({})", id, preparationFailure.getClass().getSimpleName());
            feedback(player, "Sale not started: check matching stock, supported storage and configured limits. Reference: " + id, false);
        }
    }

    private static ServerShopBlockEntity validShop(ServerPlayer player, BlockPos pos) {
        if (!TreasuryConfig.get().modules.serverShop || !player.canInteractWithBlock(pos, 4.0)
                || !player.level().getBlockState(pos).is(TreasuryContent.NUMIS_SHOP.get())
                || !(player.level().getBlockEntity(pos) instanceof ServerShopBlockEntity shop)
                || !shop.configured() || shop.mode() != ShopMode.BUY_FROM_PLAYER)
            throw new IllegalStateException("Shop access or configuration changed");
        return shop;
    }

    private static void apply(Plan plan) {
        // The last acknowledgement resumes HERE; no worker is allowed to touch game state.
        if (!plan.server.isSameThread()) throw new IllegalStateException("Server continuation required");
        try {
            boolean valid = !LedgerRuntime.stopping() && LedgerRuntime.admissionOpen() && BulkConfig.ENABLED.get()
                && plan.id.equals(active) && System.nanoTime() - plan.started < TimeUnit.SECONDS.toNanos(30)
                && plan.server.getPlayerList().getPlayer(plan.player.getUUID()) == plan.player
                && plan.player.level().dimension().location().toString().equals(plan.dimension)
                && validShop(plan.player, plan.pos) == plan.shop
                && plan.shop.price() == plan.price && plan.shop.lotSize() == plan.lotSize
                && ItemStack.matches(plan.shop.template(), plan.template)
                && plan.quantity <= BulkConfig.LIMIT.get() && plan.wallet.isBefore();
            valid = valid && plan.vanilla == BulkConfig.VANILLA.get() && plan.sophisticated == BulkConfig.SOPHISTICATED.get();
            if (valid) for (var edit : plan.edits) if (!edit.isBefore()) { valid = false; break; }
            if (!valid) { finish(plan, "CANCELLED", "Game state changed or request expired before mutation", false); return; }
        } catch (RuntimeException stale) {
            finish(plan, "CANCELLED", "Shop or source state changed before mutation", false);
            return;
        }
        try {
            var result = Transaction.commit(plan.edits, plan.wallet, plan.payment);
            String state = result.successful() ? "APPLIED_MEMORY" : result.recoveryRequired() ? "RECOVERY_REQUIRED" : "ROLLED_BACK_MEMORY";
            String detail = result.failure() == null ? "Verified item removal and payment" : result.failure().getClass().getSimpleName();
            finish(plan, state, detail, result.recoveryRequired());
        } catch (RuntimeException unexpected) {
            // Once commit was entered, no outer exception is allowed to masquerade as a preparation cancellation.
            finish(plan, "RECOVERY_REQUIRED", "Unexpected settlement exception: " + unexpected.getClass().getSimpleName(), true);
        }
    }

    private static void finish(Plan plan, String state, String detail, boolean recovery) {
        JsonObject outcome = new JsonObject();
        outcome.addProperty("state", state); outcome.addProperty("detail", detail); outcome.addProperty("observedAt", System.currentTimeMillis());
        try {
            outcome.addProperty("standardBalanceObserved", plan.wallet.access.foundry$getBalance());
            outcome.addProperty("additionalBalanceObserved", plan.wallet.access.foundry$getAdditionalBalance());
            JsonArray observed = new JsonArray();
            for (var edit : plan.edits) {
                JsonObject source = new JsonObject(); source.addProperty("source", edit.describe());
                source.addProperty("matchesBefore", edit.isBefore()); source.addProperty("matchesAfter", edit.isAfter()); observed.add(source);
            }
            outcome.add("sourceChecks", observed);
        } catch (RuntimeException inspectionFailure) {
            outcome.addProperty("inspectionFailure", inspectionFailure.getClass().getSimpleName());
            recovery = true; state = "RECOVERY_REQUIRED"; outcome.addProperty("state", state);
        }
        final boolean needsRecovery = recovery;
        final String recordedState = state;
        plan.ledger.outcome(plan.id, state, outcome.toString(), recovery).whenComplete((unused, error) -> resume(plan.server, () -> {
            if (LedgerRuntime.ledger() != plan.ledger) return;
            active = null;
            if (error != null) {
                FoundryBulkCommerce.LOGGER.error("Outcome {} could not be durably acknowledged", plan.id, error);
                LedgerRuntime.quarantine(plan.server, "Outcome acknowledgement failure " + plan.id);
                return;
            }
            if (needsRecovery) return;
            boolean sold = recordedState.equals("APPLIED_MEMORY");
            if (plan.server.getPlayerList().getPlayer(plan.player.getUUID()) == plan.player) {
                feedback(plan.player, sold ? "Sold " + plan.quantity + " items for " + plan.payment + " Spurs. Reference: " + plan.id
                    : "Sale " + recordedState + ". Reference: " + plan.id, sold);
                if (plan.skipped > 0) feedback(plan.player, "Skipped " + plan.skipped + " unsupported or duplicate-identity boxes.", false);
                refresh(plan);
            }
        }));
        if (recovery) LedgerRuntime.quarantine(plan.server, "Manual reconciliation required for " + plan.id);
    }

    private static void refresh(Plan plan) {
        try {
            plan.player.getInventory().setChanged(); plan.player.inventoryMenu.broadcastChanges();
            if (plan.player.level().dimension().location().toString().equals(plan.dimension) && validShop(plan.player, plan.pos) == plan.shop)
                TreasuryNetwork.openShop(plan.player, plan.shop);
        } catch (RuntimeException ignored) { /* A changed/closed screen never reverses a settled transaction. */ }
    }
    private static void resume(MinecraftServer server, Runnable work) {
        // If shutdown drops a continuation, its durable PREPARED intent forces conservative recovery.
        if (!server.isStopped()) server.execute(work);
    }
    private static void feedback(ServerPlayer player, String message, boolean success) {
        player.sendSystemMessage(Component.literal(message).withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
    private record Plan(UUID id, ServerPlayer player, MinecraftServer server, LocalLedger ledger, ServerShopBlockEntity shop,
        String dimension, BlockPos pos, ItemStack template, int lotSize, int price, int quantity, int payment,
        List<Transaction.Edit> edits, AccountWallet wallet, long started, int skipped, boolean vanilla, boolean sophisticated) {}

    private static final class AccountWallet implements Transaction.Wallet {
        private final ServerPlayer player;
        private final BankAccount account;
        private final BankAccountAccessor access;
        private final int beforeBalance;
        private final long beforeReserve;
        private final long beforeTotal;
        AccountWallet(ServerPlayer player, BankAccount account) {
            this.player = player;
            this.account = account;
            this.access = (BankAccountAccessor)(Object)account;
            beforeBalance = access.foundry$getBalance();
            beforeReserve = access.foundry$getAdditionalBalance();
            if (beforeBalance < 0 || beforeBalance > 1073741823 || beforeReserve < 0)
                throw new IllegalStateException("Invalid account balance");
            beforeTotal = Math.addExact((long)beforeBalance, beforeReserve);
        }
        void checkCapacity(int amount) { Math.addExact(beforeTotal, (long)amount); }
        private boolean sameAccount() { return BankService.existingAccount(player.getUUID()) == account; }
        public boolean isBefore() {
            return sameAccount() && access.foundry$getBalance() == beforeBalance && access.foundry$getAdditionalBalance() == beforeReserve;
        }
        public void credit(int amount) {
            if (!isBefore() || !BankService.credit(player.getUUID(), amount)) throw new IllegalStateException("Account credit failed");
        }
        public boolean isCredited(int amount) {
            return sameAccount() && access.foundry$getBalance() >= 0 && access.foundry$getAdditionalBalance() >= 0
                && Math.addExact((long)access.foundry$getBalance(), access.foundry$getAdditionalBalance()) == Math.addExact(beforeTotal, (long)amount);
        }
        public void restore() {
            if (!sameAccount()) throw new IllegalStateException("Account identity changed; automatic compensation refused");
            access.foundry$setBalance(beforeBalance);
            access.foundry$setAdditionalBalance(beforeReserve);
            account.markDirty();
        }
    }
}
