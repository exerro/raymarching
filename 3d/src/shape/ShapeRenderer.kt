package shape

import gl.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation
import org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0
import util.*

class ShapeRenderer(
        private val shader: GLShaderProgram,
        val shape: Shape,
        private val lookupUniform: UniformNameLookup,
        private val width: Int,
        private val height: Int,
        val camera: Camera = Camera()
): GLResource {
    var aspectRatio = width.toFloat() / height
    var framebuffer = FBO(width, height)
    var texture = createEmptyTexture(width, height)
    var sent = false

    init {
        framebuffer.attachTexture(texture, GL_COLOR_ATTACHMENT0)
        framebuffer.setDrawBuffers(GL_COLOR_ATTACHMENT0)
        println("width: $width, height: $height")
    }

    fun renderToFramebuffer() {
        val t = System.currentTimeMillis()

        framebuffer.bind()
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        setUniforms()
        val t1 = System.currentTimeMillis()
        shader.start()
        screen_quad.load()
        glDrawElements(GL_TRIANGLES, screen_quad.vertexCount, GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
        framebuffer.unbind()
        val e = System.currentTimeMillis()
        println("${e-t1}ms/${e-t}ms = ${((e-t1).toFloat()/(e-t)*1000).toInt()/10f}%")
    }

    fun renderToBuffer(buffer: GBuffer) {
        // TODO
        buffer.bind()

        Draw.pushViewport(vec2(width.toFloat(), height.toFloat()))

        glDisable(GL_CULL_FACE)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_BLEND)
        glDepthMask(false)
        glClearColor(0f, 0f, 0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        setUniforms()
        buffer.bind()
        shader.start()
        screen_quad.load()
        glDrawElements(GL_TRIANGLES, screen_quad.vertexCount, GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
        buffer.unbind()

        // glDepthMask(false)
        // glDisable(GL_CULL_FACE);
        // glDisable(GL_DEPTH_TEST);

        buffer.unbind()
        framebuffer.bind()

        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFunc(GL_ONE, GL_ONE)

        buffer.bindReading()

//        for (lightType in scene.getLightTypes()) {
//            var shader = Light.getShader(lightType)
//
//            shader.start();
//            shader.setUniform("screenSize", new vec2f(getWidth(), getHeight()));
//            shader.setUniform("viewTransform", viewMatrix);
//            shader.setUniform("projectionTransform", projectionMatrix);
//
//            for (light in scene.getLightsOfType(lightType)) {
//                light.setUniforms(viewMatrix, projectionMatrix);
//                light.render(buffer);
//            }
//        }

        Draw.popViewport()

        framebuffer.unbind()
        buffer.unbindReading()
    }

    private fun setUniforms() {
//        if (sent) return
        sent = true
        shader.setUniform("ray_position", camera.position.position())
        shader.setUniform("transform", camera.rotation.toRotationMatrix())
        shader.setUniform("aspectRatio", aspectRatio)
        shader.setUniform("FOV", camera.FOV * Math.PI.toFloat() / 180.0f)
        lookupUniform.valueNames.map { (value, name) ->
            value.setUniform(shader, name)
        }
        lookupUniform.materialNames.map { (value, name) ->
            value.getMaterial().setUniforms(shader, name)
        }
        setTransformationUniforms(shape, mat4_identity)
    }

    private fun setTransformationUniforms(shape: Shape, transform: mat4) {
        val this_transform = transform.mul(shape.getTransformation())

        if (shape is MaterialShape) {
            shader.setUniform(lookupUniform.transformationNames[shape]!!, this_transform.inverse())
            shader.setUniform(lookupUniform.transformationNames[shape]!! + "_scale",
                    this_transform.mul(vec3(1f, 1f, 1f).normalise().direction()).vec3().length()
            )
        }
        else if (shape is ShapeContainer) {
            for (child in shape.getChildren()) {
                setTransformationUniforms(child, this_transform)
            }
        }
    }

    override fun destroy() {
        framebuffer.destroy()
        texture.destroy()
        shader.destroy()
    }
}
