package shape.container

import shape.ShaderCompiler
import shape.Shape
import shape.ShapeContainer
import shape.ShapeUniformValue
import util.appendFunction
import util.appendReturn

class ShapeUnion(private vararg val children: Shape): ShapeContainer() {
    override fun getDistanceFunction2(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "min($acc, \$$i)" }

    override fun getMaterialFunction2(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "materialUnion($acc, \$$i)" }

    override fun compileMaterialFunctionHeader2(builder: ShaderCompiler): ShaderCompiler? = builder
            .appendFunction("MaterialDistance", "materialUnion", Pair("MaterialDistance", "a"), Pair("MaterialDistance", "b")) { block -> block
                    .appendReturn("a.dist < b.dist ? a : b")
            }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = children.toList()
}
