import display.Display
import gl.loadShaderProgram
import org.lwjgl.glfw.GLFW.*
import shape.*
import util.mat4_identity
import util.mat4_scale
import util.vec3
import util.vec4

object Main {
    var t: Float = 0.0f
    val WIDTH = 1080
    val HEIGHT = 720

    @JvmStatic
    fun main(args: Array<String>) {
        assert(mat4_identity.mul(mat4_scale(2.0f)) == mat4_scale(2.0f))
        val display = Display(WIDTH, HEIGHT)
        val compiler = ShaderCompiler()
        val c1 = Sphere(vec4(-4f, 2f, 0f, 0f), 8f)
        val blend: ShapeBlend = blendOfShapes(
                2f,
                Sphere(vec4(4f, -3f, 0f, 0f), 6f),
                Sphere(vec4(0f, 5f, -3f, 0f), 8f),
                Sphere(vec4(-10f, 2f, -2f, 0f), 10f),
                Line(vec4(0.0f, 0.0f, 5.0f, 0.0f), vec4(15.0f, 15.0f, -15.0f, 0.0f), 2.0f),
                Box(vec4(-10.0f, -10.0f, 5.0f, 0.0f), vec3(5.0f, 5.0f, 5.0f))
        ) as ShapeBlend
        var shape: Shape = differenceOfShapes(
                blend,
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

//        glfwSetInputMode(display.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)

        var lastX = 0f
        var lastY = 0f
        val speed = 0.8f

        glfwSetCursorPosCallback(display.window) { window, x, y ->
            val dx = (x - lastX).toFloat()
            val dy = (y - lastY).toFloat()
            lastX = x.toFloat()
            lastY = y.toFloat()
            renderer.rotateY(-dx / display.width * 0.5f)
            renderer.rotateX(-dy / display.height * 0.5f)
        }

        display.loop {
            if (display.isKeyDown(GLFW_KEY_W)) renderer.forward(speed)
            if (display.isKeyDown(GLFW_KEY_S)) renderer.forward(-speed)
            if (display.isKeyDown(GLFW_KEY_A)) renderer.right(-speed)
            if (display.isKeyDown(GLFW_KEY_D)) renderer.right(speed)
            if (display.isKeyDown(GLFW_KEY_SPACE)) renderer.up(speed)
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) renderer.up(-speed)

             c1.setPosition(vec4(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8, 0.0f, 0.0f))
            val f = 1 + Math.sin(t.toDouble() * 10).toFloat() * 3
            blend.setFactor(f)
            (blend.a as ShapeBlend).setFactor(f)
            (blend.a.a as ShapeBlend).setFactor(f)
            (blend.a.a.a as ShapeBlend).setFactor(f)
            renderer.renderToScreen()
            t += 0.01f
        }

        display.close()
    }
}
