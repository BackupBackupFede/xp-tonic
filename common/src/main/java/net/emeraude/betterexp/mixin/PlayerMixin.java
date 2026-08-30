package net.emeraude.betterexp.mixin;

import net.emeraude.betterexp.XpBoost;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Multiplies experience at the single point every source funnels through. A mixin rather than a
 * loader event because Fabric has no equivalent of NeoForge's XP events, and this keeps one
 * implementation for both.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "giveExperiencePoints", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int betterexp$boost(int amount) {
        return XpBoost.multiply((Player) (Object) this, amount);
    }
}
