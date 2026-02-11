package com.voidworld.world.gen

import com.voidworld.world.location.GameLocation
import com.voidworld.world.location.LocationType
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation

/**
 * Defines the spatial layout of ALL scripted locations across all dimensions.
 *
 * ## Overworld layout (top-down, north = -Z):
 *
 * ```
 *                    N (-Z)
 *                     |
 *        Aquaverde    |    Pirate Cove
 *        (0,-1200)    |    (-1800,-800)
 *                     |
 *                  Village
 *                 (-400,-400)
 *   W (-X) ———— CAPITAL (0,0) ———— E (+X)
 *                (center)
 *        Frosthold   |    Nocturn
 *       (-1200,800)  |   (1200,200)
 *                    |
 *        Mirewood    |    Sandport
 *       (-600,1200)  |   (800,1200)
 *                    |
 *                    S (+Z)
 *
 *        Shipwreck Island: (-2200, -1800)  (far NW)
 *        Floating Islands:  above Capital at Y=150+
 * ```
 *
 * ## Custom dimensions:
 * - Cosmic Platform: centered at (0,0) — single large platform
 * - Consciousness Planet: centered at (0,0) — vast frozen landscape
 */
object BootstrapLocations {

    // ── Helper ──────────────────────────────────────────────────────────

    private fun loc(
        id: String,
        name: String,
        dim: String,
        x1: Int, z1: Int, x2: Int, z2: Int,
        y: Int = 63,
        height: Int = 40,
        type: LocationType,
        protection: String? = null,
        tags: Set<String> = emptySet()
    ) = GameLocation(
        id = ResourceLocation("voidworld", id),
        nameKey = "location.voidworld.$id",
        dimension = ResourceLocation.tryParse(dim)!!,
        minPos = BlockPos(minOf(x1, x2), y, minOf(z1, z2)),
        maxPos = BlockPos(maxOf(x1, x2), y + height, maxOf(z1, z2)),
        type = type,
        spawnPoint = BlockPos((x1 + x2) / 2, y + 1, (z1 + z2) / 2),
        tags = tags,
        protectionLevel = protection,
        showEntryNotification = true
    )

    // ═════════════════════════════════════════════════════════════════════
    //  OVERWORLD LOCATIONS
    // ═════════════════════════════════════════════════════════════════════

    val OVERWORLD = "minecraft:overworld"

