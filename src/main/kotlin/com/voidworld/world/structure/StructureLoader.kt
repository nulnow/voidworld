package com.voidworld.world.structure

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.voidworld.VoidWorldMod
import java.io.InputStreamReader

/**
 * Loads structure definitions from JSON files in data/voidworld/structures/.
 */
object StructureLoader {

    private val gson: Gson = GsonBuilder().create()

    /**
     * Load a structure by ID (filename without .json).
     * Returns null if not found or invalid.
     */
    fun load(structureId: String): StructureDefinition? {
        val path = "data/${VoidWorldMod.MOD_ID}/structures/$structureId.json"
        return VoidWorldMod::class.java.getResourceAsStream("/$path")?.use { stream ->
            try {
                gson.fromJson(InputStreamReader(stream), StructureDefinition::class.java)
            } catch (e: Exception) {
                VoidWorldMod.LOGGER.warn("Failed to load structure $structureId: ${e.message}")
                null
            }
        }
    }

    /** List of structure IDs available in the mod. */
    fun getAvailableStructureIds(): List<String> = listOf(
        "house_city_standard",
        "house_stone_brick_mansion", "house_stone_brick_tower", "house_stone_cobble_manor",
        "building_large",
        "platform_small", "platform_medium", "platform_large", "platform_giant",
        "tree_oak", "tree_spruce", "tree_birch",
        "wall_stone", "wall_stone_brick", "wall_cobblestone", "wall_fortress",
        "tower_castle"
    )
}
