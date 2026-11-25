package com.pandafoundry.bulkcommerce;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BulkConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue VANILLA;
    public static final ModConfigSpec.BooleanValue SOPHISTICATED;
    public static final ModConfigSpec.IntValue LIMIT;
    public static final ModConfigSpec.ConfigValue<String> INSTANCE;
    public static final ModConfigSpec.IntValue REQUEST_INTERVAL;
    public static final ModConfigSpec.IntValue SCAN_LIMIT;
    public static final ModConfigSpec.IntValue SNAPSHOT_LIMIT;
    public static final ModConfigSpec.BooleanValue DEBUG_TRANSACTIONS;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLED = builder.comment("Enable bulk Sell All. False refuses Sell All rather than bypassing the ledger.").define("enabled", true);
        DEBUG_TRANSACTIONS = builder.comment("Log transaction phases at INFO for diagnosis. Includes IDs, quantities and outcome categories; never full inventory snapshots.")
                .define("debugTransactions", false);
        VANILLA = builder.comment("Include carried vanilla shulker boxes.").define("vanillaShulkers", true);
        SOPHISTICATED = builder.comment("Include supported Sophisticated Storage shulkers if installed.").define("sophisticatedShulkers", true);
        LIMIT = builder.comment("Maximum individual items per Sell All request, including loose items. Complete lots only.",
                "Hard ceiling bounds synchronous work; 2304 preserves Treasury's original default quantity limit.")
                .defineInRange("bulkTransactionLimit", 2304, 1, 1048576);
        INSTANCE = builder.comment("Optional stable server instance ID. Empty generates and persists a UUID on first start.",
                "Changing the ID of an existing ledger requires an explicit offline migration.")
                .define("serverInstanceId", "", value -> value instanceof String text && (text.isEmpty() || text.matches("[A-Za-z0-9_.:-]{1,64}")));
        REQUEST_INTERVAL = builder.comment("Global minimum interval between admitted Sell All requests, including unsuccessful scans.")
                .defineInRange("requestIntervalMillis", 250, 50, 60000);
        SCAN_LIMIT = builder.comment("Maximum outer/serialized slot inspections per request; also the ceiling for one container's API-validated inventory capacity.")
                .defineInRange("maxScannedSlots", 4096, 64, 32768);
        SNAPSHOT_LIMIT = builder.comment("Separate per-request caps for total scanned binary NBT bytes and final UTF-8 intent bytes. Oversized requests fail before mutations.")
                .defineInRange("maxSnapshotBytes", 1048576, 16384, 4194304);
        SPEC = builder.build();
    }
    private BulkConfig() {}
}
