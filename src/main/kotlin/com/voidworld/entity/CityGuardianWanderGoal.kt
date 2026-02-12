package com.voidworld.entity

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.util.DefaultRandomPos
import net.minecraft.world.phys.Vec3
import java.util.*

/**
 * Goal for CityGuardian: pick a random point within home bounds and pathfind to it.
 * When arrived or stuck, pick a new target.
 */
class CityGuardianWanderGoal(
    private val guardian: CityGuardianEntity,
    private val speed: Double
) : Goal() {

    companion object {
        private const val INTERVAL = 60
        private const val ARRIVAL_DISTANCE = 2.0
    }

    private var nextWanderTick = 0

    override fun canUse(): Boolean {
        if (!guardian.hasHomeBounds()) return false
        if (guardian.navigation.isDone) return true
        if (guardian.tickCount >= nextWanderTick) return true
        return false
    }

    override fun start() {
        guardian.pickNewWanderTarget()
        val targetX = guardian.getWanderTargetX()
        val targetZ = guardian.getWanderTargetZ()
        val targetPos = BlockPos(targetX, guardian.blockY, targetZ)
        guardian.navigation.moveTo(
            guardian.navigation.createPath(targetPos, 1) ?: return,
            speed
        )
        nextWanderTick = guardian.tickCount + INTERVAL + guardian.random.nextInt(60)
    }

    override fun canContinueToUse(): Boolean {
        if (!guardian.hasHomeBounds()) return false
        val path = guardian.navigation.path
        if (path == null || path.isDone) return false
        val dist = guardian.distanceToSqr(
            guardian.getWanderTargetX().toDouble(),
            guardian.y,
            guardian.getWanderTargetZ().toDouble()
        )
        return dist > ARRIVAL_DISTANCE * ARRIVAL_DISTANCE
    }

    override fun tick() {
        if (guardian.navigation.isDone || guardian.tickCount >= nextWanderTick) {
            guardian.pickNewWanderTarget()
            start()
        }
    }
}