    val overworldLocations = listOf(
        // ── Act 1: Starting area ────────────────────────────────────
        loc("shipwreck_beach", "Shipwreck Beach", OVERWORLD,
            -2300, -1900, -2100, -1700, y = 60, type = LocationType.WILDERNESS,
            tags = setOf("starting_area", "act_1")),

        loc("jungle", "Dense Jungle", OVERWORLD,
            -2100, -1700, -1700, -1300, y = 60, type = LocationType.WILDERNESS,
            tags = setOf("act_1", "traversal")),

        loc("cave", "Dark Cave", OVERWORLD,
            -1700, -1300, -1400, -1000, y = 30, height = 30, type = LocationType.DUNGEON,
            tags = setOf("act_1", "traversal")),

        loc("valley", "The Valley", OVERWORLD,
            -1400, -1000, -800, -600, y = 60, type = LocationType.WILDERNESS,
            tags = setOf("act_1", "traversal")),

        // ── The Village ─────────────────────────────────────────────
        loc("village", "The Village", OVERWORLD,
            -550, -550, -250, -250, y = 63, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("act_1", "has_quests")),

        loc("village_square", "Village Square", OVERWORLD,
            -450, -450, -350, -350, y = 63, type = LocationType.DISTRICT,
            tags = setOf("act_1")),

        loc("village_elder_house", "Elder's House", OVERWORLD,
            -500, -500, -460, -460, y = 63, type = LocationType.BUILDING,
            tags = setOf("act_1", "quest_npc")),

        // ── The Capital (main city) ─────────────────────────────────
        loc("capital", "The Capital", OVERWORLD,
            -400, -300, 400, 500, y = 63, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("act_2", "main_city")),

        loc("capital_gate", "City Gate", OVERWORLD,
            -50, -300, 50, -260, y = 63, type = LocationType.BUILDING,
            protection = "INDESTRUCTIBLE", tags = setOf("act_2")),

        loc("capital_market", "Market District", OVERWORLD,
            -200, -200, 0, 0, y = 63, type = LocationType.DISTRICT,
            protection = "PROTECTED", tags = setOf("act_2", "trading")),

        loc("capital_harbor", "Harbor", OVERWORLD,
            100, -200, 350, 0, y = 63, type = LocationType.DISTRICT,
            protection = "PROTECTED", tags = setOf("act_2")),

        loc("capital_residential", "Residential Quarter", OVERWORLD,
            -350, 0, -100, 200, y = 63, type = LocationType.DISTRICT,
            protection = "PROTECTED", tags = setOf("act_2", "housing")),

        loc("capital_prison", "City Prison", OVERWORLD,
            200, 100, 350, 250, y = 55, height = 20, type = LocationType.BUILDING,
            protection = "INDESTRUCTIBLE", tags = setOf("act_2", "prison")),

        loc("capital_bank", "City Bank", OVERWORLD,
            -100, -150, -50, -100, y = 63, type = LocationType.BUILDING,
            protection = "INDESTRUCTIBLE", tags = setOf("act_2", "bank")),

        // ── The Castle ──────────────────────────────────────────────
        loc("castle", "The King's Castle", OVERWORLD,
            -120, 200, 120, 500, y = 63, height = 120, type = LocationType.BUILDING,
            protection = "INDESTRUCTIBLE", tags = setOf("act_2", "act_3", "main_story")),

        loc("castle_entrance", "Castle Entrance", OVERWORLD,
            -40, 200, 40, 240, y = 63, type = LocationType.ROOM,
            tags = setOf("act_2")),

        loc("castle_throne_room", "Throne Room", OVERWORLD,
            -60, 350, 60, 430, y = 90, type = LocationType.ROOM,
            tags = setOf("act_3", "king")),

        loc("castle_tower_top", "Tower Summit", OVERWORLD,
            -30, 420, 30, 480, y = 150, height = 50, type = LocationType.ROOM,
            tags = setOf("act_3", "void_breach", "boss")),

        // ── Nocturn: Gothic City ────────────────────────────────────
        loc("nocturn", "Nocturn — Gothic City", OVERWORLD,
            1000, 0, 1400, 400, y = 63, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("gothic", "vampire", "campaign")),

        loc("nocturn_cathedral", "Dark Cathedral", OVERWORLD,
            1150, 150, 1250, 250, y = 63, height = 60, type = LocationType.BUILDING,
            protection = "INDESTRUCTIBLE", tags = setOf("gothic")),

        // ── Sandport: Desert City ───────────────────────────────────
        loc("sandport", "Sandport — Desert City", OVERWORLD,
            600, 1000, 1000, 1400, y = 68, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("desert", "trading", "campaign")),

        loc("sandport_bazaar", "Grand Bazaar", OVERWORLD,
            750, 1150, 850, 1250, y = 68, type = LocationType.DISTRICT,
            tags = setOf("desert", "trading")),

        // ── Frosthold: Snow Fortress ────────────────────────────────
        loc("frosthold", "Frosthold — Mountain Fortress", OVERWORLD,
            -1400, 600, -1000, 1000, y = 90, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("snow", "mountain", "campaign")),

        // ── Mirewood: Swamp Town ────────────────────────────────────
        loc("mirewood", "Mirewood — Swamp Settlement", OVERWORLD,
            -800, 1000, -400, 1400, y = 60, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("swamp", "alchemy", "campaign")),

        // ── Aquaverde: Venetian City ────────────────────────────────
        loc("aquaverde", "Aquaverde — City on Water", OVERWORLD,
            -200, -1400, 200, -1000, y = 58, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("water", "venetian", "campaign")),

        // ── Floating Islands ────────────────────────────────────────
        loc("sky_reaches", "Sky Reaches — Floating Islands", OVERWORLD,
            -300, -200, 300, 200, y = 150, height = 80, type = LocationType.WILDERNESS,
            tags = setOf("floating", "sky", "campaign")),

        // ── Pirate Cove ─────────────────────────────────────────────
        loc("pirate_cove", "Corsair's Rest — Pirate Cove", OVERWORLD,
            -2000, -1000, -1600, -600, y = 60, type = LocationType.CITY,
            protection = "PROTECTED", tags = setOf("pirate", "islands", "campaign")),

        // ── Void Cracks (scattered) ─────────────────────────────────
        loc("void_crack_1", "Void Crack Alpha", OVERWORLD,
            500, -500, 530, -470, y = 40, height = 80, type = LocationType.VOID_CRACK,
            tags = setOf("void", "dangerous")),

        loc("void_crack_2", "Void Crack Beta", OVERWORLD,
            -900, 300, -870, 330, y = 40, height = 80, type = LocationType.VOID_CRACK,
            tags = setOf("void", "dangerous")),

        loc("void_crack_3", "Void Crack Gamma", OVERWORLD,
            1500, 600, 1530, 630, y = 40, height = 80, type = LocationType.VOID_CRACK,
            tags = setOf("void", "dangerous"))
    )

