package net.emeraude.betterexp.mixin;

import java.util.OptionalInt;
import net.emeraude.betterexp.BetterEXPContent;
import net.emeraude.betterexp.XpBoostColor;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the bottle's liquid pulse instead of sitting on one flat green.
 *
 * <p>This is the one seam both Minecraft versions share: 1.21.1 reaches the tint through
 * {@code getColor()} and 26.2 through {@code getColorOr(int)}, but both end at this same static
 * method with the same signature — so one mixin in {@code common/} covers every target, with no
 * custom sprite and no client code.
 *
 * <p>Only mixes containing XP Boost are touched; every other potion keeps vanilla's average.
 */
@Mixin(PotionContents.class)
public abstract class PotionContentsMixin {

    @Inject(method = "getColorOptional", at = @At("HEAD"), cancellable = true)
    private static void betterexp$pulseXpBoost(
            Iterable<MobEffectInstance> effects, CallbackInfoReturnable<OptionalInt> cir) {
        Holder<MobEffect> xpBoost = BetterEXPContent.xpBoostEffect();
        if (xpBoost == null) return;

        for (MobEffectInstance instance : effects) {
            if (instance.getEffect().equals(xpBoost)) {
                cir.setReturnValue(OptionalInt.of(XpBoostColor.pulsing()));
                return;
            }
        }
    }
}
