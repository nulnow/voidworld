package com.voidworld.entity

import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

/**
 * Simple follow-owner goal for SummonedZombie.
 * Follows the owner when they are farther than minDist, keeps distance (not in-your-face).
 */
class SummonedZombieFollowOwnerGoal(
    private val zombie: SummonedZombieEntity,
    private val speed: Double,
    private val minDist: Float,
    private val maxDist: Float
) : Goal() {

    override fun canUse(): Boolean {
        val owner = zombie.owner ?: return false
        if (!owner.isAlive) return false
        val distSq = zombie.distanceToSqr(owner)
        // Don't follow when too close (minDist), follow when in range (maxDist)
        return distSq > (minDist * minDist) && distSq < (maxDist * maxDist)
    }

    override fun canContinueToUse(): Boolean = canUse()

    override fun tick() {
        val owner = zombie.owner ?: return
        zombie.navigation.moveTo(owner, speed)
    }
}
