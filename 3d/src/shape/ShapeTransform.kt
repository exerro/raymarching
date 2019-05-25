package shape

import lwaf_core.*
import util.*

data class ShapeTransform(
        var position: vec3 = vec3(0f, 0f, 0f),
        var rotation: vec3 = vec3(0f, 0f, 0f),
        var scale: vec3 = vec3(1f, 1f, 1f),
        var dynamicPosition: Boolean = false,
        var dynamicRotation: Boolean = false,
        var dynamicScale: Boolean = false
): ChangingProperty() {
    private lateinit var transformation: mat4

    fun needsRecompute(): Boolean {
        if (!::transformation.isInitialized) return true
        if (hasChanged()) return true
        return false
    }

    fun getTransformationMatrix(): mat4
            = if (needsRecompute()) computeTransformationMatrix() else transformation

    fun computeTransformationMatrix(): mat4 {
        transformation = mat4_translate(position) * (rotation.toRotationMatrix() * mat3_scale(scale)).mat4()
        changeHandled()
        return transformation
    }
}
