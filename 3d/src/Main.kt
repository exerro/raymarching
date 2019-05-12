import display.Display
import gl.loadShaderProgram
import org.lwjgl.glfw.GLFW.*
import shape.*
import util.position
import util.vec3
import util.vec4

object Main {
    var t: Float = 0.0f
    val WIDTH = 1080
    val HEIGHT = 720

    @JvmStatic
    fun main(args: Array<String>) {
        val display = Display(WIDTH, HEIGHT)
        val compiler = ShaderCompiler()
        val sphere = Sphere(vec4(-4f, 2f, 0f, 0f), 8f)
        val sphere2 = Sphere(vec4(-4f, 2f, 0f, 0f), 6f)
        val blend: ShapeBlend = ShapeBlend(
                2.0f,
                Sphere(vec4(4f, -3f, 0f, 0f), 6f),
                Sphere(vec4(0f, 5f, -3f, 0f), 8f),
                Sphere(vec4(-10f, 2f, -2f, 0f), 10f),
                Line(vec4(0.0f, 0.0f, 5.0f, 0.0f), vec4(15.0f, 15.0f, -15.0f, 0.0f), 2.0f),
                Box(vec4(-10.0f, -10.0f, 5.0f, 0.0f), vec3(5.0f, 5.0f, 5.0f)),
                ShapeDifference(
                        ShapeIntersection(
                                Sphere(vec3(20f, 0f, 0f).position(), 5f),
                                Box(vec3(20f, 0f, 0f).position(), vec3(4f))
                        ),
                        Sphere(vec3(20f, 0f, 0f).position(), 4.5f)
                )
        )
        val shape: Shape = ShapeUnion(
                ShapeDissolve(
                        2f,
                        blend,
                        sphere
                ),
                sphere2
        )
        val vertexShader = compiler.buildVertexShader()
        val fragmentShader = compiler.buildFragmentShader(shape)
        var renderer: ShapeRenderer? = null

        val speed = 50f

        display.onMouseDownCallback = { _, _ ->
            display.setMouseLocked(true)
        }

        display.onMouseUpCallback = { _, _ ->
            if (!display.isMouseDown()) display.setMouseLocked(false)
        }

        display.onMouseDragCallback = { pos, last, _, _ ->
            val dx = pos.x - last.x
            val dy = pos.y - last.y
            renderer!!.rotateY(-dx / display.width * 0.5f)
            renderer!!.rotateX(-dy / display.height * 0.5f)
        }

        display.onLoadCallback = {
            renderer = ShapeRenderer(
                    WIDTH.toFloat()/HEIGHT,
                    loadShaderProgram(vertexShader, fragmentShader),
                    shape,
                    compiler.uniformValueLookups
            )
        }

        display.onResizedCallback = { width, height ->
            renderer!!.aspectRatio = width.toFloat() / height
        }

        display.onUpdateCallback = { dt ->
            if (display.isKeyDown(GLFW_KEY_W)) renderer!!.forward(speed * dt)
            if (display.isKeyDown(GLFW_KEY_S)) renderer!!.forward(-speed * dt)
            if (display.isKeyDown(GLFW_KEY_A)) renderer!!.right(-speed * dt)
            if (display.isKeyDown(GLFW_KEY_D)) renderer!!.right(speed * dt)
            if (display.isKeyDown(GLFW_KEY_SPACE)) renderer!!.up(speed * dt)
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) renderer!!.up(-speed * dt)

            sphere.setPosition(vec4(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 4, 0.0f, 0.0f))
            sphere2.setPosition(sphere.getPosition())
            blend.setFactor(1 + Math.sin(t.toDouble() * 10).toFloat() * 3)

            println("FPS: ${1/dt}")

            t += dt
        }

        display.onDrawCallback = {
            renderer!!.renderToScreen()
        }

        display.run()
    }
}
