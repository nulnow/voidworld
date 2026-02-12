package com.voidworld.world.location

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation

/**
 * A named, bounded region in the game world with associated metadata.
 *
 * Locations are the backbone of VoidWorld's scripted world:
 * - Cities use them for protected zones, quest areas, NPC patrol routes
 * - Dungeons define rooms and encounter triggers
 * - World events are anchored to location boundaries
 * - The quest system uses locations as VISIT objectives
 */
data class GameLocation(
    /** Unique location ID, e.g. `voidworld:capital_market_district`. */
    val id: ResourceLocation,

    /** Human-readable name (localization key). */
    val nameKey: String,

    /** Dimension this location is in. */
    val dimension: ResourceLocation,

    /** Minimum corner of the bounding box (inclusive). */
    val minPos: BlockPos,

    /** Maximum corner of the bounding box (inclusive). */
    val maxPos: BlockPos,

    /** The type/role of this location. */
    val type: LocationType,

    /** Parent location (e.g., a district inside a city). */
    val parentId: ResourceLocation? = null,

    /** Tags for flexible categorization. */
    val tags: Set<String> = emptySet(),

    /** Spawn point override when teleporting to this location. */
    val spawnPoint: BlockPos? = null,

    /** Quest IDs that start or are active in this location. */
    val associatedQuests: List<ResourceLocation> = emptyList(),

    /** NPC IDs stationed at this location. */
    val associatedNpcs: List<ResourceLocation> = emptyList(),

    /** Protection level for the law system. Null = no protection. */
    val protectionLevel: String? = null,

    /** Whether the player should receive a notification when entering. */
    val showEntryNotification: Boolean = true,

    /** Optional ambient sound to play when inside. */
    val ambientSound: ResourceLocation? = null
) {
    /** Center of the bounding box. */
    val center: BlockPos get() = BlockPos(
        (minPos.x + maxPos.x) / 2,
        (minPos.y + maxPos.y) / 2,
        (minPos.z + maxPos.z) / 2
    )

    /** Check if a position falls within this location. */
    fun contains(pos: BlockPos): Boolean =
        pos.x in minPos.x..maxPos.x &&
        pos.y in minPos.y..maxPos.y &&
        pos.z in minPos.z..maxPos.z

    /** Approximate volume in blocks. */
    val volume: Long get() {
        val dx = (maxPos.x - minPos.x + 1).toLong()
        val dy = (maxPos.y - minPos.y + 1).toLong()
        val dz = (maxPos.z - minPos.z + 1).toLong()
        return dx * dy * dz
    }

    /** Convert to serializable data class. */
    fun toData(): LocationData = LocationData(
        id = id.toString(),
        nameKey = nameKey,
        dimension = dimension.toString(),
        minX = minPos.x, minY = minPos.y, minZ = minPos.z,
        maxX = maxPos.x, maxY = maxPos.y, maxZ = maxPos.z,
        type = type.name,
        parentId = parentId?.toString(),
        tags = tags.toList(),
        spawnX = spawnPoint?.x, spawnY = spawnPoint?.y, spawnZ = spawnPoint?.z,
        associatedQuests = associatedQuests.map { it.toString() },
        associatedNpcs = associatedNpcs.map { it.toString() },
        protectionLevel = protectionLevel,
        showEntryNotification = showEntryNotification,
        ambientSound = ambientSound?.toString()
    )
}

/**
 * Types of scripted locations.
 */
enum class LocationType {
    /** An entire city / major settlement. */
    CITY,

    /** A district within a city (market, residential, harbor, etc.). */
    DISTRICT,

    /** A specific building (tavern, bank, shop, prison, etc.). */
    BUILDING,

    /** A dungeon or cave system. */
    DUNGEON,

    /** A room within a dungeon or building. */
    ROOM,

    /** An outdoor region (jungle, valley, beach). */
    WILDERNESS,

    /** A quest-specific area (NPC meeting point, objective zone). */
    QUEST_AREA,

    /** A housing plot (city or wilderness). */
    HOUSING_PLOT,

    /** A void crack / portal to the other universe. */
    VOID_CRACK,

    /** A protected zone (subset of city/building). */
    PROTECTED_ZONE,

    /** A point of interest (viewpoint, landmark, hidden cache). */
    POINT_OF_INTEREST,

    /** NPC patrol route waypoint. */
    WAYPOINT,

    /** Spawn area for entities. */
    SPAWN_ZONE,

    /** Teleport destination (portal, fast-travel). */
    TELEPORT_POINT
}

/**
 * JSON-serializable data class for location persistence.
 */
data class LocationData(
    val id: String,
    val nameKey: String,
    val dimension: String,
    val minX: Int, val minY: Int, val minZ: Int,
    val maxX: Int, val maxY: Int, val maxZ: Int,
    val type: String,
    val parentId: String? = null,
    val tags: List<String> = emptyList(),
    val spawnX: Int? = null, val spawnY: Int? = null, val spawnZ: Int? = null,
    val associatedQuests: List<String> = emptyList(),
    val associatedNpcs: List<String> = emptyList(),
    val protectionLevel: String? = null,
    val showEntryNotification: Boolean = true,
    val ambientSound: String? = null
) {
    fun toGameLocation(): GameLocation = GameLocation(
        id = ResourceLocation.tryParse(id) ?: ResourceLocation("voidworld", "unknown"),
        nameKey = nameKey,
        dimension = ResourceLocation.tryParse(dimension) ?: ResourceLocation("minecraft", "overworld"),
        minPos = BlockPos(minX, minY, minZ),
        maxPos = BlockPos(maxX, maxY, maxZ),
        type = try { LocationType.valueOf(type) } catch (_: Exception) { LocationType.POINT_OF_INTEREST },
        parentId = parentId?.let { ResourceLocation.tryParse(it) },
        tags = (tags ?: emptyList()).toSet(),
        spawnPoint = if (spawnX != null && spawnY != null && spawnZ != null)
            BlockPos(spawnX, spawnY, spawnZ) else null,
        associatedQuests = (associatedQuests ?: emptyList()).mapNotNull { ResourceLocation.tryParse(it) },
        associatedNpcs = (associatedNpcs ?: emptyList()).mapNotNull { ResourceLocation.tryParse(it) },
        protectionLevel = protectionLevel,
        showEntryNotification = showEntryNotification,
        ambientSound = ambientSound?.let { ResourceLocation.tryParse(it) }
    )
}
