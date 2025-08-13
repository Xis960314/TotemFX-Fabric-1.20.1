package dev.atcrock.totemfx.mixin;

import dev.atcrock.totemfx.TotemFX;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityTotemMixin {
    @Inject(method = "tryUseTotem", at = @At("RETURN"))
    private void totemfx$afterUse(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && ((Object)this) instanceof ServerPlayerEntity player) {
            TotemFX.sendStartFX(player);
        }
    }
}
