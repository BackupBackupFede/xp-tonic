package net.emeraude.betterexp.client;

import net.emeraude.betterexp.BetterEXP;

/**
 * Loader-agnostic CLIENT entry point — TIER B/C logic goes here.
 *
 * <p>Reached only from the loaders' client entry points, never from {@link BetterEXP#init()}:
 * a dedicated server must never load this class.
 *
 * <p>Keep as little as possible here. Rendering APIs diverge both between loaders and between
 * Minecraft versions, so client code is what makes a port expensive — anything that can live in
 * {@link BetterEXP} should.
 *
 * <p>A pure server-side mod (Tier A) deletes this class and the two loader client entry points,
 * and drops "client" from fabric.mod.json + the Dist.CLIENT @Mod class.
 */
public final class BetterEXPClient {

    private static boolean initialized = false;

    private BetterEXPClient() {}

    public static synchronized void initClient() {
        if (initialized) return;
        initialized = true;

        BetterEXP.LOGGER.info("{} client loaded", BetterEXP.MOD_ID);
    }
}
