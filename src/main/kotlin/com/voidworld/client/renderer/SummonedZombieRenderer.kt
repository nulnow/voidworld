package com.voidworld.client.renderer

import com.voidworld.VoidWorldMod
import com.voidworld.entity.SummonedZombieEntity
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.resources.ResourceLocation

/**
 * Renders SummonedZombie with summoned-zombie skin.
 */
class SummonedZombieRenderer(
    context: EntityRendererProvider.Context
) : HumanoidMobRenderer<SummonedZombieEntity, HumanoidModel<SummonedZombieEntity>>(
    context,
    HumanoidModel(context.bakeLayer(ModelLayers.PLAYER)),
    0.5f
) {
    override fun getTextureLocation(entity: SummonedZombieEntity): ResourceLocation = TEXTURE

    companion object {
        private val TEXTURE = ResourceLocation(VoidWorldMod.MOD_ID, "textures/entity/summoned_zombie.png")
    }
}
