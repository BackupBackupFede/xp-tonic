package net.emeraude.betterexp.neoforge;

import net.emeraude.betterexp.BetterEXP;
import net.emeraude.betterexp.client.BetterEXPClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge CLIENT entry point (Tier B/C). A second @Mod class on the same mod id, restricted to
 * Dist.CLIENT, so it is never constructed on a dedicated server.
 * Delete this class for a Tier A mod.
 */
@Mod(value = BetterEXP.MOD_ID, dist = Dist.CLIENT)
public final class BetterEXPNeoForgeClient {

    public BetterEXPNeoForgeClient(IEventBus modBus, ModContainer container) {
        BetterEXPClient.initClient();
    }
}
