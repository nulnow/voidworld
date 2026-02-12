package com.voidworld.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.voidworld.VoidWorldMod
import com.voidworld.entity.CityGuardianEntity
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation

/**
 * Renders CityGuardian with paladin skin, iron sword and shield.
 * Texture: assets/voidworld/textures/entity/heavy_knight.png
 */
class CityGuardianRenderer(
    context: EntityRendererProvider.Context
) : HumanoidMobRenderer<CityGuardianEntity, HumanoidModel<CityGuardianEntity>>(
    context,
    HumanoidModel(context.bakeLayer(ModelLayers.PLAYER)),
    0.5f
) {
    init {
        addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
    }


    override fun getTextureLocation(entity: CityGuardianEntity): ResourceLocation =
        TEXTURE

    override fun render(
        entity: CityGuardianEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    companion object {
        private val TEXTURE = ResourceLocation(VoidWorldMod.MOD_ID, "textures/entity/heavy-knight.png")
    }
}
