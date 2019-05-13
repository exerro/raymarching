package shape

import gl.GLShaderProgram
import util.ChangingProperty

abstract class ShaderData<T>(private var value: T): ChangingProperty() {
    private var dynamic = false
    private var locked = false

    abstract fun getGLSLValue(): String
    abstract fun getGLSLType(): String
    abstract fun setUniform(shader: GLShaderProgram, uniformName: String)

    fun setDynamic(dynamic: Boolean = true) { this.dynamic = dynamic }
    fun isDynamic(): Boolean = dynamic

    fun lock() { locked = true }
    fun unlock() { locked = false }

    fun getValue(): T = value
    fun setValue(value: T) {
        if (!dynamic && locked) throw StaticValueChangeException(this)
        this.value = value
    }
}

class StaticValueChangeException(val property: ShaderData<*>): Throwable("non-dynamic property changed")
