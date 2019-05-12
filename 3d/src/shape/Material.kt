package shape

import gl.GLShaderProgram
import util.position
import util.vec3
import util.vec4

data class Material(val colour: vec4) {
    constructor(colour: vec3): this(colour.vec4(1.0f))

    fun setUniforms(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform("$uniformName.colour", colour)
    }
}

val default_material = Material(vec4(1f))