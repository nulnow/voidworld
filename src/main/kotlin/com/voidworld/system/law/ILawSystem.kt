package com.voidworld.system.law

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Law and punishment system for VoidWorld cities.
 *
 * Tracks crimes committed by players within city boundaries,
 * applies fines, and manages imprisonment / prison escape mechanics.
 *
 * Crime sources:
 * - Breaking protected blocks (city walls, buildings)
 * - Attacking friendly NPCs
 * - Stealing from shops
 * - Trespassing in restricted areas
 */
interface ILawSystem {

    /** Record a crime committed by a player. */
    fun recordCrime(player: ServerPlayer, crime: Crime, location: BlockPos)

    /** Get the current crime level of a player. */
    fun getCrimeLevel(player: ServerPlayer): Int

    /** Check if a player is currently imprisoned. */
    fun isInPrison(player: ServerPlayer): Boolean

    /** Send a player to prison. */
    fun imprison(player: ServerPlayer, durationTicks: Int)

    /** Release a player from prison (served time or pardoned). */
    fun release(player: ServerPlayer)

    /** Attempt a prison escape. Returns true if successful. */
    fun attemptEscape(player: ServerPlayer): Boolean

    /** Check if a position is within a protected zone. */
    fun isProtectedZone(cityId: ResourceLocation, pos: BlockPos): Boolean

    /** Reduce crime level over time (good behavior). */
    fun decayCrimeLevel(player: ServerPlayer, amount: Int)
}

/**
 * Types of crimes in VoidWorld cities.
 */
enum class Crime(val baseFine: Int, val crimePoints: Int) {
    BLOCK_DESTRUCTION(50, 2),
    NPC_ASSAULT(100, 5),
    THEFT(75, 3),
    TRESPASSING(30, 1),
    MURDER(200, 10),
    PRISON_ESCAPE(150, 8),
    CONTRABAND(60, 2)
}

/**
 * Represents a protected zone within a city or settlement.
 */
data class ProtectedZone(
    val cityId: ResourceLocation,
    val name: String,
    val minPos: BlockPos,
    val maxPos: BlockPos,
    val protectionLevel: ProtectionLevel
) {
    fun contains(pos: BlockPos): Boolean =
        pos.x in minPos.x..maxPos.x &&
        pos.y in minPos.y..maxPos.y &&
        pos.z in minPos.z..maxPos.z
}

enum class ProtectionLevel {
    /** Cannot be broken at all (castle walls, key structures). */
    INDESTRUCTIBLE,
    /** Can be broken but triggers heavy punishment. */
    HEAVILY_PROTECTED,
    /** Can be broken with fines. */
    PROTECTED,
    /** Free building zone (player housing plots). */
    PLAYER_ZONE
}
