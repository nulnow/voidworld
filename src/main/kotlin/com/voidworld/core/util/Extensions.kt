package com.voidworld.core.util

import com.voidworld.VoidWorldMod
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

// ═══════════════════════════════════════════════════════════════════════════
//  Kotlin extension functions and utilities for VoidWorld
// ═══════════════════════════════════════════════════════════════════════════

// ── ResourceLocation helpers ────────────────────────────────────────────

/**
 * Create a [ResourceLocation] namespaced to this mod.
 */
fun modResource(path: String): ResourceLocation =
    ResourceLocation(VoidWorldMod.MOD_ID, path)

// ── Component / text helpers ────────────────────────────────────────────

/**
 * Create a translatable text component namespaced to VoidWorld.
 * Example: `modTranslatable("quest", "find_the_king")` → key `"quest.voidworld.find_the_king"`
 */
fun modTranslatable(category: String, key: String): MutableComponent =
    Component.translatable("$category.${VoidWorldMod.MOD_ID}.$key")

/**
 * Create a literal (non-translatable) component.
 */
fun literal(text: String): MutableComponent =
    Component.literal(text)

// ── BlockPos / Vec3 helpers ─────────────────────────────────────────────

fun BlockPos.toVec3(): Vec3 = Vec3(x.toDouble(), y.toDouble(), z.toDouble())

fun BlockPos.distanceTo(other: BlockPos): Double {
    val dx = (x - other.x).toDouble()
    val dy = (y - other.y).toDouble()
    val dz = (z - other.z).toDouble()
    return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
}

// ── NBT helpers ─────────────────────────────────────────────────────────

/**
 * DSL builder for [CompoundTag].
 *
 * ```kotlin
 * val tag = nbt {
 *     putString("name", "Hero")
 *     putInt("level", 5)
 * }
 * ```
 */
inline fun nbt(block: CompoundTag.() -> Unit): CompoundTag =
    CompoundTag().apply(block)

// ── Player helpers ──────────────────────────────────────────────────────

/**
 * Send a chat message to the player (server-side only).
 */
fun ServerPlayer.sendModMessage(message: String) {
    sendSystemMessage(Component.literal("[VoidWorld] $message"))
}

/**
 * Check whether this is a server-side player entity.
 */
val Player.isServerSide: Boolean
    get() = !level().isClientSide
