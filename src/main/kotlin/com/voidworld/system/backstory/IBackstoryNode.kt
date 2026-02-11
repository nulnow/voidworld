package com.voidworld.system.backstory

import net.minecraft.resources.ResourceLocation

/**
 * Backstory selection system for VoidWorld.
 *
 * At the start of the game, the player selects their character's backstory
 * through a decision tree. These choices affect:
 * - Starting stats and equipment
 * - Available dialog options throughout the game
 * - NPC reactions and trust levels
 * - Certain quest paths and outcomes
 * - The player's relationship with various factions
 *
 * The backstory is revealed in fragments as the amnesiac protagonist
 * recovers memories during the main story.
 */
interface IBackstoryNode {

    /** Unique node identifier in the backstory tree. */
    val id: String

    /** Localization key for the question / narrative text. */
    val textKey: String

    /** Available choices at this node. */
    val choices: List<BackstoryChoice>

    /** Whether this is a leaf node (no further choices). */
    val isLeaf: Boolean get() = choices.isEmpty()
}

/**
 * A choice within the backstory selection tree.
 */
data class BackstoryChoice(
    /** Unique choice identifier (stored in player data). */
    val id: String,

    /** Localization key for the choice text. */
    val textKey: String,

    /** ID of the next [IBackstoryNode] to navigate to. Null if this is the end. */
    val nextNodeId: String?,

    /** Tags applied to the player by this choice (used in conditions). */
    val tags: List<String> = emptyList(),

    /** Stat modifiers granted by this choice. */
    val statModifiers: Map<String, Int> = emptyMap()
)

/**
 * Categories of backstory that affect different game aspects.
 *
 * Each category represents a question in the backstory tree:
 * - ORIGIN: Where the character came from
 * - PROFESSION: What they did before the shipwreck
 * - MOTIVATION: Why they were on the ship (illegally)
 * - PERSONALITY: How they deal with conflict
 * - SECRET: A hidden aspect of their past
 */
enum class BackstoryCategory {
    ORIGIN,
    PROFESSION,
    MOTIVATION,
    PERSONALITY,
    SECRET
}

/**
 * The complete backstory tree definition.
 */
data class BackstoryTree(
    val id: ResourceLocation,
    val rootNodeId: String,
    val nodes: Map<String, IBackstoryNode>
) {
    fun getNode(id: String): IBackstoryNode? = nodes[id]
}
