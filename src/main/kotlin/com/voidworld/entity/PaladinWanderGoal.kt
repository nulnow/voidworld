package com.voidworld.entity

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.util.DefaultRandomPos
import net.minecraft.world.phys.Vec3
import java.util.*

/**
 * Goal for Paladin: pick a random point within home bounds and pathfind to it.
 */
class PaladinWanderGoal(
    private val paladin: PaladinEntity,
    private val speed: Double
) : Goal() {

    companion object {
        private const val INTERVAL = 60
        private const val ARRIVAL_DISTANCE = 2.0
    }

    private var nextWanderTick = 0

    override fun canUse(): Boolean {
        if (!paladin.hasHomeBounds()) return false
        if (paladin.navigation.isDone) return true
        if (paladin.tickCount >= nextWanderTick) return true
        return false
    }

    override fun start() {
        paladin.pickNewWanderTarget()
        val targetX = paladin.getWanderTargetX()
        val targetZ = paladin.getWanderTargetZ()
        val targetPos = BlockPos(targetX, paladin.blockY, targetZ)
        paladin.navigation.moveTo(
            paladin.navigation.createPath(targetPos, 1) ?: return,
            speed
        )
        nextWanderTick = paladin.tickCount + INTERVAL + paladin.random.nextInt(60)
    }

    override fun canContinueToUse(): Boolean {
        if (!paladin.hasHomeBounds()) return false
        val path = paladin.navigation.path
        if (path == null || path.isDone) return false
        val dist = paladin.distanceToSqr(
            paladin.getWanderTargetX().toDouble(),
            paladin.y,
            paladin.getWanderTargetZ().toDouble()
        )
        return dist > ARRIVAL_DISTANCE * ARRIVAL_DISTANCE
    }

    override fun tick() {
        if (paladin.navigation.isDone || paladin.tickCount >= nextWanderTick) {
            paladin.pickNewWanderTarget()
            start()
        }
    }
}
