package com.voidworld.world.item

import com.voidworld.VoidWorldMod
import com.voidworld.core.util.sendModMessage
import com.voidworld.world.structure.StructurePlacementHistory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * Item that undoes the last structure placement.
 * Removes all blocks from the most recently placed structure.
 */
class UndoItem(properties: Properties = Properties()) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack)
        }

        val server = level.server ?: return InteractionResultHolder.fail(stack)

        if (!StructurePlacementHistory.canUndo(player.uuid)) {
            (player as? ServerPlayer)?.sendModMessage("§cNothing to undo.")
            return InteractionResultHolder.fail(stack)
        }

        val removed = StructurePlacementHistory.undo(player.uuid, server)
        (player as? ServerPlayer)?.let { sp ->
            sp.sendModMessage("§aUndone: removed $removed blocks.")
            StructurePlacementHistory.syncToPlayer(sp)
        }

        return InteractionResultHolder.success(stack)
    }

    override fun getName(stack: ItemStack): Component {
        return Component.translatable("item.${VoidWorldMod.MOD_ID}.undo")
    }
}
