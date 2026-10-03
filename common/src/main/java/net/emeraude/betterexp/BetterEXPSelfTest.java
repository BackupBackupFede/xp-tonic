package net.emeraude.betterexp;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Behaviour check for a dedicated server, off unless the environment variable
 * {@code BETTEREXP_SELFTEST=true} is set. Runs the real mixins on a connectionless player and real
 * zombies, checks the outcome, and stops the server — one command per loader per Minecraft line.
 *
 * <p>Every check that expects the mod to act has a control beside it that expects vanilla, so a
 * green board cannot come from a test that measures nothing.
 */
public final class BetterEXPSelfTest {

    private static final String SWITCH = BetterEXP.MOD_ID.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "") + "_SELFTEST";

    /** Expected potions per run of KILLS is 10 at 2.5%; P(0) is about 4e-5, P(>40) negligible. */
    private static final int KILLS = 400;

    /** Item stacks spawned while the drop check runs, recorded by SelfTestDropsMixin; null otherwise. */
    private static List<ItemStack> captured;

    private BetterEXPSelfTest() {}

    /** {@return true if the stack was recorded, in which case the entity must not be spawned} */
    public static boolean capture(ItemStack stack) {
        if (captured == null) return false;
        captured.add(stack.copy());
        return true;
    }

    public static boolean requested() {
        return "true".equalsIgnoreCase(System.getenv(SWITCH));
    }

    public static void run(MinecraftServer server) {
        List<String> failures = new ArrayList<>();
        try {
            ServerLevel level = server.overworld();
            check(failures, "registered", registered());
            check(failures, "boost x5", xpGained(server, level, 0, 10, 50));
            check(failures, "boost x6", xpGained(server, level, 1, 10, 60));
            check(failures, "control: no effect", xpGained(server, level, -1, 10, 10));
            check(failures, "costs untouched", costUntouched(server, level));
            check(failures, "drop", drops(server, level));
        } catch (Throwable t) {
            BetterEXP.LOGGER.error("[SELFTEST] crashed", t);
            failures.add("crash: " + t);
        }
        BetterEXP.LOGGER.info("[SELFTEST] RESULT {}", failures.isEmpty() ? "PASS" : "FAIL " + failures);
        server.halt(false);
    }

    private static void check(List<String> failures, String name, String error) {
        if (error == null) {
            BetterEXP.LOGGER.info("[SELFTEST] {} OK", name);
        } else {
            BetterEXP.LOGGER.error("[SELFTEST] {} FAILED: {}", name, error);
            failures.add(name + ": " + error);
        }
    }

    private static String registered() {
        if (BetterEXPContent.xpBoostEffect() == null) return "effect not registered";
        List<String> missing = new ArrayList<>();
        for (String path : new String[] {"xp_boost", "long_xp_boost", "strong_xp_boost"}) {
            String id = BetterEXP.MOD_ID + ":" + path;
            boolean found = false;
            for (var potion : BuiltInRegistries.POTION) {
                if (id.equals(String.valueOf(BuiltInRegistries.POTION.getKey(potion)))) found = true;
            }
            if (!found) missing.add(id);
        }
        return missing.isEmpty() ? null : "potions missing: " + missing;
    }

    /** Grants {@code amount} under the given amplifier (-1 = no effect) and compares the total. */
    private static String xpGained(MinecraftServer server, ServerLevel level, int amplifier, int amount, int expected) {
        ServerPlayer player = player(server, level);
        if (amplifier >= 0) player.addEffect(new MobEffectInstance(BetterEXPContent.xpBoostEffect(), 600, amplifier));
        player.giveExperiencePoints(amount);
        return player.totalExperience == expected ? null : "gained " + player.totalExperience + ", expected " + expected;
    }

    /** Enchanting and anvils arrive as negative amounts: a boosted player must not pay more. */
    private static String costUntouched(MinecraftServer server, ServerLevel level) {
        ServerPlayer player = player(server, level);
        player.addEffect(new MobEffectInstance(BetterEXPContent.xpBoostEffect(), 600, 0));
        player.giveExperiencePoints(100);
        int before = player.totalExperience;
        player.giveExperiencePoints(-7);
        int paid = before - player.totalExperience;
        return paid == 7 ? null : "a cost of 7 took " + paid;
    }

    /**
     * Kills zombies for real ({@code die}), once with a player as the killer and once with no
     * killer. The mod must drop potions only in the first case. Rotten flesh from the vanilla loot
     * table proves the drops are actually being counted. Nothing is spawned: see SelfTestDropsMixin.
     */
    private static String drops(MinecraftServer server, ServerLevel level) {
        ServerPlayer killer = player(server, level);
        int[] byPlayer = killAndCount(level, level.damageSources().playerAttack(killer));
        int[] byNothing = killAndCount(level, level.damageSources().generic());
        BetterEXP.LOGGER.info("[SELFTEST] drop: {} potions / {} flesh by player, {} potions / {} flesh by no one",
                byPlayer[0], byPlayer[1], byNothing[0], byNothing[1]);

        if (byPlayer[1] == 0 || byNothing[1] == 0) return "no rotten flesh counted either - the count is blind";
        if (byNothing[0] != 0) return byNothing[0] + " potions without a player killer";
        if (byPlayer[0] < 1 || byPlayer[0] > 40) return byPlayer[0] + " potions from " + KILLS + " player kills (expect ~10)";
        return null;
    }

    /** {@return {potions, rotten flesh} dropped by KILLS zombies} */
    private static int[] killAndCount(ServerLevel level, DamageSource source) {
        captured = new ArrayList<>();
        try {
            for (int i = 0; i < KILLS; i++) {
                LivingEntity mob = SelfTestMobs.hostile(level);
                mob.setPos(0.5, 100, 0.5);
                mob.die(source);
            }
            int potions = 0, flesh = 0;
            for (ItemStack stack : captured) {
                if (stack.is(Items.POTION)) potions += stack.getCount();
                if (stack.is(Items.ROTTEN_FLESH)) flesh += stack.getCount();
            }
            return new int[] {potions, flesh};
        } finally {
            captured = null;
        }
    }

    private static ServerPlayer player(MinecraftServer server, ServerLevel level) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "selftest");
        ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        return player;
    }
}
