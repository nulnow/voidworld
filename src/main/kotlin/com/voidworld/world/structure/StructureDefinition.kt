package com.voidworld.world.structure

import com.google.gson.annotations.SerializedName

/**
 * JSON definition of a placeable structure.
 *
 * Format:
 * ```json
 * {
 *   "name": "House City Standard",
 *   "blocks": [
 *     { "type": "minecraft:oak_planks", "relativePosition": [0, 0, 0] }
 *   ],
 *   "blockImage": "voidworld:block/structure_house_icon"
 * }
 * ```
 */
data class StructureDefinition(
    val name: String,
    val blocks: List<StructureBlockEntry>,
    @SerializedName("blockImage") val blockImage: String? = null
)

data class StructureBlockEntry(
    val type: String,
    @SerializedName("relativePosition") val relativePosition: List<Int>,
    val properties: Map<String, String>? = null
)
