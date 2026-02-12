package com.voidworld.entity

import com.voidworld.VoidWorldMod
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
 * Dark Zombie — hostile NPC with dark_zombie skin appearance.
 * Attacks players, Paladins, and City Guardians on sight.
 */
class DarkZombieEntity(
    entityType: EntityType<out DarkZombieEntity>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        val ID = ResourceLocation(VoidWorldMod.MOD_ID, "dark_zombie")

        fun createAttributes(): AttributeSupplier.Builder =
            Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0)
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, false))
        goalSelector.addGoal(3, WaterAvoidingRandomStrollGoal(this, 0.8))
        goalSelector.addGoal(4, LookAtPlayerGoal(this, Player::class.java, 8f))
        goalSelector.addGoal(5, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
        targetSelector.addGoal(3, NearestAttackableTargetGoal(this, CityGuardianEntity::class.java, 10, true, false, null))
        targetSelector.addGoal(3, NearestAttackableTargetGoal(this, PaladinEntity::class.java, 10, true, false, null))
    }
}
