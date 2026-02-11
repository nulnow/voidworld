package com.voidworld.world.location

import com.voidworld.VoidWorldMod
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.util.UUID

/**
 * Tracks which locations each player is currently inside.
 *
 * Runs every N ticks (configurable) and fires enter/exit logic
 * when a player crosses a location boundary.
 *
 * This powers:
 * - "Entering [Location Name]" HUD notifications
 * - Quest VISIT objective completion
 * - Ambient sound changes
 * - Law system zone awareness
 * - NPC schedule triggers
 */
@Mod.EventBusSubscriber(modid = VoidWorldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object LocationTracker {

    /** How often to check positions (in ticks). 10 = twice per second. */
    private const val CHECK_INTERVAL = 10

    private var tickCounter = 0

    /** Current locations per player. */
    private val playerLocations = mutableMapOf<UUID, MutableSet<ResourceLocation>>()

    /** Listeners for location enter/exit events. */
    private val enterListeners = mutableListOf<(ServerPlayer, GameLocation) -> Unit>()
    private val exitListeners = mutableListOf<(ServerPlayer, GameLocation) -> Unit>()

    // ── Public API ──────────────────────────────────────────────────────

    fun onLocationEnter(listener: (ServerPlayer, GameLocation) -> Unit) {
        enterListeners.add(listener)
    }

    fun onLocationExit(listener: (ServerPlayer, GameLocation) -> Unit) {
        exitListeners.add(listener)
    }

    /** Get all locations a player is currently inside. */
    fun getCurrentLocations(player: ServerPlayer): Set<ResourceLocation> =
        playerLocations[player.uuid] ?: emptySet()

    /** Check if a player is currently inside a specific location. */
    fun isPlayerInLocation(player: ServerPlayer, locationId: ResourceLocation): Boolean =
        playerLocations[player.uuid]?.contains(locationId) == true

    /** Remove player tracking data (on disconnect). */
    fun removePlayer(playerId: UUID) {
        playerLocations.remove(playerId)
    }

    // ── Tick handler ────────────────────────────────────────────────────

    @SubscribeEvent
    @JvmStatic
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        tickCounter++
        if (tickCounter < CHECK_INTERVAL) return
        tickCounter = 0

        val server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer() ?: return

        for (player in server.playerList.players) {
            updatePlayerLocation(player)
        }
    }

    // ── Internal ────────────────────────────────────────────────────────

    private fun updatePlayerLocation(player: ServerPlayer) {
        val currentPos = player.blockPosition()
        val dimension = player.level().dimension()

        val nowInside = LocationRegistry
            .getLocationsAt(dimension, currentPos)
            .map { it.id }
            .toSet()

        val previouslyInside = playerLocations.getOrPut(player.uuid) { mutableSetOf() }

        // Detect entries
        val entered = nowInside - previouslyInside
        for (locId in entered) {
            val location = LocationRegistry.getLocation(locId) ?: continue
            onPlayerEntered(player, location)
        }

        // Detect exits
        val exited = previouslyInside - nowInside
        for (locId in exited) {
            val location = LocationRegistry.getLocation(locId) ?: continue
            onPlayerExited(player, location)
        }

        // Update state
        previouslyInside.clear()
        previouslyInside.addAll(nowInside)
    }

    private fun onPlayerEntered(player: ServerPlayer, location: GameLocation) {
        VoidWorldMod.LOGGER.debug("${player.name.string} entered location: ${location.id}")

        // Show entry notification
        if (location.showEntryNotification) {
            player.displayClientMessage(
                Component.translatable(location.nameKey),
                true // action bar (subtitle position)
            )
        }

        // Notify listeners (quest system, law system, etc.)
        enterListeners.forEach { it(player, location) }
    }

    private fun onPlayerExited(player: ServerPlayer, location: GameLocation) {
        VoidWorldMod.LOGGER.debug("${player.name.string} exited location: ${location.id}")

        // Notify listeners
        exitListeners.forEach { it(player, location) }
    }
}
