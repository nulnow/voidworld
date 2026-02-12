package com.voidworld.core.event

import com.voidworld.VoidWorldMod
import com.voidworld.core.config.ModConfig
import com.voidworld.core.data.PlayerVoidData
import com.voidworld.core.util.PlayerAttackerTracker
import com.voidworld.core.util.sendModMessage
import com.voidworld.system.quest.QuestManager
import com.voidworld.world.location.LocationRegistry
import com.voidworld.client.gui.VoidWorldTitleScreen
import com.voidworld.core.registry.ModEntities
import com.voidworld.core.registry.ModRegistries
import com.voidworld.core.util.VoidWorldSessionFlags
import com.voidworld.entity.CityGuardianEntity
import com.voidworld.entity.SummonedZombieEntity
import com.voidworld.entity.CityGuardianSpawning
import com.voidworld.world.location.LocationTracker
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

/**
 * Forge event bus handlers for gameplay events.
 *
 * These listen on the MinecraftForge.EVENT_BUS (runtime game events),
 * NOT on the mod event bus (lifecycle events).
 */
object ModEvents {

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        VoidWorldMod.LOGGER.info("VoidWorld server systems initializing...")

        // Load data-driven content
        LocationRegistry.loadFromDataPacks(event.server)
        QuestManager.loadQuests()

        val locations = LocationRegistry.getAllLocations()
        val quests = QuestManager.getAllQuests()
        VoidWorldMod.LOGGER.info("VoidWorld loaded: ${locations.size} locations, ${quests.size} quests")

        // Wire up location enter/exit hooks to other systems
        LocationTracker.onLocationEnter { player, location ->
            if (player is ServerPlayer) {
                player.sendModMessage("[Location] Entered: ${location.id} [${location.type}]")
            }
        }

        LocationTracker.onLocationExit { player, location ->
            if (player is ServerPlayer) {
                player.sendModMessage("[Location] Left: ${location.id}")
            }
        }

