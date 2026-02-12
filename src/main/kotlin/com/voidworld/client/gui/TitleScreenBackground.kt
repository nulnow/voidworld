package com.voidworld.client.gui

import com.voidworld.VoidWorldMod
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.renderer.CubeMap
import net.minecraft.client.renderer.PanoramaRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

/**
 * Replaces the default main menu panorama with a custom image (WP.png).
 * The image must be at assets/voidworld/textures/gui/title/background/panorama_0.png through panorama_5.png.
 * (CubeMap requires 6 faces; we use the same image for all 6.)
 */
object TitleScreenBackground {

    private val PANORAMA_BASE = ResourceLocation.fromNamespaceAndPath(VoidWorldMod.MOD_ID, "textures/gui/title/background/panorama")
    private val CUSTOM_CUBE_MAP = CubeMap(PANORAMA_BASE)
    private val CUSTOM_PANORAMA = PanoramaRenderer(CUSTOM_CUBE_MAP)

    @SubscribeEvent
    fun onScreenInit(event: ScreenEvent.Init.Post) {
        val screen = event.screen
        if (screen !is TitleScreen) return

        try {
            val panoramaField = try {
                TitleScreen::class.java.getDeclaredField("panorama")
            } catch (_: NoSuchFieldException) {
                TitleScreen::class.java.getDeclaredField("f_96729_") // SRG name for production
            }
            panoramaField.isAccessible = true
            panoramaField.set(screen, CUSTOM_PANORAMA)
            VoidWorldMod.LOGGER.debug("TitleScreenBackground: Replaced panorama with custom image")
        } catch (e: Exception) {
            VoidWorldMod.LOGGER.warn("TitleScreenBackground: Could not replace panorama: ${e.message}")
        }
    }
}
