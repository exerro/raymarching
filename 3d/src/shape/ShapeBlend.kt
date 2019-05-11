package shape

class ShapeBlend(val a: Shape, val b: Shape, factor: Float): ShapeContainer() {
    val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.data

    fun setFactor(factor: Float) {
        factorUniform.data = factor
    }

    override fun getDistanceFunctionHeader(): String?
            = "float smoothMin(float a, float b, float k) {\n" +
                "\tfloat h = max(k - abs(a - b), 0) / k;\n" +
                "\treturn min(a, b) - h*h*h*k/6.0;" +
            "}"

    override fun getDistanceFunction(): String
            = "smoothMin(\$1, \$2, \$factor)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}

fun blendOfShapes(factor: Float, shape: Shape, vararg shapes: Shape): Shape {
    if (shapes.isEmpty()) return shape
    return shapes.fold(shape) { a, b -> ShapeBlend(a, b, factor) }
}
