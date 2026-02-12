package com.voidworld.client.gui

import com.voidworld.VoidWorldMod
import com.voidworld.core.util.VoidWorldSessionFlags
import com.voidworld.world.gen.WorldTemplateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.GenericDirtMessageScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

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
 *
 * Registration is done manually from [VoidWorldMod] to avoid issues
 * with KotlinForForge annotation scanning.
 */
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
    fun onScreenInit(event: ScreenEvent.Init.Post) {
        val screen = event.screen
        if (screen !is TitleScreen) return

        VoidWorldMod.LOGGER.info("VoidWorldTitleScreen: Injecting button into TitleScreen")

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
                if (widget.y >= screen.height / 4 + 24) {
                    widget.y = widget.y + 28
                }
            }
        }

        VoidWorldMod.LOGGER.info("VoidWorldTitleScreen: Button injected successfully")
    }

    /**
     * When CreateWorldScreen opens from our VoidWorld button (flag set in createNewWorld),
     * pre-fill: superflat, creative, cheats, name VoidWorld.
     */
    @SubscribeEvent
    fun onCreateWorldScreenInit(event: ScreenEvent.Init.Post) {
        val screen = event.screen
        if (screen !is CreateWorldScreen) return
        if (!VoidWorldSessionFlags.voidWorldSessionStarting) return

        VoidWorldMod.LOGGER.info("VoidWorldTitleScreen: Configuring CreateWorldScreen for VoidWorld")

        val uiState = screen.uiState
        uiState.setName(WORLD_SAVE_NAME)
        uiState.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)
        uiState.setAllowCheats(true)

        // Try to set flat preset via reflection (mapping names vary)
        try {
            val flatLoc = ResourceLocation("minecraft", "flat")
            for (extName in listOf("extendedWorldTypes", "extendedPresetList")) {
                for (normName in listOf("normalWorldTypes", "normalPresetList")) {
                    try {
                        val extended = uiState.javaClass.getDeclaredField(extName).apply { isAccessible = true }.get(uiState) as List<*>
                        val normal = uiState.javaClass.getDeclaredField(normName).apply { isAccessible = true }.get(uiState) as List<*>
                        for (entry in extended + normal) {
                            val preset = entry!!.javaClass.getMethod("preset").invoke(entry) ?: continue
                            val keyOpt = preset!!.javaClass.getMethod("unwrapKey").invoke(preset) ?: continue
                            val loc = (keyOpt as java.util.Optional<*>).map { optIt ->
                                val key = optIt!! as? net.minecraft.resources.ResourceKey<*>
                                key?.location()
                            }.orElse(null)
                            if (loc == flatLoc) {
                                uiState.javaClass.getMethod("setWorldType", entry.javaClass).invoke(uiState, entry)
                                return
                            }
                        }
                    } catch (_: Exception) { continue }
                }
            }
        } catch (_: Exception) { VoidWorldMod.LOGGER.warn("Could not set flat preset - select Superflat manually in More World Options") }

        // Schedule auto-create on first render (screen is fully ready then)
        VoidWorldSessionFlags.voidWorldCreatePending = true
    }

    /**
     * When CreateWorldScreen is first drawn (voidWorldCreatePending), auto-trigger createLevel
     * so the world is created without user clicking "Create World".
     */
    @SubscribeEvent
    fun onCreateWorldScreenRender(event: ScreenEvent.Render.Post) {
        val screen = event.screen
        if (screen !is CreateWorldScreen) return
        if (!VoidWorldSessionFlags.voidWorldCreatePending) return

        VoidWorldSessionFlags.voidWorldCreatePending = false
        val minecraft = Minecraft.getInstance()
        minecraft.execute {
            try {
                val clazz = screen.javaClass
                val createMethod = listOf("createLevel", "onCreate", "m_100972_")
                    .mapNotNull { name -> clazz.declaredMethods.find { it.name == name && it.parameterCount == 0 } }
                    .firstOrNull()
                    ?: throw NoSuchMethodException("createLevel/onCreate not found")
                createMethod.isAccessible = true
                createMethod.invoke(screen)
                VoidWorldMod.LOGGER.info("VoidWorldTitleScreen: Auto-triggered world creation")
            } catch (e: Exception) {
                VoidWorldMod.LOGGER.warn("Could not auto-create VoidWorld: ${e.message}")
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
        VoidWorldSessionFlags.voidWorldSessionStarting = true

        try {
            minecraft.forceSetScreen(
                GenericDirtMessageScreen(LOADING_TEXT)
            )

            minecraft.createWorldOpenFlows().loadLevel(
                minecraft.screen!!,
                WORLD_SAVE_NAME
            )
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

        VoidWorldSessionFlags.voidWorldSessionStarting = true
        // Open the create world screen pre-filled with VoidWorld settings
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
