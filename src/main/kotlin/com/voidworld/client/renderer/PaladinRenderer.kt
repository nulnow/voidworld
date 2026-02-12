package com.voidworld.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.voidworld.VoidWorldMod
import com.voidworld.entity.PaladinEntity
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f

/**
 * Renders Paladin with same texture as Protector.
 * Divine Shield: golden particles orbit around the entity.
 */
class PaladinRenderer(
    context: EntityRendererProvider.Context
) : HumanoidMobRenderer<PaladinEntity, HumanoidModel<PaladinEntity>>(
    context,
    HumanoidModel(context.bakeLayer(ModelLayers.PLAYER)),
    0.5f
) {
    init {
        addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
    }

    override fun getTextureLocation(entity: PaladinEntity): ResourceLocation = TEXTURE

    override fun render(
        entity: PaladinEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)

        // Divine Shield: orbiting golden particles
        val level = entity.level()
        if (level.isClientSide && entity.tickCount % 2 == 0) {
            val time = (entity.tickCount + partialTicks) * 0.15
            val radius = 1.2
            val count = 6
            val gold = DustParticleOptions(Vector3f(1f, 0.84f, 0f), 0.8f)

            for (i in 0 until count) {
                val angle = time + (i * Math.PI * 2 / count)
                val x = entity.x + Math.cos(angle) * radius
                val y = entity.y + 1.0 + Math.sin(time * 0.7) * 0.2
                val z = entity.z + Math.sin(angle) * radius
                level.addParticle(gold, x, y, z, 0.0, 0.02, 0.0)
            }
        }
    }

    companion object {
        private val TEXTURE = ResourceLocation(VoidWorldMod.MOD_ID, "textures/entity/gold-paladin.png")
    }
}
