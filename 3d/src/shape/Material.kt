package shape

import gl.GLShaderProgram
import util.vec4

data class Material(val colour: vec4) {
    fun setUniforms(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform("$uniformName.colour", colour)
    }
}

val default_material = Material(vec4(1f))