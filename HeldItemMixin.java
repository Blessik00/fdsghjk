package dev.pulse.pvp.mixin;

import dev.pulse.pvp.Anim;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** net.minecraft.client.render.item.HeldItemRenderer (intermediary class_759). */
@Mixin(targets = "net.minecraft.class_759")
public class HeldItemMixin {
    @Inject(method = "method_3228", at = @At("HEAD"), require = 0)
    private void pulse$head(class_742 player, float tickDelta, float pitch, class_1268 hand, float swing,
                            class_1799 stack, float equip, class_4587 matrices, class_4597 vcp, int light,
                            CallbackInfo ci) {
        Anim.begin(player, hand, swing, matrices);
    }

    @Inject(method = "method_3228", at = @At("RETURN"), require = 0)
    private void pulse$return(class_742 player, float tickDelta, float pitch, class_1268 hand, float swing,
                              class_1799 stack, float equip, class_4587 matrices, class_4597 vcp, int light,
                              CallbackInfo ci) {
        Anim.end(matrices);
    }

    @ModifyVariable(method = "method_3228", at = @At("HEAD"), ordinal = 3, argsOnly = true, require = 0)
    private float pulse$equip(float equip) {
        return Anim.equip(equip);
    }
}
