package shape.container

import shape.FloatShapeUniformValue
import shape.Shape
import shape.ShapeContainer
import shape.ShapeUniformValue

class ShapeTransition(private val a: Shape, private val b: Shape, transition: Float): ShapeContainer() {
    private val transitionUniform = FloatShapeUniformValue(transition)

    fun getTransition(): Float = transitionUniform.data
    fun setTransition(value: Float) {
        transitionUniform.data = value
    }

    override fun getHeader(): String?
            = "DistanceData opTransitionData(DistanceData a, DistanceData b, float t) {\n" +
                "\tvec3 col = a.material.colour.xyz * t + b.material.colour.xyz * (1 - t);\n" +
                "\treturn DistanceData(Material(vec4(col, 1)), a.distance * t + b.distance * (1 - t));\n" +
            "}\n\n"

    override fun getFunction(): String
            = "opTransitionData($1, $2, \$transition)"

    override fun getDistanceFunction(): String
            = "(\$1) * \$transition + (\$2) * (1 - \$transition)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("transition" to transitionUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
