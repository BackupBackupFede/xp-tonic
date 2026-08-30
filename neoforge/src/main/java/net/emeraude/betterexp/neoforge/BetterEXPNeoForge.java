package net.emeraude.betterexp.neoforge;

import net.emeraude.betterexp.BetterEXP;
import net.emeraude.betterexp.BetterEXPContent;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

/** NeoForge entry point (both sides). All logic is loader-agnostic in {@link BetterEXP}. */
@Mod(BetterEXP.MOD_ID)
public final class BetterEXPNeoForge {

    public BetterEXPNeoForge(IEventBus modBus, ModContainer container) {
        BetterEXP.init();
        modBus.addListener(RegisterEvent.class, BetterEXPNeoForge::onRegister);
    }

    /**
     * NeoForge unfreezes every vanilla registry for the whole RegisterEvent phase, so both entries
     * can be added from one pass. Doing it that way keeps "effect before potion" enforced by
     * {@link BetterEXPContent} itself rather than by NeoForge's registry iteration order.
     */
    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.MOB_EFFECT)) {
            BetterEXPContent.register();
        }
    }
}
