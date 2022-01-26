package v2

import lwaf_core.mat3
import lwaf_core.mat3_identity
import lwaf_core.vec3

sealed class ShapeProperty {
    abstract fun templateIs(template: ShapePropertyTemplate): Boolean
}
sealed class ShapePropertyTemplate {
    abstract fun getDefault(): ShapeProperty
}

class FloatShapeProperty(val value: Float): ShapeProperty() {
    override fun templateIs(template: ShapePropertyTemplate): Boolean = template is Vec3ShapePropertyTemplate
}

class FloatShapePropertyTemplate(private val defaultValue: Float = 0f): ShapePropertyTemplate() {
    override fun getDefault(): ShapeProperty = FloatShapeProperty(defaultValue)
}

class Vec3ShapeProperty(val value: vec3): ShapeProperty() {
    override fun templateIs(template: ShapePropertyTemplate): Boolean = template is Vec3ShapePropertyTemplate
}

class Vec3ShapePropertyTemplate(private val defaultValue: vec3 = vec3(0f)): ShapePropertyTemplate() {
    override fun getDefault(): ShapeProperty = Vec3ShapeProperty(defaultValue)
}

class Mat3ShapeProperty(val value: mat3): ShapeProperty() {
    override fun templateIs(template: ShapePropertyTemplate): Boolean = template is Mat3ShapePropertyTemplate
}

class Mat3ShapePropertyTemplate(private val defaultValue: mat3 = mat3_identity): ShapePropertyTemplate() {
    override fun getDefault(): ShapeProperty = Mat3ShapeProperty(defaultValue)
}

class ShapePropertyError(message: String): Exception(message)

class ShapeInstanceProperties(
        private val properties: Map<String, ShapeProperty>
) {
    internal fun has(name: String): Boolean {
        return properties[name] != null
    }

    internal fun set(name: String, value: ShapeProperty): ShapeInstanceProperties {
        val clone = properties.map { (name, value) -> name to value } .toMap() .toMutableMap()
        clone[name] = value
        return ShapeInstanceProperties(clone)
    }

    fun <T> get(name: String): T {
        @Suppress("UNCHECKED_CAST", "MapGetWithNotNullAssertionOperator")
        return properties[name]!! as T
    }
}
