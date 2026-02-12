package com.voidworld.core.registry

import com.voidworld.VoidWorldMod
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries

/**
 * Centralized registry for all mod content.
 *
 * All DeferredRegister instances are collected here so that [VoidWorldMod]
 * only needs to call [register] once during initialization.
 */
object ModRegistries {

    // ── Block registry ──────────────────────────────────────────────────
    val BLOCKS: DeferredRegister<Block> =
        DeferredRegister.create(ForgeRegistries.BLOCKS, VoidWorldMod.MOD_ID)

    // ── Item registry ───────────────────────────────────────────────────
    val ITEMS: DeferredRegister<Item> =
        DeferredRegister.create(ForgeRegistries.ITEMS, VoidWorldMod.MOD_ID)

    // ── Mob effect registry ──────────────────────────────────────────────
    val MOB_EFFECTS: DeferredRegister<MobEffect> =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, VoidWorldMod.MOD_ID)

    val WANTED_FOR_DESTRUCTION = MOB_EFFECTS.register("wanted_for_destruction") {
        com.voidworld.core.effect.WantedForDestructionEffect()
    }

    val DIVINE_SHIELD = MOB_EFFECTS.register("divine_shield") {
        com.voidworld.core.effect.DivineShieldEffect()
    }

    // ── Entity type registry ────────────────────────────────────────────
    val ENTITY_TYPES: DeferredRegister<EntityType<*>> =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, VoidWorldMod.MOD_ID)

    // ── Sound event registry ────────────────────────────────────────────
    val SOUND_EVENTS: DeferredRegister<SoundEvent> =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VoidWorldMod.MOD_ID)

    // ── Menu (container) type registry ──────────────────────────────────
    val MENU_TYPES: DeferredRegister<MenuType<*>> =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, VoidWorldMod.MOD_ID)

    // ── Creative mode tab registry ──────────────────────────────────────
    val CREATIVE_TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoidWorldMod.MOD_ID)

    // ── Creative tabs ───────────────────────────────────────────────────
    val VOIDWORLD_TAB = CREATIVE_TABS.register("voidworld_tab") {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.${VoidWorldMod.MOD_ID}"))
            .icon { ItemStack(Items.ENDER_EYE) } // Placeholder icon
            .displayItems { _, output ->
                // All mod items will be added here as they are registered
                ITEMS.entries.forEach { entry ->
                    output.accept(entry.get())
                }
            }
            .build()
    }

    // ── Registration ────────────────────────────────────────────────────

    /**
     * Registers all deferred registries onto the given mod event bus.
     * Called once from [VoidWorldMod] constructor.
     */
    fun register(modEventBus: IEventBus) {
        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        MOB_EFFECTS.register(modEventBus)
        ENTITY_TYPES.register(modEventBus)
        SOUND_EVENTS.register(modEventBus)
        MENU_TYPES.register(modEventBus)
        CREATIVE_TABS.register(modEventBus)

        VoidWorldMod.LOGGER.info("All registries attached to mod event bus.")
    }
}
