package shape.container

import shape.Shape
import shape.ShapeContainer
import shape.ShapeUniformValue
//
//class ShapeIntersection(private vararg val children: Shape): ShapeContainer() {
//    override fun getHeader(): String?
//            = "DistanceData opIntersectionData(DistanceData a, DistanceData b) {\n" +
//            "\treturn a.distance > b.distance ? a : b;\n" +
//            "}"
//
//    override fun getFunction(): String
//            = (2 .. children.size).fold("\$1") { acc, i -> "opIntersectionData($acc, \$$i)" }
//
//    override fun getDistanceFunction(): String
//            = (2 .. children.size).fold("\$1") { acc, i -> "max($acc, \$$i)" }
//
//    override fun getUniforms(): Map<String, ShapeUniformValue>
//            = mapOf()
//
//    override fun getChildren(): List<Shape>
//            = children.toList()
//}
