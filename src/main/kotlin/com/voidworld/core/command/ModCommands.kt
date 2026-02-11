package com.voidworld.core.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.voidworld.VoidWorldMod
import com.voidworld.core.util.sendModMessage
import com.voidworld.world.gen.WorldBootstrapper
import com.voidworld.world.location.LocationDebugWand
import com.voidworld.world.location.LocationRegistry
import com.voidworld.world.location.LocationType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.nio.file.Path

/**
 * Registers all `/vw` commands for VoidWorld.
 *
 * Command tree:
 * ```
 * /vw world bootstrap [dimension]  — Generate outlined location platforms
 * /vw world bootstrap all          — Generate for all dimensions
 * /vw location create <id> <type>  — Create location from wand selection
 * /vw location list                — List all registered locations
 * /vw location export              — Export locations to JSON file
 * /vw location tp <id>             — Teleport to a location's spawn point
 * /vw location info                — Show locations at current position
 * ```
 */
@Mod.EventBusSubscriber(modid = VoidWorldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ModCommands {

    @SubscribeEvent
    @JvmStatic
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        val dispatcher = event.dispatcher
        registerVwCommand(dispatcher)
        VoidWorldMod.LOGGER.info("VoidWorld commands registered.")
    }

    private fun registerVwCommand(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("vw")
                .requires { it.hasPermission(2) } // OP level 2

                // ── /vw world ───────────────────────────────────────
                .then(Commands.literal("world")
                    .then(Commands.literal("bootstrap")
                        .then(Commands.literal("all")
                            .executes { ctx -> bootstrapAll(ctx) }
                        )
                        .then(Commands.argument("dimension", StringArgumentType.word())
                            .suggests { _, builder ->
                                SharedSuggestionProvider.suggest(
                                    listOf("overworld", "cosmic_platform", "consciousness_planet"),
                                    builder
                                )
                            }
                            .executes { ctx -> bootstrapDimension(ctx) }
                        )
                        .executes { ctx -> bootstrapAll(ctx) }
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
    //  Command handlers
    // ═══════════════════════════════════════════════════════════════════

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
