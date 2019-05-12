package shape

class ShapeDissolve(factor: Float, private val a: Shape, private val b: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.data

    fun setFactor(factor: Float) {
        factorUniform.data = factor
    }

    override fun getDistanceFunctionHeader(): String?
            = "float smoothMax(float a, float b, float k) {\n" +
                "\tfloat h = max(k - abs(a - b), 0) / k;\n" +
                "\treturn max(a, b) + h*h*h*k/6.0;" +
            "}"

    override fun getDistanceFunction(): String
            = "smoothMax(\$1, -(\$2), \$factor)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
