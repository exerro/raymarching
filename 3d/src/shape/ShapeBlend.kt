package shape

class ShapeBlend(factor: Float, private vararg val children: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.data

    fun setFactor(factor: Float) {
        factorUniform.data = factor
    }

    override fun getHeader(): String?
            = "DistanceData smoothMin(DistanceData a, DistanceData b, float k) {\n" +
                "\tfloat h = max(k - abs(a.distance - b.distance), 0) / k;\n" +
                "\treturn a.distance < b.distance ?\n" +
                "\t    DistanceData(a.material, a.distance - h*h*h*k/6.0) :" +
                "\t    DistanceData(b.material, b.distance - h*h*h*k/6.0);" +
            "}"

    override fun getFunction(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "smoothMin($acc, \$$i, \$factor)" }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = children.toList()
}
