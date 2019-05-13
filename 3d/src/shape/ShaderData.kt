package shape

import gl.GLShaderProgram
import util.ChangingProperty

abstract class ShaderData: ChangingProperty() {
    private var dynamic = false

    abstract fun getGLSLValue(): String
    abstract fun getGLSLType(): String
    abstract fun setUniform(shader: GLShaderProgram, uniformName: String)

    fun setDynamic(dynamic: Boolean = true) { this.dynamic = dynamic }
    fun isDynamic(): Boolean = dynamic
}
