package shape

import gl.GLShaderProgram
import util.*

sealed class Shape {
    private var translation: vec3 = vec3(0f, 0f, 0f)
    private var rotation: vec3 = vec3(0f, 0f, 0f)
    private var scale: vec3 = vec3(1f, 1f, 1f)

    fun setTranslation(translation: vec3): Shape {
        this.translation = translation
        return this
    }

    fun translateBy(translation: vec3): Shape {
        this.translation = this.translation.add(translation)
        return this
    }

    fun setRotation(rotation: vec3): Shape {
        this.rotation = rotation
        return this
    }

    fun rotateBy(rotation: vec3): Shape {
        this.rotation = this.rotation.add(rotation)
        return this
    }

    fun setScale(scale: Float): Shape {
        this.scale = vec3(scale)
        return this
    }

    fun scaleBy(scale: Float): Shape {
        this.scale = this.scale.mul(scale)
        return this
    }

//    fun setScale(scale: vec3): Shape {
//        this.scale = scale
//        return this
//    }
//
//    fun scaleBy(scale: vec3): Shape {
//        this.scale = this.scale.mul(scale)
//        return this
//    }

    fun getTransformation(): mat4 = mat4_translate(translation).mul(rotation.toRotationMatrix()).mul(mat4_scale(scale))

    /**
     * Returns a header to be prepended to shader code, useful for defining a function
     */
    abstract fun getHeader(): String?

    /**
     * Return GLSL code to compute just the distance to the object
     */
    abstract fun getDistanceFunction(): String

    /**
     * Returns GLSL code to compute the distance to the object and material properties
     */
    abstract fun getFunction(): String

    /**
     * Return a list of uniform values
     */
    abstract fun getUniforms(): Map<String, ShapeUniformValue>
}

abstract class MaterialShape(material: Material): Shape() {
    private var material: Material = material

    fun getMaterial(): Material = material

    fun setMaterial(material: Material): MaterialShape {
        this.material = material
        return this
    }

    fun getColour(): vec4 = material.colour

    fun setColour(colour: vec4): MaterialShape {
        this.material.colour = colour
        return this
    }

    fun setColour(r: Float, g: Float, b: Float): MaterialShape {
        this.material.colour = vec4(r, g, b, 1f)
        return this
    }

    /**
     * Returns GLSL code to compute the distance to the object and material properties
     */
    override fun getFunction(): String {
        return "DistanceData(\$material, (${getDistanceFunction()}) * \$transformation_scale)"
    }
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
