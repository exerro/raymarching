package util

data class vec4(val x: Float, val y: Float, val z: Float, val w: Float) {
    constructor(x: Float) : this(x, x, x, x)
    constructor(x: Float, w: Float) : this(x, x, x, w)

    fun vec3(): vec3 = vec3(x, y, z)
}

data class vec3(val x: Float, val y: Float, val z: Float) {
    constructor(x: Float) : this(x, x, x)

    fun vec4(w: Float): vec4 = vec4(x, y, z, w)

    fun add(other: vec3): vec3 = vec3(x + other.x, y + other.y, z + other.z)

    fun unm(): vec3 = vec3(-x, -y, -z)

    fun mul(s: Float): vec3 = vec3(x * s, y * s, z * s)
    fun mul(other: vec3): vec3 = vec3(x * other.x, y * other.y, z * other.z)

    fun cross(other: vec3) = vec3(
            y*other.z - z*other.y,
            z*other.x - x*other.z,
            x*other.y - y*other.x
    )

    fun length(): Float = Math.sqrt((x*x + y*y + z*z).toDouble()).toFloat()

    fun normalise(): vec3 = mul(1/length())
}

fun vec3.toRotationMatrix(): mat4 = mat4_rotation(y, vec3(0f, 1f, 0f))
        .mul(mat4_rotation(x, vec3(1f, 0f, 0f)))
        .mul(mat4_rotation(z, vec3(0f, 0f, 1f)))

fun vec3.direction(): vec4 = vec4(x, y, z, 0.0f)
fun vec3.position(): vec4 = vec4(x, y, z, 1.0f)

data class vec2(val x: Float, val y: Float) {
    fun vec3(z: Float): vec3 = vec3(x, y, z)
}
