package net.emeraude.betterexp.mixin;

import net.emeraude.betterexp.PotionDrop;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the potion to what a mob drops on death. Injected into {@code LivingEntity} rather than into
 * a loot table so that the drop applies to modded hostile mobs too, without touching their loot.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
    private void betterexp$dropXpBoostPotion(ServerLevel level, DamageSource source, boolean killedByPlayer, CallbackInfo ci) {
        PotionDrop.onMobKilled((LivingEntity) (Object) this, level, source);
    }
}
