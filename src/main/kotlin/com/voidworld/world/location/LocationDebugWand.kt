package com.voidworld.world.location

import com.voidworld.VoidWorldMod
import com.voidworld.core.util.modResource
import com.voidworld.core.util.sendModMessage
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.context.UseOnContext
import java.util.UUID

/**
 * Debug tool for marking location boundaries in-game.
 *
 * ## Workflow
 * 1. Give yourself the wand: `/give @s voidworld:location_wand`
 * 2. Right-click block → sets corner 1
 * 3. Right-click another block → sets corner 2
 * 4. Run `/vw location create <id> <type>` → creates a location from the two corners
 * 5. Run `/vw location export` → writes all locations to JSON
 *
 * This class handles the state of the marking process.
 * The actual item is registered in the item package; this is the logic.
 */
object LocationDebugWand {

    /** Per-player marking state: first corner, second corner. */
    private val selections = mutableMapOf<UUID, SelectionState>()

    data class SelectionState(
        val pos1: BlockPos? = null,
        val pos2: BlockPos? = null
    )

    /**
     * Called when the wand is used on a block.
     * Alternates between setting pos1 and pos2.
     */
    fun onUseOnBlock(context: UseOnContext): InteractionResult {
        val player = context.player as? ServerPlayer ?: return InteractionResult.PASS
        val pos = context.clickedPos
        val state = selections.getOrPut(player.uuid) { SelectionState() }

        if (state.pos1 == null || (state.pos1 != null && state.pos2 != null)) {
            // Set first corner (or reset after both are set)
            selections[player.uuid] = SelectionState(pos1 = pos)
            player.sendModMessage("Corner 1 set: [${pos.x}, ${pos.y}, ${pos.z}]")
        } else {
            // Set second corner
            selections[player.uuid] = state.copy(pos2 = pos)
            player.sendModMessage("Corner 2 set: [${pos.x}, ${pos.y}, ${pos.z}]")

            val min = BlockPos(
                minOf(state.pos1.x, pos.x),
                minOf(state.pos1.y, pos.y),
                minOf(state.pos1.z, pos.z)
            )
            val max = BlockPos(
                maxOf(state.pos1.x, pos.x),
                maxOf(state.pos1.y, pos.y),
                maxOf(state.pos1.z, pos.z)
            )
            val dx = max.x - min.x + 1
            val dy = max.y - min.y + 1
            val dz = max.z - min.z + 1
            player.sendModMessage("Selection: ${dx}x${dy}x${dz} blocks (${dx * dy * dz} total)")
            player.sendModMessage("Use /vw location create <id> <type> to save.")
        }

        return InteractionResult.SUCCESS
    }

    /**
     * Get the current selection for a player.
     * Used by the `/vw location create` command.
     */
    fun getSelection(playerId: UUID): SelectionState? = selections[playerId]

    /**
     * Clear the selection for a player.
     */
    fun clearSelection(playerId: UUID) {
        selections.remove(playerId)
    }

    /**
     * Create a GameLocation from the current selection.
     */
    fun createFromSelection(
        player: ServerPlayer,
        locationId: String,
        typeName: String,
        nameKey: String? = null
    ): GameLocation? {
        val state = selections[player.uuid]
        if (state?.pos1 == null || state.pos2 == null) {
            player.sendModMessage("No selection! Right-click two blocks with the wand first.")
            return null
        }

        val type = try {
            LocationType.valueOf(typeName.uppercase())
        } catch (_: Exception) {
            player.sendModMessage("Unknown type: $typeName. Valid: ${LocationType.entries.joinToString()}")
            return null
        }

        val id = modResource(locationId)
        val min = BlockPos(
            minOf(state.pos1.x, state.pos2.x),
            minOf(state.pos1.y, state.pos2.y),
            minOf(state.pos1.z, state.pos2.z)
        )
        val max = BlockPos(
            maxOf(state.pos1.x, state.pos2.x),
            maxOf(state.pos1.y, state.pos2.y),
            maxOf(state.pos1.z, state.pos2.z)
        )

        val location = GameLocation(
            id = id,
            nameKey = nameKey ?: "location.${VoidWorldMod.MOD_ID}.$locationId",
            dimension = player.level().dimension().location(),
            minPos = min,
            maxPos = max,
            type = type,
            spawnPoint = player.blockPosition()
        )

        LocationRegistry.register(location)
        clearSelection(player.uuid)
        player.sendModMessage("Location '$locationId' created (${type.name})!")
        return location
    }
}
