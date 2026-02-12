package com.voidworld.client.gui

import com.voidworld.VoidWorldMod
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

/**
 * Injects world editing buttons into the inventory screen (E key).
 * Draws via Render.Post and handles clicks via MouseButtonPressed.Pre.
 * Registered manually in [VoidWorldMod.registerClientHandlers].
 */
object WorldEditorOverlay {

    private const val BUTTON_WIDTH = 120
    private const val BUTTON_HEIGHT = 24
    private const val BUTTON_SPACING = 26
    private const val START_X = 10
    private const val START_Y = 10

    private data class ButtonDef(val label: String, val command: String)

    private val BUTTONS = listOf(
        ButtonDef("Day", "time set day"),
        ButtonDef("Night", "time set night"),
        ButtonDef("City Guardian", "summon ${VoidWorldMod.MOD_ID}:city_guardian ~ ~ ~"),
        ButtonDef("Paladin", "summon ${VoidWorldMod.MOD_ID}:paladin ~ ~ ~"),
        ButtonDef("Dark Zombie", "summon ${VoidWorldMod.MOD_ID}:dark_zombie ~ ~ ~"),
        ButtonDef("Summoned Zombie", "summon ${VoidWorldMod.MOD_ID}:summoned_zombie ~ ~ ~"),
        ButtonDef("Creative", "gamemode creative"),
        ButtonDef("Survival", "gamemode survival"),
        ButtonDef("Heal me", "effect give @p instant_health 5 1"),
    )

    private fun isInventoryScreen(screen: net.minecraft.client.gui.screens.Screen): Boolean =
        screen is InventoryScreen || screen is CreativeModeInventoryScreen

    @SubscribeEvent
    fun onScreenRender(event: ScreenEvent.Render.Post) {
        val screen = event.screen
        if (!isInventoryScreen(screen)) return

        val gui = event.guiGraphics
        val mouseX = event.mouseX
        val mouseY = event.mouseY
        val font = Minecraft.getInstance().font

        var y = START_Y
        for (btn in BUTTONS) {
            val inside = mouseX >= START_X && mouseX < START_X + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT
            val bg = if (inside) 0xFF555555.toInt() else 0xFF333333.toInt()
            gui.fill(START_X, y, START_X + BUTTON_WIDTH, y + BUTTON_HEIGHT, bg)
            gui.fill(START_X, y + BUTTON_HEIGHT - 1, START_X + BUTTON_WIDTH, y + BUTTON_HEIGHT, 0xFF000000.toInt())
            val tw = font.width(btn.label)
            gui.drawString(font, btn.label, START_X + (BUTTON_WIDTH - tw) / 2, y + (BUTTON_HEIGHT - 8) / 2, 0xFFFFFF)
            y += BUTTON_SPACING
        }
    }

    @SubscribeEvent
    fun onMouseClick(event: ScreenEvent.MouseButtonPressed.Pre) {
        val screen = event.screen
        if (!isInventoryScreen(screen)) return
        if (event.button != 0) return

        val mouseX = event.mouseX.toInt()
        val mouseY = event.mouseY.toInt()
        var y = START_Y
        for (btn in BUTTONS) {
            if (mouseX >= START_X && mouseX < START_X + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT) {
                runCommand(btn.command)
                event.setCanceled(true)
                return
            }
            y += BUTTON_SPACING
        }
    }

    private fun runCommand(command: String) {
        val minecraft = Minecraft.getInstance()
        minecraft.execute {
            val server = minecraft.singleplayerServer ?: return@execute
            val serverPlayer = server.getPlayerList().getPlayer(minecraft.player?.uuid ?: return@execute) ?: return@execute
            val source = serverPlayer.createCommandSourceStack()
            server.commands.performPrefixedCommand(source, command)
        }
    }
}
