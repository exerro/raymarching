//import shape.primitive.Line
import gl.Display
import gl.Draw
import org.lwjgl.glfw.GLFW.*
import raymarch.RaymarchingShapeRenderer
import raymarch.RenderOptions
import shape.*
import shape.container.*
import shape.primitive.*
import util.LogType
import util.Logging
import util.enable
import util.vec3

object Main {
    var t: Float = 0.0f
    val WIDTH = 1080
    val HEIGHT = 720

    @JvmStatic
    fun main(args: Array<String>) {
        Logging.enable(LogType.ERROR)
        Logging.enable(LogType.WARNING)
//        Logging.enable(LogType.INFO)
        Logging.enable(LogType.SHADER_COMPILE)
//        Logging.enable(LogType.SHADER_UNIFORM)

        val display = Display(WIDTH, HEIGHT)
        val sphere = Sphere(8f).setColour(1.0f, 0.5f, 0.5f).setScale(1f).setTranslation(vec3(-4f, 2f, 0f))
        val sphere2 = Sphere(6f).setColour(1.0f, 0.5f, 0.5f).setTranslation(vec3(-4f, 2f, 0f))
        sphere.dynamicPosition()
        sphere2.dynamicPosition()
//        val line = Line(vec3(-7.5f, -7.5f, 7.5f), vec3(7.5f, 7.5f, -7.5f), 2.0f).setTranslation(vec3(7.5f, 7.5f, 7.5f))
        val cube = Box(vec3(5.0f, 5.0f, 5.0f)).setTranslation(vec3(-10.0f, -10.0f, 5.0f))
        val blend = ShapeBlend(
                2f,
                Sphere(6f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(4f, -3f, 0f)),
                Sphere(8f).setColour(0.3f, 0.9f, 0.6f).setTranslation(vec3(0f, 5f, -3f)),
                Sphere(10f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(-10f, 2f, -2f)),
//                line,
                cube,
                ShapeDifference(
                        ShapeIntersection(
                                Sphere(5f).setColour(0.9f, 0.9f, 0.3f),
                                Box(vec3(4f)).setColour(0.9f, 0.3f, 0.9f).setTranslation(vec3(0f, 0f, 0f))
                        ),
                        Sphere(4.5f).setColour(0.3f, 0.9f, 0.9f)
                ).setTranslation(vec3(20f, 0f, 0f))
        )
        blend.getFactorUniform().setDynamic()
        val inner = ShapeUnion(
                Sphere(1f).setTranslation(vec3(2f, 0f, 0f)).setColour(0f, 1f, 0f),
                Sphere(1f).setTranslation(vec3(-1.5f, 0f, 2f)).setColour(1f, 0f, 0f),
                Sphere(1f).setTranslation(vec3(-1.5f, 0f, -2f)).setColour(0f, 0f, 1f)
        ).setTranslation(vec3(-40f, 0f, 0f))
        inner.dynamicRotation()
        val transition = ShapeTransition(
                ShapeUnion(
                        ShapeDissolve(
                        3f,
                                Sphere(5f).setTranslation(vec3(-40f, 0f, 0f)),
                                Sphere(4f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(-40f, 2f, 0f))
                        ),
                        inner
                ),
//                Sphere(0f).setTranslation(vec3(-40f, 0f, 0f)),
                Box(vec3(4f)).setColour(1f, 0.5f, 0.5f).setTranslation(vec3(-40f, 0f, 0f)),
                0f
        )
        transition.getTransitionProperty().setDynamic()
        var shape: Shape = ShapeUnion(
                ShapeDissolve(
                        5f,
                        blend,
                        sphere
                ),
                sphere2,
                transition,
                Sphere(0f)
        )

        var stuff = arrayOf<Shape>()
        val spheres = ((1 .. 5).map { a -> (1 .. 5).map { b -> Pair(a, b) } }.flatten().map { (a, b) ->
            Sphere(0.4f)
                    .setTranslation(vec3(
                            a.toFloat(),
                            b.toFloat(),
                            Math.sin((a.toFloat() * 5 + b.toFloat() * 3).toDouble()).toFloat()
                    ).mul(1.2f))
                    .setColour(
                            a.toFloat() / 5f,
                            b.toFloat() / 5f,
                            1 + Math.sin((a.toFloat() * 5 + b.toFloat() * 3).toDouble()).toFloat() / 2
                    )
//                    .setColour(1f, 1f, 1f)
                    .setScale(1f)
        })

        spheres.map { v -> stuff = arrayOf(*stuff, v) }

//        shape = ShapeBlend(
//                1.7f,
//                *stuff,
//                Box(vec3(10f, 0.1f, 10f))
//        )

//        shape.getFactorUniform().setDynamic()
//        shape.dynamicPosition()
//        shape.setScale(10f)
//        shape.dynamicPosition()

        fun box_outline(size: vec3): Shape {
            return ShapeDifference(
                    Box(size),
                    ShapeUnion(
                            Box(size.mul(vec3(1.01f, 0.8f, 0.8f))),
                            Box(size.mul(vec3(0.8f, 1.01f, 0.8f))),
                            Box(size.mul(vec3(0.8f, 0.8f, 1.01f)))
                    )
            )
        }

//        shape = ShapeUnion(
//                box_outline(vec3(10f)),
//                Box(vec3(7f)).setColour(1f, 0f, 0f).setTranslation(vec3(10f, 0f, 0f))
////                box_outline(vec3(7f)),
////                box_outline(vec3(4f))
//        )

        shape = transition

        lateinit var renderer: RaymarchingShapeRenderer

        val speed = 10f
        val options = RenderOptions().enableReflections(2).enableShadows()
        var lastFPS = 0
        var advanceTime = true

        display.onMouseDownCallback = { _, _ ->
            display.setMouseLocked(true)
        }

        display.onMouseUpCallback = { _, _ ->
            if (!display.isMouseDown()) display.setMouseLocked(false)
        }

        display.onMouseDragCallback = { pos, last, _, _ ->
            val dx = pos.x - last.x
            val dy = pos.y - last.y
            renderer.camera.rotateY(-dx / display.width * 0.5f)
            renderer.camera.rotateX(-dy / display.height * 0.5f)
        }

        display.onLoadCallback = {
            Draw.init()
            renderer = RaymarchingShapeRenderer()
            renderer.loadShape(shape, options)
            renderer.loadBuffer(WIDTH, HEIGHT)
            renderer.camera.forward(-30f)
        }

        display.onUnloadCallback = {
            renderer.destroy()
        }

        display.onResizedCallback = { width, height ->
            renderer.loadBuffer(width, height)
        }

        display.onKeyPressedCallback = { key, mods ->
            var set = false

            if (key == GLFW_KEY_TAB && (mods and GLFW_MOD_CONTROL) != 0) {
                options.enableReflections(if (options.maxReflectionCount() == 0) 2 else 0)
                set = true
            }

            if (key == GLFW_KEY_TAB && (mods and GLFW_MOD_CONTROL) == 0) {
                if (options.shadowsEnabled()) options.disableShadows() else options.enableShadows()
                set = true
            }

            if (key == GLFW_KEY_ENTER) {
                advanceTime = !advanceTime
            }

            if (set) renderer.loadShape(shape, options)
        }

        display.onUpdateCallback = { dt ->
            if (display.isKeyDown(GLFW_KEY_W)) renderer.camera.forward(speed * dt)
            if (display.isKeyDown(GLFW_KEY_S)) renderer.camera.forward(-speed * dt)
            if (display.isKeyDown(GLFW_KEY_A)) renderer.camera.right(-speed * dt)
            if (display.isKeyDown(GLFW_KEY_D)) renderer.camera.right(speed * dt)
            if (display.isKeyDown(GLFW_KEY_SPACE)) renderer.camera.up(speed * dt)
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) renderer.camera.up(-speed * dt)

            sphere.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
            sphere2.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
//            cube.setTranslation(vec3(-10.0f, Math.sin(t.toDouble() * 2).toFloat() * 8 - 2, 5.0f))
////            shape.rotateBy(vec3(0f, -dt/3, 0f))
//            line.rotateBy(vec3(0f, -dt, 0f))
            blend.setFactor(4.5f + Math.sin(t.toDouble() * 5).toFloat() * 4f)
            transition.setTransition(0.5f + Math.sin(t.toDouble()).toFloat() * 0.5f)
            inner.rotateBy(vec3(0f, dt * 3, 0f))
            inner.getChildren().map { it.translateBy(vec3(0f, 0f, 0f)) }
//            shape.setScale(1.4f + 0.5f * Math.sin(3 * t.toDouble()).toFloat())
//            blend.setFactor(1 + Math.sin(t.toDouble() * 10).toFloat() * 3)

//            box.rotateBy(vec3(0f, -dt*2, 0f))

//            shape.setFactor(1f + Math.sin(t.toDouble()).toFloat() * 0.8f)

//            (shape as ShapeContainer).getChildren().map { child ->
//                child.setTranslation(vec3(child.getPosition().x, child.getPosition().y, Math.sin((t * 3 + child.getPosition().x * 5 + child.getPosition().y * 3).toDouble()).toFloat()))
//            }

            if (display.FPS != lastFPS) {
                println("FPS: ${display.FPS}")
                lastFPS = display.FPS
            }

            t += dt * (if (advanceTime) 1 else 0)
        }

        display.onDrawCallback = {
            renderer.renderToFramebuffer()
            Draw.texture(renderer.getTexture())
//            buffer?.debugDraw()
        }

        display.run()
    }
}
