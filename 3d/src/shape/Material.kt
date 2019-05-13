package shape

import gl.GLShaderProgram
import util.vec3
import util.vec4

data class Material(var colour: vec4) {
    constructor(colour: vec3): this(colour.vec4(1.0f))

    fun setUniforms(shader: GLShaderProgram, uniformName: String) {
        shader.setUniform("$uniformName.colour", colour.vec3())
    }
}

fun default_material() = Material(vec4(1f))