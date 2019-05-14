package shape.primitive

import raymarch.RaymarchShaderCompiler
import shape.*
import util.appendDefinition
import util.appendFunction
import util.appendReturn
import util.vec3

class Box(size: vec3, material: Material = default_material()) : MaterialShape(material) {
    override fun getDistanceFunction(): String
            = "sdBox(\$position, \$size)"

    override fun compileDistanceFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler = builder
            .appendFunction("float", "sdBox", Pair("vec3", "position"), Pair("vec3", "size")) { block -> block
                    .appendDefinition("vec3", "d", "abs(position) - size")
                    .appendReturn("length(max(d,0.0)) + min(max(d.x,max(d.y,d.z)),0.0)")
            }

    private val sizeUniform = Vec3ShapeUniformValue(size)

    fun getSize(size: vec3) = sizeUniform.getValue()
    fun setSize(size: vec3) {
        sizeUniform.setValue(size)
    }

    override fun getUniforms(): Map<String, ShaderData<*>> = mapOf(
            "size" to sizeUniform
    )

}