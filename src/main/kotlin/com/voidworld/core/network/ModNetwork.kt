package com.voidworld.core.network

import com.voidworld.VoidWorldMod
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel

/**
 * Central network handler for client-server communication.
 *
 * All custom packets (quest updates, dialog choices, economy transactions,
 * stealth states, summon commands, etc.) are registered here.
 */
object ModNetwork {

    private const val PROTOCOL_VERSION = "1"
    private var nextId = 0

    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(VoidWorldMod.MOD_ID, "main"),
        { PROTOCOL_VERSION },
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    )

    /**
     * Register all packet types. Called from [VoidWorldMod.onCommonSetup].
     *
     * Example registration:
     * ```kotlin
     * registerPacket<QuestUpdatePacket>(NetworkDirection.PLAY_TO_CLIENT)
     * registerPacket<DialogChoicePacket>(NetworkDirection.PLAY_TO_SERVER)
     * ```
     */
    fun register() {
        VoidWorldMod.LOGGER.info("Registering network packets...")
        CHANNEL.messageBuilder(StructureHistorySyncPacket::class.java, nextId++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder { msg, buf -> msg.encode(buf) }
            .decoder { StructureHistorySyncPacket.decode(it) }
            .consumerMainThread { msg, ctx -> msg.handle(ctx) }
            .add()
    }

    // ── Utility methods ─────────────────────────────────────────────────

    /**
     * Send a packet to a specific player (server -> client).
     */
    fun sendToPlayer(player: ServerPlayer, msg: Any) {
        CHANNEL.send(PacketDistributor.PLAYER.with { player }, msg)
    }

    /**
     * Send a packet to all players tracking a certain entity.
     */
    fun sendToAllTracking(entity: net.minecraft.world.entity.Entity, msg: Any) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with { entity }, msg)
    }

    /**
     * Send a packet to the server (client -> server).
     */
    fun sendToServer(msg: Any) {
        CHANNEL.sendToServer(msg)
    }

    /**
     * Send a packet to all connected players.
     */
    fun sendToAll(msg: Any) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg)
    }
}
