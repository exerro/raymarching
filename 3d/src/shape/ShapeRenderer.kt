package shape

import gl.GLShaderProgram
import gl.screen_quad
import org.lwjgl.opengl.GL11
import util.vec4

class ShapeRenderer(val aspectRatio: Float, val shader: GLShaderProgram, val shape: Shape, val uniforms: Map<ShapeUniformValue, String>) {
    var FOV: Float = 70.0f
    var position = vec4(0.0f, 0.0f, 30.0f, 0.0f)

    fun renderToScreen() {
        shader.setUniform("ray_position", position)
        shader.setUniform("aspectRatio", aspectRatio)
        shader.setUniform("FOV", FOV * Math.PI.toFloat() / 180.0f)
        uniforms.map { (value, name) ->
            value.setUniform(shader, name)
        }
        shader.start()
        screen_quad.load()
        GL11.glDrawElements(GL11.GL_TRIANGLES, screen_quad.vertexCount, GL11.GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
    }
}
