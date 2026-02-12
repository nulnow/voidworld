package com.voidworld.core.registry

import com.voidworld.VoidWorldMod
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
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

    val EDITOR_WOOD_BLOCK = BLOCKS.register("editor_wood") { com.voidworld.world.block.EditorWoodBlock() }

    // ── Item registry ───────────────────────────────────────────────────
    val ITEMS: DeferredRegister<Item> =
        DeferredRegister.create(ForgeRegistries.ITEMS, VoidWorldMod.MOD_ID)

    val EDITOR_WOOD_ITEM = ITEMS.register("editor_wood") { BlockItem(EDITOR_WOOD_BLOCK.get(), Item.Properties()) }

    val UNDO_ITEM = ITEMS.register("undo") { com.voidworld.world.item.UndoItem(Item.Properties()) }

    val STRUCTURE_HOUSE_CITY_ITEM = ITEMS.register("structure_house_city_standard") {
        com.voidworld.world.item.StructurePlacementItem("house_city_standard", Item.Properties())
    }
    val STRUCTURE_TREE_OAK_ITEM = ITEMS.register("structure_tree_oak") {
        com.voidworld.world.item.StructurePlacementItem("tree_oak", Item.Properties())
    }
    val STRUCTURE_TREE_SPRUCE_ITEM = ITEMS.register("structure_tree_spruce") {
        com.voidworld.world.item.StructurePlacementItem("tree_spruce", Item.Properties())
    }
    val STRUCTURE_TREE_BIRCH_ITEM = ITEMS.register("structure_tree_birch") {
        com.voidworld.world.item.StructurePlacementItem("tree_birch", Item.Properties())
    }
    val STRUCTURE_WALL_STONE_ITEM = ITEMS.register("structure_wall_stone") {
        com.voidworld.world.item.StructurePlacementItem("wall_stone", Item.Properties())
    }
    val STRUCTURE_WALL_STONE_BRICK_ITEM = ITEMS.register("structure_wall_stone_brick") {
        com.voidworld.world.item.StructurePlacementItem("wall_stone_brick", Item.Properties())
    }
    val STRUCTURE_WALL_COBBLESTONE_ITEM = ITEMS.register("structure_wall_cobblestone") {
        com.voidworld.world.item.StructurePlacementItem("wall_cobblestone", Item.Properties())
    }
    val STRUCTURE_HOUSE_STONE_MANSION_ITEM = ITEMS.register("structure_house_stone_brick_mansion") {
        com.voidworld.world.item.StructurePlacementItem("house_stone_brick_mansion", Item.Properties())
    }
    val STRUCTURE_HOUSE_STONE_TOWER_ITEM = ITEMS.register("structure_house_stone_brick_tower") {
        com.voidworld.world.item.StructurePlacementItem("house_stone_brick_tower", Item.Properties())
    }
    val STRUCTURE_HOUSE_STONE_MANOR_ITEM = ITEMS.register("structure_house_stone_cobble_manor") {
        com.voidworld.world.item.StructurePlacementItem("house_stone_cobble_manor", Item.Properties())
    }
    val STRUCTURE_BUILDING_LARGE_ITEM = ITEMS.register("structure_building_large") {
        com.voidworld.world.item.StructurePlacementItem("building_large", Item.Properties())
    }
    val STRUCTURE_PLATFORM_SMALL_ITEM = ITEMS.register("structure_platform_small") {
        com.voidworld.world.item.StructurePlacementItem("platform_small", Item.Properties())
    }
    val STRUCTURE_PLATFORM_MEDIUM_ITEM = ITEMS.register("structure_platform_medium") {
        com.voidworld.world.item.StructurePlacementItem("platform_medium", Item.Properties())
    }
    val STRUCTURE_PLATFORM_LARGE_ITEM = ITEMS.register("structure_platform_large") {
        com.voidworld.world.item.StructurePlacementItem("platform_large", Item.Properties())
    }
    val STRUCTURE_PLATFORM_GIANT_ITEM = ITEMS.register("structure_platform_giant") {
        com.voidworld.world.item.StructurePlacementItem("platform_giant", Item.Properties())
    }
    val STRUCTURE_WALL_FORTRESS_ITEM = ITEMS.register("structure_wall_fortress") {
        com.voidworld.world.item.StructurePlacementItem("wall_fortress", Item.Properties())
    }
    val STRUCTURE_TOWER_CASTLE_ITEM = ITEMS.register("structure_tower_castle") {
        com.voidworld.world.item.StructurePlacementItem("tower_castle", Item.Properties())
    }

    private val EDITOR_ONLY_ITEMS = setOf(
        EDITOR_WOOD_ITEM, UNDO_ITEM,
        STRUCTURE_HOUSE_CITY_ITEM,
        STRUCTURE_HOUSE_STONE_MANSION_ITEM, STRUCTURE_HOUSE_STONE_TOWER_ITEM, STRUCTURE_HOUSE_STONE_MANOR_ITEM,
        STRUCTURE_BUILDING_LARGE_ITEM,
        STRUCTURE_PLATFORM_SMALL_ITEM, STRUCTURE_PLATFORM_MEDIUM_ITEM, STRUCTURE_PLATFORM_LARGE_ITEM, STRUCTURE_PLATFORM_GIANT_ITEM,
        STRUCTURE_TREE_OAK_ITEM, STRUCTURE_TREE_SPRUCE_ITEM, STRUCTURE_TREE_BIRCH_ITEM,
        STRUCTURE_WALL_STONE_ITEM, STRUCTURE_WALL_STONE_BRICK_ITEM, STRUCTURE_WALL_COBBLESTONE_ITEM,
        STRUCTURE_WALL_FORTRESS_ITEM, STRUCTURE_TOWER_CASTLE_ITEM
    )

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
                ITEMS.entries.forEach { entry ->
                    if (entry !in EDITOR_ONLY_ITEMS) output.accept(entry.get())
                }
            }
            .build()
    }

    val VOIDWORLD_EDITOR_TAB = CREATIVE_TABS.register("voidworld_editor") {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.${VoidWorldMod.MOD_ID}.editor"))
            .icon { ItemStack(EDITOR_WOOD_ITEM.get()) }
            .displayItems { _, output ->
                output.accept(EDITOR_WOOD_ITEM.get())
                output.accept(UNDO_ITEM.get())
                output.accept(STRUCTURE_HOUSE_CITY_ITEM.get())
                output.accept(STRUCTURE_HOUSE_STONE_MANSION_ITEM.get())
                output.accept(STRUCTURE_HOUSE_STONE_TOWER_ITEM.get())
                output.accept(STRUCTURE_HOUSE_STONE_MANOR_ITEM.get())
                output.accept(STRUCTURE_BUILDING_LARGE_ITEM.get())
                output.accept(STRUCTURE_PLATFORM_SMALL_ITEM.get())
                output.accept(STRUCTURE_PLATFORM_MEDIUM_ITEM.get())
                output.accept(STRUCTURE_PLATFORM_LARGE_ITEM.get())
                output.accept(STRUCTURE_PLATFORM_GIANT_ITEM.get())
                output.accept(STRUCTURE_TREE_OAK_ITEM.get())
                output.accept(STRUCTURE_TREE_SPRUCE_ITEM.get())
                output.accept(STRUCTURE_TREE_BIRCH_ITEM.get())
                output.accept(STRUCTURE_WALL_STONE_ITEM.get())
                output.accept(STRUCTURE_WALL_STONE_BRICK_ITEM.get())
                output.accept(STRUCTURE_WALL_COBBLESTONE_ITEM.get())
                output.accept(STRUCTURE_WALL_FORTRESS_ITEM.get())
                output.accept(STRUCTURE_TOWER_CASTLE_ITEM.get())
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
