package shape.container

import shape.FloatShapeUniformValue
import shape.Shape
import shape.ShapeContainer
//
//class ShapeDissolve(factor: Float, private val a: Shape, private val b: Shape): ShapeContainer() {
//    private val factorUniform = FloatShapeUniformValue(factor)
//
//    fun getFactor(): Float = factorUniform.data
//
//    fun setFactor(factor: Float) {
//        factorUniform.data = factor
//    }
//
//    override fun getHeader(): String?
//            = "DistanceData opDissolveData(DistanceData a, DistanceData b, float k) {\n" +
//                "\tfloat h = clamp( 0.5 - 0.5*(a.distance+b.distance)/k, 0, 1 );\n" +
//                "\tvec3 c1 = a.material.colour.xyz;\n" +
//                "\tvec3 c2 = b.material.colour.xyz;\n" +
//                "\tvec3 col = c1 * (1 - h) + c2 * h;\n" +
//                "\treturn DistanceData(Material(vec4(col, 1)), a.distance * (1 - h) - b.distance * h + h*(1-h)*k);\n" +
//            "}\n\n" +
//            "float opDissolve(float a, float b, float k) {\n" +
//            "\tfloat h = max(k - abs(a - b), 0) / k;\n" +
//            "\treturn max(a, b) + h*h*h*k/6.0;\n" +
//            "}"
//
//    override fun getFunction(): String
//            = "opDissolveData(\$1, \$2, \$factor)"
//
//    override fun getDistanceFunction(): String
//            = "opDissolve(\$1, -(\$2), \$factor)"
//
//    override fun getUniforms(): Map<String, ShapeUniformValue>
//            = mapOf("factor" to factorUniform)
//
//    override fun getChildren(): List<Shape>
//            = listOf(a, b)
//}
