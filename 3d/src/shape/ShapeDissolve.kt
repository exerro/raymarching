package shape

class ShapeDissolve(factor: Float, private val a: Shape, private val b: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.data

    fun setFactor(factor: Float) {
        factorUniform.data = factor
    }

    override fun getHeader(): String?
            = "DistanceData smoothMax(DistanceData a, DistanceData b, float k) {\n" +
                "\tfloat h = max(k - abs(a.distance + b.distance), 0) / k;\n" +
                "\treturn a.distance > -b.distance ?\n" +
                "\t    DistanceData(a.material, a.distance + h*h*h*k/6.0) :" +
                "\t    DistanceData(b.material, -b.distance + h*h*h*k/6.0);" +
            "}"

    override fun getFunction(): String
            = "smoothMax(\$1, \$2, \$factor)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
