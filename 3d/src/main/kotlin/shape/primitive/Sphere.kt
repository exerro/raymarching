package shape.primitive

import shape.*

class Sphere(radius: Float, material: Material = default_material()) : MaterialShape(material) {
    override fun getDistanceFunction(): String
            = "(length(\$position) - \$radius)"

    private val radiusUniform = FloatShapeUniformValue(radius)

    fun getRadius(): Float = radiusUniform.getValue()
    fun setRadius(radius: Float) {
        radiusUniform.setValue(radius)
    }

    override fun getUniforms(): Map<String, ShaderData<*>> = mapOf(
            "radius" to radiusUniform
    )

}