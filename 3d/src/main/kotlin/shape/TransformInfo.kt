package shape

import lwaf_core.plus
import lwaf_core.times
import lwaf_core.vec3

class TransformInfo(
        val transform: ShapeTransform,
        val parent: TransformInfo? = null
) {
    private val parentTranslated: Boolean = parent?.translated ?: false
    private val parentRotated: Boolean = parent?.rotated ?: false
    private val parentScaled: Boolean = parent?.scaled ?: false
    val translated: Boolean = transform.position != vec3(0f, 0f, 0f) || parentTranslated
    private val rotated: Boolean = transform.rotation != vec3(0f, 0f, 0f) || parentRotated
    val scaled: Boolean = transform.scale != vec3(1f, 1f, 1f) || parentScaled
    private val dynamicTranslation: Boolean = transform.dynamicPosition || (parent?.dynamicTranslation?: false)
    private val dynamicRotation: Boolean = transform.dynamicRotation || (parent?.dynamicRotation?: false)
    val dynamicScale: Boolean = transform.dynamicScale || (parent?.dynamicScale ?: false)
    private val dynamic: Boolean = (parent?.dynamic ?: false) || dynamicTranslation || dynamicRotation || dynamicScale
    val dynamicOrRotated: Boolean = dynamic || rotated

    fun getTranslation(): vec3 = parent?.getTranslation()?.plus(transform.position.times(parent.getScale())) ?: transform.position
    fun getRotation(): vec3 = parent?.getRotation()?.plus(transform.rotation) ?: transform.rotation
    fun getScale(): vec3 = parent?.getScale()?.times(transform.scale) ?: transform.scale
}