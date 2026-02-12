package com.voidworld.world.item

import com.voidworld.VoidWorldMod
import com.voidworld.world.structure.StructureLoader
import com.voidworld.world.structure.StructurePlacementHistory
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries

/**
 * Item that places a structure when used on a block.
 * The structure is rotated based on the player's facing direction.
 */
class StructurePlacementItem(
    private val structureId: String,
    properties: Properties = Properties()
) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        if (level.isClientSide) return InteractionResult.SUCCESS

        val structure = StructureLoader.load(structureId) ?: return InteractionResult.FAIL
        val origin = context.clickedPos
        val facing = context.horizontalDirection

        val serverLevel = level as ServerLevel
        val blocks = structure.blocks
        val placedPositions = mutableListOf<BlockPos>()

        for (entry in blocks) {
            val block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(entry.type))
                ?: continue
            val (dx, dy, dz) = Triple(
                entry.relativePosition.getOrNull(0) ?: 0,
                entry.relativePosition.getOrNull(1) ?: 0,
                entry.relativePosition.getOrNull(2) ?: 0
            )
            val (rx, rz) = rotateOffset(dx, dz, facing)
            val pos = origin.offset(rx, dy, rz)
            val state = block.defaultBlockState()
            serverLevel.setBlock(pos, state, Block.UPDATE_ALL)
            if (!state.isAir) placedPositions.add(pos.immutable())
        }

        val player = context.player
        if (player != null && placedPositions.isNotEmpty()) {
            StructurePlacementHistory.push(player.uuid, structureId, level.dimension(), placedPositions)
            (player as? ServerPlayer)?.let { StructurePlacementHistory.syncToPlayer(it) }
        }

        if (player?.isCreative != true) {
            context.itemInHand.shrink(1)
        }

        return InteractionResult.SUCCESS
    }

    /**
     * Rotate (dx, dz) so that structure's +Z faces the player's facing direction.
     * Structure default: +X = right, +Z = forward.
     */
    private fun rotateOffset(dx: Int, dz: Int, facing: Direction): Pair<Int, Int> = when (facing) {
        Direction.SOUTH -> Pair(dx, dz)
        Direction.NORTH -> Pair(-dx, -dz)
        Direction.EAST -> Pair(dz, -dx)
        Direction.WEST -> Pair(-dz, dx)
        else -> Pair(dx, dz)
    }

    override fun getName(stack: net.minecraft.world.item.ItemStack): Component {
        val structure = StructureLoader.load(structureId)
        return if (structure != null) {
            Component.translatable("structure.${VoidWorldMod.MOD_ID}.$structureId")
                .let { if (it.string == "structure.${VoidWorldMod.MOD_ID}.$structureId") Component.literal(structure.name) else it }
        } else {
            Component.literal("Structure: $structureId")
        }
    }
}
