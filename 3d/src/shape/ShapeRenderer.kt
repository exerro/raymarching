package shape

import gl.GLShaderProgram
import gl.screen_quad
import org.lwjgl.opengl.GL11
import util.*

class ShapeRenderer(var aspectRatio: Float, val shader: GLShaderProgram, val shape: Shape, val lookupUniform: UniformNameLookup) {
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
        lookupUniform.valueNames.map { (value, name) ->
            value.setUniform(shader, name)
        }
        lookupUniform.materialNames.map { (value, name) ->
            value.material.setUniforms(shader, name)
        }
        setTransformationUniforms(shape, mat4_identity)
        shader.start()
        screen_quad.load()
        GL11.glDrawElements(GL11.GL_TRIANGLES, screen_quad.vertexCount, GL11.GL_UNSIGNED_INT, 0)
        screen_quad.unload()
        shader.stop()
    }

    fun getFacing(): vec3 = rotation.toRotationMatrix().mul(vec3(0f, 0f, -1f).direction()).vec3()
    fun getUp(): vec3 = rotation.toRotationMatrix().mul(vec3(0f, 1f, 0f).direction()).vec3()
    fun getRight(): vec3 = rotation.toRotationMatrix().mul(vec3(1f, 0f, 0f).direction()).vec3()

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
}

private fun vec3.flat(): vec3 = vec3(x, 0f, z).normalise()
