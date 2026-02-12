package com.voidworld.core.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

/**
 * Curse effect: «Преступник для уничтожения».
 * City Guardians attack any entity with this effect.
 * Removed on death. Duration: 10 minutes.
 */
class WantedForDestructionEffect : MobEffect(MobEffectCategory.HARMFUL, 0x8B0000) {

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int) {
        // No per-tick logic — just a marker for guardian targeting
    }

    override fun isDurationEffectTick(duration: Int, amplifier: Int): Boolean = false
}
