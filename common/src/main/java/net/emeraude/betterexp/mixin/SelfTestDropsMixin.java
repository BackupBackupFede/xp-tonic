package net.emeraude.betterexp.mixin;

import net.emeraude.betterexp.BetterEXPSelfTest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Self-test only: while the drop check runs, item entities are recorded instead of spawned. Counting
 * them in the world does not work on 26.x, where no chunk is entity-accessible during server init.
 * Inert whenever the self-test is not capturing.
 */
@Mixin(ServerLevel.class)
public abstract class SelfTestDropsMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void betterexp$captureDrop(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ItemEntity item && BetterEXPSelfTest.capture(item.getItem())) {
            cir.setReturnValue(true);
        }
    }
}
