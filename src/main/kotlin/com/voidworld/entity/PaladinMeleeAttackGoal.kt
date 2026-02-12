package com.voidworld.entity

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal

/**
 * MeleeAttackGoal with 1.5x attack reach for Paladin.
 */
class PaladinMeleeAttackGoal(
    mob: PathfinderMob,
    speed: Double,
    pauseWhenMobIdle: Boolean
) : MeleeAttackGoal(mob, speed, pauseWhenMobIdle) {

    override fun getAttackReachSqr(target: LivingEntity): Double {
        return super.getAttackReachSqr(target) * 2.25  // 1.5^2 for squared distance
    }
}
