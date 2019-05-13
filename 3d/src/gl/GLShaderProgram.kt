package gl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL32
import util.*

class GLShaderProgram(private val programID: Int, private val fragmentID: Int, private val geometryID: Int?, private val vertexID: Int, private val instanced: Boolean): GLResource {
    private var active: Boolean = false

    fun setUniform(name: String, value: Boolean) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniform1i(GL20.glGetUniformLocation(programID, name), if (value) 1 else 0)
        if (!active) GL20.glUseProgram(0)
    }

    fun setUniform(name: String, value: Float) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniform1f(GL20.glGetUniformLocation(programID, name), value)
        if (!active) GL20.glUseProgram(0)
    }

    fun setUniform(name: String, value: vec2) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniform2f(GL20.glGetUniformLocation(programID, name), value.x, value.y)
        if (!active) GL20.glUseProgram(0)
    }

    fun setUniform(name: String, value: vec3) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniform3f(GL20.glGetUniformLocation(programID, name), value.x, value.y, value.z)
        if (!active) GL20.glUseProgram(0)
    }

    fun setUniform(name: String, value: vec4) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniform4f(GL20.glGetUniformLocation(programID, name), value.x, value.y, value.z, value.w)
        if (!active) GL20.glUseProgram(0)
    }

    fun setUniform(name: String, value: mat4) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(programID, name), true, value.elements)
        if (!active) GL20.glUseProgram(0)
    }

    fun setUniform(name: String, value: mat3) {
        Logging.log(LogType.SHADER_UNIFORM) { "Setting shader uniform $name to $value" }
        if (!active) GL20.glUseProgram(programID)
        GL20.glUniformMatrix3fv(GL20.glGetUniformLocation(programID, name), true, value.elements)
        if (!active) GL20.glUseProgram(0)
    }

    fun start() {
        GL20.glUseProgram(programID)
        active = true
    }

    fun stop() {
        GL20.glUseProgram(0)
        active = false
    }

    override fun destroy() {
        GL20.glDeleteShader(fragmentID)
        if (geometryID != null) GL20.glDeleteShader(geometryID)
        GL20.glDeleteShader(vertexID)
        GL20.glDeleteProgram(programID)
    }
}

class ShaderLoadException(shaderID: Int, val shaderContent: String) : Exception(GL20.glGetShaderInfoLog(shaderID))
class ProgramLoadException(programID: Int) : Exception(GL20.glGetProgramInfoLog(programID))

fun loadShaderProgram(vertexShader: String, fragmentShader: String, instanced: Boolean = false): GLShaderProgram
        = loadShaderProgram(vertexShader, null, fragmentShader, instanced)

fun loadShaderProgram(vertexShader: String, geometryShader: String?, fragmentShader: String, instanced: Boolean = false): GLShaderProgram {
    val programID = GL20.glCreateProgram()
    val vertexID = loadShader(vertexShader, GL20.GL_VERTEX_SHADER)
    val fragmentID = loadShader(fragmentShader, GL20.GL_FRAGMENT_SHADER)
    val geometryID = if (geometryShader != null) loadShader(geometryShader, GL32.GL_GEOMETRY_SHADER) else null

    GL20.glAttachShader(programID, vertexID)

    if (geometryID != null) {
        GL20.glAttachShader(programID, geometryID)
    }

    GL20.glAttachShader(programID, fragmentID)
    GL20.glLinkProgram(programID)

    if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
        throw ProgramLoadException(programID)
    }

    GL20.glValidateProgram(programID)
    GL20.glDetachShader(programID, vertexID)
    GL20.glDetachShader(programID, fragmentID)

    if (geometryID != null) {
        GL20.glDetachShader(programID, geometryID)
    }

    return GLShaderProgram(programID, fragmentID, geometryID, vertexID, instanced)
}

private fun loadShader(shaderContent: String, shaderType: Int): Int {
    val shaderID = GL20.glCreateShader(shaderType)

    GL20.glShaderSource(shaderID, shaderContent)
    GL20.glCompileShader(shaderID)

    if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
        System.err.println(shaderContent)
        throw ShaderLoadException(shaderID, shaderContent)
    }

    return shaderID
}
