package shape

import gl.GLShaderProgram
import util.vec2
import util.vec3
import util.vec4

class FloatShapeUniformValue(value: Float) : ShaderData<Float>(value) {
    override fun getGLSLValue(): String
            = getValue().toString()

    override fun getGLSLType(): String
            = "float"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, getValue())
}

class Vec2ShapeUniformValue(data: vec2) : ShaderData<vec2>(data) {
    override fun getGLSLValue(): String
            = "vec2(${getValue().x}, ${getValue().y})"

    override fun getGLSLType(): String
            = "vec2"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, getValue())
}

class Vec3ShapeUniformValue(data: vec3) : ShaderData<vec3>(data) {
    override fun getGLSLValue(): String
            = "vec3(${getValue().x}, ${getValue().y}, ${getValue().z})"

    override fun getGLSLType(): String
            = "vec3"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, getValue())
}

class Vec4ShapeUniformValue(data: vec4) : ShaderData<vec4>(data) {
    override fun getGLSLValue(): String
            = "vec4(${getValue().x}, ${getValue().y}, ${getValue().z}, ${getValue().w})"

    override fun getGLSLType(): String
            = "vec4"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, getValue())
}
