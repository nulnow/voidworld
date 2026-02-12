package com.voidworld.world.gen

import com.voidworld.VoidWorldMod
import com.voidworld.world.location.GameLocation
import com.voidworld.world.location.LocationRegistry
import com.voidworld.world.location.LocationType
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SignBlock
import net.minecraft.world.level.block.entity.SignBlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * Generates schematic outlines for all VoidWorld locations.
 *
 * When `/vw world bootstrap` is run, this places:
 * - A flat floor platform for each location (material varies by type)
 * - Colored boundary walls (1 block high) around the perimeter
 * - Corner pillars (3 blocks high) for visibility
 * - A sign at the entrance with the location name and type
 * - A beacon-style marker at the center (glowstone pillar)
 *
 * The player then builds the actual content on top of these outlines.
 */
object WorldBootstrapper {

    // ── Block palette per location type ─────────────────────────────────

    private data class Palette(
        val floor: BlockState,
        val border: BlockState,
        val corner: BlockState,
        val center: BlockState
    )

    private val palettes = mapOf(
        LocationType.CITY to Palette(
            floor = Blocks.STONE_BRICKS.defaultBlockState(),
            border = Blocks.RED_CONCRETE.defaultBlockState(),
            corner = Blocks.RED_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.GLOWSTONE.defaultBlockState()
        ),
        LocationType.DISTRICT to Palette(
            floor = Blocks.SMOOTH_STONE.defaultBlockState(),
            border = Blocks.ORANGE_CONCRETE.defaultBlockState(),
            corner = Blocks.ORANGE_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.SEA_LANTERN.defaultBlockState()
        ),
        LocationType.BUILDING to Palette(
            floor = Blocks.POLISHED_ANDESITE.defaultBlockState(),
            border = Blocks.YELLOW_CONCRETE.defaultBlockState(),
            corner = Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.GLOWSTONE.defaultBlockState()
        ),
        LocationType.DUNGEON to Palette(
            floor = Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
            border = Blocks.PURPLE_CONCRETE.defaultBlockState(),
            corner = Blocks.PURPLE_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.SOUL_LANTERN.defaultBlockState()
        ),
        LocationType.ROOM to Palette(
            floor = Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            border = Blocks.MAGENTA_CONCRETE.defaultBlockState(),
            corner = Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.LANTERN.defaultBlockState()
        ),
        LocationType.WILDERNESS to Palette(
            floor = Blocks.GRASS_BLOCK.defaultBlockState(),
            border = Blocks.GREEN_CONCRETE.defaultBlockState(),
            corner = Blocks.GREEN_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.GLOWSTONE.defaultBlockState()
        ),
        LocationType.QUEST_AREA to Palette(
            floor = Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
            border = Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
            corner = Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.SEA_LANTERN.defaultBlockState()
        ),
        LocationType.VOID_CRACK to Palette(
            floor = Blocks.OBSIDIAN.defaultBlockState(),
            border = Blocks.BLACK_CONCRETE.defaultBlockState(),
            corner = Blocks.CRYING_OBSIDIAN.defaultBlockState(),
            center = Blocks.END_ROD.defaultBlockState()
        ),
        LocationType.TELEPORT_POINT to Palette(
            floor = Blocks.END_STONE_BRICKS.defaultBlockState(),
            border = Blocks.CYAN_CONCRETE.defaultBlockState(),
            corner = Blocks.CYAN_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.BEACON.defaultBlockState()
        ),
        LocationType.SPAWN_ZONE to Palette(
            floor = Blocks.NETHERRACK.defaultBlockState(),
            border = Blocks.RED_NETHER_BRICKS.defaultBlockState(),
            corner = Blocks.MAGMA_BLOCK.defaultBlockState(),
            center = Blocks.SOUL_LANTERN.defaultBlockState()
        ),
        LocationType.POINT_OF_INTEREST to Palette(
            floor = Blocks.END_STONE_BRICKS.defaultBlockState(),
            border = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
            corner = Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.defaultBlockState(),
            center = Blocks.END_ROD.defaultBlockState()
        )
    )

    private val defaultPalette = Palette(
        floor = Blocks.STONE.defaultBlockState(),
        border = Blocks.WHITE_CONCRETE.defaultBlockState(),
        corner = Blocks.WHITE_GLAZED_TERRACOTTA.defaultBlockState(),
        center = Blocks.GLOWSTONE.defaultBlockState()
    )

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Bootstrap all dimensions.
     * @return total locations placed
     */
    fun bootstrapAll(server: MinecraftServer): Int {
        var total = 0
        for ((dimName, _) in BootstrapLocations.allByDimension) {
            total += bootstrapDimension(server, dimName)
        }
        return total
    }

    /**
     * Ensures cosmic_platform has platforms generated. If the main spawn area is void (air),
     * runs bootstrap. Safe to call multiple times — only generates when needed.
     */
    fun ensureCosmicPlatformBootstrapped(server: MinecraftServer): Int {
        val level = resolveLevel(server, "cosmic_platform") ?: return 0
        val spawnCheck = BlockPos(0, 64, 0)
        if (!level.getBlockState(spawnCheck).isAir) {
            return 0 // Already bootstrapped
        }
        return bootstrapDimension(server, "cosmic_platform")
    }

