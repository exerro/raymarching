package raymarch

import lwaf_3D.Camera
import lwaf_core.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import shape.*
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max

class RaymarchingShapeRenderer(
        private val fov: Float,
        val camera: Camera = Camera().setPerspectiveProjection(1f)
): GLResource {
    private lateinit var shape: Shape
    private lateinit var shader: GLShaderProgram
    private lateinit var lookup: UniformNameLookup
    private lateinit var dimensions: vec2
    private lateinit var framebuffer: GLFramebuffer
    private lateinit var texture: GLTexture
    private var shapeLoaded: Boolean = false
    private var bufferLoaded: Boolean = false

    fun loadShape(shape: Shape = this.shape, options: RenderOptions = RenderOptions()) {
        if (shapeLoaded) { unloadShape() }
        this.shape = shape
        val compiler = RaymarchShaderCompiler()

        shape.notifyChanged()

        loadUniformNameLookup(shape, compiler.lookup)

        compiler.setOptions(options)
        compiler.generateFragmentShaderStart()

        if (options.maxReflectionCount() != 0) {
            compiler.generateReflectionFragmentShaderMain()
        }
        else {
            compiler.generateDefaultFragmentShaderMain()
        }

        compiler.generateFragmentShaderUniforms(shape)
        compiler.generateDistanceFunctionHeaders(shape)
        compiler.generateMaterialFunctionHeaders(shape)
        compiler.generateFunctionDefinitions(shape)

        val vertexShaderText = String(Files.readAllBytes(Paths.get("src/glsl/vertex.glsl")))
        val fragmentShaderText = compiler.getText()

        shader = loadShaderProgram(vertexShaderText, fragmentShaderText)
        lookup = compiler.lookup
        shapeLoaded = true

        shape.lock()
    }

    fun getTexture(): GLTexture {
        if (!bufferLoaded) throw IllegalStateException("cannot get texture of renderer without a loaded buffer")
        return texture
    }

    fun unloadShape() {
        if (!shapeLoaded) throw IllegalStateException("cannot unload shape from renderer without a loaded shape")
        shapeLoaded = false
        this.shape.unlock()
    }

    fun loadBuffer(width: Int, height: Int) {
        if (bufferLoaded) unloadBuffer()

        camera.setPerspectiveProjection(width.toFloat() / height, fov)
        dimensions = vec2(width.toFloat(), height.toFloat())
        framebuffer = GLFramebuffer(width, height)
        texture = createEmptyTexture(width, height)

        framebuffer.attachTexture(texture, GL30.GL_COLOR_ATTACHMENT0)
        framebuffer.setDrawBuffers(GL30.GL_COLOR_ATTACHMENT0)

        bufferLoaded = true
    }

    fun unloadBuffer() {
        if (!bufferLoaded) throw IllegalStateException("cannot unload buffer from renderer without a loaded buffer")
        bufferLoaded = false
        texture.destroy()
        framebuffer.destroy()
    }


    fun renderToFramebuffer() {
        if (!bufferLoaded) throw IllegalStateException("cannot draw renderer without a loaded buffer")
        if (!shapeLoaded) throw IllegalStateException("cannot draw renderer without a loaded shape")

        framebuffer.bind()
        GL11.glClearColor(0f, 0f, 0f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)

        setUniforms()

        GLView(vec2(0f), dimensions).setViewport()
        shader.start()
        screen_quad.load()
        GL11.glDrawElements(GL11.GL_TRIANGLES, screen_quad.vertexCount, GL11.GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
        framebuffer.unbind()
    }

    private fun setUniforms() {
        shader.setUniform("cameraPosition", camera.position)
        shader.setUniform("transform", camera.rotation.toRotationMatrix())
        shader.setUniform("aspectRatio", dimensions.x / dimensions.y)
        shader.setUniform("FOV", fov * Math.PI.toFloat() / 180.0f)

        lookup.valueNames.map { (value, name) ->
            if (value.hasChanged()) {
                value.setUniform(shader, name)
                value.changeHandled()
            }
        }

        lookup.shapeNames.map { (shape, name) ->
            if (shape is MaterialShape) {
                if (shape.getMaterial().colour.hasChanged()) {
                    shape.getMaterial().colour.setUniform(shader, "${name}_material.colour")
                    shape.getMaterial().colour.changeHandled()
                }
            }
        }

        setTransformationUniforms(shape, mat4_identity, TransformInfo(shape.transform))
    }

    private fun setTransformationUniforms(shape: Shape, transform: mat4, ti: TransformInfo) {
        if (shape is MaterialShape && ti.dynamicOrRotated && shape.transform.needsRecompute()) {
            val thisTransform = transform * shape.transform.getTransformationMatrix()
            shape.transform.changeHandled()
            shader.setUniform("${lookup.shapeNames[shape]!!}_transform", thisTransform.inverse())

            if (ti.dynamicScale) {
                val scaled = (thisTransform * vec3(1f, 1f, 1f).vec4(0f)).vec3()
                val divisor = max(max(1/scaled.x, 1/scaled.y), 1/scaled.z)
                shader.setUniform("${lookup.shapeNames[shape]!!}_divisor", divisor)
            }
        }
        else if (shape is ShapeContainer) {
            val thisTransform = transform * shape.transform.getTransformationMatrix()
            for (child in shape.getChildren()) {
                setTransformationUniforms(child, thisTransform, TransformInfo(child.transform, ti))
            }
        }
    }

    override fun destroy() {
        if (bufferLoaded) unloadBuffer()
        if (shapeLoaded) unloadShape()
    }
}