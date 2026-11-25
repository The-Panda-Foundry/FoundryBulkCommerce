package com.pandafoundry.bulkcommerce.ledger;

import com.pandafoundry.bulkcommerce.BulkConfig;
import com.pandafoundry.bulkcommerce.BulkSales;
import com.pandafoundry.bulkcommerce.FoundryBulkCommerce;
import com.pandafoundry.bulkcommerce.RuntimeCompatibility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class LedgerRuntime {
    private static volatile LocalLedger ledger;
    private static volatile boolean stopping = true, emergency;
    private static boolean orderlyLoopExit, savesReturned;
    private LedgerRuntime() {}
    public static LocalLedger ledger() { return ledger; }
    public static boolean stopping() { return stopping; }
    public static boolean admissionOpen() {
        LocalLedger local = ledger;
        return !stopping && !emergency && local != null && local.health().ready() && !local.health().blocked();
    }
    public static void starting(ServerStartingEvent event) {
        stopping = false; emergency = false; orderlyLoopExit = false; savesReturned = false;
        BulkSales.reset();
        MinecraftServer server = event.getServer();
        LocalLedger local = new LocalLedger(server.getWorldPath(LevelResource.ROOT).resolve("data/foundrybulkcommerce/ledger.sqlite"), BulkConfig.INSTANCE.get());
        ledger = local;
        try {
            local.open(RuntimeCompatibility.verifier()).whenComplete((health, error) -> {
                if (error != null) {
                    emergency = true;
                    FoundryBulkCommerce.LOGGER.error("Commerce ledger failed to open; player admission remains closed", error);
                } else FoundryBulkCommerce.LOGGER.info("Commerce ledger ready: instance={}, recoveryBlocked={}", health.instance(), health.blocked());
            });
        } catch (RuntimeException failure) {
            emergency = true;
            FoundryBulkCommerce.LOGGER.error("Commerce compatibility verification could not start; admission remains closed", failure);
        }
    }
    public static void onStopping(ServerStoppingEvent event) { stopping = true; savesReturned = false; }
    public static void orderlyLoopExit() { orderlyLoopExit = true; }
    public static void savesReturned() { savesReturned = true; }
    public static void stopped(ServerStoppedEvent event) {
        stopping = true;
        LocalLedger local = ledger;
        if (local != null) local.close(orderlyLoopExit && savesReturned && !emergency && !BulkSales.hasActive()).whenComplete((unused, error) -> {
            if (error != null) FoundryBulkCommerce.LOGGER.error("Ledger shutdown failed; recovery will be required", error);
        });
    }
    public static void quarantine(MinecraftServer server, String reason) {
        emergency = true;
        LocalLedger local = ledger;
        if (local != null) local.trip(reason).exceptionally(error -> {
            FoundryBulkCommerce.LOGGER.error("Could not persist emergency breaker; retain the journal and unclean boot evidence", error);
            return null;
        });
        // A bulk-only pause would leave ambiguous funds available through other mods or manual shop actions.
        for (var player : java.util.List.copyOf(server.getPlayerList().getPlayers()))
            player.connection.disconnect(Component.literal("Commerce recovery containment: " + reason + ". Please contact an administrator."));
    }
}
