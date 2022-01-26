package shape.container

import raymarch.RaymarchShaderCompiler
import shape.*
import util.appendFunction
import util.appendReturn

class ShapeUnion(private vararg val children: Shape): ShapeContainer() {
    override fun getDistanceFunction(): String
            = applyToAllChildren("min(\$a, \$b)")

    override fun getMaterialFunction(): String
            = applyToAllChildren("materialUnion(\$a, \$b)")

    override fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = builder
            .appendFunction("MaterialDistance", "materialUnion", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b")) { block -> block
                    .appendReturn("a.dist < b.dist ? a : b")
            }

    override fun getUniforms(): Map<String, ShaderData<*>>
            = mapOf()

    override fun getChildren(): List<Shape>
            = children.toList()
}
