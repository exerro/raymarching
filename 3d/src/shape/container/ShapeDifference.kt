package shape.container

import shape.Shape
import shape.ShapeContainer
import shape.ShapeUniformValue

class ShapeDifference(private val a: Shape, private val b: Shape): ShapeContainer() {
    override fun getHeader(): String?
            = "DistanceData sdDifference(DistanceData a, DistanceData b) {\n" +
            "\treturn a.distance > -b.distance ? a : DistanceData(b.material, -b.distance);\n" +
            "}"

    override fun getFunction(): String
            = "sdDifference($1, $2)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
