package util

data class vec4(val x: Float, val y: Float, val z: Float, val w: Float) {
    fun vec3(): vec3 = vec3(x, y, z)
}

data class vec3(val x: Float, val y: Float, val z: Float) {

}

fun vec3.direction(): vec4 = vec4(x, y, z, 0.0f)
fun vec3.position(): vec4 = vec4(x, y, z, 1.0f)

data class vec2(val x: Float, val y: Float) {

}
