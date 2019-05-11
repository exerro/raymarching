package shape

import gl.GLShaderProgram
import util.vec2
import util.vec3
import util.vec4

abstract class Shape {
    /**
     * Returns a header to be prepended to shader code, useful for defining a function
     */
    abstract fun getDistanceFunctionHeader(): String?

    /**
     * Returns GLSL code to compute the distance to the object
     */
    abstract fun getDistanceFunction(): String

    /**
     * Return a list of uniform values
     */
    abstract fun getUniforms(): Map<String, ShapeUniformValue>
}

abstract class ShapeContainer: Shape() {
    /**
     * Gets the list of children of this shape
     */
    abstract fun getChildren(): List<Shape>
}

sealed class ShapeUniformValue {
    abstract fun setUniform(shader: GLShaderProgram, uniformName: String)
    abstract fun getGLSLType(): String
}

class FloatShapeUniformValue(var data: Float) : ShapeUniformValue() {
    override fun getGLSLType(): String
            = "float"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

class Vec2ShapeUniformValue(var data: vec2) : ShapeUniformValue() {
    override fun getGLSLType(): String
            = "vec2"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

class Vec3ShapeUniformValue(var data: vec3) : ShapeUniformValue() {
    override fun getGLSLType(): String
            = "vec3"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

class Vec4ShapeUniformValue(var data: vec4) : ShapeUniformValue() {
    override fun getGLSLType(): String
            = "vec4"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}
