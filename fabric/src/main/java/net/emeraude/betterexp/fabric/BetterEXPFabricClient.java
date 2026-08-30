package net.emeraude.betterexp.fabric;

import net.emeraude.betterexp.client.BetterEXPClient;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric CLIENT entry point (Tier B/C). Never loaded on a dedicated server.
 * Delete this class and the "client" entrypoint in fabric.mod.json for a Tier A mod.
 */
public final class BetterEXPFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BetterEXPClient.initClient();
    }
}
