package com.voidworld.core.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.voidworld.VoidWorldMod
import com.voidworld.core.config.ModConfig
import com.voidworld.core.data.PlayerVoidData
import com.voidworld.core.util.sendModMessage
import com.voidworld.system.economy.BankManager
import com.voidworld.system.quest.QuestManager
import com.voidworld.core.registry.ModEntities
import com.voidworld.entity.SummonedZombieEntity
import com.voidworld.world.dimension.ModDimensions
import com.voidworld.world.gen.WorldBootstrapper
import com.voidworld.world.location.LocationDebugWand
import com.voidworld.world.location.LocationRegistry
import com.voidworld.world.location.LocationType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.registries.Registries
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.nio.file.Path

/**
 * Registers all `/vw` commands for VoidWorld.
 *
 * Command tree:
 * ```
 * /vw status                       — Show full mod status overview
 * /vw dimension list               — List all available dimensions
 * /vw dimension tp <name>          — Teleport to a dimension
 * /vw dimension info               — Info about the current dimension
 * /vw world bootstrap [dimension]  — Generate outlined location platforms
 * /vw world bootstrap all          — Generate for all dimensions
 * /vw location create <id> <type>  — Create location from wand selection
 * /vw location list                — List all registered locations
 * /vw location export              — Export locations to JSON file
 * /vw location tp <id>             — Teleport to a location's spawn point
 * /vw location info                — Show locations at current position
 * /vw summon <entity> [count]      — Spawn mod mobs at player position
 * ```
 */
