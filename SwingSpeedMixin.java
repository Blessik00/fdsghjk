package dev.pulse.pvp.mixin;

import dev.pulse.pvp.Anim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** LivingEntity#getHandSwingDuration (intermediary class_1309#method_6119). */
@Mixin(targets = "net.minecraft.class_1309")
public class SwingSpeedMixin {
    @Inject(method = "method_6119", at = @At("RETURN"), cancellable = true, require = 0)
    private void pulse$swing(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Anim.swingDuration(this, cir.getReturnValue()));
    }
}
