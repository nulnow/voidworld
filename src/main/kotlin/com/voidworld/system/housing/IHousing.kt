package com.voidworld.system.housing

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Player housing system for VoidWorld.
 *
 * Players can own a home in one of several location types:
 * - City plot (close to trade/quests, costs currency, small area)
 * - Free plot (outside city, larger area, no cost)
 * - Wilderness (anywhere outside cities, no protection)
 * - Floating islands (special campaign unlock)
 * - Venetian city (waterfront property)
 *
 * City plots provide protection from other players and law system bonuses.
 * Wilderness homes have no protection — blocks can be freely broken.
 */
interface IHousing {

    /** Get all plots owned by a player. */
    fun getPlayerPlots(player: ServerPlayer): List<Plot>

    /** Claim a plot for a player. */
    fun claimPlot(player: ServerPlayer, plotId: ResourceLocation): ClaimResult

    /** Abandon a plot (returns it to available pool). */
    fun abandonPlot(player: ServerPlayer, plotId: ResourceLocation): Boolean

    /** Check if a position is within any player's plot. */
    fun getPlotAt(pos: BlockPos): Plot?

    /** Check if a player can build at a position. */
    fun canBuild(player: ServerPlayer, pos: BlockPos): Boolean

    /** Get all available (unclaimed) plots in a city. */
    fun getAvailablePlots(cityId: ResourceLocation): List<Plot>
}

/**
 * Represents a buildable plot of land.
 */
data class Plot(
    val id: ResourceLocation,
    val cityId: ResourceLocation?,  // null = wilderness
    val type: PlotType,
    val minPos: BlockPos,
    val maxPos: BlockPos,
    val owner: UUID? = null,
    val purchasePrice: Int = 0,
    val rentPerDay: Int = 0
) {
    val isClaimed: Boolean get() = owner != null

    fun contains(pos: BlockPos): Boolean =
        pos.x in minPos.x..maxPos.x &&
        pos.y in minPos.y..maxPos.y &&
        pos.z in minPos.z..maxPos.z
}

enum class PlotType {
    CITY_SMALL,
    CITY_MEDIUM,
    CITY_LARGE,
    FREE_PLOT,
    FLOATING_ISLAND,
    WATERFRONT,
    WILDERNESS
}

enum class ClaimResult {
    SUCCESS,
    ALREADY_CLAIMED,
    INSUFFICIENT_FUNDS,
    MAX_PLOTS_REACHED,
    NOT_AVAILABLE,
    PREREQUISITES_NOT_MET
}
