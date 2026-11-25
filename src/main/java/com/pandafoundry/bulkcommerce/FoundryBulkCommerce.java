package com.pandafoundry.bulkcommerce;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import com.pandafoundry.bulkcommerce.ledger.LedgerCommands;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FoundryBulkCommerce.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class FoundryBulkCommerce {
    public static final String MOD_ID = "foundrybulkcommerce";
    public static final Logger LOGGER = LogUtils.getLogger();
    public FoundryBulkCommerce(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, BulkConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(LedgerRuntime::starting);
        NeoForge.EVENT_BUS.addListener(LedgerRuntime::onStopping);
        NeoForge.EVENT_BUS.addListener(LedgerRuntime::stopped);
        NeoForge.EVENT_BUS.addListener(LedgerCommands::register);
        LOGGER.info("Foundry: Bulk Commerce 0.1.0 loaded; sells run only on Treasury Sell All requests");
    }
}
