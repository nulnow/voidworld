package com.voidworld.system.quest

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

/**
 * Represents a single quest in the VoidWorld quest system.
 *
 * Quests are data-driven (loaded from JSON in `data/voidworld/quests/`)
 * and progress through a series of [QuestStage]s.
 */
interface IQuest {

    /** Unique quest identifier, e.g. `"voidworld:village_rescue"`. */
    val id: ResourceLocation

    /** Localization key for the quest name. */
    val nameKey: String

    /** Localization key for the quest description. */
    val descriptionKey: String

    /** The quest category for journal organization. */
    val category: QuestCategory

    /** Whether the quest is part of the main storyline. */
    val isMainStory: Boolean

    /** Ordered list of stages this quest progresses through. */
    val stages: List<QuestStage>

    /** Prerequisites: quest IDs that must be completed first. */
    val prerequisites: List<ResourceLocation>

    /** Check whether a player can start this quest. */
    fun canStart(player: Player): Boolean

    /** Called when the player starts the quest. */
    fun onStart(player: Player)

    /** Called when the player completes a stage. */
    fun onStageComplete(player: Player, stage: QuestStage)

    /** Called when the entire quest is completed. */
    fun onComplete(player: Player)

    /** Serialize quest progress to NBT. */
    fun saveProgress(tag: CompoundTag)

    /** Deserialize quest progress from NBT. */
    fun loadProgress(tag: CompoundTag)
}

/**
 * A single stage within a quest.
 */
data class QuestStage(
    val id: String,
    val descriptionKey: String,
    val objectives: List<QuestObjective>,
    val isOptional: Boolean = false
)

/**
 * An individual objective within a quest stage.
 */
data class QuestObjective(
    val id: String,
    val type: ObjectiveType,
    val target: String,          // e.g. entity id, block id, location name
    val requiredCount: Int = 1,
    var currentCount: Int = 0,
    val descriptionKey: String
) {
    val isComplete: Boolean get() = currentCount >= requiredCount
}

enum class ObjectiveType {
    KILL,           // Kill specific entities
    COLLECT,        // Collect items
    TALK_TO,        // Speak with an NPC
    VISIT,          // Visit a location
    ESCORT,         // Escort an NPC
    CRAFT,          // Craft an item
    BUILD,          // Build a structure
    SURVIVE,        // Survive for a duration
    STEALTH,        // Complete a stealth section
    PUZZLE,         // Solve a puzzle
    CUSTOM          // Custom scripted objective
}

enum class QuestCategory {
    MAIN_STORY,
    SIDE_QUEST,
    CITY_QUEST,
    CAMPAIGN,       // Sub-campaigns (pirate, underwater, etc.)
    BOUNTY,         // Repeatable bounties
    EXPLORATION
}
