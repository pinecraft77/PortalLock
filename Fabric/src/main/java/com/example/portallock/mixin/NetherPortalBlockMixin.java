package com.example.portallock.mixin;

import com.example.portallock.ReflectPortalLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.block.NetherPortalBlock", remap = false)
public abstract class NetherPortalBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true, remap = false)
    private void portallock$onNetherPortalInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean bl,
            CallbackInfo ci
    ) {
        ReflectPortalLogic.onNetherPortalInside(level, pos, entity, ci);
    }
}
