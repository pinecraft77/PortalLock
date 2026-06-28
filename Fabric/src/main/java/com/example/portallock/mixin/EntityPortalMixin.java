package com.example.portallock.mixin;

import com.example.portallock.ReflectPortalLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer", remap = false)
public abstract class EntityPortalMixin {
    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private void portallock$tick(CallbackInfo ci) {
        ReflectPortalLogic.tick((Object) this);
    }
}
