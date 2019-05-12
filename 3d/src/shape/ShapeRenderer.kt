package shape

import gl.GLShaderProgram
import gl.screen_quad
import org.lwjgl.opengl.GL11
import util.*

class ShapeRenderer(var aspectRatio: Float, val shader: GLShaderProgram, val shape: Shape, val lookup: NameLookup) {
    var FOV: Float = 70.0f
    var position = vec3(0.0f, 0.0f, 30.0f)
    var rotation = vec3(0f, 0f, 0f)

    fun forward(distance: Float) {
        position = position.add(getFacing().flat().mul(distance))
    }

    fun right(distance: Float) {
        position = position.add(getRight().mul(distance))
    }

    fun up(distance: Float) {
        position = position.add(vec3(0f, distance, 0f))
    }

    fun rotateX(theta: Float) {
        rotation = rotation.add(vec3(theta, 0f, 0f))
    }

    fun rotateY(theta: Float) {
        rotation = rotation.add(vec3(0f, theta, 0f))
    }

    fun rotateZ(theta: Float) {
        rotation = rotation.add(vec3(0f, 0f, theta))
    }

    fun renderToScreen() {
        shader.setUniform("ray_position", position.position())
        shader.setUniform("transform", rotation.toRotationMatrix())
        shader.setUniform("aspectRatio", aspectRatio)
        shader.setUniform("FOV", FOV * Math.PI.toFloat() / 180.0f)
        lookup.uniformValues.map { (value, name) ->
            value.setUniform(shader, name)
        }
        lookup.materials.map { (value, name) ->
            value.material.setUniforms(shader, name)
        }
        shader.start()
        screen_quad.load()
        GL11.glDrawElements(GL11.GL_TRIANGLES, screen_quad.vertexCount, GL11.GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
    }

    fun getFacing(): vec3 = rotation.toRotationMatrix().mul(vec3(0f, 0f, -1f).direction()).vec3()
    fun getUp(): vec3 = rotation.toRotationMatrix().mul(vec3(0f, 1f, 0f).direction()).vec3()
    fun getRight(): vec3 = rotation.toRotationMatrix().mul(vec3(1f, 0f, 0f).direction()).vec3()
}

private fun vec3.flat(): vec3 = vec3(x, 0f, z).normalise()
private fun vec3.toRotationMatrix(): mat4 = mat4_rotation(y, vec3(0f, 1f, 0f))
                                       .mul(mat4_rotation(x, vec3(1f, 0f, 0f)))
                                       .mul(mat4_rotation(z, vec3(0f, 0f, 1f)))
private fun vec3.toInverseRotationMatrix(): mat4 = mat4_identity
                                              .mul(mat4_rotation(z, vec3(0f, 0f, 1f)))
                                              .mul(mat4_identity)
                                              .mul(mat4_rotation(x, vec3(1f, 0f, 0f)))
                                              .mul(mat4_identity)
                                              .mul(mat4_rotation(y, vec3(0f, 1f, 0f)))
