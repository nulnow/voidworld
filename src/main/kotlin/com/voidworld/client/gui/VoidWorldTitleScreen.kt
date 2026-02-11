package com.voidworld.client.gui

import com.voidworld.VoidWorldMod
import com.voidworld.world.gen.WorldTemplateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.GenericDirtMessageScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.nio.file.Path

/**
 * Modifies the Minecraft title screen to add a "Enter VoidWorld" button.
 *
 * Behavior:
 * - If a VoidWorld save already exists → loads it directly
 * - If no save exists → creates a new world with the VoidWorld preset
 *   and installs the world template (pre-built maps)
 *
 * The button is injected via [ScreenEvent.Init.Post] on the FORGE bus,
 * so it works alongside other mods that modify the title screen.
 */
@Mod.EventBusSubscriber(
    modid = VoidWorldMod.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    value = [Dist.CLIENT]
)
object VoidWorldTitleScreen {

    /** The name used for the VoidWorld save directory. */
    const val WORLD_SAVE_NAME = "VoidWorld"

    /** Display name shown in the world list. */
    private val WORLD_DISPLAY_NAME = Component.literal("VoidWorld")

    /** Button text. */
    private val BUTTON_TEXT = Component.translatable("menu.${VoidWorldMod.MOD_ID}.play")
    private val BUTTON_TEXT_CONTINUE = Component.translatable("menu.${VoidWorldMod.MOD_ID}.continue")
    private val LOADING_TEXT = Component.translatable("menu.${VoidWorldMod.MOD_ID}.loading")

    @SubscribeEvent
    @JvmStatic
    fun onScreenInit(event: ScreenEvent.Init.Post) {
        val screen = event.screen
        if (screen !is TitleScreen) return

        val minecraft = Minecraft.getInstance()
        val saveExists = doesVoidWorldSaveExist(minecraft)

        val buttonText = if (saveExists) BUTTON_TEXT_CONTINUE else BUTTON_TEXT

        // Position: above the "Singleplayer" button area
        // Vanilla buttons start at screen.height/4 + 48, spaced by 24
        // We place our button prominently above them
        val buttonWidth = 200
        val buttonX = screen.width / 2 - buttonWidth / 2
        val buttonY = screen.height / 4 + 24 // just above "Singleplayer"

        val voidWorldButton = Button.builder(buttonText) { _ ->
            onPlayButtonClicked(minecraft, saveExists)
        }
            .pos(buttonX, buttonY)
            .size(buttonWidth, 20)
            .build()

        event.addListener(voidWorldButton)

        // Shift existing buttons down to make room
        // Vanilla buttons are at y = height/4 + 48, 72, 96, etc.
        // We push them down by 28 pixels
        for (widget in event.listenersList) {
            if (widget !== voidWorldButton && widget is net.minecraft.client.gui.components.AbstractWidget) {
                if (widget.y >= screen.height / 4 + 48) {
                    widget.y = widget.y + 28
                }
            }
        }
    }

    // ── Internal ────────────────────────────────────────────────────────

    private fun onPlayButtonClicked(minecraft: Minecraft, saveExists: Boolean) {
        if (saveExists) {
            loadExistingWorld(minecraft)
        } else {
            createNewWorld(minecraft)
        }
    }

    /**
     * Load an existing VoidWorld save.
     */
    private fun loadExistingWorld(minecraft: Minecraft) {
        VoidWorldMod.LOGGER.info("Loading existing VoidWorld save...")

        try {
            val levelStorage = minecraft.levelSource
            val access = levelStorage.createAccess(WORLD_SAVE_NAME)
            val summary = access.dataTag

            if (summary != null) {
                minecraft.forceSetScreen(
                    GenericDirtMessageScreen(LOADING_TEXT)
                )
                // Close access before Minecraft opens it for real
                access.close()

                minecraft.createWorldOpenFlows().loadLevel(
                    minecraft.screen!!,
                    WORLD_SAVE_NAME
                )
            } else {
                access.close()
                VoidWorldMod.LOGGER.warn("VoidWorld save exists but has no data. Creating new world.")
                createNewWorld(minecraft)
            }
        } catch (e: Exception) {
            VoidWorldMod.LOGGER.error("Failed to load VoidWorld save", e)
            createNewWorld(minecraft)
        }
    }

    /**
     * Create a new VoidWorld save with the template installed.
     */
    private fun createNewWorld(minecraft: Minecraft) {
        VoidWorldMod.LOGGER.info("Creating new VoidWorld...")

        // Install world template before world creation
        val savesDir = minecraft.levelSource.baseDir
        val worldDir = savesDir.resolve(WORLD_SAVE_NAME)

        // Pre-install template if available
        if (!WorldTemplateManager.isTemplateInstalled(worldDir)) {
            val installed = WorldTemplateManager.installTemplate(worldDir)
            if (installed) {
                WorldTemplateManager.markTemplateInstalled(worldDir)
                VoidWorldMod.LOGGER.info("World template installed to: $worldDir")
            }
        }

        // Open the create world screen pre-filled with VoidWorld settings
        // The player can review settings and click "Create"
        CreateWorldScreen.openFresh(minecraft, minecraft.screen)
    }

    /**
     * Check if a VoidWorld save directory exists.
     */
    private fun doesVoidWorldSaveExist(minecraft: Minecraft): Boolean {
        return try {
            val levelList = minecraft.levelSource.findLevelCandidates()
            levelList.levels.any { it.directoryName() == WORLD_SAVE_NAME }
        } catch (_: Exception) {
            false
        }
    }
}
