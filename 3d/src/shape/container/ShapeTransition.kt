package shape.container

import shape.*

class ShapeTransition(private val a: Shape, private val b: Shape, transition: Float): ShapeContainer() {
    override fun getDistanceFunction2(): String {
        TODO("not implemented")
    }

    override fun getMaterialFunction2(): String {
        TODO("not implemented")
    }

    override fun compileDistanceFunctionHeader2(builder: ShaderCompiler): ShaderCompiler {
        TODO("not implemented")
    }

    override fun compileMaterialFunctionHeader2(builder: ShaderCompiler): ShaderCompiler {
        TODO("not implemented")
    }

    private val transitionUniform = FloatShapeUniformValue(transition)

    fun getTransition(): Float = transitionUniform.data
    fun setTransition(value: Float) {
        transitionUniform.data = value
    }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf("transition" to transitionUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
