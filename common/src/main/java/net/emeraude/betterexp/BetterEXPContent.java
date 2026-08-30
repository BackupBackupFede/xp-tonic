package net.emeraude.betterexp;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/**
 * The mod's static-registry entries: the XP Boost effect, and the three potions that grant it.
 *
 * <p>The three follow vanilla's own potion-family shape, copied from {@code Potions.STRENGTH} —
 * base, extended, concentrated — so the trade-off is one players already know rather than one this
 * mod invents. All three carry the same display name; the tooltip is what separates them, exactly
 * as vanilla's {@code long_strength} and {@code strong_strength} are both "Potion of Strength".
 *
 * <p>Both registries are written through vanilla
 * {@link Registry#register(Registry, String, Object)} — the String overload on purpose:
 * {@code ResourceLocation} was renamed to {@code Identifier} in MC 26.x, so naming that type
 * anywhere would fork this source per Minecraft version. The String overload has the same signature
 * in every target and parses "namespace:path" itself.
 *
 * <p>{@link #register()} must run inside each loader's registration window — immediately on Fabric,
 * inside {@code RegisterEvent} on NeoForge, where the vanilla registries are unfrozen. The effect is
 * registered before the potions because they hold a {@link Holder} of it.
 */
public final class BetterEXPContent {

    /**
     * Registry path of the effect, and the shared name of all three potions — which is what the
     * vanilla item translation keys are built from ("item.minecraft.potion.effect." + name).
     */
    public static final String XP_BOOST = "xp_boost";

    /**
     * 8:00 at x5 — the baseline bottle.
     *
     * <p>Deliberately far longer than a vanilla potion of the same shape. Vanilla's 3:00 is priced
     * for something you brew on demand; this one drops once in roughly forty kills, so its worth
     * has to be measured in what a player can plan a whole mining trip around.
     */
    public static final int DURATION_TICKS = 9600;

    /** 20:00 at x5 — the marathon. Rarest of the three, and the one worth the most overall. */
    public static final int LONG_DURATION_TICKS = 24000;

    /** 8:00 at x6 — same length as the baseline, hits harder. */
    public static final int STRONG_DURATION_TICKS = 9600;

    /** Amplifier 1, which {@link XpBoost} reads as x6. */
    public static final int STRONG_AMPLIFIER = 1;

    private static Holder<MobEffect> xpBoostEffect;
    private static Holder<Potion> xpBoostPotion;
    private static Holder<Potion> longXpBoostPotion;
    private static Holder<Potion> strongXpBoostPotion;

    private BetterEXPContent() {}

    public static synchronized void register() {
        if (xpBoostEffect != null) return;

        MobEffect effect = Registry.register(BuiltInRegistries.MOB_EFFECT, id(XP_BOOST), new XpBoostEffect());
        xpBoostEffect = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);

        xpBoostPotion = potion(XP_BOOST, DURATION_TICKS, 0);
        longXpBoostPotion = potion("long_" + XP_BOOST, LONG_DURATION_TICKS, 0);
        strongXpBoostPotion = potion("strong_" + XP_BOOST, STRONG_DURATION_TICKS, STRONG_AMPLIFIER);

        BetterEXP.LOGGER.info("registered effect {} and its three potions", id(XP_BOOST));
    }

    /**
     * {@return the XP Boost effect, or null before registration has run}
     *
     * <p>Nullable on purpose: the mixins are installed before registration and must stay inert until
     * it has happened.
     */
    public static Holder<MobEffect> xpBoostEffect() {
        return xpBoostEffect;
    }

    /** {@return the 3:00 x2 potion, or null before registration has run} */
    public static Holder<Potion> xpBoostPotion() {
        return xpBoostPotion;
    }

    /** {@return the 8:00 x2 potion, or null before registration has run} */
    public static Holder<Potion> longXpBoostPotion() {
        return longXpBoostPotion;
    }

    /** {@return the 1:30 x3 potion, or null before registration has run} */
    public static Holder<Potion> strongXpBoostPotion() {
        return strongXpBoostPotion;
    }

    private static Holder<Potion> potion(String path, int duration, int amplifier) {
        Potion potion = Registry.register(
                BuiltInRegistries.POTION,
                id(path),
                new Potion(XP_BOOST, new MobEffectInstance(xpBoostEffect, duration, amplifier)));
        return BuiltInRegistries.POTION.wrapAsHolder(potion);
    }

    private static String id(String path) {
        return BetterEXP.MOD_ID + ":" + path;
    }
}
