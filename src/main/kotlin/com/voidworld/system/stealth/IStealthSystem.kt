package com.voidworld.system.stealth

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

/**
 * Stealth mechanics system for VoidWorld.
 *
 * Used in:
 * - Rooftop stealth missions in cities
 * - Infiltration of the castle
 * - Gothic city vampire gameplay (rooftop movement, hiding)
 * - Prison escape sequences
 *
 * Detection is influenced by:
 * - Light level at player's position
 * - Distance to NPCs / guards
 * - Player movement speed (sneaking vs running)
 * - Equipment (stealth gear reduces detection)
 * - Line of sight obstruction
 */
interface IStealthSystem {

    /** Get the current stealth state for a player. */
    fun getStealthState(player: ServerPlayer): StealthState

    /** Calculate detection probability from a specific entity. */
    fun calculateDetection(player: ServerPlayer, detector: LivingEntity): Float

    /** Check if a player is currently hidden from all nearby entities. */
    fun isHidden(player: ServerPlayer): Boolean

    /** Enter stealth mode (triggered by specific actions/items). */
    fun enterStealthMode(player: ServerPlayer)

    /** Exit stealth mode. */
    fun exitStealthMode(player: ServerPlayer)

    /** Called every tick for stealth-active players. */
    fun tickStealth(player: ServerPlayer)

    /** Check if a position provides cover (shadow, behind wall, on rooftop). */
    fun hasCover(pos: BlockPos): Boolean
}

/**
 * Current stealth state for a player.
 */
data class StealthState(
    /** Whether stealth mode is active. */
    val isActive: Boolean = false,

    /** Current visibility level (0.0 = invisible, 1.0 = fully visible). */
    val visibility: Float = 1.0f,

    /** How alert nearby guards are (0.0 = unaware, 1.0 = combat). */
    val alertLevel: Float = 0.0f,

    /** Whether the player is currently on a rooftop. */
    val isOnRooftop: Boolean = false,

    /** Whether the player is in a shadow/dark area. */
    val isInShadow: Boolean = false,

    /** Active stealth mission, if any. */
    val missionId: String? = null
)

/**
 * Alert levels for the guard AI system.
 */
enum class AlertLevel(val threshold: Float) {
    UNAWARE(0.0f),
    SUSPICIOUS(0.3f),
    SEARCHING(0.6f),
    COMBAT(0.9f);

    companion object {
        fun fromValue(value: Float): AlertLevel =
            entries.last { value >= it.threshold }
    }
}
