package com.voidworld.entity

import com.voidworld.VoidWorldMod
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
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
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * Summoned Zombie — friendly pet entity.
 * Keeps distance from owner and other summoned zombies. Attacks hostile mobs like guards.
 */
class SummonedZombieEntity(
    entityType: EntityType<out SummonedZombieEntity>,
    level: Level
) : PathfinderMob(entityType, level) {

    var ownerUuid: UUID? = null
        private set

    val owner: Player?
        get() {
            val uuid = ownerUuid ?: return null
            return level().getPlayerByUUID(uuid)
        }

    companion object {
        val ID = ResourceLocation(VoidWorldMod.MOD_ID, "summoned_zombie")

        fun createAttributes(): AttributeSupplier.Builder =
            createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 2.0)
    }

    fun setOwner(player: Player) {
        ownerUuid = player.uuid
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, false))
        goalSelector.addGoal(3, net.minecraft.world.entity.ai.goal.AvoidEntityGoal(this, Player::class.java, 5f, 0.9, 1.2) { it == owner })
        goalSelector.addGoal(3, net.minecraft.world.entity.ai.goal.AvoidEntityGoal(this, SummonedZombieEntity::class.java, 4f, 0.9, 1.0))
        goalSelector.addGoal(4, SummonedZombieFollowOwnerGoal(this, 1.0, 12f, 20f))
        goalSelector.addGoal(5, LookAtPlayerGoal(this, Player::class.java, 8f))

        // Only Monster + Slime (unlike guards, do NOT attack WantedForDestruction)
        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false, null))
        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Slime::class.java, 10, true, false, null))
    }

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        ownerUuid?.let { tag.putUUID("Owner", it) }
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        if (tag.hasUUID("Owner")) ownerUuid = tag.getUUID("Owner")
    }
}
