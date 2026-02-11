package com.voidworld.system.quest

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

/**
 * A dialog tree used in NPC conversations and quest interactions.
 *
 * Dialogs are data-driven (loaded from `data/voidworld/dialogs/`) and support:
 * - Branching based on player choices
 * - Condition checks (quest state, backstory, items, etc.)
 * - Triggering quest starts/completions
 * - Economy transactions (shop dialogs)
 */
interface IDialog {

    /** Unique dialog identifier. */
    val id: ResourceLocation

    /** The root node of this dialog tree. */
    val rootNode: DialogNode
}

/**
 * A single node in a dialog tree.
 */
data class DialogNode(
    /** Unique node ID within the dialog. */
    val id: String,

    /** The NPC's text (localization key). */
    val textKey: String,

    /** Available player responses / choices. */
    val choices: List<DialogChoice>,

    /** Actions to execute when this node is reached. */
    val actions: List<DialogAction> = emptyList()
)

/**
 * A player's response option in a dialog.
 */
data class DialogChoice(
    /** Display text (localization key). */
    val textKey: String,

    /** The next [DialogNode] id to navigate to. Empty string = end dialog. */
    val nextNodeId: String,

    /** Condition that must be met for this choice to be visible. */
    val condition: DialogCondition? = null,

    /** Actions to execute when this choice is selected. */
    val actions: List<DialogAction> = emptyList()
)

/**
 * Conditions for showing/hiding dialog choices.
 */
sealed class DialogCondition {
    data class QuestComplete(val questId: ResourceLocation) : DialogCondition()
    data class QuestActive(val questId: ResourceLocation) : DialogCondition()
    data class HasItem(val itemId: ResourceLocation, val count: Int = 1) : DialogCondition()
    data class BackstoryChoice(val choiceId: String) : DialogCondition()
    data class MinCurrency(val amount: Int) : DialogCondition()
    data class CrimeLevelBelow(val maxLevel: Int) : DialogCondition()
    data class Not(val inner: DialogCondition) : DialogCondition()
    data class And(val conditions: List<DialogCondition>) : DialogCondition()
    data class Or(val conditions: List<DialogCondition>) : DialogCondition()

    /** Evaluate this condition for the given player. */
    fun evaluate(player: Player): Boolean = when (this) {
        is QuestComplete -> {
            val data = com.voidworld.core.data.PlayerVoidData.get(player)
            data?.completedQuests?.contains(questId.toString()) == true
        }
        is QuestActive -> {
            val data = com.voidworld.core.data.PlayerVoidData.get(player)
            data?.activeQuests?.contains(questId.toString()) == true
        }
        is HasItem -> {
            player.inventory.items.any {
                it.item.builtInRegistryHolder().key()?.location() == itemId && it.count >= count
            }
        }
        is BackstoryChoice -> {
            val data = com.voidworld.core.data.PlayerVoidData.get(player)
            data?.backstoryChoices?.contains(choiceId) == true
        }
        is MinCurrency -> {
            val data = com.voidworld.core.data.PlayerVoidData.get(player)
            (data?.currency ?: 0) >= amount
        }
        is CrimeLevelBelow -> {
            val data = com.voidworld.core.data.PlayerVoidData.get(player)
            (data?.crimeLevel ?: 0) < maxLevel
        }
        is Not -> !inner.evaluate(player)
        is And -> conditions.all { it.evaluate(player) }
        is Or -> conditions.any { it.evaluate(player) }
    }
}

/**
 * Actions that can be triggered by dialog nodes or choices.
 */
sealed class DialogAction {
    data class StartQuest(val questId: ResourceLocation) : DialogAction()
    data class CompleteQuest(val questId: ResourceLocation) : DialogAction()
    data class GiveCurrency(val amount: Int) : DialogAction()
    data class TakeCurrency(val amount: Int) : DialogAction()
    data class GiveItem(val itemId: ResourceLocation, val count: Int = 1) : DialogAction()
    data class TakeItem(val itemId: ResourceLocation, val count: Int = 1) : DialogAction()
    data class SetBackstoryChoice(val choiceId: String) : DialogAction()
    data class Teleport(val x: Double, val y: Double, val z: Double) : DialogAction()
    data object EndDialog : DialogAction()
}
