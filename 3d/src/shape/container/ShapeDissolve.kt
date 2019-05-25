package shape.container

import raymarch.RaymarchShaderCompiler
import shape.FloatShapeUniformValue
import shape.ShaderData
import shape.Shape
import shape.ShapeContainer
import util.appendDefinition
import util.appendFunction
import util.appendReturn

class ShapeDissolve(factor: Float, private val a: Shape, private val b: Shape): ShapeContainer() {
    private val factorUniform = FloatShapeUniformValue(factor)

    fun getFactor(): Float = factorUniform.getValue()
    fun getFactorUniform(): FloatShapeUniformValue = factorUniform

    fun setFactor(factor: Float) {
        factorUniform.setValue(factor)
    }

    override fun getDistanceFunction(): String
            = applyToAllChildren("opDissolve(\$a, \$b, \$factor)")

    override fun getMaterialFunction(): String
            = applyToAllChildren("materialDissolve(\$a, \$b, \$factor, \$distance)")

    override fun compileDistanceFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("float", "opDissolve", Pair("float", "a"), Pair("float", "b"), Pair("float", "k")) { block -> block
                    .appendDefinition("float", "h", "clamp( 0.5 - 0.5*(a+b)/k, 0.0, 1.0 )")
                    .appendReturn("mix(a, -b, h ) + k*h*(1.0-h)")
            }

    override fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("MaterialDistance", "materialDissolve", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b"), Pair("float", "k"), Pair("float", "d")) { block -> block
                    .appendDefinition("float", "h", "clamp( 0.5 + 0.5*(a.dist+b.dist)/k, 0, 1 )")
                    .appendDefinition("vec3", "col", "mix(a.material.colour, b.material.colour, 1-h)")
                    .appendDefinition("float", "ref", "mix(a.material.reflectivity, b.material.reflectivity, 1-h)")
                    .appendReturn("MaterialDistance(Material(col, ref), d)")
                    .appendReturn("MaterialDistance(Material(col, ref), mix(a.dist, -b.dist, h) + h*(1-h)*k)") // TODO
            }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf("factor" to factorUniform)

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
