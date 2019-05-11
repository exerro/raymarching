package shape

class ShapeUnion(val a: Shape, val b: Shape): ShapeContainer {
    override fun getDistanceFunctionHeader(): String?
            = null

    override fun getDistanceFunction(): String
            = "min($1, $2)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
