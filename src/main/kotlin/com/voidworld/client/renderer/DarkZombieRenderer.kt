package com.voidworld.client.renderer

import com.voidworld.VoidWorldMod
import com.voidworld.entity.DarkZombieEntity
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.resources.ResourceLocation

/**
 * Renders DarkZombie with dark_zombie skin (humanoid model).
 */
class DarkZombieRenderer(
    context: EntityRendererProvider.Context
) : HumanoidMobRenderer<DarkZombieEntity, HumanoidModel<DarkZombieEntity>>(
    context,
    HumanoidModel(context.bakeLayer(ModelLayers.PLAYER)),
    0.5f
) {
    override fun getTextureLocation(entity: DarkZombieEntity): ResourceLocation = TEXTURE

    companion object {
        private val TEXTURE = ResourceLocation(VoidWorldMod.MOD_ID, "textures/entity/dark_zombie.png")
    }
}
