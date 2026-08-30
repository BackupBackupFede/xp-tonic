package net.emeraude.betterexp.fabric;

import net.emeraude.betterexp.BetterEXP;
import net.fabricmc.api.ModInitializer;

/** Fabric entry point (both sides). All logic is loader-agnostic in {@link BetterEXP}. */
public final class BetterEXPFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        BetterEXP.init();
        // Registration already happened, from BuiltInRegistriesMixin: by the time this runs the
        // vanilla registries are frozen.
    }
}
