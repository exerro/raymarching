package shape

import lwaf_core.*

class Camera {
    var FOV: Float = 70.0f
    var position = vec3(0.0f, 0.0f, 0.0f)
    var rotation = vec3(0f, 0f, 0f)

    fun forward(distance: Float) {
        position += getFacing().flat() * distance
    }

    fun right(distance: Float) {
        position += getRight().flat() * distance
    }

    fun up(distance: Float) {
        position += getUp() * distance
    }

    fun rotateX(theta: Float) {
        rotation += vec3(theta, 0f, 0f)
    }

    fun rotateY(theta: Float) {
        rotation += vec3(0f, theta, 0f)
    }

    fun rotateZ(theta: Float) {
        rotation += vec3(0f, 0f, theta)
    }

    fun getFacing(): vec3 = rotation.toRotationMatrix() * vec3(0f, 0f, -1f)
    fun getUp(): vec3 = rotation.toRotationMatrix() * vec3(0f, 1f, 0f)
    fun getRight(): vec3 = rotation.toRotationMatrix() * vec3(1f, 0f, 0f)

}

private fun vec3.flat(): vec3 = vec3(x, 0f, z).normalise()
