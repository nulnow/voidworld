package com.voidworld.entity

import com.voidworld.core.registry.ModEntities
import com.voidworld.VoidWorldMod
import com.voidworld.entity.PaladinEntity
import com.voidworld.world.location.LocationRegistry
import com.voidworld.world.location.LocationType
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

/**
 * Spawns CityGuardian and Paladin NPCs in city locations.
 * Called on server start after locations are loaded.
 */
object CityGuardianSpawning {

    private const val GUARDIANS_PER_CITY = 2
    private const val PALADINS_PER_CITY = 1

    fun spawnInCities(server: MinecraftServer) {
        val overworld = server.getLevel(Level.OVERWORLD) ?: return
        val cityLocations = LocationRegistry.getLocationsByType(LocationType.CITY)
            .filter { it.dimension == Level.OVERWORLD.location() }

        var guardians = 0
        var paladins = 0
        for (loc in cityLocations) {
            guardians += spawnInLocation(overworld, loc.minPos, loc.maxPos, GUARDIANS_PER_CITY, ModEntities.CITY_GUARDIAN.get()) { e ->
                (e as? CityGuardianEntity)?.let {
                    it.setHomeBounds(loc.minPos.x, loc.minPos.y, loc.minPos.z, loc.maxPos.x, loc.maxPos.y, loc.maxPos.z)
                    it.pickNewWanderTarget()
                }
            }
            paladins += spawnInLocation(overworld, loc.minPos, loc.maxPos, PALADINS_PER_CITY, ModEntities.PALADIN.get()) { e ->
                (e as? PaladinEntity)?.let {
                    it.setHomeBounds(loc.minPos.x, loc.minPos.y, loc.minPos.z, loc.maxPos.x, loc.maxPos.y, loc.maxPos.z)
                    it.pickNewWanderTarget()
                }
            }
        }

        if (guardians > 0 || paladins > 0) {
            VoidWorldMod.LOGGER.info("Spawned $guardians Protectors and $paladins Paladins in ${cityLocations.size} cities")
        }
    }

    private fun spawnInLocation(
        level: ServerLevel,
        min: BlockPos,
        max: BlockPos,
        count: Int,
        entityType: net.minecraft.world.entity.EntityType<*>,
        setup: (net.minecraft.world.entity.Entity) -> Unit
    ): Int {
        var spawned = 0

        for (i in 0 until count) {
            val x = min.x + level.random.nextInt(max.x - min.x + 1)
            val z = min.z + level.random.nextInt(max.z - min.z + 1)
            val y = findSurfaceY(level, x, z, min.y, max.y) ?: continue

            val entity = entityType.create(level) ?: continue
            entity.moveTo(x + 0.5, y.toDouble(), z + 0.5, level.random.nextFloat() * 360f, 0f)
            setup(entity)

            level.addFreshEntity(entity)
            spawned++
        }

        return spawned
    }

    private fun findSurfaceY(level: ServerLevel, x: Int, z: Int, minY: Int, maxY: Int): Int? {
        for (y in maxY downTo minY) {
            val pos = BlockPos(x, y, z)
            val below = level.getBlockState(pos)
            val air = level.getBlockState(pos.above())
            if (below.blocksMotion() && air.isAir) {
                return y + 1
            }
        }
        return null
    }
}
