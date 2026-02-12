package com.voidworld

import com.voidworld.core.config.ModConfig
import com.voidworld.core.network.ModNetwork
import com.voidworld.core.registry.ModEntities
import com.voidworld.core.registry.ModRegistries
import com.voidworld.entity.CityGuardianEntity
import com.voidworld.entity.DarkZombieEntity
import com.voidworld.entity.PaladinEntity
import com.voidworld.entity.SummonedZombieEntity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig as ForgeModConfig
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * VoidWorld — Main mod entry point.
 *
 * An epic RPG adventure set in the Minecraft universe where the player
 * must uncover the secrets of false vacuum and decide the fate of reality.
 */
@Mod(VoidWorldMod.MOD_ID)
class VoidWorldMod {

    companion object {
        const val MOD_ID = "voidworld"

        @JvmField
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    }

    init {
        LOGGER.info("VoidWorld is initializing...")

        val modEventBus = MOD_BUS

        // Register all deferred registries
        ModRegistries.register(modEventBus)

        // Force ModEntities to load before RegisterEvent fires (entity types must be registered early)
        ModEntities.CITY_GUARDIAN
        ModEntities.DARK_ZOMBIE
        ModEntities.PALADIN
        ModEntities.SUMMONED_ZOMBIE

        // Register mod configuration
        ModLoadingContext.get().registerConfig(ForgeModConfig.Type.COMMON, ModConfig.spec)
        ModLoadingContext.get().registerConfig(ForgeModConfig.Type.CLIENT, ModConfig.clientSpec)

        // Register lifecycle event listeners
        modEventBus.addListener(::onCommonSetup)
        modEventBus.addListener(::onClientSetup)
        modEventBus.addListener(::onEntityAttributes)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(::onRegisterRenderers)
        }

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this)

        // Manually register all event handlers on FORGE bus
        // (KotlinForForge annotation scanning may not pick up @Mod.EventBusSubscriber on Kotlin objects)
        MinecraftForge.EVENT_BUS.register(com.voidworld.core.event.ModEvents)
        MinecraftForge.EVENT_BUS.register(com.voidworld.core.command.ModCommands)
        MinecraftForge.EVENT_BUS.register(com.voidworld.core.data.PlayerVoidData.EventHandler)

        // Register client-side event handlers
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClientHandlers()
        }

        LOGGER.info("VoidWorld initialization complete.")
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("VoidWorld common setup...")
        event.enqueueWork {
            ModNetwork.register()
        }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.info("VoidWorld client setup...")
        // Client-side setup: register renderers, screens, key bindings, etc.
    }

    private fun onRegisterRenderers(event: net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers) {
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

    private fun onEntityAttributes(event: EntityAttributeCreationEvent) {
        event.put(ModEntities.CITY_GUARDIAN.get(), CityGuardianEntity.createAttributes().build())
        event.put(ModEntities.DARK_ZOMBIE.get(), DarkZombieEntity.createAttributes().build())
        event.put(ModEntities.PALADIN.get(), PaladinEntity.createAttributes().build())
        event.put(ModEntities.SUMMONED_ZOMBIE.get(), SummonedZombieEntity.createAttributes().build())
    }

    /**
     * Registers client-only event handlers on the FORGE event bus.
     * Separated into its own method to avoid classloading client classes on the server.
     */
    private fun registerClientHandlers() {
        LOGGER.info("Registering client-side event handlers...")
        MinecraftForge.EVENT_BUS.register(com.voidworld.client.gui.VoidWorldTitleScreen)
    }
}
