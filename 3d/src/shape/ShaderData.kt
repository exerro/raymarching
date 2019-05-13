package shape

import gl.GLShaderProgram
import util.ChangingProperty

abstract class ShaderData<T>(private var value: T): ChangingProperty() {
    private var dynamic = false
    private var locked = false

    abstract fun getGLSLValue(): String
    abstract fun getGLSLType(): String
    abstract fun setUniform(shader: GLShaderProgram, uniformName: String)

    fun setDynamic(dynamic: Boolean = true) {
        if (locked && dynamic) throw StaticValueChangeException(this, "dynamic status of property changed")
        this.dynamic = dynamic
    }
    fun isDynamic(): Boolean = dynamic

    fun lock() { locked = true }
    fun unlock() { locked = false }

    fun getValue(): T = value
    fun setValue(value: T) {
        if (!dynamic && locked) throw StaticValueChangeException(this, "non-dynamic property changed")
        this.value = value
    }
}

class StaticValueChangeException(val property: ShaderData<*>, error: String): Throwable(error)
