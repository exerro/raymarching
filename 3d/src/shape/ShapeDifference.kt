package shape

class ShapeDifference(private val a: Shape, private val b: Shape): ShapeContainer() {
    override fun getDistanceFunctionHeader(): String?
            = null

    override fun getDistanceFunction(): String
            = "max($1, -($2))"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
