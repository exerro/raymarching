package shape

import gl.GLShaderProgram
import util.vec3

data class Material(var colour: vec3): ShaderData() {
    override fun getGLSLType(): String
            = "Material"

    override fun getGLSLValue(): String
            = "Material(vec3(${colour.x}, ${colour.y}, ${colour.z}))"

    override fun setUniform(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform("$uniformName.colour", colour)
    }
}

fun default_material() = Material(vec3(1f))
