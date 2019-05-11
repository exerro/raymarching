import display.Display
import gl.loadShaderProgram
import shape.*
import util.vec4

object Main {
    var t: Float = 0.0f
    val WIDTH = 720
    val HEIGHT = 540

    @JvmStatic
    fun main(args: Array<String>) {
        val display = Display(WIDTH, HEIGHT)
        val compiler = ShaderCompiler()
        val c1 = Sphere(vec4(-4f, 2f, 0f, 0f), 8f)
        var shape: Shape = unionOfShapes(
                Sphere(vec4(4f, 0f, 0f, 0f), 8f),
                Sphere(vec4(2f, 3f, -1f, 0f), 8f),
                c1
        )
        val vertexShader = compiler.buildVertexShader()
        val fragmentShader = compiler.buildFragmentShader(shape)

        println(vertexShader)
        println(fragmentShader)

        val renderer = ShapeRenderer(
                WIDTH.toFloat()/HEIGHT,
                loadShaderProgram(vertexShader, fragmentShader),
                shape,
                compiler.uniformValueLookups
        )

        display.loop {
            c1.setPosition(vec4(Math.sin(t.toDouble() * 10).toFloat() * 8, 0.0f, 0.0f, 0.0f))
            renderer.renderToScreen()
            t += 0.01f
        }

        display.close()
    }
}
