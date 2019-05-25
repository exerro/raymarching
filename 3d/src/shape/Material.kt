package shape

import lwaf_core.GLShaderProgram
import lwaf_core.vec3

class Material(colour: vec3, reflectivity: Float = 0.02f) {
    val colour = MaterialColour(colour)
    val reflectivity = MaterialReflectivity(reflectivity)

    fun isDynamic(): Boolean = colour.isDynamic() || reflectivity.isDynamic()

    fun getGLSLValue(): String = "Material(${colour.getGLSLValue()}, ${reflectivity.getGLSLValue()})"
}

class MaterialColour(colour: vec3): ShaderData<vec3>(colour) {
    override fun getGLSLValue(): String
            = "vec3(${getValue().x}, ${getValue().y}, ${getValue().z})"

    override fun getGLSLType(): String
            = "vec3"

    override fun setUniform(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform(uniformName, getValue())
    }
}

class MaterialReflectivity(reflectivity: Float): ShaderData<Float>(reflectivity) {
    override fun getGLSLValue(): String
            = "${getValue()}"

    override fun getGLSLType(): String
            = "float"

    override fun setUniform(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform(uniformName, getValue())
    }
}

fun default_material() = Material(vec3(1f))
