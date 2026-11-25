package com.pandafoundry.bulkcommerce;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(value = FoundryBulkCommerce.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class FoundryBulkCommerce {
    public static final String MOD_ID = "foundrybulkcommerce";
    public static final Logger LOGGER = LogUtils.getLogger();
    public FoundryBulkCommerce(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, BulkConfig.SPEC);
        LOGGER.info("Foundry: Bulk Commerce 0.1.0 loaded; sells run only on Treasury Sell All requests");
    }
}
