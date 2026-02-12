package com.voidworld.core.registry

import com.voidworld.VoidWorldMod
import com.voidworld.entity.CityGuardianEntity
import com.voidworld.entity.DarkZombieEntity
import com.voidworld.entity.PaladinEntity
import com.voidworld.entity.SummonedZombieEntity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.Level

object ModEntities {

    val CITY_GUARDIAN = ModRegistries.ENTITY_TYPES.register("city_guardian") {
        EntityType.Builder.of(
            { type: EntityType<CityGuardianEntity>, level: Level ->
                CityGuardianEntity(type, level)
            },
            MobCategory.CREATURE
        )
            .sized(0.6f, 1.8f)
            .build("${VoidWorldMod.MOD_ID}:city_guardian")
    }

    val DARK_ZOMBIE = ModRegistries.ENTITY_TYPES.register("dark_zombie") {
        EntityType.Builder.of(
            { type: EntityType<DarkZombieEntity>, level: Level ->
                DarkZombieEntity(type, level)
            },
            MobCategory.MONSTER
        )
            .sized(0.6f, 1.8f)
            .build("${VoidWorldMod.MOD_ID}:dark_zombie")
    }

    val PALADIN = ModRegistries.ENTITY_TYPES.register("paladin") {
        EntityType.Builder.of(
            { type: EntityType<PaladinEntity>, level: Level ->
                PaladinEntity(type, level)
            },
            MobCategory.CREATURE
        )
            .sized(0.6f, 1.8f)
            .build("${VoidWorldMod.MOD_ID}:paladin")
    }

    val SUMMONED_ZOMBIE = ModRegistries.ENTITY_TYPES.register("summoned_zombie") {
        EntityType.Builder.of(
            { type: EntityType<SummonedZombieEntity>, level: Level ->
                SummonedZombieEntity(type, level)
            },
            MobCategory.CREATURE
        )
            .sized(0.6f, 1.8f)
            .build("${VoidWorldMod.MOD_ID}:summoned_zombie")
    }
}
