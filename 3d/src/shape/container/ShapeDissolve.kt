package shape.container

import shape.FloatShapeUniformValue
import shape.Shape
import shape.ShapeContainer
import shape.ShapeUniformValue

class ShapeDissolve(factor: Float, private val a: Shape, private val b: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.data

    fun setFactor(factor: Float) {
        factorUniform.data = factor
    }

    override fun getHeader(): String?
            = "DistanceData opDissolveData(DistanceData a, DistanceData b, float k) {\n" +
                "\tfloat h = max(k - abs(a.distance + b.distance), 0) / k;\n" +
                "\treturn a.distance > -b.distance ?\n" +
                "\t    DistanceData(a.material, a.distance + h*h*h*k/6.0) :\n" +
                "\t    DistanceData(b.material, -b.distance + h*h*h*k/6.0);\n" +
            "}\n\n" +
            "float opDissolve(float a, float b, float k) {\n" +
            "\tfloat h = max(k - abs(a - b), 0) / k;\n" +
            "\treturn max(a, b) + h*h*h*k/6.0;\n" +
            "}"

    override fun getFunction(): String
            = "opDissolveData(\$1, \$2, \$factor)"

    override fun getDistanceFunction(): String
            = "opDissolve(\$1, -(\$2), \$factor)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
