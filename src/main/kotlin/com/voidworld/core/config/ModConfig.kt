package com.voidworld.core.config

import net.minecraftforge.common.ForgeConfigSpec

/**
 * Mod configuration using Forge's config system.
 *
 * Split into COMMON (synced server config) and CLIENT (local rendering/UI prefs).
 */
object ModConfig {

    // ══════════════════════════════════════════════════════════════════════
    //  COMMON CONFIG  (server-authoritative, synced to clients)
    // ══════════════════════════════════════════════════════════════════════

    private val commonBuilder = ForgeConfigSpec.Builder()

    // ── Economy ─────────────────────────────────────────────────────────
    val STARTING_CURRENCY: ForgeConfigSpec.IntValue
    val BLOCK_DESTRUCTION_FINE: ForgeConfigSpec.IntValue
    val PRISON_ESCAPE_DIFFICULTY: ForgeConfigSpec.IntValue
    val BANK_INTEREST_RATE: ForgeConfigSpec.DoubleValue

    // ── Stealth ─────────────────────────────────────────────────────────
    val STEALTH_DETECTION_RANGE: ForgeConfigSpec.DoubleValue
    val STEALTH_LIGHT_FACTOR: ForgeConfigSpec.DoubleValue

    // ── Summon system ───────────────────────────────────────────────────
    val MAX_SUMMON_ABILITIES: ForgeConfigSpec.IntValue
    val SUMMON_COOLDOWN_TICKS: ForgeConfigSpec.IntValue

    // ── Quests ──────────────────────────────────────────────────────────
    val QUEST_MARKER_RENDER_DISTANCE: ForgeConfigSpec.IntValue

    // ── Void cracks ─────────────────────────────────────────────────────
    val VOID_CRACK_SPAWN_CHANCE: ForgeConfigSpec.DoubleValue
    val VOID_CREATURE_DIFFICULTY_MULTIPLIER: ForgeConfigSpec.DoubleValue

    val spec: ForgeConfigSpec

    init {
        commonBuilder.push("economy")
        STARTING_CURRENCY = commonBuilder
            .comment("Amount of currency a new player starts with")
            .defineInRange("startingCurrency", 100, 0, 100_000)
        BLOCK_DESTRUCTION_FINE = commonBuilder
            .comment("Fine for destroying a protected city block")
            .defineInRange("blockDestructionFine", 50, 0, 10_000)
        PRISON_ESCAPE_DIFFICULTY = commonBuilder
            .comment("Prison escape difficulty (1=easy, 10=very hard)")
            .defineInRange("prisonEscapeDifficulty", 5, 1, 10)
        BANK_INTEREST_RATE = commonBuilder
            .comment("Daily interest rate for bank deposits (0.0 to 1.0)")
            .defineInRange("bankInterestRate", 0.01, 0.0, 1.0)
        commonBuilder.pop()

        commonBuilder.push("stealth")
        STEALTH_DETECTION_RANGE = commonBuilder
            .comment("Base NPC detection range in blocks")
            .defineInRange("detectionRange", 16.0, 1.0, 64.0)
        STEALTH_LIGHT_FACTOR = commonBuilder
            .comment("How much light level affects detection (0=none, 1=full)")
            .defineInRange("lightFactor", 0.7, 0.0, 1.0)
        commonBuilder.pop()

        commonBuilder.push("summon")
        MAX_SUMMON_ABILITIES = commonBuilder
            .comment("Maximum number of abilities a summoned creature can have")
            .defineInRange("maxAbilities", 6, 1, 20)
        SUMMON_COOLDOWN_TICKS = commonBuilder
            .comment("Cooldown in ticks between summon uses")
            .defineInRange("cooldownTicks", 200, 0, 12000)
        commonBuilder.pop()

        commonBuilder.push("quests")
        QUEST_MARKER_RENDER_DISTANCE = commonBuilder
            .comment("Maximum render distance for quest markers in blocks")
            .defineInRange("markerRenderDistance", 64, 16, 256)
        commonBuilder.pop()

        commonBuilder.push("voidCracks")
        VOID_CRACK_SPAWN_CHANCE = commonBuilder
            .comment("Chance (0.0-1.0) for a void crack to spawn in an eligible chunk")
            .defineInRange("spawnChance", 0.02, 0.0, 1.0)
        VOID_CREATURE_DIFFICULTY_MULTIPLIER = commonBuilder
            .comment("Difficulty multiplier for void creatures")
            .defineInRange("difficultyMultiplier", 1.5, 0.1, 10.0)
        commonBuilder.pop()

        spec = commonBuilder.build()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CLIENT CONFIG  (local only — rendering, HUD, key preferences)
    // ══════════════════════════════════════════════════════════════════════

    private val clientBuilder = ForgeConfigSpec.Builder()

    val SHOW_QUEST_HUD: ForgeConfigSpec.BooleanValue
    val SHOW_STEALTH_INDICATOR: ForgeConfigSpec.BooleanValue
    val SHOW_ECONOMY_HUD: ForgeConfigSpec.BooleanValue
    val DIALOG_TEXT_SPEED: ForgeConfigSpec.IntValue

    val clientSpec: ForgeConfigSpec

    init {
        clientBuilder.push("hud")
        SHOW_QUEST_HUD = clientBuilder
            .comment("Show the quest tracker HUD overlay")
            .define("showQuestHud", true)
        SHOW_STEALTH_INDICATOR = clientBuilder
            .comment("Show stealth detection indicator")
            .define("showStealthIndicator", true)
        SHOW_ECONOMY_HUD = clientBuilder
            .comment("Show currency balance in HUD")
            .define("showEconomyHud", true)
        clientBuilder.pop()

        clientBuilder.push("dialog")
        DIALOG_TEXT_SPEED = clientBuilder
            .comment("Dialog text reveal speed (characters per tick, 0=instant)")
            .defineInRange("textSpeed", 2, 0, 20)
        clientBuilder.pop()

        clientSpec = clientBuilder.build()
    }
}
