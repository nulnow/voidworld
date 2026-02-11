package com.voidworld.system.economy

import net.minecraft.server.level.ServerPlayer

/**
 * Economy system interface for VoidWorld.
 *
 * Handles:
 * - Player wallet (carried currency)
 * - Bank accounts with interest
 * - Fines from the law system
 * - Trade transactions
 * - Reward payouts from quests
 */
interface IEconomy {

    /** Get a player's wallet balance (on-hand currency). */
    fun getBalance(player: ServerPlayer): Int

    /** Get a player's bank balance. */
    fun getBankBalance(player: ServerPlayer): Int

    /** Add currency to a player's wallet. Returns new balance. */
    fun addCurrency(player: ServerPlayer, amount: Int): Int

    /** Remove currency from a player's wallet. Returns true if sufficient funds. */
    fun removeCurrency(player: ServerPlayer, amount: Int): Boolean

    /** Deposit from wallet to bank. Returns true if sufficient funds. */
    fun bankDeposit(player: ServerPlayer, amount: Int): Boolean

    /** Withdraw from bank to wallet. Returns true if sufficient funds. */
    fun bankWithdraw(player: ServerPlayer, amount: Int): Boolean

    /** Apply a fine to the player (removes from wallet, then bank if insufficient). */
    fun applyFine(player: ServerPlayer, amount: Int, reason: String): Boolean

    /** Transfer currency between two players. */
    fun transfer(from: ServerPlayer, to: ServerPlayer, amount: Int): Boolean
}

/**
 * Record of a financial transaction for auditing / player history.
 */
data class Transaction(
    val type: TransactionType,
    val amount: Int,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    QUEST_REWARD,
    TRADE_BUY,
    TRADE_SELL,
    FINE,
    BANK_DEPOSIT,
    BANK_WITHDRAW,
    BANK_INTEREST,
    PLAYER_TRANSFER
}
