import display.Display
import gl.ShaderLoadException
import gl.loadShaderProgram
import gl.screen_quad
import org.lwjgl.opengl.GL11.*
import shape.ShaderCompiler
import shape.ShapeUnion
import shape.Sphere
import util.vec4

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val compiler = ShaderCompiler()
        val shape = ShapeUnion(
                Sphere(vec4(0f, 0f, 0f, 0f), 10f),
                Sphere(vec4(0f, 0f, 0f, 0f), 5f)
        )
        val vertexShader = compiler.buildVertexShader()
        val fragmentShader = compiler.buildFragmentShader(shape)
        val display = Display()
        val quad = screen_quad

        println(vertexShader)
        println(fragmentShader)

        val shader = try { loadShaderProgram(vertexShader, fragmentShader) } catch (e: ShaderLoadException) {
            e.printStackTrace()
            println(e.shaderContent)
            null
        }

        display.loop {
            shader!!.start()
            quad.load()
            glDrawElements(GL_TRIANGLES, quad.vertexCount, GL_UNSIGNED_INT, 0)
            quad.unload()
            shader!!.stop()
        }

        display.close()
    }
}
