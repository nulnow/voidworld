package com.voidworld.system.puzzle

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Puzzle system for VoidWorld.
 *
 * Supports multiple puzzle types encountered throughout the game:
 * - Programming challenges (redstone logic, command sequences)
 * - Combinatorics puzzles (lock combinations, pattern matching)
 * - Riddles (text-based, from NPCs or ancient inscriptions)
 * - Environmental puzzles (pressure plates, lever sequences)
 * - Void puzzles (unique mechanics from the other universe)
 */
interface IPuzzle {

    /** Unique puzzle identifier. */
    val id: ResourceLocation

    /** Localization key for puzzle name. */
    val nameKey: String

    /** The type of puzzle. */
    val type: PuzzleType

    /** Difficulty level (1-10). */
    val difficulty: Int

    /** Whether the puzzle can be attempted multiple times. */
    val isRepeatable: Boolean

    /** Initialize the puzzle for a player (randomize if applicable). */
    fun initialize(player: ServerPlayer)

    /** Submit a solution attempt. Returns result. */
    fun submitSolution(player: ServerPlayer, solution: Any): PuzzleResult

    /** Get a hint for the puzzle. */
    fun getHint(player: ServerPlayer, hintLevel: Int): String

    /** Reset the puzzle to its initial state. */
    fun reset(player: ServerPlayer)
}

enum class PuzzleType {
    PROGRAMMING,    // Logic gates, code sequences
    COMBINATORICS,  // Mathematical pattern puzzles
    RIDDLE,         // Text-based riddles
    ENVIRONMENTAL,  // Physical in-world puzzles
    VOID_LOGIC,     // Puzzles using void universe mechanics
    SEQUENCE,       // Pattern/sequence recognition
    CIPHER          // Encryption/decryption puzzles
}

/**
 * Result of a puzzle solution attempt.
 */
sealed class PuzzleResult {
    /** Puzzle solved correctly. */
    data class Solved(val rewardKey: String) : PuzzleResult()

    /** Incorrect solution with feedback. */
    data class Incorrect(val feedbackKey: String) : PuzzleResult()

    /** Partial progress (for multi-step puzzles). */
    data class Partial(val progress: Float, val feedbackKey: String) : PuzzleResult()

    /** Puzzle timed out. */
    data object TimedOut : PuzzleResult()
}
