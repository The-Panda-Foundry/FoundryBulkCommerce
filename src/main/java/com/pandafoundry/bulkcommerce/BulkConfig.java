package com.pandafoundry.bulkcommerce;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BulkConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue VANILLA;
    public static final ModConfigSpec.BooleanValue SOPHISTICATED;
    public static final ModConfigSpec.IntValue LIMIT;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLED = builder.comment("Enable verified bulk Sell All. False leaves Treasury's original behaviour.").define("enabled", true);
        VANILLA = builder.comment("Include carried vanilla shulker boxes.").define("vanillaShulkers", true);
        SOPHISTICATED = builder.comment("Include supported Sophisticated Storage shulkers if installed.").define("sophisticatedShulkers", true);
        LIMIT = builder.comment("Maximum individual items per Sell All request, including loose items. Complete lots only.",
                "Hard ceiling bounds synchronous work; 2304 preserves Treasury's original default quantity limit.")
                .defineInRange("bulkTransactionLimit", 2304, 1, 1048576);
        SPEC = builder.build();
    }
    private BulkConfig() {}
}
