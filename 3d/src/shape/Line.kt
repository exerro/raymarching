package shape

import util.vec4

class Line(a: vec4, b: vec4, radius: Float) : Shape() {
    private val positionAUniform: Vec4ShapeUniformValue = Vec4ShapeUniformValue(a)
    private val positionBUniform: Vec4ShapeUniformValue = Vec4ShapeUniformValue(b)
    private val radiusUniform: FloatShapeUniformValue = FloatShapeUniformValue(radius)

    fun getA(): vec4 = positionAUniform.data
    fun getB(): vec4 = positionBUniform.data
    fun getRadius(): Float = radiusUniform.data

    fun setA(position: vec4) {
        positionAUniform.data = position
    }

    fun setB(position: vec4) {
        positionBUniform.data = position
    }

    fun setRadius(radius: Float) {
        radiusUniform.data = radius
    }

    override fun getDistanceFunctionHeader(): String?
            = "float sdLine(vec3 p, vec3 a, vec3 b, float r) {\n" +
                "\tvec3 pa = p - a, ba = b - a;\n" +
                "\tfloat h = clamp( dot(pa,ba)/dot(ba,ba), 0.0, 1.0 );\n" +
                "\treturn length( pa - ba*h ) - r;\n" +
            "}"

    override fun getDistanceFunction(): String {
        return "sdLine(\$ray_position.xyz, \$a.xyz, \$b.xyz, \$radius)"
    }

    override fun getUniforms(): Map<String, ShapeUniformValue> {
        return mapOf(
                "a" to positionAUniform,
                "b" to positionBUniform,
                "radius" to radiusUniform
        )
    }

}