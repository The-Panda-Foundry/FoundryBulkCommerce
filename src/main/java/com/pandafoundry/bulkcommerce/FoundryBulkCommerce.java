package com.pandafoundry.bulkcommerce;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import com.pandafoundry.bulkcommerce.ledger.LedgerCommands;
import net.neoforged.neoforge.common.NeoForge;

@Mod(FoundryBulkCommerce.MOD_ID)
public final class FoundryBulkCommerce {
    public static final String MOD_ID = "foundrybulkcommerce";
    public static final String VERSION = "0.2.2";
    public static final Logger LOGGER = LogUtils.getLogger();
    public FoundryBulkCommerce(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, BulkConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(LedgerRuntime::starting);
        NeoForge.EVENT_BUS.addListener(LedgerRuntime::onStopping);
        NeoForge.EVENT_BUS.addListener(LedgerRuntime::stopped);
        NeoForge.EVENT_BUS.addListener(LedgerCommands::register);
        LOGGER.info("[BulkCommerce] {} initialised on physical {}; server lifecycle and audit-command handlers registered. Ledger opens only for a local logical server.", VERSION, FMLEnvironment.dist);
    }
}
