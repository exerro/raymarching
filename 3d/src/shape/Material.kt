package shape

import gl.GLShaderProgram
import util.vec3

class Material(colour: vec3) {
    val colour = MaterialColour(colour)
}

class MaterialColour(colour: vec3): ShaderData<vec3>(colour) {
    override fun getGLSLValue(): String
            = "Material(vec3(${getValue().x}, ${getValue().y}, ${getValue().z}))"

    override fun getGLSLType(): String
            = "vec3"

    override fun setUniform(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform(uniformName, getValue())
    }
}

fun default_material() = Material(vec3(1f))
