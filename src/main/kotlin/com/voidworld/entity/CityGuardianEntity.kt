package com.voidworld.entity

import com.voidworld.VoidWorldMod
import com.voidworld.core.registry.ModRegistries
import net.minecraft.network.chat.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

/**
 * City Guardian — NPC that spawns in cities and patrols by randomly walking to points.
 * Armed with iron sword and shield. Attacks hostile mobs; hostile mobs attack back.
 *
 * Spawned in city locations on server start.
 */
class CityGuardianEntity(
    entityType: EntityType<out CityGuardianEntity>,
    level: Level
) : PathfinderMob(entityType, level) {

    /** Home area bounds — guardian stays within this box when wandering. */
    private var homeBounds: AABB? = null

    /** Current wander target. */
    private var wanderTargetX: Int = 0
    private var wanderTargetZ: Int = 0

    companion object {
        val ID = ResourceLocation(VoidWorldMod.MOD_ID, "city_guardian")

        fun createAttributes(): AttributeSupplier.Builder =
            Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ARMOR, 15.0)      // full iron armor equivalent
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
    }

    init {
        listOf(
            EquipmentSlot.HEAD to ItemStack(Items.IRON_HELMET),
            EquipmentSlot.CHEST to ItemStack(Items.IRON_CHESTPLATE),
            EquipmentSlot.LEGS to ItemStack(Items.IRON_LEGGINGS),
            EquipmentSlot.FEET to ItemStack(Items.IRON_BOOTS),
            EquipmentSlot.MAINHAND to ItemStack(Items.IRON_SWORD),
            EquipmentSlot.OFFHAND to createGuardianShield()
        ).forEach { (slot, stack) ->
            setItemSlot(slot, stack)
            setDropChance(slot, 1.0f)
        }
        setCustomName(Component.translatable("entity.${VoidWorldMod.MOD_ID}.city_guardian"))
        setCustomNameVisible(true)
    }

    /** Creates shield with guardian banner pattern (adapted from /give command). */
    private fun createGuardianShield(): ItemStack {
        val shield = ItemStack(Items.SHIELD)
        val blockEntityTag = CompoundTag()
        blockEntityTag.putInt("Base", 0) // white base

        val patterns = ListTag()
        // Pattern format: {Color: int (0-15), Pattern: "id"}
        listOf(
            15 to "ts",   // black top stripe
            15 to "bs",   // black bottom stripe
            3 to "flo",   // light blue flower
            15 to "cbo",  // black curly border
            14 to "mc",   // red mojang/middle circle
            15 to "cr"    // black creeper
        ).forEach { (color, patternId) ->
            val p = CompoundTag()
            p.putInt("Color", color)
            p.putString("Pattern", patternId)
            patterns.add(p)
        }
        blockEntityTag.put("Patterns", patterns)
        shield.getOrCreateTag().put("BlockEntityTag", blockEntityTag)
        return shield
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, true))
        goalSelector.addGoal(3, CityGuardianWanderGoal(this, 0.5))
        goalSelector.addGoal(4, LookAtPlayerGoal(this, Player::class.java, 8f))

        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false, null))
        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Slime::class.java, 10, true, false, null))
        targetSelector.addGoal(0, NearestAttackableTargetGoal(this, LivingEntity::class.java, 10, true, false) { target ->
            target.hasEffect(ModRegistries.WANTED_FOR_DESTRUCTION.get())
        })
    }

    /** Set the patrol bounds (from the location this guardian belongs to). */
    fun setHomeBounds(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) {
        homeBounds = AABB(
            minX.toDouble(), minY.toDouble(), minZ.toDouble(),
            maxX.toDouble(), maxY.toDouble(), maxZ.toDouble()
        )
    }

    /** Pick a new random point within home bounds and set it as wander target. */
    fun pickNewWanderTarget() {
        val bounds = homeBounds ?: return
        val r = random
        wanderTargetX = (bounds.minX + r.nextDouble() * (bounds.maxX - bounds.minX)).toInt()
        wanderTargetZ = (bounds.minZ + r.nextDouble() * (bounds.maxZ - bounds.minZ)).toInt()
    }

    fun getWanderTargetX(): Int = wanderTargetX
    fun getWanderTargetZ(): Int = wanderTargetZ
    fun hasHomeBounds(): Boolean = homeBounds != null

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        homeBounds?.let { box ->
            tag.putDouble("HomeMinX", box.minX)
            tag.putDouble("HomeMinY", box.minY)
            tag.putDouble("HomeMinZ", box.minZ)
            tag.putDouble("HomeMaxX", box.maxX)
            tag.putDouble("HomeMaxY", box.maxY)
            tag.putDouble("HomeMaxZ", box.maxZ)
        }
        tag.putInt("WanderTargetX", wanderTargetX)
        tag.putInt("WanderTargetZ", wanderTargetZ)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        if (tag.contains("HomeMinX")) {
            homeBounds = AABB(
                tag.getDouble("HomeMinX"), tag.getDouble("HomeMinY"), tag.getDouble("HomeMinZ"),
                tag.getDouble("HomeMaxX"), tag.getDouble("HomeMaxY"), tag.getDouble("HomeMaxZ")
            )
        }
        wanderTargetX = tag.getInt("WanderTargetX")
        wanderTargetZ = tag.getInt("WanderTargetZ")
    }

}
