package com.voidworld.system.summon

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Summoned creature system for VoidWorld.
 *
 * The god of the dead universe can create clones of beings that assist the player.
 * Players obtain a fragment of surviving consciousness from the void universe
 * and customize their summoned creature with qualities and abilities.
 *
 * Customization aspects:
 * - Base form (chosen from collected void consciousness fragments)
 * - Abilities (combat, utility, stealth, exploration)
 * - Appearance (colors, patterns, size modifiers)
 * - Personality traits (affects AI behavior)
 */
interface ISummon {

    /** Unique summon creature ID for this player. */
    val id: ResourceLocation

    /** Display name (set by player). */
    var displayName: String

    /** The base form template. */
    val baseForm: SummonForm

    /** Equipped abilities (limited by config). */
    val abilities: MutableList<SummonAbility>

    /** Current level / experience. */
    var level: Int
    var experience: Int

    /** Whether the summon is currently active in the world. */
    var isActive: Boolean

    /** Summon the creature at the player's location. */
    fun summon(player: ServerPlayer): Boolean

    /** Dismiss the summoned creature. */
    fun dismiss(player: ServerPlayer)

    /** Add an ability to this summon. */
    fun addAbility(ability: SummonAbility): Boolean

    /** Remove an ability from this summon. */
    fun removeAbility(abilityId: ResourceLocation): Boolean

    /** Serialize to NBT for persistence. */
    fun saveToNbt(): CompoundTag

    /** Deserialize from NBT. */
    fun loadFromNbt(tag: CompoundTag)
}

/**
 * Base form templates for summoned creatures.
 * Obtained from void consciousness fragments found at void cracks.
 */
data class SummonForm(
    val id: ResourceLocation,
    val nameKey: String,
    val baseHealth: Float,
    val baseAttack: Float,
    val baseSpeed: Float,
    val size: SummonSize,
    val movementType: MovementType,
    val abilitySlots: Int
)

enum class SummonSize { TINY, SMALL, MEDIUM, LARGE, HUGE }

enum class MovementType { GROUND, FLYING, SWIMMING, PHASING }

/**
 * An ability that can be equipped on a summoned creature.
 * Parts from void universe creatures can be used to create abilities.
 */
data class SummonAbility(
    val id: ResourceLocation,
    val nameKey: String,
    val type: AbilityType,
    val cooldownTicks: Int,
    val manaCost: Int = 0,
    val descriptionKey: String
)

enum class AbilityType {
    ATTACK,         // Direct damage ability
    DEFEND,         // Shield / damage reduction
    HEAL,           // Heal the player or summon
    BUFF,           // Temporary stat boost
    DEBUFF,         // Weaken enemies
    UTILITY,        // Non-combat (light, mining, scouting)
    TRANSPORT,      // Carry / fly the player
    STEALTH,        // Help with stealth missions
    SPECIAL         // Unique void-powered abilities
}
