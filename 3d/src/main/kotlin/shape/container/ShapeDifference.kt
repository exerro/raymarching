package shape.container

import raymarch.RaymarchShaderCompiler
import shape.ShaderData
import shape.Shape
import shape.ShapeContainer
import util.appendFunction
import util.appendReturn

class ShapeDifference(private val a: Shape, private val b: Shape): ShapeContainer() {
    override fun getDistanceFunction(): String
            = applyToAllChildren("max(\$a, -(\$b))")

    override fun getMaterialFunction(): String
            = applyToAllChildren("materialDifference(\$a, \$b)")

    override fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("MaterialDistance", "materialDifference", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b")) { block -> block
                    .appendReturn("a.dist > -b.dist ? a : MaterialDistance(b.material, -b.dist)")
            }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf()

    override fun getChildren(): List<Shape>
            = listOf(a, b)
}
