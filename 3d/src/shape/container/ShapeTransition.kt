package shape.container

import shape.*

class ShapeTransition(private val a: Shape, private val b: Shape, transition: Float): ShapeContainer() {
    override fun getDistanceFunction(): String {
        TODO("not implemented")
    }

    override fun getMaterialFunction(): String {
        TODO("not implemented")
    }

    override fun compileDistanceFunctionHeader(builder: ShaderCompiler): ShaderCompiler {
        TODO("not implemented")
    }

    override fun compileMaterialFunctionHeader(builder: ShaderCompiler): ShaderCompiler {
        TODO("not implemented")
    }

    private val transitionUniform = FloatShapeUniformValue(transition)

    fun getTransition(): Float = transitionUniform.getValue()
    fun setTransition(value: Float) {
        transitionUniform.setValue(value)
    }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf("transition" to transitionUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
