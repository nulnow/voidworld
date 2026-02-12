package com.voidworld.world.gen

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.voidworld.VoidWorldMod
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Void chunk generator for VoidWorld.
 *
 * Generates completely empty chunks. Pre-built map content is placed
 * by copying region files from the world template into the save directory
 * before/at world creation time.
 *
 * Chunks that don't have pre-built region data remain void (empty),
 * creating natural boundaries for the playable map area.
 *
 * Can optionally generate a bedrock floor at Y=0 or leave truly void.
 */
class VoidWorldChunkGenerator(
    biomeSource: FixedBiomeSource,
    private val generateBedrockFloor: Boolean = false
) : ChunkGenerator(biomeSource) {

    companion object {
        val CODEC: Codec<VoidWorldChunkGenerator> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("bedrock_floor", false)
                    .forGetter { it.generateBedrockFloor }
            ).apply(instance) { bedrock ->
                VoidWorldChunkGenerator(
                    FixedBiomeSource(Holder.direct(
                        // Will be replaced with actual biome holder at registration time
                        net.minecraft.world.level.biome.Biome.BiomeBuilder()
                            .downfall(0.5f)
                            .temperature(0.8f)
                            .hasPrecipitation(true)
                            .specialEffects(
                                net.minecraft.world.level.biome.BiomeSpecialEffects.Builder()
                                    .fogColor(0xC0D8FF)
                                    .waterColor(0x3F76E4)
                                    .waterFogColor(0x050533)
                                    .skyColor(0x78A7FF)
                                    .build()
                            )
                            .mobSpawnSettings(net.minecraft.world.level.biome.MobSpawnSettings.EMPTY)
                            .generationSettings(net.minecraft.world.level.biome.BiomeGenerationSettings.EMPTY)
                            .build()
                    )),
                    bedrock
                )
            }
        }
    }

    override fun codec(): Codec<out ChunkGenerator> = CODEC

    override fun applyCarvers(
        level: WorldGenRegion,
        seed: Long,
        random: RandomState,
        biomeManager: BiomeManager,
        structureManager: StructureManager,
        chunk: ChunkAccess,
        step: GenerationStep.Carving
    ) {
        // No carving in void world
    }

    override fun buildSurface(
        level: WorldGenRegion,
        structureManager: StructureManager,
        random: RandomState,
        chunk: ChunkAccess
    ) {
        // No surface in void world, unless bedrock floor is enabled
        if (generateBedrockFloor) {
            val pos = BlockPos.MutableBlockPos()
            for (x in 0..15) {
                for (z in 0..15) {
                    pos.set(chunk.pos.minBlockX + x, chunk.minBuildHeight, chunk.pos.minBlockZ + z)
                    chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false)
                }
            }
        }
    }

    override fun fillFromNoise(
        executor: Executor,
        blender: Blender,
        random: RandomState,
        structureManager: StructureManager,
        chunk: ChunkAccess
    ): CompletableFuture<ChunkAccess> {
        // Return the chunk as-is (empty)
        return CompletableFuture.completedFuture(chunk)
    }

    override fun getBaseHeight(
        x: Int, z: Int,
        type: Heightmap.Types,
        level: LevelHeightAccessor,
        random: RandomState
    ): Int {
        return if (generateBedrockFloor) level.minBuildHeight + 1 else level.minBuildHeight
    }

    override fun getBaseColumn(x: Int, z: Int, height: LevelHeightAccessor, random: RandomState): NoiseColumn {
        val states = arrayOfNulls<BlockState>(height.height)
        for (i in states.indices) {
            states[i] = Blocks.AIR.defaultBlockState()
        }
        if (generateBedrockFloor && states.isNotEmpty()) {
            states[0] = Blocks.BEDROCK.defaultBlockState()
        }
        return NoiseColumn(height.minBuildHeight, states)
    }

    override fun addDebugScreenInfo(info: MutableList<String>, random: RandomState, pos: BlockPos) {
        info.add("VoidWorld Generator")
        info.add("Bedrock floor: $generateBedrockFloor")
    }

    override fun getMinY(): Int = 0

    override fun getGenDepth(): Int = 256

    override fun getSeaLevel(): Int = 63

    override fun spawnOriginalMobs(region: WorldGenRegion) {
        // No natural mob spawning in the void world
    }
}
