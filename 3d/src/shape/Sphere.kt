package shape

class Sphere(radius: Float, material: Material = default_material) : MaterialShape(material) {
    private val radiusUniform: FloatShapeUniformValue = FloatShapeUniformValue(radius)

    fun getRadius(): Float = radiusUniform.data

    fun setRadius(radius: Float) {
        radiusUniform.data = radius
    }

    override fun getHeader(): String?
            = null

    override fun getDistanceFunction(): String
            = "length(\$ray_position) - \$radius"

    override fun getUniforms(): Map<String, ShapeUniformValue> = mapOf(
            "radius" to radiusUniform
    )

}