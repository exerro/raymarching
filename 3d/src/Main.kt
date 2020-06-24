//import shape.primitive.Line
import lwaf_3D.property.moveBy
import lwaf_3D.property.rotateBy
import lwaf_core.*
import org.lwjgl.glfw.GLFW.*
import raymarch.RaymarchingShapeRenderer
import raymarch.RenderOptions
import shape.*
import shape.container.*
import shape.primitive.*
import shape.primitive.torusInverted

fun boxOutline(size: vec3, colour: vec3 = vec3(1f, 1f, 1f), reflectivity: Float = 0.3f): Shape {
    return ShapeDifference(
            ShapeDifference(
                    Box(size).setColour(colour).setReflectivity(reflectivity),
                    Box(size * vec3(1.01f, 0.8f, 0.8f)).setColour(colour).setReflectivity(reflectivity)
            ),
            Box(size * vec3(0.8f, 0.8f, 1.01f)).setColour(colour).setReflectivity(reflectivity)
    )
}

object Main {
    var t: Float = 0.0f
    var ts = 1f
    val WIDTH = 720
    val HEIGHT = 540

    @JvmStatic
    fun main(args: Array<String>) {
        Logging.enable()

        val display = Display(WIDTH, HEIGHT)
        val sphere = Sphere(8f).setColour(1.0f, 0.5f, 0.5f).setScale(1f).setTranslation(vec3(-4f, 2f, 0f)).setReflectivity(0f)
        val sphere2 = Sphere(6f).setColour(1.0f, 0.5f, 0.5f).setTranslation(vec3(-4f, 2f, 0f)).setReflectivity(0.5f)
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
        val inner = ShapeBlend(
                0f,
                Sphere(1f).setTranslation(vec3(2f, 0f, 0f)).setColour(0f, 0.6f, 0f).setReflectivity(0f),
                Sphere(1f).setTranslation(vec3(-1.5f, 0f, 2f)).setColour(0.6f, 0f, 0f).setReflectivity(0.3f),
                Sphere(1f).setTranslation(vec3(-1.5f, 0f, -2f)).setColour(0f, 0f, 0.6f).setReflectivity(0.9f)
        ).setTranslation(vec3(-40f, 0f, 0f))
        inner.dynamicRotation()
        inner.getFactorUniform().setDynamic()
        val transition = ShapeTransition(
                ShapeUnion(
                        ShapeDissolve(
                        3f,
                                Sphere(5f).setTranslation(vec3(-40f, 0f, 0f)),
                                Sphere(4f).setColour(0.3f, 0.6f, 0.9f).setTranslation(vec3(-40f, 2f, 0f)).setReflectivity(0.1f)
                        ),
                        inner
                ),
//                Sphere(0f).setTranslation(vec3(-40f, 0f, 0f)),
                Box(vec3(4f)).setColour(1f, 0.5f, 0.5f).setTranslation(vec3(-40f, 0f, 0f)).setReflectivity(0f),
                0f
        )
        transition.getTransitionProperty().setDynamic()

        var stuff = arrayOf<Shape>()
        val spheres = ((1 .. 5).map { a -> (1 .. 5).map { b -> Pair(a, b) } }.flatten().map { (a, b) ->
            Sphere(4f)
                    .setReflectivity(Math.pow(Math.random(), 2.0).toFloat())
                    .setTranslation(vec3(
                            a.toFloat() * 10,
                            Math.sin((a.toFloat() * 5 + b.toFloat() * 3).toDouble()).toFloat() * 3 - 7,
                            b.toFloat() * 10
                    ) * 1.2f)
                    .setColour(
                            a.toFloat() / 5f,
                            b.toFloat() / 5f,
                            1 + Math.sin((a.toFloat() * 5 + b.toFloat() * 3).toDouble()).toFloat() / 2
                    )
//                    .setColour(1f, 1f, 1f)
                    .setScale(1f)
        })

        spheres.map { v -> stuff = arrayOf(*stuff, v) }

        var shape: Shape = ShapeUnion(
                ShapeDissolve(
                        5f,
                        blend,
                        sphere
                ),
//                ShapeBlend(0f, *stuff),
                sphere2,
                transition,
                ShapeUnion(
                    boxOutline(vec3(10f), vec3(1f, 1f, 1f), 0.5f),
                    boxOutline(vec3(7f), vec3(0.8f, 0.2f, 0.5f), 0f),
                    Box(vec3(3f)).setReflectivity(0.6f).setColour(0.4f, 0.9f, 1f).setRotation(vec3(0.7853975f))
//                    Box(vec3(3f)).setReflectivity(0.6f).setColour(0.4f, 0.9f, 1f)
                ).translateBy(vec3(40f, 0f, 0f)),
                ShapePlane(vec3(0f, -20f, 0f), vec3(0f, 1f, 0f)).setReflectivity(0.3f).setColour(0.9f, 0.3f, 0.6f)
        )

        val torus = Torus(10f, 3f).setColour(1f, 0f, 0f).setReflectivity(0.5f)

//        shape = ShapeUnion(
//                torus,
//                Torus(10f, 0.5f).setColour(0f, 1f, 0f).setReflectivity(0.5f),
//                Sphere(3f).setColour(0f, 0f, 1f).setReflectivity(0.5f),
//                ShapePlane(vec3(0f, -20f, 0f), vec3(0f, 1f, 0f)).setReflectivity(0.3f).setColour(0.9f, 0.3f, 0.6f)
//        )

        // Box(vec3(1f)).setColour(0f, 0f, 1f).setReflectivity(0.5f)

        val a = ShapeUnion(
                torus,
                Torus(8f, 1f).setColour(1f, 0f, 1f).setReflectivity(0.5f).setTranslation(vec3(0f, 10f, 0f)),
                Sphere(2f).setColour(1f, 1f, 0f).setReflectivity(0f).setTranslation(vec3(0f, 5f, 0f))
        )
        val b = ShapeUnion(
                ShapeDifference(
                        ShapeDifference(
                                ShapeDissolve(1f,
                                        ShapeBlend(0.7f,
                                                Box(vec3(10f)).setTranslation(vec3(1f, 3f, 0f)).setColour(0f, 0f, 1f).setReflectivity(0.3f),
                                                Sphere(2f).setTranslation(vec3(5f, 10.5f, 9.5f)).setColour(1f, 1f, 0f).setReflectivity(0.5f),
                                                Sphere(2f).setTranslation(vec3(-3f, 10.5f, 9.5f)).setColour(1f, 1f, 0f).setReflectivity(0.5f)
                                        ),
                                        Box(vec3(8f)).setTranslation(vec3(1f, 8.1f, 0f)).setReflectivity(0.7f).setColour(0.4f, 0.4f, 1f)
                                ),
                                Box(vec3(11f, 2f, 11f)).setTranslation(vec3(1f, 6f, 0f)).setReflectivity(0.9f)
                        ),
                        Sphere(14f).setTranslation(vec3(-10f, 15f, 10f))
                ),
                Sphere(1.5f).setTranslation(vec3(-3f, 3f, -4f)).setColour(1f, 1f, 0f).setReflectivity(0.5f),
                Sphere(1.5f).setTranslation(vec3(-3f, 3f, 4f)).setColour(1f, 1f, 0f).setReflectivity(0.5f),
                Sphere(1.5f).setTranslation(vec3(5f, 3f, -4f)).setColour(1f, 1f, 0f).setReflectivity(0.5f),
                Sphere(1.5f).setTranslation(vec3(5f, 3f, 4f)).setColour(1f, 1f, 0f).setReflectivity(0.5f),
                ShapeBlend(0.8f,
                        Sphere(3f).setTranslation(vec3(1f, 8f, 0f)).setColour(1f, 0f, 0f).setReflectivity(0.3f),
                        Sphere(2f).setTranslation(vec3(1f, 3f, 0f)).setColour(1f, 1f, 0f).setReflectivity(0.5f),
                        Torus(3.9f, 1.2f).setTranslation(vec3(1f, 8f, 0f)).setColour(1f, 0f, 1f).setReflectivity(0.7f)
                )
        )
        val tt = ShapeTransition(a, b, 0.5f)
        tt.getTransitionProperty().setDynamic(true)

//        shape = ShapeUnion(
//                tt,
//                ShapePlane(vec3(0f, -20f, 0f), vec3(0f, 1f, 0f)).setReflectivity(0.3f).setColour(0.9f, 0.3f, 0.6f)
//        )

//        shape = ShapeBlend(
//                1.7f,
//                *stuff,
//                Box(vec3(10f, 0.1f, 10f))
//        )

//        shape.getFactorUniform().setDynamic()
//        shape.dynamicPosition()
//        shape.setScale(10f)
//        shape.dynamicPosition()
//
//        shape = ShapeUnion(
//                *stuff,
//                ShapePlane(vec3(0f, -20f, 0f), vec3(0f, 1f, 0f)).setReflectivity(0.3f).setColour(0.9f, 0.3f, 0.6f)
//        )

        lateinit var renderer: RaymarchingShapeRenderer

        val speed = 10f
        val options = RenderOptions().enableReflections(3).disableShadows()
        var frames = 0
        var advanceTime = false
        lateinit var context2D: DrawContext2D
        lateinit var font: Font

        display.attachMouseDownCallback { _, _ ->
            display.setMouseLocked(true)
        }

        display.attachMouseUpCallback { _, _ ->
            if (!display.isMouseDown()) display.setMouseLocked(false)
        }

        display.attachMouseDragCallback { pos, last, _, _ ->
            val dx = pos.x - last.x
            val dy = pos.y - last.y
            renderer.camera.rotateBy(vec3(0f, -dx / display.getWindowSize().x * 0.5f, 0f))
            renderer.camera.rotateBy(vec3(-dy / display.getWindowSize().y * 0.5f, 0f, 0f))
        }

        display.attachLoadCallback {
            renderer = RaymarchingShapeRenderer(70f)
            renderer.loadShape(shape, options)
            renderer.loadBuffer(WIDTH, HEIGHT)
            renderer.camera.moveBy(renderer.camera.forward * -30f)
            renderer.camera.moveBy(renderer.camera.right * -20f)
            renderer.camera.rotateBy(vec3(0f, -Math.PI.toFloat() * 0.5f, 0f))
            context2D = DrawContext2D(GLView(vec2(0f), display.getWindowSize()))
            font = loadFont("res/font/open-sans/OpenSans-Regular.fnt")
        }

        display.attachUnloadCallback {
            renderer.destroy()
        }

        display.attachResizedCallback { width, height ->
            renderer.loadBuffer(width, height)
            context2D = DrawContext2D(GLView(vec2(0f), display.getWindowSize()))
        }

        display.attachKeyPressedCallback { key, mods ->
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

            if (key == GLFW_KEY_P) {
                torus.setInverted(-torus.getInverted())
            }

            if (key == GLFW_KEY_T) {
                ts = if (ts == 1f) 0.2f else 1f
            }

            if (set) renderer.loadShape(shape, options)
        }

        display.attachUpdateCallback { dt ->
            frames++

            if (display.isKeyDown(GLFW_KEY_W)) renderer.camera.moveBy(renderer.camera.flatForward * speed * dt)
            if (display.isKeyDown(GLFW_KEY_S)) renderer.camera.moveBy(renderer.camera.flatForward * -speed * dt)
            if (display.isKeyDown(GLFW_KEY_A)) renderer.camera.moveBy(renderer.camera.flatRight * -speed * dt)
            if (display.isKeyDown(GLFW_KEY_D)) renderer.camera.moveBy(renderer.camera.flatRight * speed * dt)
            if (display.isKeyDown(GLFW_KEY_SPACE)) renderer.camera.moveBy(renderer.camera.flatUp * speed * dt)
            if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) renderer.camera.moveBy(renderer.camera.flatUp * -speed * dt)

            tt.setTransition(0.5f + Math.sin(t.toDouble()).toFloat() * 0.5f)

            sphere.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
            sphere2.setTranslation(vec3(0.0f, Math.sin(t.toDouble() * 3).toFloat() * 8 + 5, 0.0f))
//            cube.setTranslation(vec3(-10.0f, Math.sin(t.toDouble() * 2).toFloat() * 8 - 2, 5.0f))
////            shape.rotateBy(vec3(0f, -dt/3, 0f))
//            line.rotateBy(vec3(0f, -dt, 0f))
            inner.setFactor(1 + Math.sin(t.toDouble() / 1f).toFloat())
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

            t += dt * (if (advanceTime) 1 else 0) * ts
        }

        display.attachDrawCallback {
            renderer.renderToFramebuffer()
            context2D.drawTexture(renderer.getTexture(), vec2(0f, 0f))
            context2D.write(display.fps.toString(), font, vec2(0f))
            context2D.write("${(0.5f + Math.sin(t.toDouble()).toFloat() * 0.5f * 100f).toInt() / 100f} / ${(t * 100f).toInt() / 100f}", font, vec2(0f, 80f))
//            buffer?.debugDraw()
        }

        display.run()
    }
}
