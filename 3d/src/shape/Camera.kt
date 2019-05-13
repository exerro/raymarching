package shape

import util.direction
import util.toRotationMatrix
import util.vec3

class Camera {
    var FOV: Float = 70.0f
    var position = vec3(0.0f, 0.0f, 0.0f)
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

    fun getFacing(): vec3 = rotation.toRotationMatrix().mul(vec3(0f, 0f, -1f).direction()).vec3()
    fun getUp(): vec3 = rotation.toRotationMatrix().mul(vec3(0f, 1f, 0f).direction()).vec3()
    fun getRight(): vec3 = rotation.toRotationMatrix().mul(vec3(1f, 0f, 0f).direction()).vec3()

}

private fun vec3.flat(): vec3 = vec3(x, 0f, z).normalise()
