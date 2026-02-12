package com.voidworld.core.network

import com.voidworld.client.gui.StructureHistoryOverlay
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

/**
 * Syncs structure placement history to the client for HUD display.
 * Sent when a structure is placed or undone.
 */
data class StructureHistorySyncPacket(val entries: List<Pair<String, Int>>) {

    fun encode(buf: FriendlyByteBuf) {
        buf.writeVarInt(entries.size)
        for ((structureId, blockCount) in entries) {
            buf.writeUtf(structureId)
            buf.writeVarInt(blockCount)
        }
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): StructureHistorySyncPacket {
            val size = buf.readVarInt()
            val entries = mutableListOf<Pair<String, Int>>()
            repeat(size) {
                entries.add(buf.readUtf() to buf.readVarInt())
            }
            return StructureHistorySyncPacket(entries)
        }
    }

    fun handle(ctx: Supplier<NetworkEvent.Context>) {
        StructureHistoryOverlay.updateHistory(entries)
        ctx.get().packetHandled = true
    }
}
