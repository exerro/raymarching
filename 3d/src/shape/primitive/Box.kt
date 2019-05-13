package shape.primitive

import shape.*
import util.appendDefinition
import util.appendFunction
import util.appendReturn
import util.vec3

class Box(size: vec3, material: Material = default_material()) : MaterialShape(material) {
    override fun getDistanceFunction2(): String
            = "sdBox(\$position, \$size)"

    override fun getMaterialFunction2(): String
            = "materialBox(\$position, \$size, \$material)"

    override fun compileDistanceFunctionHeader2(builder: ShaderCompiler): ShaderCompiler = builder
            .appendFunction("float", "sdBox", Pair("vec3", "position"), Pair("vec3", "size")) { block -> block
                    .appendDefinition("vec3", "d", "abs(position) - size")
                    .appendReturn("length(max(d,0.0)) + min(max(d.x,max(d.y,d.z)),0.0)")
            }

    override fun compileMaterialFunctionHeader2(builder: ShaderCompiler): ShaderCompiler = builder
            .appendFunction("MaterialDistance", "materialBox", Pair("vec3", "position"), Pair("vec3", "size"), Pair("Material", "material")) { block -> block
                    .appendReturn("MaterialDistance(material, sdBox(position, size))")
            }

    private val sizeUniform = Vec3ShapeUniformValue(size)

    fun getSize(size: vec3) = sizeUniform.data

    fun setSize(size: vec3) {
        sizeUniform.data = size
    }

    override fun getUniforms(): Map<String, ShapeUniformValue> = mapOf(
            "size" to sizeUniform
    )

}