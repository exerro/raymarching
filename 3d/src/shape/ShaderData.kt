package shape

import gl.GLShaderProgram

abstract class ShaderData {
    private var changed = true
    private var dynamic = false

    abstract fun getGLSLValue(): String
    abstract fun getGLSLType(): String
    abstract fun setUniform(shader: GLShaderProgram, uniformName: String)

    fun notifyChanged() { changed = true }
    fun changeHandled() { changed = false }
    fun hasChanged(): Boolean = changed

    fun setDynamic(dynamic: Boolean = true) { this.dynamic = dynamic }
    fun isDynamic(): Boolean = dynamic
}
