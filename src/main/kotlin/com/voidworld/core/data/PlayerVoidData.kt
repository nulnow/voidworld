package com.voidworld.core.data

import com.voidworld.VoidWorldMod
import com.voidworld.core.util.nbt
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityToken
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

/**
 * Per-player persistent data for all VoidWorld systems.
 *
 * Attached as a Forge Capability to every player entity, this holds:
 * - Economy balance & bank state
 * - Active / completed quests
 * - Backstory choices
 * - Summon creature configuration
 * - Crime record & prison state
 * - Stealth stats
 */
class PlayerVoidData {

    // ── Economy ─────────────────────────────────────────────────────────
    var currency: Int = 100
    var bankBalance: Int = 0

    // ── Quests ──────────────────────────────────────────────────────────
    val activeQuests: MutableSet<String> = mutableSetOf()
    val completedQuests: MutableSet<String> = mutableSetOf()

    // ── Backstory ───────────────────────────────────────────────────────
    var backstorySelected: Boolean = false
    val backstoryChoices: MutableList<String> = mutableListOf()

    // ── Law ─────────────────────────────────────────────────────────────
    var crimeLevel: Int = 0
    var isInPrison: Boolean = false
    var prisonTimeRemaining: Int = 0 // ticks

    // ── Summon ──────────────────────────────────────────────────────────
    var hasSummon: Boolean = false
    var summonId: String = ""

    // ── Serialization ───────────────────────────────────────────────────

    fun saveToNbt(): CompoundTag = nbt {
        putInt("currency", currency)
        putInt("bankBalance", bankBalance)

        put("activeQuests", CompoundTag().also { tag ->
            activeQuests.forEachIndexed { i, q -> tag.putString("q$i", q) }
            tag.putInt("count", activeQuests.size)
        })
        put("completedQuests", CompoundTag().also { tag ->
            completedQuests.forEachIndexed { i, q -> tag.putString("q$i", q) }
            tag.putInt("count", completedQuests.size)
        })

        putBoolean("backstorySelected", backstorySelected)
        put("backstoryChoices", CompoundTag().also { tag ->
            backstoryChoices.forEachIndexed { i, c -> tag.putString("c$i", c) }
            tag.putInt("count", backstoryChoices.size)
        })

        putInt("crimeLevel", crimeLevel)
        putBoolean("isInPrison", isInPrison)
        putInt("prisonTimeRemaining", prisonTimeRemaining)

        putBoolean("hasSummon", hasSummon)
        putString("summonId", summonId)
    }

    fun loadFromNbt(tag: CompoundTag) {
        currency = tag.getInt("currency")
        bankBalance = tag.getInt("bankBalance")

        activeQuests.clear()
        tag.getCompound("activeQuests").let { q ->
            repeat(q.getInt("count")) { i -> activeQuests.add(q.getString("q$i")) }
        }
        completedQuests.clear()
        tag.getCompound("completedQuests").let { q ->
            repeat(q.getInt("count")) { i -> completedQuests.add(q.getString("q$i")) }
        }

        backstorySelected = tag.getBoolean("backstorySelected")
        backstoryChoices.clear()
        tag.getCompound("backstoryChoices").let { c ->
            repeat(c.getInt("count")) { i -> backstoryChoices.add(c.getString("c$i")) }
        }

        crimeLevel = tag.getInt("crimeLevel")
        isInPrison = tag.getBoolean("isInPrison")
        prisonTimeRemaining = tag.getInt("prisonTimeRemaining")

        hasSummon = tag.getBoolean("hasSummon")
        summonId = tag.getString("summonId")
    }

    // ── Capability wiring ───────────────────────────────────────────────

    companion object {
        val CAPABILITY: Capability<PlayerVoidData> =
            CapabilityManager.get(object : CapabilityToken<PlayerVoidData>() {})

        val RESOURCE = ResourceLocation(VoidWorldMod.MOD_ID, "player_void_data")

        fun get(player: Player): PlayerVoidData? =
            player.getCapability(CAPABILITY).orElse(null)
    }

    class Provider : ICapabilitySerializable<CompoundTag> {
        private val data = PlayerVoidData()
        private val lazyOptional = LazyOptional.of { data }

        override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
            return if (cap == CAPABILITY) lazyOptional.cast() else LazyOptional.empty()
        }

        override fun serializeNBT(): CompoundTag = data.saveToNbt()

        override fun deserializeNBT(nbt: CompoundTag) = data.loadFromNbt(nbt)
    }

    @Mod.EventBusSubscriber(modid = VoidWorldMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    object EventHandler {
        @SubscribeEvent
        @JvmStatic
        fun onAttachCapabilities(event: AttachCapabilitiesEvent<Player>) {
            if (event.`object` is Player) {
                event.addCapability(RESOURCE, Provider())
            }
        }
    }
}
