package com.voidworld.system.npc

import com.voidworld.system.quest.IDialog
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

/**
 * Defines an NPC's identity, behavior, and interactions.
 *
 * NPCs can be:
 * - Quest givers (villagers, mercenaries, king's advisors)
 * - Traders (shop keepers, black market dealers)
 * - Guards (enforce law system)
 * - Ambient (populate cities, provide world-building dialog)
 * - Story characters (the king, the void god, the mercenary captain)
 */
interface INpc {

    /** Unique NPC identifier. */
    val id: ResourceLocation

    /** Localization key for NPC name. */
    val nameKey: String

    /** The NPC's profession/role. */
    val role: NpcRole

    /** Dialog tree for this NPC. May change based on quest state. */
    fun getDialog(player: Player): IDialog?

    /** Whether this NPC is currently interactable. */
    fun canInteract(player: Player): Boolean

    /** Called when a player right-clicks this NPC. */
    fun onInteract(player: Player)

    /** Trade offers if this NPC is a merchant. */
    fun getTradeOffers(): List<TradeOffer> {
        return emptyList()
    }
}

enum class NpcRole {
    QUEST_GIVER,
    TRADER,
    GUARD,
    MERCENARY,
    STORY_CHARACTER,
    AMBIENT,
    TRAINER,       // Teaches abilities for summons
    BANKER,
    PRISON_WARDEN
}

/**
 * A simple trade offer for NPC merchants.
 */
data class TradeOffer(
    val buyItemId: ResourceLocation,
    val buyCount: Int,
    val sellItemId: ResourceLocation,
    val sellCount: Int,
    val currencyCost: Int = 0,  // If > 0, uses mod currency instead of items
    val maxUses: Int = -1       // -1 = unlimited
)
