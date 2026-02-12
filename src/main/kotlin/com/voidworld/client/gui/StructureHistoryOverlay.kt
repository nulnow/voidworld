package com.voidworld.client.gui

import com.voidworld.VoidWorldMod
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraftforge.client.event.RenderGuiEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
/**
 * HUD overlay showing structure placement history on the right side.
 * Updated via [StructureHistorySyncPacket] from the server.
 */
object StructureHistoryOverlay {

    private var historyEntries: List<Pair<String, Int>> = emptyList()
    private val historyLock = Any()

    /**
     * Called from packet handler when server syncs history.
     */
    fun updateHistory(entries: List<Pair<String, Int>>) {
        synchronized(historyLock) {
            historyEntries = entries.toList()
        }
    }

    @SubscribeEvent
    fun onRenderGui(event: RenderGuiEvent.Post) {
        val minecraft = Minecraft.getInstance()
        if (minecraft.player == null || minecraft.level == null) return

        val entries = synchronized(historyLock) { historyEntries }
        if (entries.isEmpty()) return

        val gui = event.guiGraphics
        val font = minecraft.font
        val screenWidth = gui.guiWidth()

        val padding = 4
        val lineHeight = 8
        val maxLines = 6
        val boxWidth = 110
        val titleHeight = 9

        val displayEntries = entries.takeLast(maxLines)
        val boxHeight = titleHeight + displayEntries.size * lineHeight + padding * 2

        val x = screenWidth - boxWidth - 8
        val y = 50

        // Background
        gui.fill(x, y, x + boxWidth, y + boxHeight, 0x80000000.toInt())
        gui.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFF404040.toInt())

        // Title
        gui.drawString(font, Component.translatable("gui.voidworld.structure_history"), x + padding, y + 2, 0xFFFFFF)

        val startIndex = entries.size - displayEntries.size + 1
        val maxTextWidth = boxWidth - padding * 2
        var lineY = y + titleHeight + padding
        for ((idx, entry) in displayEntries.withIndex()) {
            val (structureId, blockCount) = entry
            val num = startIndex + idx
            val name = Component.translatable("structure.${VoidWorldMod.MOD_ID}.$structureId")
            val shortName = if (name.string.startsWith("structure.")) structureId else name.string
            val text = "$num. $shortName ($blockCount)"
            val display = if (font.width(text) > maxTextWidth) {
                font.substrByWidth(Component.literal(text), maxTextWidth - font.width("…")).string + "…"
            } else text
            gui.drawString(font, display, x + padding, lineY, 0xCCCCCC)
            lineY += lineHeight
        }
    }
}
