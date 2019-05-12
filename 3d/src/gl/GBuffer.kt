package gl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13

import org.lwjgl.opengl.GL30.*
import util.vec2

class GBuffer(val width: Int, val height: Int): GLResource {
    private val fbo = FBO(width, height)
    val colourTexture = fbo.attachTexture(createEmptyTexture(width, height), GL_COLOR_ATTACHMENT0)
    val positionTexture = fbo.attachTexture(createEmptyTexture(width, height, GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT), GL_COLOR_ATTACHMENT1)
    val normalTexture = fbo.attachTexture(createEmptyTexture(width, height, GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT), GL_COLOR_ATTACHMENT2)
    val lightingTexture = fbo.attachTexture(createEmptyTexture(width, height, GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT), GL_COLOR_ATTACHMENT3)

    val depthTexture: Texture
        get() = fbo.depthTexture

    init {
        fbo.setDrawBuffers(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3)
    }

    fun bind() {
        fbo.bind()
    }

    fun unbind() {
        fbo.unbind()
    }

    fun bindReading() {
        for (i in 0..3) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTexture.textureID)
            GL13.glActiveTexture(GL13.GL_TEXTURE1)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, positionTexture.textureID)
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalTexture.textureID)
            GL13.glActiveTexture(GL13.GL_TEXTURE3)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightingTexture.textureID)
        }
    }

    fun unbindReading() {
        for (i in 0..3) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
            GL13.glActiveTexture(GL13.GL_TEXTURE1)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
            GL13.glActiveTexture(GL13.GL_TEXTURE3)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        }
    }

    override fun destroy() {
        fbo.destroy()
        colourTexture.destroy()
        positionTexture.destroy()
        normalTexture.destroy()
        lightingTexture.destroy()
    }

}

fun GBuffer.debugDraw() {
    Draw.setViewport(vec2(width.toFloat(), height.toFloat()))
    Draw.texture(colourTexture, vec2(0f, 0f), vec2(0.5f, 0.5f))
    Draw.texture(normalTexture, vec2(width / 2f, height / 2f), vec2(0.5f, 0.5f))
    Draw.texture(positionTexture, vec2(0f, height / 2f), vec2(0.5f, 0.5f))
    Draw.texture(depthTexture, vec2(width / 2f, 0f), vec2(0.5f, 0.5f))
}
