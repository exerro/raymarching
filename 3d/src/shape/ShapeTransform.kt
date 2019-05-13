package shape

import gl.GLShaderProgram
import util.*

data class ShapeTransform(
        var position: vec3 = vec3(0f, 0f, 0f),
        var rotation: vec3 = vec3(0f, 0f, 0f),
        var scale: vec3 = vec3(1f, 1f, 1f),
        var dynamicPosition: Boolean = false,
        var dynamicRotation: Boolean = false,
        var dynamicScale: Boolean = false
): ShaderData() {
    override fun getGLSLValue(): String {
        TODO("not implemented")
    }

    override fun getGLSLType(): String {
        TODO("not implemented")
    }

    override fun setUniform(shader: GLShaderProgram, uniformName: String) {
        TODO("not implemented")
    }

    private lateinit var transformation: mat4

    fun needsRecompute(): Boolean {
        if (!::transformation.isInitialized) return true
        if (hasChanged()) return true
        return false
    }

    fun getTransformationMatrix(): mat4
            = if (needsRecompute()) computeTransformationMatrix() else transformation

    fun computeTransformationMatrix(): mat4 {
        transformation = mat4_translate(position).mul(rotation.toRotationMatrix()).mul(mat4_scale(scale))
        changeHandled()
        return transformation
    }

    fun isDynamicOrRotated(): Boolean
            = dynamicPosition || dynamicRotation || dynamicScale || rotation != vec3(0f, 0f, 0f)
}
