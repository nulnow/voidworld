package com.voidworld.system.donation

import com.voidworld.VoidWorldMod

/**
 * Donation integration for supporting further VoidWorld universe projects.
 *
 * This system provides:
 * - In-game link to donation page
 * - Optional cosmetic rewards for donors (non-pay-to-win)
 * - Progress tracker showing funding goals for next projects
 *
 * IMPORTANT: This is purely optional and does not affect gameplay.
 * All game content is accessible without donations.
 */
object DonationIntegration {

    /** URL for the donation / support page. */
    var donationUrl: String = "https://voidworld.example.com/support"
        private set

    /** Whether the donation button is shown in the main menu. */
    var isEnabled: Boolean = true
        private set

    /**
     * Configure donation integration from mod config.
     */
    fun configure(url: String, enabled: Boolean) {
        donationUrl = url
        isEnabled = enabled
        VoidWorldMod.LOGGER.info("Donation integration configured: enabled=$enabled")
    }

    /**
     * Get the list of cosmetic reward tiers (non-gameplay-affecting).
     */
    fun getRewardTiers(): List<DonationTier> = listOf(
        DonationTier("supporter", "donation.voidworld.tier.supporter", 5),
        DonationTier("patron", "donation.voidworld.tier.patron", 15),
        DonationTier("champion", "donation.voidworld.tier.champion", 30),
        DonationTier("void_walker", "donation.voidworld.tier.void_walker", 50)
    )
}

/**
 * A donation tier with cosmetic rewards.
 */
data class DonationTier(
    val id: String,
    val nameKey: String,
    val minimumAmount: Int  // in USD or equivalent
)