    // ═════════════════════════════════════════════════════════════════════
    //  COSMIC PLATFORM DIMENSION
    // ═════════════════════════════════════════════════════════════════════

    val COSMIC = "voidworld:cosmic_platform"

    val cosmicLocations = listOf(
        loc("cosmic_main_platform", "Main Observatory", COSMIC,
            -100, -100, 100, 100, y = 64, type = LocationType.BUILDING,
            tags = setOf("act_5", "cosmic")),

        loc("cosmic_lab", "King's Laboratory", COSMIC,
            -40, -80, 40, -20, y = 64, type = LocationType.ROOM,
            tags = setOf("act_5", "research")),

        loc("cosmic_capsule_hall", "Consciousness Capsule Hall", COSMIC,
            -80, 20, 80, 80, y = 64, type = LocationType.ROOM,
            tags = setOf("act_5", "discovery")),

        loc("cosmic_demon_arena", "Demon Arena", COSMIC,
            120, -60, 220, 60, y = 64, type = LocationType.SPAWN_ZONE,
            tags = setOf("act_5", "combat", "boss")),

        loc("cosmic_teleporter", "Dimensional Teleporter", COSMIC,
            -20, -20, 20, 20, y = 64, type = LocationType.TELEPORT_POINT,
            tags = setOf("act_5"))
    )

    // ═════════════════════════════════════════════════════════════════════
    //  CONSCIOUSNESS PLANET DIMENSION
    // ═════════════════════════════════════════════════════════════════════

    val NOOSPHERE = "voidworld:consciousness_planet"

    val noosphereLocations = listOf(
        loc("noo_landing", "Landing Site", NOOSPHERE,
            -30, -30, 30, 30, y = 64, type = LocationType.TELEPORT_POINT,
            tags = setOf("act_5", "consciousness")),

        loc("noo_whispering_plains", "The Whispering Plains", NOOSPHERE,
            -200, -200, 200, 0, y = 64, type = LocationType.WILDERNESS,
            tags = setOf("act_5", "talking_terrain")),

        loc("noo_frozen_city", "Frozen City of Echoes", NOOSPHERE,
            -150, 50, 150, 300, y = 64, type = LocationType.CITY,
            tags = setOf("act_5", "absorbed_civilization")),

        loc("noo_core", "Planetary Core — The Choice", NOOSPHERE,
            -40, 350, 40, 430, y = 40, height = 60, type = LocationType.QUEST_AREA,
            tags = setOf("act_6", "final_choice", "endings"))
    )

    // ═════════════════════════════════════════════════════════════════════

    /** All locations grouped by dimension name. */
    val allByDimension = mapOf(
        "overworld" to overworldLocations,
        "cosmic_platform" to cosmicLocations,
        "consciousness_planet" to noosphereLocations
    )

    /** Flat list of all locations. */
    val all = overworldLocations + cosmicLocations + noosphereLocations
}
