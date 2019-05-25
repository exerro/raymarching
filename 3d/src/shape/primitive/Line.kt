package shape.primitive

import shape.*
//
//class Line(a: vec3, b: vec3, radius: Float, material: Material = default_material()) : MaterialShape(material) {
//    private val positionAUniform = Vec3ShapeUniformValue(a)
//    private val positionBUniform = Vec3ShapeUniformValue(b)
//    private val radiusUniform = FloatShapeUniformValue(radius)
//
//    fun getA(): vec3 = positionAUniform.data
//    fun getB(): vec3 = positionBUniform.data
//    fun getRadius(): Float = radiusUniform.data
//
//    fun setA(position: vec3) {
//        positionAUniform.data = position
//    }
//
//    fun setB(position: vec3) {
//        positionBUniform.data = position
//    }
//
//    fun setRadius(radius: Float) {
//        radiusUniform.data = radius
//    }
//
//    override fun getHeader(): String?
//            = "float sdLine(vec3 p, vec3 a, vec3 b, float r) {\n" +
//                "\tvec3 pa = p - a, ba = b - a;\n" +
//                "\tfloat h = clamp( dot(pa,ba)/dot(ba,ba), 0.0, 1.0 );\n" +
//                "\treturn length( pa - ba*h ) - r;\n" +
//            "}"
//
//    override fun getDistanceFunction(): String {
//        return "sdLine(\$ray_position.xyz, \$a, \$b, \$radius)"
//    }
//
//    override fun getUniforms(): Map<String, ShapeUniformValue> {
//        return mapOf(
//                "a" to positionAUniform,
//                "b" to positionBUniform,
//                "radius" to radiusUniform
//        )
//    }
//
//}
