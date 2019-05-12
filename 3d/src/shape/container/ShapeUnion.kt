package shape.container

import shape.Shape
import shape.ShapeContainer
import shape.ShapeUniformValue

class ShapeUnion(private vararg val children: Shape): ShapeContainer() {
    override fun getHeader(): String?
            = "DistanceData opUnionData(DistanceData a, DistanceData b) {\n" +
                "\treturn a.distance < b.distance ? a : b;\n" +
            "}"

    override fun getFunction(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "opUnionData($acc, \$$i)" }

    override fun getDistanceFunction(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "min($acc, \$$i)" }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = children.toList()
}
