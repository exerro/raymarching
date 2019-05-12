package shape

class ShapeBlend(factor: Float, private vararg val children: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

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
            = (2 .. children.size).fold("\$1") { acc, i -> "smoothMin($acc, \$$i, \$factor)" }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = children.toList()
}
