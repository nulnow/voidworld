package com.voidworld.world.structure

import com.voidworld.VoidWorldMod
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * History of structure placements for each player.
 * Used by the Undo item to remove the last placed structure's blocks.
 */
object StructurePlacementHistory {

    data class PlacementEntry(
        val structureId: String,
        val dimension: ResourceKey<Level>,
        val positions: List<BlockPos>
    )

    private val playerHistory = ConcurrentHashMap<UUID, ArrayDeque<PlacementEntry>>()

    private fun getOrCreateStack(playerId: UUID): ArrayDeque<PlacementEntry> {
        return playerHistory.getOrPut(playerId) { ArrayDeque() }
    }

    /**
     * Record a structure placement. Call after placing blocks.
     */
    fun push(playerId: UUID, structureId: String, dimension: ResourceKey<Level>, positions: List<BlockPos>) {
        if (positions.isEmpty()) return
        getOrCreateStack(playerId).addLast(PlacementEntry(structureId, dimension, positions.toList()))
        VoidWorldMod.LOGGER.debug("Structure placement recorded: ${positions.size} blocks for player $playerId")
    }

    /**
     * Pop and return the last placement, or null if history is empty.
     */
    fun pop(playerId: UUID): PlacementEntry? {
        return getOrCreateStack(playerId).removeLastOrNull()
    }

    /**
     * Check if the player has any placements to undo.
     */
    fun canUndo(playerId: UUID): Boolean {
        return getOrCreateStack(playerId).isNotEmpty()
    }

    /**
     * Execute undo: remove all blocks from the last placement.
     * @return number of blocks removed, or 0 if nothing to undo
     */
    fun undo(playerId: UUID, server: MinecraftServer): Int {
        val entry = pop(playerId) ?: return 0
        val level = server.getLevel(entry.dimension) ?: return 0

        for (pos in entry.positions) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
        }

        VoidWorldMod.LOGGER.debug("Undo: removed ${entry.positions.size} blocks for player $playerId")
        return entry.positions.size
    }

    /**
     * Clear history when player logs out.
     */
    fun clear(playerId: UUID) {
        playerHistory.remove(playerId)
    }

    /**
     * Get history for client sync: list of (structureId, blockCount).
     */
    fun getHistoryForSync(playerId: UUID): List<Pair<String, Int>> {
        return getOrCreateStack(playerId).map { it.structureId to it.positions.size }
    }

    /**
     * Sync current history to the given player's client for HUD display.
     */
    fun syncToPlayer(player: net.minecraft.server.level.ServerPlayer) {
        val entries = getHistoryForSync(player.uuid)
        com.voidworld.core.network.ModNetwork.sendToPlayer(
            player,
            com.voidworld.core.network.StructureHistorySyncPacket(entries)
        )
    }
}
