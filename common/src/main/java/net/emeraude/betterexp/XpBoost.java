package net.emeraude.betterexp;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * The multiplier itself, applied to every experience amount a player is granted — mob kills, ores,
 * furnaces, fishing, breeding — from {@code Player.giveExperiencePoints}.
 */
public final class XpBoost {

    /**
     * Multiplier at amplifier 0, i.e. what the baseline and extended bottles give.
     *
     * <p>Started at 2, then 3, now 5 — each step taken because playtesting still read as flat. A
     * multiplier on a mob worth 5 points is small in absolute terms whatever the factor, so what
     * finally makes the potion feel like treasure is a big factor over a long duration. This is
     * the one number to turn if it now overshoots.
     */
    public static final int BASE_MULTIPLIER = 5;

    private XpBoost() {}

    /** {@return the experience {@code amount} the player should actually receive} */
    public static int multiply(Player player, int amount) {
        // Negative amounts are costs, not rewards: enchanting and anvil work arrive here as
        // negatives, and multiplying one would make the potion double the price of enchanting.
        // Zero is left alone so the result can never differ from vanilla for a no-op grant.
        if (amount <= 0) return amount;

        // The experience economy is the server's; the client never calls this in vanilla.
        if (player.level().isClientSide()) return amount;

        Holder<MobEffect> effect = BetterEXPContent.xpBoostEffect();
        if (effect == null) return amount;

        MobEffectInstance active = player.getEffect(effect);
        if (active == null) return amount;

        // Amplifier 0 is x5 (the baseline and extended bottles), amplifier 1 is x6 (the
        // concentrated one). Reading the amplifier rather than branching on the potion means
        // /effect and any future variant work for free. Drinking again while boosted refreshes the
        // duration at the same amplifier — vanilla's own rule — so the multiplier never stacks.
        int factor = BASE_MULTIPLIER + active.getAmplifier();

        // Integer multiplication: the result is always a whole number and always >= vanilla's, so
        // there is no rounding step that could pay a boosted player less than an unboosted one.
        long boosted = (long) amount * factor;
        return (int) Math.min(boosted, Integer.MAX_VALUE);
    }
}
