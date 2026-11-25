package com.pandafoundry.bulkcommerce.mixin;

import com.google.gson.JsonObject;
import com.pandafoundry.bulkcommerce.BulkSales;
import dev.yassou.numismaticstreasury.network.ServerActionHandler;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerActionHandler.class, remap = false)
public abstract class TreasurySellAllMixin {
    // This invocation is reached only AFTER Treasury's module/proximity/block/configured/mode guards.
    @Inject(method = "tradeShop(Lnet/minecraft/server/level/ServerPlayer;Lcom/google/gson/JsonObject;Z)V",
        at = @At(value = "INVOKE", target = "Ldev/yassou/numismaticstreasury/server/InventoryUtil;countMatching(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)I", ordinal = 0),
        cancellable = true, require = 1, expect = 1)
    private static void foundry$sellAll(ServerPlayer player, JsonObject data, boolean sellAll, CallbackInfo ci) {
        if (!sellAll) return;
        ci.cancel(); // Never fall back to an unverified payment after an addon failure.
        BulkSales.sell(player, data);
    }
}
