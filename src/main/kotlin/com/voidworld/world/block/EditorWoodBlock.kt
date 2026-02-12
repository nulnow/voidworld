package com.voidworld.world.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor

/**
 * Simple wood block for VoidWorld Editor creative tab.
 */
class EditorWoodBlock : Block(
    BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
        .strength(2.0f)
        .sound(SoundType.WOOD)
) {
    override fun isFlammable(state: BlockState, level: BlockGetter, pos: BlockPos, direction: net.minecraft.core.Direction) = true
    override fun getFlammability(state: BlockState, level: BlockGetter, pos: BlockPos, direction: net.minecraft.core.Direction) = 5
    override fun getFireSpreadSpeed(state: BlockState, level: BlockGetter, pos: BlockPos, direction: net.minecraft.core.Direction) = 20
}
