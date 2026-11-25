package com.pandafoundry.bulkcommerce;

import com.google.gson.JsonObject;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import com.pandafoundry.bulkcommerce.storage.ItemPolicy;
import com.pandafoundry.bulkcommerce.storage.SaleSource;
import com.pandafoundry.bulkcommerce.storage.SophisticatedSources;
import com.pandafoundry.bulkcommerce.storage.VanillaSources;
import com.pandafoundry.bulkcommerce.transaction.Transaction;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.yassou.numismaticstreasury.block.entity.ServerShopBlockEntity;
import dev.yassou.numismaticstreasury.block.entity.ShopMode;
import dev.yassou.numismaticstreasury.network.TreasuryNetwork;
import dev.yassou.numismaticstreasury.server.BankService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class BulkSales {
    // Server-thread-only circuit breaker and reentrancy guard; neither retains player/storage data.
    private static boolean recoveryRequired;
    private static boolean active;
    private BulkSales() {}

    public static void sell(ServerPlayer player, JsonObject data) {
        if (player.getServer() == null || !player.getServer().isSameThread())
            throw new IllegalStateException("Bulk selling must execute on the server thread");
        if (recoveryRequired || active) {
            feedback(player, "Bulk selling is paused. Please contact a server administrator.", false);
            return;
        }
        active = true;
        ServerShopBlockEntity shop = null;
        UUID transactionId = UUID.randomUUID();
        try {
            // The Mixin reaches here after all Treasury guards. Re-read the authoritative shop only.
            if (!(player.level().getBlockEntity(BlockPos.of(data.get("pos").getAsLong())) instanceof ServerShopBlockEntity found)
                    || !found.configured() || found.mode() != ShopMode.BUY_FROM_PLAYER)
                throw new IllegalStateException("Validated shop is no longer available");
            shop = found;
            ItemStack template = shop.template().copy();
            if (!ItemPolicy.saleable(template)) {
                feedback(player, "Bulk selling supports ordinary items; containers and block-entity items are excluded.", false);
                return;
            }
            int lotSize = shop.lotSize();
            int price = shop.price();
            if (lotSize <= 0 || price <= 0) throw new IllegalStateException("Invalid shop lot or price");
            List<SaleSource> sources = new ArrayList<>(VanillaSources.collect(player, template, BulkConfig.VANILLA.get()));
            int skipped = 0;
            if (BulkConfig.SOPHISTICATED.get() && ModList.get().isLoaded("sophisticatedstorage") && ModList.get().isLoaded("sophisticatedcore")) {
                var collected = SophisticatedSources.collect(player, template);
                sources.addAll(collected.sources());
                skipped = collected.skipped();
            }
            long available = 0;
            for (SaleSource source : sources) available = Math.addExact(available, source.available());
            int quantity = Transaction.quantity(available, lotSize, BulkConfig.LIMIT.get());
            if (quantity == 0) {
                feedback(player, "No complete lot fits the available items and bulk transaction limit.", false);
                if (skipped > 0) excluded(player, skipped);
                return;
            }
            int total = Math.multiplyExact(price, quantity / lotSize);
            BankAccount account = BankService.account(player);
            AccountWallet wallet = new AccountWallet(player, account);
            wallet.checkCapacity(total);
            List<Transaction.Edit> edits = new ArrayList<>();
            int remaining = quantity;
            for (SaleSource source : sources) {
                int take = (int)Math.min((long)remaining, source.available());
                if (take > 0) { edits.add(source.remove(take)); remaining -= take; }
                if (remaining == 0) break;
            }
            if (remaining != 0) throw new IllegalStateException("Sale plan underflow");
            var result = Transaction.commit(edits, wallet, total);
            if (result.successful()) {
                feedback(player, "Sold " + quantity + " items for " + total + " Spurs.", true);
            } else {
                recoveryRequired = result.recoveryRequired();
                FoundryBulkCommerce.LOGGER.error("Bulk sale {} for {} failed; recoveryRequired={}; quantity={}, payment={}",
                    transactionId, player.getUUID(), recoveryRequired, quantity, total, result.failure());
                if (recoveryRequired) {
                    FoundryBulkCommerce.LOGGER.error("RECONCILE {}: original balance={}, reserve={}; current balance={}, reserve={}",
                        transactionId, wallet.beforeBalance, wallet.beforeReserve, wallet.access.foundry$getBalance(), wallet.access.foundry$getAdditionalBalance());
                    for (Transaction.Edit edit : edits)
                        FoundryBulkCommerce.LOGGER.error("RECONCILE {}: {}", transactionId, edit.recoveryDetails());
                    feedback(player, "Bulk sale needs administrator recovery. Bulk selling has been paused. Reference: " + transactionId, false);
                } else feedback(player, "Sale cancelled; item changes and any attempted payment were rolled back.", false);
            }
            if (skipped > 0) excluded(player, skipped);
        } catch (RuntimeException failure) {
            // Planning uses copies. Transaction.commit handles every mutation/credit failure internally.
            FoundryBulkCommerce.LOGGER.error("Bulk sale {} for {} could not be prepared", transactionId, player.getUUID(), failure);
            feedback(player, "Bulk sale could not be completed. Reference: " + transactionId, false);
        } finally {
            // A failed screen refresh must not undo a successfully settled sale.
            try {
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                if (shop != null) TreasuryNetwork.openShop(player, shop);
            } catch (RuntimeException refreshFailure) {
                FoundryBulkCommerce.LOGGER.warn("Could not refresh inventory/shop after bulk sale {}", transactionId, refreshFailure);
            }
            active = false;
        }
    }

    private static void excluded(ServerPlayer player, int count) {
        feedback(player, "Skipped " + count + " unsupported, linked, legacy or duplicate-identity shulker boxes.", false);
    }
    private static void feedback(ServerPlayer player, String text, boolean success) {
        player.sendSystemMessage(Component.literal(text).withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

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
