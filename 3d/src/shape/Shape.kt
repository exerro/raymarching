package shape

import gl.GLShaderProgram
import util.vec4

interface Shape {
    /**
     * Returns a header to be prepended to shader code, useful for defining a function
     */
    fun getDistanceFunctionHeader(): String?

    /**
     * Returns GLSL code to compute the distance to the object
     */
    fun getDistanceFunction(): String

    /**
     * Return a list of uniform values
     */
    fun getUniforms(): Map<String, ShapeUniformValue>

    /**
     * Sets uniforms in a shader given their name lookups
     */
    fun setUniforms(shader: GLShaderProgram, names: Map<ShapeUniformValue, String>) {
        names.map { (value, name) ->
            value.setUniform(shader, name)
        }
    }
}

interface ShapeContainer: Shape {
    /**
     * Gets the list of children of this shape
     */
    fun getChildren(): List<Shape>
}

sealed class ShapeUniformValue {
    abstract fun setUniform(shader: GLShaderProgram, uniformName: String)
    abstract fun getGLSLType(): String
}

data class FloatShapeUniformValue(var data: Float) : ShapeUniformValue() {
    override fun getGLSLType(): String
            = "float"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}

data class Vec4ShapeUniformValue(var data: vec4) : ShapeUniformValue() {
    override fun getGLSLType(): String
            = "vec4"

    override fun setUniform(shader: GLShaderProgram, uniformName: String)
            = shader.setUniform(uniformName, data)
}
