package v2

import lwaf_core.length
import lwaf_core.minus

val sphere = ObjectShape(
        mapOf("radius" to FloatShapePropertyTemplate(1f)),
        "%distance = length(%position) - %radius"
) { position, properties ->
    position.length() - properties.get<FloatShapeProperty>("radius").value
}

val union = CompoundShape(
        mapOf(),
        "%blend = %first < %second ? 0 : 1"
) { d0, d1, _ -> if (d0 < d1) 0f else 1f }

val translate = SpaceTransformShape(
        mapOf("translation" to Vec3ShapePropertyTemplate()),
        "%out = %in - %translation"
) { position, properties ->
    position - properties.get<Vec3ShapeProperty>("translation").value
}
