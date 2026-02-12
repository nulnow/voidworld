package com.voidworld.world.dimension

import com.voidworld.VoidWorldMod
import com.voidworld.core.util.modResource
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType

/**
 * Custom dimension definitions for VoidWorld.
 *
 * Each dimension has:
 * - A [ResourceKey] for the Level (used to get ServerLevel)
 * - A [ResourceKey] for the DimensionType (light, sky, ceiling, etc.)
 * - Pre-built region files in the world template
 *
 * Dimensions are registered through JSON data packs in:
 * `data/voidworld/dimension/` and `data/voidworld/dimension_type/`
 *
 * The actual terrain comes from pre-built region files, not generation.
 */
object ModDimensions {

    // ── Level keys (used for teleportation, dimension checks) ───────────

    /** The main overworld — contains all cities, villages, void cracks. */
    val OVERWORLD: ResourceKey<Level> = Level.OVERWORLD

    /**
     * Cosmic Platform — the king's research station beyond the Great Attractor.
     * Magically shielded from the false vacuum. Low gravity feel, star background.
     */
    val COSMIC_PLATFORM: ResourceKey<Level> = ResourceKey.create(
        Registries.DIMENSION,
        modResource("cosmic_platform")
    )

    /**
     * Noosphere — the frozen consciousness planet.
     * Terrain made of materialized consciousness. Eerie atmosphere.
     */
    val CONSCIOUSNESS_PLANET: ResourceKey<Level> = ResourceKey.create(
        Registries.DIMENSION,
        modResource("consciousness_planet")
    )

    /**
     * Dev — empty void dimension for testing. Single stone platform generated on first entry.
     */
    val DEV: ResourceKey<Level> = ResourceKey.create(
        Registries.DIMENSION,
        modResource("dev")
    )

    // ── Dimension Type keys ─────────────────────────────────────────────

    val COSMIC_PLATFORM_TYPE: ResourceKey<DimensionType> = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        modResource("cosmic_platform")
    )

    val CONSCIOUSNESS_PLANET_TYPE: ResourceKey<DimensionType> = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        modResource("consciousness_planet")
    )

    val DEV_TYPE: ResourceKey<DimensionType> = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        modResource("dev")
    )

    // ── Helpers ─────────────────────────────────────────────────────────

    /** All custom dimension level keys for iteration. */
    val ALL_CUSTOM_DIMENSIONS = listOf(COSMIC_PLATFORM, CONSCIOUSNESS_PLANET, DEV)

    /** Check if a dimension key belongs to VoidWorld. */
    fun isVoidWorldDimension(key: ResourceKey<Level>): Boolean =
        key.location().namespace == VoidWorldMod.MOD_ID

    fun init() {
        VoidWorldMod.LOGGER.info("VoidWorld dimensions registered: ${ALL_CUSTOM_DIMENSIONS.size} custom dimensions.")
    }
}
