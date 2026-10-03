package net.emeraude.betterexp;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;

/** Zombie moved from entity.monster to entity.monster.zombie in 26.x, hence one copy per line. */
final class SelfTestMobs {

    private SelfTestMobs() {}

    static LivingEntity hostile(ServerLevel level) {
        return new Zombie(level);
    }
}