object ModCommands {

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        val dispatcher = event.dispatcher
        registerVwCommand(dispatcher)
        VoidWorldMod.LOGGER.info("VoidWorld commands registered.")
    }

    private fun registerVwCommand(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("vw")
                .requires { it.hasPermission(0) } // All players (was 2 — required OP, broke in worlds without cheats)
                .executes { ctx -> showStatus(ctx) } // /vw alone → same as /vw status

                // ── /vw world ───────────────────────────────────────
                .then(Commands.literal("world")
                    .then(Commands.literal("bootstrap")
                        .then(Commands.literal("all")
                            .executes { ctx -> bootstrapAll(ctx) }
                        )
                        .then(Commands.argument("dimension", StringArgumentType.word())
                            .suggests { _, builder ->
                                SharedSuggestionProvider.suggest(
                                    listOf("overworld", "cosmic_platform", "consciousness_planet", "dev"),
                                    builder
                                )
                            }
                            .executes { ctx -> bootstrapDimension(ctx) }
                        )
                        .executes { ctx -> bootstrapAll(ctx) }
                    )
                )

                // ── /vw status ────────────────────────────────────
                .then(Commands.literal("status")
                    .executes { ctx -> showStatus(ctx) }
                )

                // ── /vw dimension ─────────────────────────────────
                .then(Commands.literal("dimension")
                    .then(Commands.literal("list")
                        .executes { ctx -> dimensionList(ctx) }
                    )
                    .then(Commands.literal("tp")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests { _, builder ->
                                SharedSuggestionProvider.suggest(DIMENSION_NAMES.keys, builder)
                            }
                            .executes { ctx -> dimensionTeleport(ctx) }
                        )
                    )
                    .then(Commands.literal("info")
                        .executes { ctx -> dimensionInfo(ctx) }
                    )
                )

                // ── /vw location ────────────────────────────────────
                // ── /vw summon ──────────────────────────────────────
                .then(Commands.literal("summon")
                    .then(Commands.argument("entity", StringArgumentType.word())
                        .suggests { _, builder ->
                            SharedSuggestionProvider.suggest(MOD_ENTITY_IDS, builder)
                        }
                        .executes { ctx -> summonModEntity(ctx, 1) }
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                            .executes { ctx -> summonModEntity(ctx, IntegerArgumentType.getInteger(ctx, "count")) }
                        )
                    )
                )

                // ── /vw location ────────────────────────────────────
                .then(Commands.literal("location")
                    .then(Commands.literal("create")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("type", StringArgumentType.word())
                                .suggests { _, builder ->
                                    SharedSuggestionProvider.suggest(
                                        LocationType.entries.map { it.name.lowercase() },
                                        builder
                                    )
                                }
                                .executes { ctx -> locationCreate(ctx) }
                            )
                        )
                    )
                    .then(Commands.literal("list")
                        .executes { ctx -> locationList(ctx) }
                    )
                    .then(Commands.literal("export")
                        .executes { ctx -> locationExport(ctx) }
                    )
                    .then(Commands.literal("tp")
                        .then(Commands.argument("id", StringArgumentType.greedyString())
                            .suggests { _, builder ->
                                SharedSuggestionProvider.suggest(
                                    LocationRegistry.getAllLocations().map { it.id.toString() },
                                    builder
                                )
                            }
                            .executes { ctx -> locationTeleport(ctx) }
                        )
                    )
                    .then(Commands.literal("info")
                        .executes { ctx -> locationInfo(ctx) }
                    )
                )
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Dimension name mapping
    // ═══════════════════════════════════════════════════════════════════

    /** Mod entity IDs → EntityType for /vw summon. */
    private val MOD_ENTITY_IDS: List<String> = listOf(
        "city_guardian",
        "dark_zombie",
        "paladin",
        "summoned_zombie"
    )

    private fun getModEntityType(id: String): EntityType<*>? {
        return when (id.lowercase()) {
            "city_guardian" -> ModEntities.CITY_GUARDIAN.get()
            "dark_zombie" -> ModEntities.DARK_ZOMBIE.get()
            "paladin" -> ModEntities.PALADIN.get()
            "summoned_zombie" -> ModEntities.SUMMONED_ZOMBIE.get()
            else -> null
        }
    }

    /** Short aliases → dimension ResourceKey for quick teleportation. */
    private val DIMENSION_NAMES: Map<String, ResourceKey<Level>> = mapOf(
        "overworld"             to Level.OVERWORLD,
        "nether"                to Level.NETHER,
        "end"                   to Level.END,
        "cosmic_platform"       to ModDimensions.COSMIC_PLATFORM,
        "consciousness_planet"  to ModDimensions.CONSCIOUSNESS_PLANET,
        "dev"                  to ModDimensions.DEV
    )

    // ═══════════════════════════════════════════════════════════════════
    //  Command handlers
    // ═══════════════════════════════════════════════════════════════════

    private fun showStatus(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val data = PlayerVoidData.get(player)

        player.sendModMessage("══════ VoidWorld Status ══════")
        player.sendModMessage("Mod version: 0.1.0")
        player.sendModMessage("")

        // Economy
        player.sendModMessage("--- Economy ---")
        if (data != null) {
            player.sendModMessage("  Wallet: ${data.currency} coins")
            player.sendModMessage("  Bank: ${data.bankBalance} coins")
            player.sendModMessage("  Interest rate: ${ModConfig.BANK_INTEREST_RATE.get()}")
        } else {
            player.sendModMessage("  [No player data - capability not attached]")
        }
        player.sendModMessage("")

        // Quests
        player.sendModMessage("--- Quests ---")
        val totalQuests = QuestManager.getAllQuests().size
        if (data != null) {
            player.sendModMessage("  Registered quests: $totalQuests")
            player.sendModMessage("  Active: ${data.activeQuests.size}")
            player.sendModMessage("  Completed: ${data.completedQuests.size}")
        } else {
            player.sendModMessage("  Registered quests: $totalQuests")
        }
        player.sendModMessage("")

        // Backstory
        player.sendModMessage("--- Backstory ---")
        if (data != null) {
            player.sendModMessage("  Selected: ${data.backstorySelected}")
            if (data.backstoryChoices.isNotEmpty()) {
                player.sendModMessage("  Choices: ${data.backstoryChoices.joinToString(", ")}")
            }
        }
        player.sendModMessage("")

        // Law & Crime
        player.sendModMessage("--- Law System ---")
        if (data != null) {
            player.sendModMessage("  Crime level: ${data.crimeLevel}")
            player.sendModMessage("  In prison: ${data.isInPrison}")
            if (data.isInPrison) {
                player.sendModMessage("  Time remaining: ${data.prisonTimeRemaining / 20}s")
            }
            player.sendModMessage("  Block destruction fine: ${ModConfig.BLOCK_DESTRUCTION_FINE.get()} coins")
        }
        player.sendModMessage("")

        // Summon
        player.sendModMessage("--- Summon System ---")
        if (data != null) {
            player.sendModMessage("  Has summon: ${data.hasSummon}")
            if (data.hasSummon) {
                player.sendModMessage("  Summon ID: ${data.summonId}")
            }
            player.sendModMessage("  Max abilities: ${ModConfig.MAX_SUMMON_ABILITIES.get()}")
            player.sendModMessage("  Cooldown: ${ModConfig.SUMMON_COOLDOWN_TICKS.get() / 20}s")
        }
        player.sendModMessage("")

        // Stealth
        player.sendModMessage("--- Stealth ---")
        player.sendModMessage("  Detection range: ${ModConfig.STEALTH_DETECTION_RANGE.get()} blocks")
        player.sendModMessage("  Light factor: ${ModConfig.STEALTH_LIGHT_FACTOR.get()}")
        player.sendModMessage("")

        // Locations
        player.sendModMessage("--- World ---")
        val allLocations = LocationRegistry.getAllLocations()
        player.sendModMessage("  Registered locations: ${allLocations.size}")
        val pos = player.blockPosition()
        val dim = player.level().dimension()
        val currentLocations = LocationRegistry.getLocationsAt(dim, pos)
        if (currentLocations.isNotEmpty()) {
            player.sendModMessage("  You are in: ${currentLocations.joinToString { "${it.id} [${it.type}]" }}")
        } else {
            player.sendModMessage("  You are in: [wilderness / unregistered area]")
        }
        player.sendModMessage("  Position: ${pos.x}, ${pos.y}, ${pos.z}")

        player.sendModMessage("══════════════════════════════")
        return 1
    }

    // ── Dimension commands ──────────────────────────────────────────

    private fun dimensionList(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val server = player.server

        player.sendModMessage("══════ Dimensions ══════")

        // Show all loaded server levels
        for ((alias, key) in DIMENSION_NAMES) {
            val level = server.getLevel(key)
            val status = if (level != null) "§aLoaded" else "§cNot loaded"
            val current = if (player.level().dimension() == key) " §e← you are here" else ""
            player.sendModMessage("  $alias (${key.location()}) — $status$current")
        }

        // Also list any other loaded dimensions not in our map
        val knownKeys = DIMENSION_NAMES.values.toSet()
        for (level in server.allLevels) {
            if (level.dimension() !in knownKeys) {
                val current = if (player.level().dimension() == level.dimension()) " §e← you are here" else ""
                player.sendModMessage("  ${level.dimension().location()} — §aLoaded$current")
            }
        }

        player.sendModMessage("════════════════════════")
        return 1
    }

    private fun dimensionTeleport(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val name = StringArgumentType.getString(ctx, "name")

        // Try alias first, then try as a full ResourceLocation
        val dimKey = DIMENSION_NAMES[name]
            ?: run {
                val rl = ResourceLocation.tryParse(name)
                if (rl != null) ResourceKey.create(Registries.DIMENSION, rl) else null
            }

        if (dimKey == null) {
            player.sendModMessage("§cUnknown dimension: $name")
            player.sendModMessage("Use §e/vw dimension list§r to see available dimensions.")
            return 0
        }

        // Check if already in this dimension
        if (player.level().dimension() == dimKey) {
            player.sendModMessage("§eYou are already in ${dimKey.location()}!")
            return 0
        }

        val targetLevel: ServerLevel? = player.server.getLevel(dimKey)
        if (targetLevel == null) {
            player.sendModMessage("§cDimension ${dimKey.location()} is not loaded on this server.")
            return 0
        }

        // Auto-bootstrap cosmic_platform: generate platforms & wizard temple if dimension is void
        if (dimKey == ModDimensions.COSMIC_PLATFORM) {
            val bootstrapped = WorldBootstrapper.ensureCosmicPlatformBootstrapped(player.server)
            if (bootstrapped > 0) {
                player.sendModMessage("§eGenerated $bootstrapped platforms in Cosmic Platform.")
            }
        }
        // Auto-generate dev platform on first entry
        if (dimKey == ModDimensions.DEV) {
            if (WorldBootstrapper.ensureDevPlatformGenerated(player.server)) {
                player.sendModMessage("§eGenerated stone platform in Dev dimension.")
            }
        }

        // Determine safe spawn position in target dimension
        val targetPos = findSafeSpawn(targetLevel, player)

        player.sendModMessage("Teleporting to §b${dimKey.location()}§r...")
        player.teleportTo(
            targetLevel,
            targetPos.x.toDouble() + 0.5,
            targetPos.y.toDouble(),
            targetPos.z.toDouble() + 0.5,
            player.yRot,
            player.xRot
        )
        player.sendModMessage("§aArrived at ${dimKey.location()} (${targetPos.x}, ${targetPos.y}, ${targetPos.z})")
        return 1
    }

    private fun dimensionInfo(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val dim = player.level().dimension()
        val level = player.level() as ServerLevel

        player.sendModMessage("══════ Dimension Info ══════")
        player.sendModMessage("  Name: ${dim.location()}")
        player.sendModMessage("  Is VoidWorld: ${ModDimensions.isVoidWorldDimension(dim)}")
        player.sendModMessage("  Seed: ${level.seed}")
        player.sendModMessage("  Day time: ${level.dayTime}")
        player.sendModMessage("  Players here: ${level.players().size}")

        val pos = player.blockPosition()
        player.sendModMessage("  Your position: ${pos.x}, ${pos.y}, ${pos.z}")

        // Show locations in this dimension
        val locsHere = LocationRegistry.getAllLocations().filter {
            it.dimension == dim.location()
        }
        player.sendModMessage("  Locations in this dimension: ${locsHere.size}")

        player.sendModMessage("════════════════════════════")
        return 1
    }

    /**
     * Find a safe Y level to spawn the player in the target dimension.
     * Tries the shared spawn point first, otherwise scans downward from max build height.
     */
    private fun findSafeSpawn(level: ServerLevel, player: ServerPlayer): BlockPos {
        // Try the world spawn first
        val spawn = level.sharedSpawnPos
        if (spawn.y > level.minBuildHeight) {
            return spawn
        }

        // Fallback: search at (0, ?, 0) or player's current XZ
        val x = 0
        val z = 0
        for (y in level.maxBuildHeight downTo level.minBuildHeight) {
            val pos = BlockPos(x, y, z)
            val block = level.getBlockState(pos)
            val above = level.getBlockState(pos.above())
            val above2 = level.getBlockState(pos.above(2))
            if (!block.isAir && above.isAir && above2.isAir) {
                return pos.above()
            }
        }

        // Absolute fallback: spawn at Y=64
        return BlockPos(x, 64, z)
    }

    // ── Summon commands ─────────────────────────────────────────────

    private fun summonModEntity(ctx: CommandContext<CommandSourceStack>, count: Int): Int {
        val player = ctx.source.playerOrException
        val id = StringArgumentType.getString(ctx, "entity")
        val entityType = getModEntityType(id)
            ?: run {
                val rl = ResourceLocation.tryParse(if (id.contains(":")) id else "${VoidWorldMod.MOD_ID}:$id")
                if (rl != null) {
                    player.server.registryAccess()
                        .registryOrThrow(Registries.ENTITY_TYPE)
                        .getOptional(rl)
                        .orElse(null)
                } else null
            }

        if (entityType == null) {
            player.sendModMessage("§cUnknown mod entity: $id")
            player.sendModMessage("Available: ${MOD_ENTITY_IDS.joinToString(", ")}")
            return 0
        }

        val level = player.level() as ServerLevel
        val pos = player.blockPosition()
        var spawned = 0

        for (i in 0 until count) {
            val offsetX = (i % 3 - 1) * 2
            val offsetZ = (i / 3 - 1) * 2
            val spawnPos = pos.offset(offsetX, 0, offsetZ)

            val entity = entityType.create(level) ?: continue
            if (entity is SummonedZombieEntity) entity.setOwner(player)
            entity.moveTo(
                spawnPos.x + 0.5,
                spawnPos.y.toDouble(),
                spawnPos.z + 0.5,
                level.random.nextFloat() * 360f,
                0f
            )
            if (level.addFreshEntity(entity)) {
                spawned++
            }
        }

        val entityId = entityType.builtInRegistryHolder().key().location().toString()
        player.sendModMessage("§aSpawned $spawned × $entityId at (${pos.x}, ${pos.y}, ${pos.z})")
        return spawned
    }

    // ── Bootstrap commands ───────────────────────────────────────────

    private fun bootstrapAll(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        player.sendModMessage("Bootstrapping all dimensions...")

        val count = WorldBootstrapper.bootstrapAll(player.server)
        player.sendModMessage("Done! Placed outlines for $count locations across all dimensions.")
        return count
    }

    private fun bootstrapDimension(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val dimName = StringArgumentType.getString(ctx, "dimension")
        player.sendModMessage("Bootstrapping dimension: $dimName...")

        val count = WorldBootstrapper.bootstrapDimension(player.server, dimName)
        player.sendModMessage("Done! Placed outlines for $count locations in $dimName.")
        return count
    }

    private fun locationCreate(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val id = StringArgumentType.getString(ctx, "id")
        val type = StringArgumentType.getString(ctx, "type")

        val location = LocationDebugWand.createFromSelection(player, id, type)
        return if (location != null) 1 else 0
    }

    private fun locationList(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val locations = LocationRegistry.getAllLocations()

        if (locations.isEmpty()) {
            player.sendModMessage("No locations registered.")
            return 0
        }

        player.sendModMessage("=== Locations (${locations.size}) ===")
        for (loc in locations.sortedBy { it.id.toString() }) {
            val size = "(${loc.maxPos.x - loc.minPos.x + 1}x${loc.maxPos.z - loc.minPos.z + 1})"
            player.sendModMessage("  ${loc.id} [${loc.type}] $size at ${loc.dimension}")
        }
        return locations.size
    }

    private fun locationExport(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val exportPath = Path.of("config", VoidWorldMod.MOD_ID, "exported_locations.json")

        LocationRegistry.exportToFile(exportPath)
        player.sendModMessage("Exported ${LocationRegistry.getAllLocations().size} locations to: $exportPath")
        return 1
    }

    private fun locationTeleport(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val idStr = StringArgumentType.getString(ctx, "id")
        val id = net.minecraft.resources.ResourceLocation.tryParse(idStr)

        if (id == null) {
            player.sendModMessage("Invalid location ID: $idStr")
            return 0
        }

        val location = LocationRegistry.getLocation(id)
        if (location == null) {
            player.sendModMessage("Location not found: $id")
            return 0
        }

        val tp = location.spawnPoint ?: location.center
        player.teleportTo(tp.x.toDouble() + 0.5, tp.y.toDouble(), tp.z.toDouble() + 0.5)
        player.sendModMessage("Teleported to ${location.id}")
        return 1
    }

    private fun locationInfo(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.playerOrException
        val pos = player.blockPosition()
        val dim = player.level().dimension()

        val locations = LocationRegistry.getLocationsAt(dim, pos)
        if (locations.isEmpty()) {
            player.sendModMessage("You are not inside any registered location.")
            return 0
        }

        player.sendModMessage("=== Current locations ===")
        for (loc in locations) {
            player.sendModMessage("  ${loc.id} [${loc.type}] protection=${loc.protectionLevel ?: "none"}")
        }
        return locations.size
    }
}
