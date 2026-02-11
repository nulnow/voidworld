package com.voidworld;

import com.voidworld.core.registry.ModRegistries;
import com.voidworld.core.config.ModConfig;
import com.voidworld.core.network.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * VoidWorld — Main mod entry point.
 * <p>
 * An epic RPG adventure set in the Minecraft universe where the player
 * must uncover the secrets of false vacuum and decide the fate of reality.
 */
@Mod(VoidWorldMod.MOD_ID)
public class VoidWorldMod {

    public static final String MOD_ID = "voidworld";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public VoidWorldMod() {
        LOGGER.info("VoidWorld is initializing...");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register all deferred registries
        ModRegistries.INSTANCE.register(modEventBus);

        // Register mod configuration
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.INSTANCE.getSpec());
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.INSTANCE.getClientSpec());

        // Register lifecycle event listeners
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("VoidWorld initialization complete.");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("VoidWorld common setup...");
        event.enqueueWork(() -> {
            ModNetwork.INSTANCE.register();
        });
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("VoidWorld client setup...");
        // Client-side setup: register renderers, screens, key bindings, etc.
    }
}
