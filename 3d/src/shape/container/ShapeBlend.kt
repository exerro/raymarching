package shape.container

import shape.FloatShapeUniformValue
import shape.Shape
import shape.ShapeContainer
//
//class ShapeBlend(factor: Float, private vararg val children: Shape): ShapeContainer() {
//    private val factorUniform = FloatShapeUniformValue(factor)
//
//    fun getFactor(): Float = factorUniform.data
//
//    fun setFactor(factor: Float) {
//        factorUniform.data = factor
//    }
//
//    override fun getHeader(): String?
//            = "DistanceData opBlendData(DistanceData a, DistanceData b, float k) {\n" +
//                "\tfloat h = clamp( 0.5 + 0.5*(a.distance-b.distance)/k, 0, 1 );\n" +
//                "\tvec3 c1 = a.material.colour.xyz;\n" +
//                "\tvec3 c2 = b.material.colour.xyz;\n" +
//                "\tvec3 col = c1 * (1 - h) + c2 * h;\n" +
//                "\treturn DistanceData(Material(vec4(col, 1)), a.distance * (1 - h) + b.distance * h - h*(1-h)*k);\n" +
//            "}\n\n" +
//            "float opBlend(float a, float b, float k) {\n" +
//            "\tfloat h = max(k - abs(a - b), 0) / k;\n" +
//            "\treturn min(a, b) - h*h*h*k/6.0;\n" +
//            "}"
//
//    override fun getFunction(): String
//            = (2 .. children.size).fold("\$1") { acc, i -> "opBlendData($acc, \$$i, \$factor)" }
//
//    override fun getDistanceFunction(): String
//            = (2 .. children.size).fold("\$1") { acc, i -> "opBlend($acc, \$$i, \$factor)" }
//
//    override fun getUniforms(): Map<String, ShapeUniformValue>
//            = mapOf("factor" to factorUniform)
//
//    override fun getChildren(): List<Shape>
//            = children.toList()
//}
