package com.voidworld.core.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

/**
 * Божественный щит — повышенная защита, золотые щиты вокруг паладина.
 * Visual: orbiting golden shields (rendered in PaladinRenderer).
 */
class DivineShieldEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0xFFD700) {

    init {
        addAttributeModifier(
            Attributes.ARMOR,
            "5F28A31E-7C41-4B89-9B2A-1D2E3F4A5B6C",
            8.0,
            AttributeModifier.Operation.ADDITION
        )
        addAttributeModifier(
            Attributes.ARMOR_TOUGHNESS,
            "6E39B42F-8D52-5C9A-0C3B-2E4F5A6B7C8D",
            2.0,
            AttributeModifier.Operation.ADDITION
        )
    }

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int) {
        // No per-tick logic — attribute modifiers handle protection
    }

    override fun isDurationEffectTick(duration: Int, amplifier: Int): Boolean = false
}
