package com.voidworld.system.quest

import com.voidworld.VoidWorldMod
import com.voidworld.core.data.PlayerVoidData
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Server-side quest manager.
 *
 * Responsible for:
 * - Loading quest definitions from data packs
 * - Tracking player quest progress
 * - Triggering quest events (start, stage advance, completion)
 * - Syncing quest state to the client for HUD/journal display
 */
object QuestManager {

    /** All registered quest definitions, keyed by quest ID. */
    private val questRegistry = mutableMapOf<ResourceLocation, IQuest>()

    /** Register a quest definition. Usually called during data loading. */
    fun registerQuest(quest: IQuest) {
        questRegistry[quest.id] = quest
        VoidWorldMod.LOGGER.debug("Registered quest: ${quest.id}")
    }

    /** Get a quest definition by ID. */
    fun getQuest(id: ResourceLocation): IQuest? = questRegistry[id]

    /** Get all registered quests. */
    fun getAllQuests(): Collection<IQuest> = questRegistry.values

    /** Get quests available for a player (prerequisites met, not completed). */
    fun getAvailableQuests(player: ServerPlayer): List<IQuest> {
        val data = PlayerVoidData.get(player) ?: return emptyList()
        return questRegistry.values.filter { quest ->
            !data.activeQuests.contains(quest.id.toString()) &&
            !data.completedQuests.contains(quest.id.toString()) &&
            quest.canStart(player)
        }
    }

    /** Start a quest for a player. */
    fun startQuest(player: ServerPlayer, questId: ResourceLocation): Boolean {
        val quest = questRegistry[questId] ?: return false
        val data = PlayerVoidData.get(player) ?: return false

        if (!quest.canStart(player)) return false

        data.activeQuests.add(questId.toString())
        quest.onStart(player)
        // TODO: Sync to client
        VoidWorldMod.LOGGER.info("Player ${player.name.string} started quest: $questId")
        return true
    }

    /** Complete a quest for a player. */
    fun completeQuest(player: ServerPlayer, questId: ResourceLocation): Boolean {
        val quest = questRegistry[questId] ?: return false
        val data = PlayerVoidData.get(player) ?: return false

        data.activeQuests.remove(questId.toString())
        data.completedQuests.add(questId.toString())
        quest.onComplete(player)
        // TODO: Sync to client, grant rewards
        VoidWorldMod.LOGGER.info("Player ${player.name.string} completed quest: $questId")
        return true
    }

    /** Called on server start to load quest definitions from datapacks. */
    fun loadQuests() {
        questRegistry.clear()
        // TODO: Load from JSON data files in data/voidworld/quests/
        VoidWorldMod.LOGGER.info("Loaded ${questRegistry.size} quests from data packs.")
    }
}
