package shape.container

import raymarch.RaymarchShaderCompiler
import shape.*
import util.appendDefinition
import util.appendFunction
import util.appendReturn

class ShapeBlend(factor: Float, private vararg val children: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.getValue()
    fun getFactorUniform(): FloatShapeUniformValue = factorUniform

    fun setFactor(factor: Float) {
        factorUniform.setValue(factor)
    }

    override fun getDistanceFunction(): String
            = applyToAllChildren("opBlend(\$a, \$b, \$factor)")

    override fun getMaterialFunction(): String
            = applyToAllChildren("materialBlend(\$a, \$b, \$factor)")

    override fun compileDistanceFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("float", "opBlend", Pair("float", "a"), Pair("float", "b"), Pair("float", "k")) { block -> block
                    .appendDefinition("float", "h", "clamp( 0.5 + 0.5*(a-b)/k, 0, 1 )")
                    .appendReturn("mix(a, b, h) - h*(1-h)*k")
            }

    override fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("MaterialDistance", "materialBlend", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b"), Pair("float", "k")) { block -> block
                    .appendDefinition("float", "h", "clamp( 0.5 + 0.5*(a.dist-b.dist)/k, 0, 1 )")
                    .appendDefinition("vec3", "col", "mix(a.material.colour, b.material.colour, h)")
                    .appendDefinition("float", "ref", "mix(a.material.reflectivity, b.material.reflectivity, h)")
                    .appendReturn("MaterialDistance(Material(col, ref), mix(a.dist, b.dist, h) - h*(1-h)*k)")
            }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = children.toList()
}
