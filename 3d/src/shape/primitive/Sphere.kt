package shape.primitive

import shape.*

class Sphere(radius: Float, material: Material = default_material()) : MaterialShape(material) {
    override fun getDistanceFunction2(): String
            = "(length(\$position) - \$radius)"

    override fun getMaterialFunction2(): String
            = "MaterialDistance(\$material, (length(\$position) - \$radius))"

    private val radiusUniform = FloatShapeUniformValue(radius)

    fun getRadius(): Float = radiusUniform.data

    fun setRadius(radius: Float) {
        radiusUniform.data = radius
    }

    override fun getUniforms(): Map<String, ShapeUniformValue> = mapOf(
            "radius" to radiusUniform
    )

}