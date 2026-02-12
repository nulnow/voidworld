package com.voidworld.entity

import com.voidworld.VoidWorldMod
import com.voidworld.core.registry.ModRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LightBlock
import net.minecraft.world.phys.AABB

/**
 * Paladin — NPC with same AI as Protector.
 * Divine Shield effect: orbiting golden shields, +8 armor, +2 toughness.
 * Gold sword, gold armor. Same appearance as Protector for now.
 */
class PaladinEntity(
    entityType: EntityType<out PaladinEntity>,
    level: Level
) : PathfinderMob(entityType, level) {

    private var homeBounds: AABB? = null
    private var wanderTargetX: Int = 0
    private var wanderTargetZ: Int = 0
    private var lastLightPos: BlockPos? = null

    companion object {
        val ID = ResourceLocation(VoidWorldMod.MOD_ID, "paladin")

        fun createAttributes(): AttributeSupplier.Builder =
            Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.525)   // 1.5x base (0.35 * 1.5)
                .add(Attributes.ATTACK_DAMAGE, 10.0)    // 2x damage
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ARMOR, 11.0)            // gold armor + Divine Shield adds 8
                .add(Attributes.ARMOR_TOUGHNESS, 0.0)   // Divine Shield adds 2
    }

    init {
        listOf(
            EquipmentSlot.HEAD to ItemStack(Items.GOLDEN_HELMET),
            EquipmentSlot.CHEST to ItemStack(Items.GOLDEN_CHESTPLATE),
            EquipmentSlot.LEGS to ItemStack(Items.GOLDEN_LEGGINGS),
            EquipmentSlot.FEET to ItemStack(Items.GOLDEN_BOOTS),
            EquipmentSlot.MAINHAND to createPaladinSword(),
            EquipmentSlot.OFFHAND to createPaladinShield()
        ).forEach { (slot, stack) ->
            setItemSlot(slot, stack)
            setDropChance(slot, 1.0f)
        }
        addEffect(MobEffectInstance(ModRegistries.DIVINE_SHIELD.get(), Int.MAX_VALUE, 0, false, true, true))
        addEffect(MobEffectInstance(MobEffects.REGENERATION, Int.MAX_VALUE, 0, false, false, false))  // gradual heal aura
        setCustomName(Component.translatable("entity.${VoidWorldMod.MOD_ID}.paladin"))
        setCustomNameVisible(true)
    }

    private fun createPaladinSword(): ItemStack {
        val sword = ItemStack(Items.GOLDEN_SWORD)
        EnchantmentHelper.setEnchantments(mapOf(Enchantments.FIRE_ASPECT to 1), sword)
        return sword
    }

    private fun createPaladinShield(): ItemStack {
        val shield = ItemStack(Items.SHIELD)
        val blockEntityTag = net.minecraft.nbt.CompoundTag()
        blockEntityTag.putInt("Base", 4)  // yellow base (golden)
        shield.getOrCreateTag().put("BlockEntityTag", blockEntityTag)
        return shield
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(2, PaladinMeleeAttackGoal(this, 1.0, true))
        goalSelector.addGoal(3, PaladinWanderGoal(this, 0.5))
        goalSelector.addGoal(4, LookAtPlayerGoal(this, Player::class.java, 8f))

        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Monster::class.java, 10, true, false, null))
        targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Slime::class.java, 10, true, false, null))
        targetSelector.addGoal(0, NearestAttackableTargetGoal(this, LivingEntity::class.java, 10, true, false) { target ->
            target.hasEffect(ModRegistries.WANTED_FOR_DESTRUCTION.get())
        })
    }

    fun setHomeBounds(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) {
        homeBounds = AABB(
            minX.toDouble(), minY.toDouble(), minZ.toDouble(),
            maxX.toDouble(), maxY.toDouble(), maxZ.toDouble()
        )
    }

    fun pickNewWanderTarget() {
        val bounds = homeBounds ?: return
        val r = random
        wanderTargetX = (bounds.minX + r.nextDouble() * (bounds.maxX - bounds.minX)).toInt()
        wanderTargetZ = (bounds.minZ + r.nextDouble() * (bounds.maxZ - bounds.minZ)).toInt()
    }

    fun getWanderTargetX(): Int = wanderTargetX
    fun getWanderTargetZ(): Int = wanderTargetZ
    fun hasHomeBounds(): Boolean = homeBounds != null

    override fun tick() {
        super.tick()
        // Emit torch-like light (level 14) around the paladin
        if (!level().isClientSide) {
            val pos = blockPosition().above()
            if (pos != lastLightPos) {
                lastLightPos?.let { old ->
                    if (level().getBlockState(old).`is`(Blocks.LIGHT)) {
                        level().setBlock(old, Blocks.AIR.defaultBlockState(), 3)
                    }
                }
                if (level().getBlockState(pos).isAir || level().getBlockState(pos).`is`(Blocks.LIGHT)) {
                    level().setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 14), 3)
                }
                lastLightPos = pos
            }
        }
    }

    override fun remove(removalReason: net.minecraft.world.entity.Entity.RemovalReason) {
        super.remove(removalReason)
        if (!level().isClientSide) {
            lastLightPos?.let { pos ->
                if (level().getBlockState(pos).`is`(Blocks.LIGHT)) {
                    level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
                }
            }
        }
    }

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
