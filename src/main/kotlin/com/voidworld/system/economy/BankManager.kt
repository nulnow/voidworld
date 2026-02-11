package com.voidworld.system.economy

import com.voidworld.VoidWorldMod
import com.voidworld.core.config.ModConfig
import com.voidworld.core.data.PlayerVoidData
import net.minecraft.server.level.ServerPlayer

/**
 * Server-side economy manager implementing [IEconomy].
 *
 * All currency operations go through this manager to ensure consistency
 * and proper event firing / client synchronization.
 */
object BankManager : IEconomy {

    override fun getBalance(player: ServerPlayer): Int {
        return PlayerVoidData.get(player)?.currency ?: 0
    }

    override fun getBankBalance(player: ServerPlayer): Int {
        return PlayerVoidData.get(player)?.bankBalance ?: 0
    }

    override fun addCurrency(player: ServerPlayer, amount: Int): Int {
        val data = PlayerVoidData.get(player) ?: return 0
        data.currency += amount
        // TODO: Sync to client, fire EconomyChangeEvent
        return data.currency
    }

    override fun removeCurrency(player: ServerPlayer, amount: Int): Boolean {
        val data = PlayerVoidData.get(player) ?: return false
        if (data.currency < amount) return false
        data.currency -= amount
        // TODO: Sync to client
        return true
    }

    override fun bankDeposit(player: ServerPlayer, amount: Int): Boolean {
        val data = PlayerVoidData.get(player) ?: return false
        if (data.currency < amount) return false
        data.currency -= amount
        data.bankBalance += amount
        VoidWorldMod.LOGGER.debug("${player.name.string} deposited $amount to bank")
        return true
    }

    override fun bankWithdraw(player: ServerPlayer, amount: Int): Boolean {
        val data = PlayerVoidData.get(player) ?: return false
        if (data.bankBalance < amount) return false
        data.bankBalance -= amount
        data.currency += amount
        VoidWorldMod.LOGGER.debug("${player.name.string} withdrew $amount from bank")
        return true
    }

    override fun applyFine(player: ServerPlayer, amount: Int, reason: String): Boolean {
        val data = PlayerVoidData.get(player) ?: return false
        var remaining = amount

        // Take from wallet first
        val fromWallet = minOf(data.currency, remaining)
        data.currency -= fromWallet
        remaining -= fromWallet

        // Then from bank if needed
        if (remaining > 0) {
            val fromBank = minOf(data.bankBalance, remaining)
            data.bankBalance -= fromBank
            remaining -= fromBank
        }

        VoidWorldMod.LOGGER.info("Fined ${player.name.string} $amount for: $reason (unpaid: $remaining)")
        // TODO: If remaining > 0, player has debt — trigger prison?
        return remaining == 0
    }

    override fun transfer(from: ServerPlayer, to: ServerPlayer, amount: Int): Boolean {
        if (!removeCurrency(from, amount)) return false
        addCurrency(to, amount)
        return true
    }

    /**
     * Apply daily interest to all online players' bank balances.
     * Called from a scheduled tick or day-change event.
     */
    fun applyInterest(player: ServerPlayer) {
        val data = PlayerVoidData.get(player) ?: return
        val rate = ModConfig.BANK_INTEREST_RATE.get()
        val interest = (data.bankBalance * rate).toInt()
        if (interest > 0) {
            data.bankBalance += interest
            VoidWorldMod.LOGGER.debug("Applied $interest interest to ${player.name.string}'s bank")
        }
    }
}
