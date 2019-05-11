package shape

class ShapeDifference(val a: Shape, val b: Shape): ShapeContainer() {
    override fun getDistanceFunctionHeader(): String?
            = null

    override fun getDistanceFunction(): String
            = "max($1, -($2))"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}

fun differenceOfShapes(shape: Shape, vararg shapes: Shape): Shape {
    if (shapes.isEmpty()) return shape

    return shapes.fold(shape) { a, b -> ShapeDifference(a, b) }
}