        // Spawn CityGuardians in city locations
        CityGuardianSpawning.spawnInCities(event.server)
    }

    private const val GUARDIAN_HIT_THRESHOLD = 3
    private val guardianHitCounts = java.util.concurrent.ConcurrentHashMap<java.util.UUID, Int>()

    @SubscribeEvent
    fun onLivingHurt(event: LivingHurtEvent) {
        if (event.entity.level().isClientSide) return

        val victim = event.entity
        val source = event.source
        val attacker = source.entity as? LivingEntity ?: source.directEntity as? LivingEntity

        // Guardian/Paladin hit tracking
        if ((victim is CityGuardianEntity || victim is com.voidworld.entity.PaladinEntity) && attacker is Player) {
            val newCount = guardianHitCounts.merge(attacker.uuid, 1) { a, b -> a + b } ?: 1
            if (newCount >= GUARDIAN_HIT_THRESHOLD) {
                guardianHitCounts.remove(attacker.uuid)
                val effect = ModRegistries.WANTED_FOR_DESTRUCTION.get()
                val instance = net.minecraft.world.effect.MobEffectInstance(effect, 10 * 60 * 20, 0, false, true, true)
                attacker.addEffect(instance)
                (attacker as? ServerPlayer)?.sendModMessage("[Guardians] You have been marked! City guardians will hunt you for 10 minutes.")
            }
        }

        // Player attacker tracking (for SummonedZombie)
        if (victim is Player && attacker != null && attacker !== victim) {
            PlayerAttackerTracker.recordHit(victim.uuid, attacker.uuid)
        }
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        VoidWorldMod.LOGGER.info("VoidWorld server shutting down, cleaning up...")
        LocationRegistry.clear()
        guardianHitCounts.clear()
        PlayerAttackerTracker.clear()
    }

    /** Set of players who have already received the welcome message this session. */
    private val welcomedPlayers = mutableSetOf<java.util.UUID>()

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinLevelEvent) {
        val entity = event.entity
        if (event.level.isClientSide) return

        // Make hostile mobs (including Slime/MagmaCube) target City Guardians and Paladins
        if ((entity is Monster || entity is Slime) && entity !is CityGuardianEntity && entity !is com.voidworld.entity.PaladinEntity) {
            entity.targetSelector.addGoal(2, NearestAttackableTargetGoal(entity, CityGuardianEntity::class.java, 10, true, false, null))
            entity.targetSelector.addGoal(2, NearestAttackableTargetGoal(entity, com.voidworld.entity.PaladinEntity::class.java, 10, true, false, null))
        }
    }

    @SubscribeEvent
    fun onPlayerJoin(event: EntityJoinLevelEvent) {
        val entity = event.entity
        if (entity is ServerPlayer && !event.level.isClientSide) {
            VoidWorldMod.LOGGER.info("Player joined: ${entity.name.string}")

            // VoidWorld start: night + 10 summoned zombies
            if (VoidWorldSessionFlags.voidWorldSessionStarting && event.level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                entity.server?.execute {
                    val level = event.level as net.minecraft.server.level.ServerLevel
                    (level.levelData as net.minecraft.world.level.storage.ServerLevelData).setDayTime(13000)  // night
                    val zombieType = ModEntities.SUMMONED_ZOMBIE.get()
                    for (i in 0 until 10) {
                        val zombie = zombieType.create(level) ?: continue
                        zombie.setOwner(entity)
                        val offsetX = (i % 4 - 2) * 2
                        val offsetZ = (i / 4 - 1) * 2
                        zombie.moveTo(
                            entity.x + offsetX,
                            entity.y,
                            entity.z + offsetZ,
                            level.random.nextFloat() * 360f,
                            0f
                        )
                        level.addFreshEntity(zombie)
                    }
                    VoidWorldSessionFlags.voidWorldSessionStarting = false
                }
            }

            // Only send welcome once per session
            if (welcomedPlayers.add(entity.uuid)) {
                entity.server?.execute {
                    sendWelcomeMessage(entity)
                }
            }
        }
    }

    private fun sendWelcomeMessage(player: ServerPlayer) {
        val data = PlayerVoidData.get(player)

        player.sendModMessage("══════ Welcome to VoidWorld ══════")
        player.sendModMessage("Version 0.1.0 | Type /vw status for full info")
        player.sendModMessage("")

        if (data != null) {
            player.sendModMessage("Wallet: ${data.currency} coins | Bank: ${data.bankBalance} coins")
            player.sendModMessage("Active quests: ${data.activeQuests.size} | Completed: ${data.completedQuests.size}")
            player.sendModMessage("Crime level: ${data.crimeLevel}${if (data.isInPrison) " [IN PRISON]" else ""}")

            if (!data.backstorySelected) {
                player.sendModMessage("[!] Backstory not yet selected")
            }
            if (data.hasSummon) {
                player.sendModMessage("Summon: ${data.summonId}")
            }
        } else {
            player.sendModMessage("[Warning] Player data not initialized")
        }

        val locations = LocationRegistry.getAllLocations()
        val quests = QuestManager.getAllQuests()
        player.sendModMessage("World: ${locations.size} locations | ${quests.size} quests loaded")
        player.sendModMessage("══════════════════════════════════")
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        LocationTracker.removePlayer(event.entity.uuid)
        welcomedPlayers.remove(event.entity.uuid)
        VoidWorldMod.LOGGER.info("Player left: ${event.entity.name.string}")
    }

    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        // Persist custom data (quests, economy, summon) across death/dimension change
        if (event.isWasDeath) {
            VoidWorldMod.LOGGER.info("Player died, persisting VoidWorld data...")
            val original = PlayerVoidData.get(event.original)
            val clone = PlayerVoidData.get(event.entity)
            if (original != null && clone != null) {
                clone.loadFromNbt(original.saveToNbt())
                VoidWorldMod.LOGGER.info("Player data copied to respawned entity")
            }

            val player = event.entity
            if (player is ServerPlayer) {
                player.server?.execute {
                    player.sendModMessage("[Death] Your VoidWorld data has been preserved.")
                    val data = PlayerVoidData.get(player)
                    if (data != null) {
                        player.sendModMessage("[Economy] Wallet: ${data.currency} | Bank: ${data.bankBalance}")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        val player = event.player
        val pos = event.pos
        val level = player.level()

        if (player is ServerPlayer) {
            // Check if in a registered location
            val dim = level.dimension()
            val currentLocations = LocationRegistry.getLocationsAt(dim, pos)
            if (currentLocations.isNotEmpty()) {
                val protectedLocs = currentLocations.filter { !it.protectionLevel.isNullOrEmpty() }
                if (protectedLocs.isNotEmpty()) {
                    val fine = ModConfig.BLOCK_DESTRUCTION_FINE.get()
                    player.sendModMessage("[Law] Block destroyed in protected zone: ${protectedLocs.first().id}")
                    player.sendModMessage("[Law] Fine applied: $fine coins")

                    val data = PlayerVoidData.get(player)
                    if (data != null) {
                        data.crimeLevel++
                        data.currency = maxOf(0, data.currency - fine)
                        player.sendModMessage("[Economy] Balance: ${data.currency} coins | Crime level: ${data.crimeLevel}")
                    }
                } else {
                    player.sendModMessage("[Debug] Block broken at ${pos.x},${pos.y},${pos.z} in ${currentLocations.first().id}")
                }
            }
        }
    }
}
