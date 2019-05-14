package shape.container

import raymarch.RaymarchShaderCompiler
import shape.ShaderData
import shape.Shape
import shape.ShapeContainer
import util.appendFunction
import util.appendReturn

class ShapeIntersection(private vararg val children: Shape): ShapeContainer() {
    override fun getDistanceFunction(): String
            = applyToAllChildren("max(\$a, \$b)")

    override fun getMaterialFunction(): String
            = applyToAllChildren("materialIntersection(\$a, \$b)")

    override fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("MaterialDistance", "materialIntersection", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b")) { block -> block
                    .appendReturn("a.dist > b.dist ? a : b")
            }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf()

    override fun getChildren(): List<Shape>
            = children.toList()
}
