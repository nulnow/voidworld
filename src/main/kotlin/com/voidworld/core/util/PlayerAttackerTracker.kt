package com.voidworld.core.util

import java.util.UUID

/**
 * Tracks how many times each entity has attacked each player.
 * Used by SummonedZombie to target entities that attacked their owner 2+ times.
 */
object PlayerAttackerTracker {
    private val playerHits = java.util.concurrent.ConcurrentHashMap<UUID, java.util.concurrent.ConcurrentHashMap<UUID, Int>>()

    fun recordHit(playerUuid: UUID, attackerUuid: UUID) {
        playerHits
            .getOrPut(playerUuid) { java.util.concurrent.ConcurrentHashMap<UUID, Int>() }
            .merge(attackerUuid, 1) { a, b -> a + b }
    }

    fun getHitCount(playerUuid: UUID, attackerUuid: UUID): Int {
        return playerHits[playerUuid]?.get(attackerUuid) ?: 0
    }

    fun clear() {
        playerHits.clear()
    }
}
