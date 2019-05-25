package shape

import lwaf_core.plus
import lwaf_core.times
import lwaf_core.vec3
import raymarch.RaymarchShaderCompiler

sealed class Shape {
    val transform = ShapeTransform()
    var compiled = false

    fun getPosition(): vec3 = transform.position
    fun getRotation(): vec3 = transform.rotation
    fun getScale(): vec3 = transform.scale


    /**
     * Return a list of uniform values
     */
    abstract fun getUniforms(): Map<String, ShaderData<*>>

    abstract fun getDistanceFunction(): String
    abstract fun getMaterialFunction(): String
    open fun compileDistanceFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = null
    open fun compileMaterialFunctionHeader(builder: RaymarchShaderCompiler): RaymarchShaderCompiler? = null

    open fun lock() {
        getUniforms().values.map { it.lock() }
    }

    open fun unlock() {
        getUniforms().values.map { it.unlock() }
    }

    open fun notifyChanged() {
        getUniforms().values.map { it.notifyChanged() }
        transform.notifyChanged()
    }
}

abstract class MaterialShape(internal val material: Material): Shape() {
    fun getMaterial(): Material = material

    override fun getMaterialFunction(): String
            = "MaterialDistance(\$material, \$distance)"

    override fun lock() {
        super.lock()
        material.colour.lock()
    }

    override fun unlock() {
        super.unlock()
        material.colour.unlock()
    }

    override fun notifyChanged() {
        super.notifyChanged()
        material.colour.notifyChanged()
    }
}

abstract class ShapeContainer: Shape() {
    /**
     * Gets the list of children of this shape
     */
    abstract fun getChildren(): List<Shape>

    protected fun applyToAllChildren(func: String, init: Int = 0, last: Int = getChildren().size): String {
        return if (init == last - 1) {
            "\$${init + 1}"
        } else if ((last - init) % 2 == 1) {
            func.replace("\$a", applyToAllChildren(func, init, last - 1)).replace("\$b", "\$$last")
        } else {
            func.replace("\$a", applyToAllChildren(func, init, init + (last - init) / 2)).replace("\$b", applyToAllChildren(func, init + (last - init) / 2, last))
        }
    }

    override fun lock() {
        super.lock()
        getChildren().map { it.lock() }
    }

    override fun unlock() {
        super.unlock()
        getChildren().map { it.unlock() }
    }

    override fun notifyChanged() {
        super.notifyChanged()
        getChildren().map { it.notifyChanged() }
    }
}

fun <S: Shape> S.setTranslation(translation: vec3): S {
    transform.position = translation
    this.transform.notifyChanged()
    return this
}

fun <S: Shape> S.translateBy(translation: vec3): S
        = setTranslation(this.transform.position + translation)

fun <S: Shape> S.setRotation(rotation: vec3): S {
    this.transform.rotation = rotation
    this.transform.notifyChanged()
    return this
}

fun <S: Shape> S.rotateBy(rotation: vec3): S
        = setRotation(transform.rotation + rotation)

fun <S: Shape> S.setScale(scale: vec3): S {
    transform.scale = scale
    this.transform.notifyChanged()
    return this
}

fun <S: Shape> S.scaleBy(scale: vec3): S
        = setScale(transform.scale * scale)

fun <S: Shape> S.setScale(scale: Float): S
        = setScale(vec3(scale))

fun <S: Shape> S.scaleBy(scale: Float): S
        = setScale(transform.scale * scale)

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
    this.material.colour.setValue(colour)
    return this
}

fun <M: MaterialShape> M.setColour(r: Float, g: Float, b: Float): M
        = setColour(vec3(r, g, b))

fun <M: MaterialShape> M.setReflectivity(reflectivity: Float): M {
    this.material.reflectivity.setValue(reflectivity)
    return this
}

data class TransformModificationException(val property: String, val shape: ShapeTransform): Throwable()
