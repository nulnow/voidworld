package com.voidworld.world.gen

import com.voidworld.VoidWorldMod
import net.minecraft.server.MinecraftServer
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

/**
 * Manages pre-built world templates for VoidWorld.
 *
 * ## How it works
 *
 * 1. You build the map in creative mode (vanilla or with WorldEdit/WorldPainter)
 * 2. Copy the region files (`.mca` files) from the world save into
 *    `worldtemplate/overworld/`, `worldtemplate/void_dimension/`, etc.
 * 3. Place the `worldtemplate/` folder next to the mod JAR (in the mods/ directory)
 *    or inside the JAR as a resource
 * 4. When a new VoidWorld is created, this manager copies the template regions
 *    into the new world's save directory
 *
 * ## Directory layout
 *
 * ```
 * worldtemplate/
 *   overworld/
 *     region/
 *       r.0.0.mca
 *       r.-1.0.mca
 *       ...
 *     entities/
 *       r.0.0.mca
 *       ...
 *   cosmic_platform/
 *     region/
 *       r.0.0.mca
 *   consciousness_planet/
 *     region/
 *       r.0.0.mca
 * ```
 *
 * ## Important
 * Region files contain 32x32 chunks (512x512 blocks).
 * File `r.X.Z.mca` contains chunks from (X*32, Z*32) to (X*32+31, Z*32+31).
 */
object WorldTemplateManager {

    /** Name of the template directory to look for. */
    private const val TEMPLATE_DIR = "worldtemplate"

    /** Mapping from template subdirectory name to MC dimension save folder. */
    private val dimensionMapping = mapOf(
        "overworld" to "",                          // root of save directory
        "the_nether" to "DIM-1",
        "the_end" to "DIM1",
        "cosmic_platform" to "dimensions/voidworld/cosmic_platform",
        "consciousness_planet" to "dimensions/voidworld/consciousness_planet"
    )

    /**
     * Find the world template directory.
     * Checks multiple locations in priority order.
     */
    fun findTemplateDir(): Path? {
        // 1. Check next to the game directory (server root or .minecraft)
        val gameDir = Path.of("").toAbsolutePath()
        val candidates = listOf(
            gameDir.resolve(TEMPLATE_DIR),
            gameDir.resolve("mods/$TEMPLATE_DIR"),
            gameDir.resolve("config/$TEMPLATE_DIR")
        )

        return candidates.firstOrNull { Files.isDirectory(it) }
    }

    /**
     * Install the world template into a new world's save directory.
     *
     * Call this AFTER the world save directory is created but BEFORE
     * the server fully loads the world (so the chunks get loaded from
     * the copied region files instead of being generated).
     *
     * @param worldSavePath Path to the world save directory (e.g., saves/VoidWorld/)
     * @return true if template was installed successfully
     */
    fun installTemplate(worldSavePath: Path): Boolean {
        val templateDir = findTemplateDir()
        if (templateDir == null) {
            VoidWorldMod.LOGGER.warn("No world template found. World will generate void chunks.")
            VoidWorldMod.LOGGER.info("To use pre-built maps, create a '$TEMPLATE_DIR/' directory.")
            return false
        }

        VoidWorldMod.LOGGER.info("Installing world template from: $templateDir")

        var copiedFiles = 0
        var copiedDimensions = 0

        for ((templateName, saveFolderName) in dimensionMapping) {
            val templateDimDir = templateDir.resolve(templateName)
            if (!Files.isDirectory(templateDimDir)) continue

            val targetDir = if (saveFolderName.isEmpty()) {
                worldSavePath
            } else {
                worldSavePath.resolve(saveFolderName)
            }

            val count = copyDirectory(templateDimDir, targetDir)
            if (count > 0) {
                copiedDimensions++
                copiedFiles += count
                VoidWorldMod.LOGGER.info("  Copied $count files for dimension: $templateName")
            }
        }

        if (copiedFiles > 0) {
            VoidWorldMod.LOGGER.info("Template installed: $copiedFiles files across $copiedDimensions dimensions.")
        } else {
            VoidWorldMod.LOGGER.warn("Template directory found but contained no files.")
        }

        return copiedFiles > 0
    }

    /**
     * Check if a world save already has template data installed.
     * Prevents overwriting an existing save.
     */
    fun isTemplateInstalled(worldSavePath: Path): Boolean {
        val markerFile = worldSavePath.resolve(".voidworld_template_installed")
        return Files.exists(markerFile)
    }

    /**
     * Mark a world as having the template installed.
     */
    fun markTemplateInstalled(worldSavePath: Path) {
        val markerFile = worldSavePath.resolve(".voidworld_template_installed")
        Files.writeString(markerFile, "VoidWorld template installed at ${java.time.Instant.now()}")
    }

    /**
     * Get info about the template (for display in world creation screen).
     */
    fun getTemplateInfo(): TemplateInfo? {
        val templateDir = findTemplateDir() ?: return null

        val dimensions = mutableListOf<String>()
        var totalSizeMb = 0L

        for ((templateName, _) in dimensionMapping) {
            val dimDir = templateDir.resolve(templateName)
            if (Files.isDirectory(dimDir)) {
                dimensions.add(templateName)
                totalSizeMb += directorySize(dimDir) / (1024 * 1024)
            }
        }

        return TemplateInfo(
            path = templateDir,
            dimensions = dimensions,
            totalSizeMb = totalSizeMb
        )
    }

    // ── Utility ─────────────────────────────────────────────────────────

    private fun copyDirectory(source: Path, target: Path): Int {
        if (!Files.isDirectory(source)) return 0

        var count = 0
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                val targetDir = target.resolve(source.relativize(dir))
                Files.createDirectories(targetDir)
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val targetFile = target.resolve(source.relativize(file))
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING)
                count++
                return FileVisitResult.CONTINUE
            }
        })
        return count
    }

    private fun directorySize(dir: Path): Long {
        var size = 0L
        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                size += attrs.size()
                return FileVisitResult.CONTINUE
            }
        })
        return size
    }

    data class TemplateInfo(
        val path: Path,
        val dimensions: List<String>,
        val totalSizeMb: Long
    )
}
