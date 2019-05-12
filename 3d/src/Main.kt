import gl.Display
import gl.Draw
import gl.GBuffer
import gl.debugDraw
import gl.loadShaderProgram
import org.lwjgl.glfw.GLFW.*
import shape.*
import shape.container.*
import shape.primitive.Box
import shape.primitive.Line
import shape.primitive.Sphere
import util.*

object Main {
    var t: Float = 0.0f
    val WIDTH = 1080
    val HEIGHT = 720

    @JvmStatic
    fun main(args: Array<String>) {
        val display = Display(WIDTH, HEIGHT)
        val compiler = ShaderCompiler()
        val sphere = Sphere(8f).setColour(0.3f, 0.9f, 0.6f).setScale(1f).setTranslation(vec3(-4f, 2f, 0f))
        val sphere2 = Sphere(6f).setColour(1.0f, 0.0f, 0.0f).setTranslation(vec3(-4f, 2f, 0f))
        val blend = ShapeBlend(
                4.0f,
                Sphere(6f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(4f, -3f, 0f)),
                Sphere(8f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(0f, 5f, -3f)),
                Sphere(10f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(-10f, 2f, -2f)),
                Line(vec3(0.0f, 0.0f, 5.0f), vec3(15.0f, 15.0f, -15.0f), 2.0f),
                Box(vec3(5.0f, 5.0f, 5.0f)).setTranslation(vec3(-10.0f, -10.0f, 5.0f)),
                ShapeDifference(
                        ShapeIntersection(
                                Sphere(5f).setColour(0.9f, 0.9f, 0.3f),
                                Box(vec3(4f)).setColour(0.9f, 0.3f, 0.9f).setTranslation(vec3(0f, 0f, 0f))
                        ),
                        Sphere(4.5f).setColour(0.3f, 0.9f, 0.9f)
                ).setTranslation(vec3(20f, 0f, 0f))
        )
        var shape: Shape = ShapeUnion(
                ShapeDissolve(
                        2f,
                        blend,
                        sphere
                ),
                sphere2
        )
        val vertexShader = compiler.buildVertexShader()
        val fragmentShader = compiler.buildBufferedFragmentShader(shape)
        var renderer: ShapeRenderer? = null
        var buffer: GBuffer? = null

        println(fragmentShader)

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
            Draw.init()

            renderer = ShapeRenderer(
                    loadShaderProgram(vertexShader, fragmentShader),
                    shape,
                    compiler.lookup,
                    WIDTH,
                    HEIGHT
            )

            buffer = GBuffer(WIDTH, HEIGHT)
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

            sphere.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
            sphere2.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
//            shape.rotateBy(vec3(0f, dt, 0f))
//            shape.setScale(1.4f + 0.5f * Math.sin(3 * t.toDouble()).toFloat())
//            blend.setFactor(1 + Math.sin(t.toDouble() * 10).toFloat() * 3)

            println("FPS: ${1/dt}")

            t += dt
        }

        display.onDrawCallback = {
            renderer?.renderToBuffer(buffer!!)
            buffer?.debugDraw()
//            renderer!!.renderToScreen()
        }

        display.run()
    }
}
