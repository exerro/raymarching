package shape.container

import shape.Shape
import shape.ShapeContainer
//
//class ShapeDifference(private val a: Shape, private val b: Shape): ShapeContainer() {
//    override fun getHeader(): String?
//            = "DistanceData opDifferenceData(DistanceData a, DistanceData b) {\n" +
//            "\treturn a.distance > -b.distance ? a : DistanceData(b.material, -b.distance);\n" +
//            "}\n\n"
//
//    override fun getFunction(): String
//            = "opDifferenceData($1, $2)"
//
//    override fun getDistanceFunction(): String
//            = "max(\$1, -(\$2))"
//
//    override fun getUniforms(): Map<String, ShapeUniformValue>
//            = mapOf()
//
//    override fun getChildren(): List<Shape>
//            = listOf(a, b)
//}
