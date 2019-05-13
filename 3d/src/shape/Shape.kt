package shape

import util.vec3

sealed class Shape {
    val transform = ShapeTransform()
    var compiled = false

    fun getPosition(): vec3 = transform.position
    fun getRotation(): vec3 = transform.rotation
    fun getScale(): vec3 = transform.scale


    /**
     * Return a list of uniform values
     */
    abstract fun getUniforms(): Map<String, ShaderData>

    abstract fun getDistanceFunction(): String
    abstract fun getMaterialFunction(): String
    open fun compileDistanceFunctionHeader(builder: ShaderCompiler): ShaderCompiler? = null
    open fun compileMaterialFunctionHeader(builder: ShaderCompiler): ShaderCompiler? = null
}

abstract class MaterialShape(material: Material): Shape() {
    internal val material: Material = material

    fun getMaterial(): Material = material
    fun getColour(): vec3 = material.colour

    override fun getMaterialFunction(): String
            = "MaterialDistance(\$material, \$distance)"
}

abstract class ShapeContainer: Shape() {
    /**
     * Gets the list of children of this shape
     */
    abstract fun getChildren(): List<Shape>
}

fun <S: Shape> S.setTranslation(translation: vec3): S {
    transform.position = translation
    return this
}

fun <S: Shape> S.translateBy(translation: vec3): S
        = setTranslation(this.transform.position.add(translation))

fun <S: Shape> S.setRotation(rotation: vec3): S {
    this.transform.rotation = rotation
    return this
}

fun <S: Shape> S.rotateBy(rotation: vec3): S
        = setRotation(transform.rotation.add(rotation))

fun <S: Shape> S.setScale(scale: vec3): S {
    transform.scale = scale
    return this
}

fun <S: Shape> S.scaleBy(scale: vec3): S
        = setScale(transform.scale.mul(scale))

fun <S: Shape> S.setScale(scale: Float): S
        = setScale(vec3(scale))

fun <S: Shape> S.scaleBy(scale: Float): S
        = setScale(transform.scale.mul(scale))

fun <S: Shape> S.dynamicPosition(): S {
    transform.dynamicPosition = true
    return this
}

fun <S: Shape> S.dynamicRotation(): S {
    transform.dynamicRotation = true
    return this
}

fun <S: Shape> S.dynamicScale(): S {
    transform.dynamicScale = true
    return this
}

fun <M: MaterialShape> M.setColour(colour: vec3): M {
    this.material.colour = colour
    return this
}

fun <M: MaterialShape> M.setColour(r: Float, g: Float, b: Float): M
        = setColour(vec3(r, g, b))

data class TransformModificationException(val property: String, val shape: ShapeTransform): Throwable()
