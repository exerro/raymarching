package v2

import lwaf_core.mat3
import lwaf_core.minus
import lwaf_core.vec3
import shape.Material

sealed class Shape(internal val propertyTemplates: Map<String, ShapePropertyTemplate>)
sealed class ShapeInstance(internal val properties: ShapeInstanceProperties)

class ObjectShape(
        properties: Map<String, ShapePropertyTemplate>,
        val glslDistanceFunctionString: String,
        val computeDistance: (vec3, ShapeInstanceProperties) -> Float
): Shape(properties) {
    fun instance() = ObjectShapeInstance(Material(), this, propertyTemplates.getProperties())
}

class CompoundShape(
        properties: Map<String, ShapePropertyTemplate>,
        val glslBlendFactorFunctionString: String,
        val computeBlendFactor: (Float, Float, ShapeInstanceProperties) -> Float
): Shape(properties) {
    fun instance(vararg children: ShapeInstance) = CompoundShapeInstance(children.toList(), this, propertyTemplates.getProperties())
}

class SpaceTransformShape(
        properties: Map<String, ShapePropertyTemplate>,
        val glslPositionTransform: String,
        val spaceTransformFunction: (vec3, ShapeInstanceProperties) -> vec3
): Shape(properties) {
    fun instance(child: ShapeInstance) = SpaceTransformShapeInstance(child, this, propertyTemplates.getProperties())
}

class ObjectShapeInstance(
        val material: Material,
        val owner: ObjectShape,
        properties: ShapeInstanceProperties
): ShapeInstance(properties)

class CompoundShapeInstance(
        val children: List<ShapeInstance>,
        val owner: CompoundShape,
        properties: ShapeInstanceProperties
): ShapeInstance(properties)

class SpaceTransformShapeInstance(
        val child: ShapeInstance,
        val owner: SpaceTransformShape,
        properties: ShapeInstanceProperties
): ShapeInstance(properties)

fun <S: ShapeInstance> S.copyWith(properties: ShapeInstanceProperties): S {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is ObjectShapeInstance -> ObjectShapeInstance(material, owner, properties)
        is CompoundShapeInstance -> CompoundShapeInstance(children, owner, properties)
        is SpaceTransformShapeInstance -> SpaceTransformShapeInstance(child, owner, properties)
        else -> error("what")
    } as S
}

fun <S: ShapeInstance> S.set(property: String, value: ShapeProperty): ShapeInstance {
    if (!properties.has(property)) {
        throw ShapePropertyError("No such property '$property'")
    }

    if (!properties.get<ShapeProperty>(property).javaClass.isAssignableFrom(value.javaClass)) {
        throw ShapePropertyError("Invalid value for property '$property' (expected ${properties.get<ShapeProperty>(property).javaClass.simpleName})")
    }

    return copyWith(properties.set(property, value))
}

fun <S: ShapeInstance> S.set(property: String, value: Float) = set(property, FloatShapeProperty(value))
fun <S: ShapeInstance> S.set(property: String, value: vec3) = set(property, Vec3ShapeProperty(value))
fun <S: ShapeInstance> S.set(property: String, value: mat3) = set(property, Mat3ShapeProperty(value))

fun <S: ShapeInstance, T> S.get(property: String): T = properties.get(property)

private fun Map<String, ShapePropertyTemplate>.getProperties()
        = ShapeInstanceProperties(map { (name, template) -> name to template.getDefault() } .toMap())
