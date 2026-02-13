package com.voidworld.client

import com.voidworld.core.registry.ModEntities
import net.minecraftforge.client.event.EntityRenderersEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Client-only setup: entity renderers, etc.
 * Must be in a separate class so it's never loaded on the server.
 */
object ClientModSetup {

    fun register() {
        MOD_BUS.addListener(::onRegisterRenderers)
    }

    private fun onRegisterRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(ModEntities.CITY_GUARDIAN.get()) { context ->
            com.voidworld.client.renderer.CityGuardianRenderer(context)
        }
        event.registerEntityRenderer(ModEntities.DARK_ZOMBIE.get()) { context ->
            com.voidworld.client.renderer.DarkZombieRenderer(context)
        }
        event.registerEntityRenderer(ModEntities.PALADIN.get()) { context ->
            com.voidworld.client.renderer.PaladinRenderer(context)
        }
        event.registerEntityRenderer(ModEntities.SUMMONED_ZOMBIE.get()) { context ->
            com.voidworld.client.renderer.SummonedZombieRenderer(context)
        }
    }
}
