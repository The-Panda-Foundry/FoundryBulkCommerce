package com.pandafoundry.bulkcommerce;

import net.minecraft.client.Minecraft;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.LogTestReporter;
import net.minecraft.gametest.framework.TestReporter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Disposable development fixture, excluded from production/source JARs. Never loads in dedicated runs. */
@Mod(value = FoundryBulkCommerce.MOD_ID, dist = Dist.CLIENT)
public final class IntegratedAcceptanceHarness {
    public IntegratedAcceptanceHarness() {
        if (Boolean.getBoolean("foundrybulkcommerce.integratedAcceptance")) NeoForge.EVENT_BUS.addListener(this::started);
    }
    private void started(ServerStartedEvent event) {
        if (!(event.getServer() instanceof net.minecraft.client.server.IntegratedServer)) throw new IllegalStateException("Integrated acceptance requires physical CLIENT and a real integrated server");
        GlobalTestReporter.replaceWith(new TestReporter() {
            private final LogTestReporter log = new LogTestReporter();
            public void onTestFailed(GameTestInfo info) { log.onTestFailed(info); finish(false); }
            public void onTestSuccess(GameTestInfo info) { finish(true); }
            private void finish(boolean passed) {
                FoundryBulkCommerce.LOGGER.info("[BulkCommerce acceptance] physical CLIENT / integrated server result={}", passed ? "PASS" : "FAIL");
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().stop());
            }
        });
        event.getServer().execute(() -> event.getServer().getCommands().performPrefixedCommand(
            event.getServer().createCommandSourceStack(), "test run installedadapters"));
    }
}
