package shape.container

import raymarch.RaymarchShaderCompiler
import shape.*
import util.appendFunction
import util.appendReturn

class ShapeTransition(private val a: Shape, private val b: Shape, transition: Float): ShapeContainer() {
    private val transitionUniform = FloatShapeUniformValue(transition)

    fun getTransition(): Float = transitionUniform.getValue()
    fun getTransitionProperty(): FloatShapeUniformValue = transitionUniform

    fun setTransition(value: Float): ShapeTransition {
        transitionUniform.setValue(value)
        return this
    }

    override fun getDistanceFunction(): String
            = "mix(\$1, \$2, \$transition)"

    override fun getMaterialFunction(): String
            = "materialTransition(\$1, \$2, \$transition)"

    override fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler = builder
            .appendFunction("MaterialDistance", "materialTransition", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b"), Pair("float", "transition")) { block -> block
                block.appendReturn("MaterialDistance(Material(mix(a.material.colour, b.material.colour, transition)), mix(a.dist, b.dist, transition))")
            }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf("transition" to transitionUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
