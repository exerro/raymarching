import gl.*
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
        val line = Line(vec3(-7.5f, -7.5f, 7.5f), vec3(7.5f, 7.5f, -7.5f), 2.0f).setTranslation(vec3(7.5f, 7.5f, 7.5f))
        val cube = Box(vec3(5.0f, 5.0f, 5.0f)).setTranslation(vec3(-10.0f, -10.0f, 5.0f))
        val blend = ShapeBlend(
                2f,
                Sphere(6f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(4f, -3f, 0f)),
                Sphere(8f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(0f, 5f, -3f)),
                Sphere(10f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(-10f, 2f, -2f)),
                line,
                cube,
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
                        5f,
                        blend,
                        sphere
                ),
                sphere2
        )

        fun box_outline(size: vec3): Shape {
            return ShapeDifference(
                    Box(size),
                    ShapeUnion(
                            Box(size.mul(vec3(1.01f, 0.9f, 0.9f))),
                            Box(size.mul(vec3(0.9f, 1.01f, 0.9f))),
                            Box(size.mul(vec3(0.9f, 0.9f, 1.01f)))
                    )
            )
        }

//        shape = ShapeUnion(
//                box_outline(vec3(10f)),
//                box_outline(vec3(7f)),
//                box_outline(vec3(4f))
//        )

        val vertexShader = compiler.buildVertexShader()
        val fragmentShader = compiler.buildFragmentShader(shape)
        var renderer: ShapeRenderer? = null
        var buffer: GBuffer? = null

        println(fragmentShader)

        val speed = 50f
        var lastFPS = 0

        display.onMouseDownCallback = { _, _ ->
            display.setMouseLocked(true)
        }

        display.onMouseUpCallback = { _, _ ->
            if (!display.isMouseDown()) display.setMouseLocked(false)
        }

        display.onMouseDragCallback = { pos, last, _, _ ->
            val dx = pos.x - last.x
            val dy = pos.y - last.y
            renderer?.camera?.rotateY(-dx / display.width * 0.5f)
            renderer?.camera?.rotateX(-dy / display.height * 0.5f)
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

        display.onUnloadCallback = {
            renderer?.destroy()
            buffer?.destroy()
        }

        display.onResizedCallback = { width, height ->
            renderer = ShapeRenderer(
                    loadShaderProgram(vertexShader, fragmentShader),
                    shape,
                    compiler.lookup,
                    width,
                    height,
                    renderer!!.camera
            )
        }

        display.onUpdateCallback = { dt ->
            if (display.isKeyDown(GLFW_KEY_W)) renderer?.camera?.forward(speed * dt)
            if (display.isKeyDown(GLFW_KEY_S)) renderer?.camera?.forward(-speed * dt)
            if (display.isKeyDown(GLFW_KEY_A)) renderer?.camera?.right(-speed * dt)
            if (display.isKeyDown(GLFW_KEY_D)) renderer?.camera?.right(speed * dt)
            if (display.isKeyDown(GLFW_KEY_SPACE)) renderer?.camera?.up(speed * dt)
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) renderer?.camera?.up(-speed * dt)

            sphere.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
            sphere2.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
            cube.setTranslation(vec3(-10.0f, Math.sin(t.toDouble() * 2).toFloat() * 8 - 2, 5.0f))
//            shape.rotateBy(vec3(0f, -dt/3, 0f))
            line.rotateBy(vec3(0f, -dt, 0f))
//            shape.setScale(1.4f + 0.5f * Math.sin(3 * t.toDouble()).toFloat())
//            blend.setFactor(1 + Math.sin(t.toDouble() * 10).toFloat() * 3)

//            box.rotateBy(vec3(0f, -dt*2, 0f))

            if (display.FPS != lastFPS) {
                println("FPS: ${display.FPS}")
                lastFPS = display.FPS
            }

            t += dt
        }

        display.onDrawCallback = {
            renderer?.renderToFramebuffer()
             Draw.texture(renderer?.texture!!)
//            buffer?.debugDraw()
        }

        display.run()
    }
}
