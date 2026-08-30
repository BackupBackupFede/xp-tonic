package net.emeraude.betterexp.fabric.mixin;

import net.emeraude.betterexp.BetterEXPContent;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric's registration window.
 *
 * <p>Fabric Loader on its own does not reopen the vanilla registries: by the time a
 * {@code ModInitializer} runs, {@code BuiltInRegistries.bootStrap()} has already frozen them, and
 * adding an entry throws "Registry is already frozen". Only Fabric API's registry-sync module
 * unfreezes them — and this mod deliberately has no Fabric API dependency.
 *
 * <p>So the entries go in at the last moment vanilla itself leaves open: the head of
 * {@code freeze()}, after {@code createContents()} has added every vanilla entry. Client and server
 * both run this bootstrap, in the same place, so the resulting registry ids match on both ends.
 *
 * <p>NeoForge does not need this — it has {@code RegisterEvent} — so this mixin is in the Fabric
 * module and is listed only by that loader's mixin config.
 */
@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {

    @Inject(method = "freeze", at = @At("HEAD"))
    private static void betterexp$registerBeforeFreeze(CallbackInfo ci) {
        BetterEXPContent.register();
    }
}
