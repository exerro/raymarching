package shape

import gl.GLShaderProgram
import util.vec2
import util.vec3
import util.vec4

class FloatShapeUniformValue(var data: Float) : ShaderData() {
    override fun getGLSLValue(): String
            = data.toString()

    override fun getGLSLType(): String
            = "float"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

class Vec2ShapeUniformValue(var data: vec2) : ShaderData() {
    override fun getGLSLValue(): String
            = "vec2(${data.x}, ${data.y})"

    override fun getGLSLType(): String
            = "vec2"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

class Vec3ShapeUniformValue(var data: vec3) : ShaderData() {
    override fun getGLSLValue(): String
            = "vec3(${data.x}, ${data.y}, ${data.z})"

    override fun getGLSLType(): String
            = "vec3"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

class Vec4ShapeUniformValue(var data: vec4) : ShaderData() {
    override fun getGLSLValue(): String
            = "vec4(${data.x}, ${data.y}, ${data.z}, ${data.w})"

    override fun getGLSLType(): String
            = "vec4"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}
