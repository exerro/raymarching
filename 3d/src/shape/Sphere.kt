package shape

import util.vec4

class Sphere(position: vec4, radius: Float) : Shape() {
    private val positionUniform: Vec4ShapeUniformValue = Vec4ShapeUniformValue(position)
    private val radiusUniform: FloatShapeUniformValue = FloatShapeUniformValue(radius)

    fun getPosition(): vec4 = positionUniform.data
    fun getRadius(): Float = radiusUniform.data

    fun setPosition(position: vec4) {
        positionUniform.data = position
    }

    fun setRadius(radius: Float) {
        radiusUniform.data = radius
    }

    override fun getDistanceFunctionHeader(): String?
            = null

    override fun getDistanceFunction(): String
            = "distance(\$ray_position, \$position) - \$radius"

    override fun getUniforms(): Map<String, ShapeUniformValue> = mapOf(
            "position" to positionUniform,
            "radius" to radiusUniform
    )

}