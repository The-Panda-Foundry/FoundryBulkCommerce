package com.pandafoundry.bulkcommerce.mixin;

import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** ServerStoppedEvent also fires after crashes: distinguish normal loop exit and a returned save/close path. */
@Mixin(value = MinecraftServer.class, remap = false)
public abstract class ServerShutdownMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/server/ServerLifecycleHooks;handleServerStopping(Lnet/minecraft/server/MinecraftServer;)V", shift = At.Shift.AFTER), require = 1)
    private void foundry$orderlyExit(CallbackInfo ci) { LedgerRuntime.orderlyLoopExit(); }
    @Inject(method = "stopServer", at = @At("RETURN"), require = 1)
    private void foundry$saveReturned(CallbackInfo ci) { LedgerRuntime.savesReturned(); }
}
