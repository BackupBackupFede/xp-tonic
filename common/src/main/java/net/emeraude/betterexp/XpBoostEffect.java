package net.emeraude.betterexp;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The XP Boost effect. It carries no per-tick behaviour: {@link XpBoost} reads it off the player at
 * the moment experience is granted, which is both cheaper and impossible to desynchronise.
 *
 * <p>The colour is vanilla's experience-orb green. It tints the potion bottle and the effect
 * particles, so it is the only "model" work this mod needs.
 */
public final class XpBoostEffect extends MobEffect {

    private static final int EXPERIENCE_GREEN = 0x7FCC19;

    public XpBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, EXPERIENCE_GREEN);
    }
}
