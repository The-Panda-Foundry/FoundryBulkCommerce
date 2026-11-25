package com.pandafoundry.bulkcommerce.mixin;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import java.net.SocketAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerList.class, remap = false)
public abstract class PlayerAdmissionMixin {
    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true, require = 1)
    private void foundry$recoveryGate(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Component> ci) {
        if (!LedgerRuntime.admissionOpen()) ci.setReturnValue(Component.literal("Commerce ledger is starting or requires administrator recovery. Please try again later."));
    }
}
