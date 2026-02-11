package com.voidworld.core.event

import com.voidworld.VoidWorldMod
import com.voidworld.system.quest.QuestManager
import com.voidworld.world.location.LocationRegistry
import com.voidworld.world.location.LocationTracker
import net.minecraft.world.entity.player.Player
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

/**
 * Forge event bus handlers for gameplay events.
 *
 * These listen on the MinecraftForge.EVENT_BUS (runtime game events),
 * NOT on the mod event bus (lifecycle events).
 */
@Mod.EventBusSubscriber(modid = VoidWorldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ModEvents {

    @SubscribeEvent
    @JvmStatic
    fun onServerStarting(event: ServerStartingEvent) {
        VoidWorldMod.LOGGER.info("VoidWorld server systems initializing...")

        // Load data-driven content
        LocationRegistry.loadFromDataPacks(event.server)
        QuestManager.loadQuests()

        // Wire up location enter/exit hooks to other systems
        LocationTracker.onLocationEnter { player, location ->
            // Quest system: check VISIT objectives
            // Law system: enter protected zone awareness
            // Ambient sound: start playing location ambient
        }

        LocationTracker.onLocationExit { player, location ->
            // Quest system: update tracking
            // Ambient sound: stop location ambient
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onServerStopping(event: ServerStoppingEvent) {
        VoidWorldMod.LOGGER.info("VoidWorld server shutting down, cleaning up...")
        LocationRegistry.clear()
    }

    @SubscribeEvent
    @JvmStatic
    fun onPlayerJoin(event: EntityJoinLevelEvent) {
        val entity = event.entity
        if (entity is Player && !event.level.isClientSide) {
            VoidWorldMod.LOGGER.debug("Player joined: ${entity.name.string}")
            // Sync player data: quests, economy, backstory, summon state
            // Check if first join — trigger backstory selection
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        LocationTracker.removePlayer(event.entity.uuid)
    }

    @SubscribeEvent
    @JvmStatic
    fun onPlayerClone(event: PlayerEvent.Clone) {
        // Persist custom data (quests, economy, summon) across death/dimension change
        if (event.isWasDeath) {
            VoidWorldMod.LOGGER.debug("Player died, persisting VoidWorld data...")
            // Copy capabilities from original to clone
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        // Check if the block is in a protected zone (city walls, castle, etc.)
        // If so, apply law system: fine the player, increment crime counter
        // If crime threshold exceeded, trigger prison arrest
        val player = event.player
        val pos = event.pos
        val level = player.level()

        // TODO: Check ProtectedZoneManager for this position
        // if (ProtectedZoneManager.isProtected(level, pos)) {
        //     LawManager.recordCrime(player, Crime.BLOCK_DESTRUCTION, pos)
        // }
    }
}