    /**
     * Bootstrap a single dimension.
     * @return number of locations placed
     */
    fun bootstrapDimension(server: MinecraftServer, dimensionName: String): Int {
        val locations = BootstrapLocations.allByDimension[dimensionName]
        if (locations == null) {
            VoidWorldMod.LOGGER.warn("Unknown dimension for bootstrap: $dimensionName")
            return 0
        }

        val level = resolveLevel(server, dimensionName)
        if (level == null) {
            VoidWorldMod.LOGGER.warn("Could not resolve ServerLevel for: $dimensionName")
            return 0
        }

        var count = 0
        for (location in locations) {
            placeLocationOutline(level, location)
            LocationRegistry.register(location)
            count++
        }

        VoidWorldMod.LOGGER.info("Bootstrapped $count locations in $dimensionName")
        return count
    }

    // ── Outline placement ───────────────────────────────────────────────

    private fun placeLocationOutline(level: ServerLevel, location: GameLocation) {
        val palette = palettes[location.type] ?: defaultPalette
        val min = location.minPos
        val max = location.maxPos
        val floorY = min.y

        // 1. Floor platform
        placeFloor(level, min.x, min.z, max.x, max.z, floorY, palette.floor)

        // 2. Border walls (1 block high on top of floor)
        placeBorder(level, min.x, min.z, max.x, max.z, floorY + 1, palette.border)

        // 3. Corner pillars (3 blocks high)
        placeCornerPillars(level, min.x, min.z, max.x, max.z, floorY + 1, palette.corner)

        // 4. Center marker (glowstone pillar 3 high)
        val cx = (min.x + max.x) / 2
        val cz = (min.z + max.z) / 2
        for (dy in 0..2) {
            level.setBlockAndUpdate(BlockPos(cx, floorY + 1 + dy, cz), palette.center)
        }

        // 5. Sign at south edge center with location name
        placeInfoSign(level, cx, floorY + 1, min.z, location)

        VoidWorldMod.LOGGER.debug("Placed outline for: ${location.id}")
    }

    private fun placeFloor(
        level: ServerLevel,
        x1: Int, z1: Int, x2: Int, z2: Int,
        y: Int, block: BlockState
    ) {
        // For large areas, only place floor at edges + grid every 16 blocks
        // to save time and show the outline without filling everything
        val width = x2 - x1
        val depth = z2 - z1
        val isLarge = width > 100 || depth > 100

        for (x in x1..x2) {
            for (z in z1..z2) {
                val isEdge = x == x1 || x == x2 || z == z1 || z == z2
                val isGrid = (x - x1) % 16 == 0 || (z - z1) % 16 == 0
                val isCorner = (x == x1 || x == x2) && (z == z1 || z == z2)

                if (!isLarge || isEdge || isGrid || isCorner) {
                    level.setBlockAndUpdate(BlockPos(x, y, z), block)
                }
            }
        }
    }

    private fun placeBorder(
        level: ServerLevel,
        x1: Int, z1: Int, x2: Int, z2: Int,
        y: Int, block: BlockState
    ) {
        // North and south edges
        for (x in x1..x2) {
            level.setBlockAndUpdate(BlockPos(x, y, z1), block)
            level.setBlockAndUpdate(BlockPos(x, y, z2), block)
        }
        // West and east edges
        for (z in z1..z2) {
            level.setBlockAndUpdate(BlockPos(x1, y, z), block)
            level.setBlockAndUpdate(BlockPos(x2, y, z), block)
        }
    }

    private fun placeCornerPillars(
        level: ServerLevel,
        x1: Int, z1: Int, x2: Int, z2: Int,
        y: Int, block: BlockState
    ) {
        val corners = listOf(
            BlockPos(x1, y, z1), BlockPos(x2, y, z1),
            BlockPos(x1, y, z2), BlockPos(x2, y, z2)
        )
        for (corner in corners) {
            for (dy in 0..2) {
                level.setBlockAndUpdate(corner.above(dy), block)
            }
            // Torch on top
            level.setBlockAndUpdate(corner.above(3), Blocks.TORCH.defaultBlockState())
        }
    }

    private fun placeInfoSign(
        level: ServerLevel,
        x: Int, y: Int, z: Int,
        location: GameLocation
    ) {
        val signPos = BlockPos(x, y, z - 1)
        val signState = Blocks.OAK_SIGN.defaultBlockState()
        level.setBlockAndUpdate(signPos, signState)

        val blockEntity = level.getBlockEntity(signPos)
        if (blockEntity is SignBlockEntity) {
            // Front side: name and type
            val idShort = location.id.path
            val typeName = location.type.name

            blockEntity.setText(
                blockEntity.getFrontText()
                    .setMessage(0, Component.literal("=== ${typeName} ==="))
                    .setMessage(1, Component.literal(idShort))
                    .setMessage(2, Component.literal("${location.maxPos.x - location.minPos.x + 1}x${location.maxPos.z - location.minPos.z + 1}"))
                    .setMessage(3, Component.literal(location.tags.take(3).joinToString(","))),
                true
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun resolveLevel(server: MinecraftServer, dimensionName: String): ServerLevel? {
        return when (dimensionName) {
            "overworld" -> server.getLevel(Level.OVERWORLD)
            "cosmic_platform" -> {
                val key = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation("voidworld", "cosmic_platform")
                )
                server.getLevel(key)
            }
            "consciousness_planet" -> {
                val key = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation("voidworld", "consciousness_planet")
                )
                server.getLevel(key)
            }
            else -> null
        }
    }
}
