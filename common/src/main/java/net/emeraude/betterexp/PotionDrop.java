package net.emeraude.betterexp;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * The only way to obtain the potions: a rare drop from hostile mobs killed by a player.
 *
 * <p>The rate is vanilla's own rare-drop convention, the one behind a zombie's iron ingot: 2.5%,
 * plus one percentage point per level of Looting, so 5.5% at Looting III. Reusing a number players
 * already have an intuition for beats inventing one.
 *
 * <p>One roll decides whether anything drops; a second decides which bottle. Nothing about the mob
 * or the dimension enters into it — rarity is the only axis, so there is no rule to document and
 * nothing to maintain as Minecraft adds mobs.
 *
 * <p>Called from the tail of {@code LivingEntity.dropCustomDeathLoot}, which vanilla only reaches
 * when the mob is allowed to drop loot at all — so the doMobLoot game rule is honoured for free.
 */
public final class PotionDrop {

    /** Drop chance with no Looting — vanilla's rare-drop rate. */
    public static final float BASE_CHANCE = 0.025F;

    /** Added per level of Looting on the killer, as vanilla rare drops do. */
    public static final float LOOTING_BONUS = 0.01F;

    /** Share of successful rolls that yield the concentrated bottle. */
    private static final float STRONG_SHARE = 0.25F;

    /**
     * Share that yields the marathon bottle. The rest is the baseline.
     *
     * <p>The extended one is rarer than the concentrated one on purpose: at twenty minutes it is
     * worth the most of the three, so it has to be the hardest to get. Rarity and payoff line up.
     */
    private static final float LONG_SHARE = 0.15F;

    private PotionDrop() {}

    public static void onMobKilled(LivingEntity victim, ServerLevel level, DamageSource source) {
        if (victim.getType().getCategory() != MobCategory.MONSTER) return;

        // The killing blow has to come from a player. getEntity() is the shooter for projectiles, so
        // bows count — but drowning, suffocation and fall-damage farms do not, which is the point.
        if (!(source.getEntity() instanceof Player killer)) return;

        float chance = BASE_CHANCE + LOOTING_BONUS * lootingLevel(level, killer);
        if (victim.getRandom().nextFloat() >= chance) return;

        Holder<Potion> potion = rollVariant(victim.getRandom().nextFloat());
        if (potion == null) return;

        ItemStack bottle = PotionContents.createItemStack(Items.POTION, potion);
        ItemEntity drop = new ItemEntity(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), bottle);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }

    /** {@return the bottle this roll yields, or null before registration has run} */
    private static Holder<Potion> rollVariant(float roll) {
        if (roll < STRONG_SHARE) return BetterEXPContent.strongXpBoostPotion();
        if (roll < STRONG_SHARE + LONG_SHARE) return BetterEXPContent.longXpBoostPotion();
        return BetterEXPContent.xpBoostPotion();
    }

    /**
     * {@return the killer's Looting level, or 0}
     *
     * <p>Looked up through the level's registry rather than a hardcoded holder: enchantments are
     * data-driven since 1.21, so the entry only exists once a world is loaded.
     */
    private static int lootingLevel(ServerLevel level, Player killer) {
        return level.holderLookup(Registries.ENCHANTMENT)
                .get(Enchantments.LOOTING)
                .map(looting -> EnchantmentHelper.getEnchantmentLevel(looting, killer))
                .orElse(0);
    }
}
