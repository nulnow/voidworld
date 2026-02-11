package com.voidworld.world.location

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.voidworld.VoidWorldMod
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Registry of all scripted locations across all dimensions.
 *
 * Locations are loaded from JSON files and define named regions in the world
 * with coordinates, boundaries, types, and associations to quests/NPCs/events.
 *
 * ## Workflow
 * 1. Build the map in creative mode
 * 2. Use the debug wand (`/vw location mark`) to record corner coordinates
 * 3. Export marked locations to JSON (`/vw location export`)
 * 4. Include the JSON in `data/voidworld/locations/`
 * 5. On server start, all locations are loaded and indexed spatially
 *
 * ## Location lookup
 * - By ID: O(1) hash lookup
 * - By position: chunk-indexed spatial lookup for enter/exit triggers
 */
object LocationRegistry {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /** All registered locations, keyed by their ID. */
    private val locations = mutableMapOf<ResourceLocation, GameLocation>()

    /** Spatial index: chunk coordinate -> list of locations overlapping that chunk. */
    private val chunkIndex = mutableMapOf<Long, MutableList<GameLocation>>()

    // ── Public API ──────────────────────────────────────────────────────

    /** Get a location by its ID. */
    fun getLocation(id: ResourceLocation): GameLocation? = locations[id]

    /** Get all locations. */
    fun getAllLocations(): Collection<GameLocation> = locations.values

    /** Get all locations in a specific dimension. */
    fun getLocationsInDimension(dimension: ResourceKey<Level>): List<GameLocation> =
        locations.values.filter { it.dimension == dimension.location() }

    /** Get all locations of a specific type. */
    fun getLocationsByType(type: LocationType): List<GameLocation> =
        locations.values.filter { it.type == type }

    /**
     * Find which location(s) a position falls within.
     * Uses chunk-based spatial index for efficiency.
     */
    fun getLocationsAt(dimension: ResourceKey<Level>, pos: BlockPos): List<GameLocation> {
        val chunkKey = chunkKey(pos.x shr 4, pos.z shr 4)
        val candidates = chunkIndex[chunkKey] ?: return emptyList()
        return candidates.filter { loc ->
            loc.dimension == dimension.location() && loc.contains(pos)
        }
    }

    /**
     * Check if a position is inside any location of the given type.
     */
    fun isInLocationType(dimension: ResourceKey<Level>, pos: BlockPos, type: LocationType): Boolean =
        getLocationsAt(dimension, pos).any { it.type == type }

    /**
     * Find the nearest location to a position within a given radius.
     */
    fun findNearest(
        dimension: ResourceKey<Level>,
        pos: BlockPos,
        maxDistance: Double = 256.0,
        type: LocationType? = null
    ): GameLocation? {
        val dimId = dimension.location()
        return locations.values
            .filter { it.dimension == dimId && (type == null || it.type == type) }
            .filter { it.center.distanceTo(pos) <= maxDistance }
            .minByOrNull { it.center.distanceTo(pos) }
    }

    // ── Registration ────────────────────────────────────────────────────

    /** Register a single location. */
    fun register(location: GameLocation) {
        locations[location.id] = location
        indexLocation(location)
    }

    /** Remove a location by ID. */
    fun unregister(id: ResourceLocation) {
        val loc = locations.remove(id) ?: return
        removeFromIndex(loc)
    }

    /** Clear all locations (called before reload). */
    fun clear() {
        locations.clear()
        chunkIndex.clear()
    }

    // ── Loading from JSON ───────────────────────────────────────────────

    /**
     * Load locations from the mod's data directory.
     * Called on server start.
     */
    fun loadFromDataPacks(server: MinecraftServer) {
        clear()
        val resourceManager = server.resourceManager
        val locationPath = "locations"
        val prefix = "${VoidWorldMod.MOD_ID}:$locationPath/"

        // Scan for all JSON files in data/voidworld/locations/
        val resources = resourceManager.listResources(locationPath) { it.path.endsWith(".json") }

        for ((resLoc, resource) in resources) {
            if (resLoc.namespace != VoidWorldMod.MOD_ID) continue
            try {
                val reader = InputStreamReader(resource.open())
                val locData = gson.fromJson(reader, LocationData::class.java)
                reader.close()

                val location = locData.toGameLocation()
                register(location)
                VoidWorldMod.LOGGER.debug("Loaded location: ${location.id}")
            } catch (e: Exception) {
                VoidWorldMod.LOGGER.error("Failed to load location from $resLoc", e)
            }
        }

        VoidWorldMod.LOGGER.info("Loaded ${locations.size} locations from data packs.")
    }

    /**
     * Load locations from a standalone JSON file (e.g., exported from debug wand).
     */
    fun loadFromFile(path: Path) {
        if (!Files.exists(path)) {
            VoidWorldMod.LOGGER.warn("Location file not found: $path")
            return
        }
        try {
            val reader = Files.newBufferedReader(path)
            val listType = object : TypeToken<List<LocationData>>() {}.type
            val locList: List<LocationData> = gson.fromJson(reader, listType)
            reader.close()

            locList.forEach { data ->
                val loc = data.toGameLocation()
                register(loc)
            }
            VoidWorldMod.LOGGER.info("Loaded ${locList.size} locations from file: $path")
        } catch (e: Exception) {
            VoidWorldMod.LOGGER.error("Failed to load locations from file: $path", e)
        }
    }

    /**
     * Export all current locations to a JSON file.
     * Useful for the debug wand workflow.
     */
    fun exportToFile(path: Path) {
        val dataList = locations.values.map { it.toData() }
        val json = gson.toJson(dataList)
        Files.createDirectories(path.parent)
        Files.writeString(path, json)
        VoidWorldMod.LOGGER.info("Exported ${dataList.size} locations to: $path")
    }

    // ── Spatial indexing ─────────────────────────────────────────────────

    private fun indexLocation(location: GameLocation) {
        val minCx = location.minPos.x shr 4
        val minCz = location.minPos.z shr 4
        val maxCx = location.maxPos.x shr 4
        val maxCz = location.maxPos.z shr 4

        for (cx in minCx..maxCx) {
            for (cz in minCz..maxCz) {
                val key = chunkKey(cx, cz)
                chunkIndex.getOrPut(key) { mutableListOf() }.add(location)
            }
        }
    }

    private fun removeFromIndex(location: GameLocation) {
        val minCx = location.minPos.x shr 4
        val minCz = location.minPos.z shr 4
        val maxCx = location.maxPos.x shr 4
        val maxCz = location.maxPos.z shr 4

        for (cx in minCx..maxCx) {
            for (cz in minCz..maxCz) {
                chunkIndex[chunkKey(cx, cz)]?.remove(location)
            }
        }
    }

    private fun chunkKey(cx: Int, cz: Int): Long =
        (cx.toLong() and 0xFFFFFFFFL) or ((cz.toLong() and 0xFFFFFFFFL) shl 32)

    // ── Helper: distance from center ────────────────────────────────────

    private fun BlockPos.distanceTo(other: BlockPos): Double {
        val dx = (x - other.x).toDouble()
        val dy = (y - other.y).toDouble()
        val dz = (z - other.z).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }
}
