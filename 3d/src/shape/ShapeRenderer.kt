package shape

import gl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation
import org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0
import util.*
import kotlin.math.max

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
        framebuffer.bind()
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        setUniforms()

        shader.start()
        screen_quad.load()
        glDrawElements(GL_TRIANGLES, screen_quad.vertexCount, GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
        framebuffer.unbind()
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
        shader.setUniform("cameraPosition", camera.position)
        shader.setUniform("transform", camera.rotation.toRotationMatrix().mat3())
        shader.setUniform("aspectRatio", aspectRatio)
        shader.setUniform("FOV", camera.FOV * Math.PI.toFloat() / 180.0f)
        lookupUniform.valueNames.map { (value, name) ->
            if (value.hasChanged()) {
                value.setUniform(shader, name)
                value.changeHandled()
            }
        }
        lookupUniform.shapeNames.map { (shape, name) ->
            if (shape is MaterialShape) {
                if (shape.getMaterial().hasChanged()) {
                    shape.getMaterial().setUniform(shader, "${name}_material")
                    shape.getMaterial().notifyChanged()
                }
            }
        }
        setTransformationUniforms(shape, mat4_identity, TransformInfo(shape.transform))
    }

    private fun setTransformationUniforms(shape: Shape, transform: mat4, ti: TransformInfo) {
        if (shape is MaterialShape && ti.dynamicOrRotated && shape.transform.needsRecompute()) {
            val this_transform = transform.mul(shape.transform.getTransformationMatrix())
            shader.setUniform("${lookupUniform.shapeNames[shape]!!}_transform", this_transform.inverse())

            if (ti.dynamicScale) {
                val scaled = this_transform.mul(vec3(1f, 1f, 1f).direction()).vec3()
                val divisor = max(max(1/scaled.x, 1/scaled.y), 1/scaled.z)
                shader.setUniform("${lookupUniform.shapeNames[shape]!!}_divisor", divisor)
            }
        }
        else if (shape is ShapeContainer) {
            val this_transform = transform.mul(shape.transform.getTransformationMatrix())
            for (child in shape.getChildren()) {
                setTransformationUniforms(child, this_transform, TransformInfo(child.transform, ti))
            }
        }
    }

    override fun destroy() {
        framebuffer.destroy()
        texture.destroy()
        shader.destroy()
    }
}
