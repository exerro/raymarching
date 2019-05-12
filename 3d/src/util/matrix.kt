package util

class mat4(vararg val elements: Float) {
    fun mul(other: mat4): mat4 {
        val result = FloatArray(16)

        for (i in 0 .. 3) for (j in 0 .. 3) for (k in 0 .. 3) {
            result[i * 4 + j] += elements[i * 4 + k] * other.elements[k * 4 + j]
        }

        return mat4(*result)
    }

    fun mul(v: vec4): vec4 = vec4(
            elements[0] * v.x + elements[1] * v.y + elements[2] * v.z + elements[3] * v.w,
            elements[4] * v.x + elements[5] * v.y + elements[6] * v.z + elements[7] * v.w,
            elements[8] * v.x + elements[9] * v.y + elements[10] * v.z + elements[11] * v.w,
            elements[12] * v.x + elements[13] * v.y + elements[14] * v.z + elements[15] * v.w
    )

    fun transpose(): mat4 = mat4(
            elements[0], elements[4], elements[8],  elements[12],
            elements[1], elements[5], elements[9],  elements[13],
            elements[2], elements[6], elements[10], elements[14],
            elements[3], elements[7], elements[11], elements[15]
    )

    fun inverse(): mat4 {
        val inv = FloatArray(16)
        var i: Int

        inv[0] = elements[5]  * elements[10] * elements[15] -
                elements[5]  * elements[11] * elements[14] -
                elements[9]  * elements[6]  * elements[15] +
                elements[9]  * elements[7]  * elements[14] +
                elements[13] * elements[6]  * elements[11] -
                elements[13] * elements[7]  * elements[10];

        inv[4] = -elements[4]  * elements[10] * elements[15] +
                elements[4]  * elements[11] * elements[14] +
                elements[8]  * elements[6]  * elements[15] -
                elements[8]  * elements[7]  * elements[14] -
                elements[12] * elements[6]  * elements[11] +
                elements[12] * elements[7]  * elements[10];

        inv[8] = elements[4]  * elements[9] * elements[15] -
                elements[4]  * elements[11] * elements[13] -
                elements[8]  * elements[5] * elements[15] +
                elements[8]  * elements[7] * elements[13] +
                elements[12] * elements[5] * elements[11] -
                elements[12] * elements[7] * elements[9];

        inv[12] = -elements[4]  * elements[9] * elements[14] +
                elements[4]  * elements[10] * elements[13] +
                elements[8]  * elements[5] * elements[14] -
                elements[8]  * elements[6] * elements[13] -
                elements[12] * elements[5] * elements[10] +
                elements[12] * elements[6] * elements[9];

        inv[1] = -elements[1]  * elements[10] * elements[15] +
                elements[1]  * elements[11] * elements[14] +
                elements[9]  * elements[2] * elements[15] -
                elements[9]  * elements[3] * elements[14] -
                elements[13] * elements[2] * elements[11] +
                elements[13] * elements[3] * elements[10];

        inv[5] = elements[0]  * elements[10] * elements[15] -
                elements[0]  * elements[11] * elements[14] -
                elements[8]  * elements[2] * elements[15] +
                elements[8]  * elements[3] * elements[14] +
                elements[12] * elements[2] * elements[11] -
                elements[12] * elements[3] * elements[10];

        inv[9] = -elements[0]  * elements[9] * elements[15] +
                elements[0]  * elements[11] * elements[13] +
                elements[8]  * elements[1] * elements[15] -
                elements[8]  * elements[3] * elements[13] -
                elements[12] * elements[1] * elements[11] +
                elements[12] * elements[3] * elements[9];

        inv[13] = elements[0]  * elements[9] * elements[14] -
                elements[0]  * elements[10] * elements[13] -
                elements[8]  * elements[1] * elements[14] +
                elements[8]  * elements[2] * elements[13] +
                elements[12] * elements[1] * elements[10] -
                elements[12] * elements[2] * elements[9];

        inv[2] = elements[1]  * elements[6] * elements[15] -
                elements[1]  * elements[7] * elements[14] -
                elements[5]  * elements[2] * elements[15] +
                elements[5]  * elements[3] * elements[14] +
                elements[13] * elements[2] * elements[7] -
                elements[13] * elements[3] * elements[6];

        inv[6] = -elements[0]  * elements[6] * elements[15] +
                elements[0]  * elements[7] * elements[14] +
                elements[4]  * elements[2] * elements[15] -
                elements[4]  * elements[3] * elements[14] -
                elements[12] * elements[2] * elements[7] +
                elements[12] * elements[3] * elements[6];

        inv[10] = elements[0]  * elements[5] * elements[15] -
                elements[0]  * elements[7] * elements[13] -
                elements[4]  * elements[1] * elements[15] +
                elements[4]  * elements[3] * elements[13] +
                elements[12] * elements[1] * elements[7] -
                elements[12] * elements[3] * elements[5];

        inv[14] = -elements[0]  * elements[5] * elements[14] +
                elements[0]  * elements[6] * elements[13] +
                elements[4]  * elements[1] * elements[14] -
                elements[4]  * elements[2] * elements[13] -
                elements[12] * elements[1] * elements[6] +
                elements[12] * elements[2] * elements[5];

        inv[3] = -elements[1] * elements[6] * elements[11] +
                elements[1] * elements[7] * elements[10] +
                elements[5] * elements[2] * elements[11] -
                elements[5] * elements[3] * elements[10] -
                elements[9] * elements[2] * elements[7] +
                elements[9] * elements[3] * elements[6];

        inv[7] = elements[0] * elements[6] * elements[11] -
                elements[0] * elements[7] * elements[10] -
                elements[4] * elements[2] * elements[11] +
                elements[4] * elements[3] * elements[10] +
                elements[8] * elements[2] * elements[7] -
                elements[8] * elements[3] * elements[6];

        inv[11] = -elements[0] * elements[5] * elements[11] +
                elements[0] * elements[7] * elements[9] +
                elements[4] * elements[1] * elements[11] -
                elements[4] * elements[3] * elements[9] -
                elements[8] * elements[1] * elements[7] +
                elements[8] * elements[3] * elements[5];

        inv[15] = elements[0] * elements[5] * elements[10] -
                elements[0] * elements[6] * elements[9] -
                elements[4] * elements[1] * elements[10] +
                elements[4] * elements[2] * elements[9] +
                elements[8] * elements[1] * elements[6] -
                elements[8] * elements[2] * elements[5];

        val det = 1 / (elements[0] * inv[0] + elements[1] * inv[4] + elements[2] * inv[8] + elements[3] * inv[12])

        return mat4(*inv.map { det * it }.toFloatArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as mat4

        if (!elements.contentEquals(other.elements)) return false

        return true
    }

    override fun hashCode(): Int {
        return elements.contentHashCode()
    }
}

val mat4_identity = mat4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
)

fun mat4_translate(translation: vec3) = mat4(
        1.0f, 0.0f, 0.0f, translation.x,
        0.0f, 1.0f, 0.0f, translation.y,
        0.0f, 0.0f, 1.0f, translation.z,
        0.0f, 0.0f, 0.0f, 1.0f
)

fun mat4_scale(scale: vec3) = mat4(
        scale.x, 0.0f,    0.0f,    0.0f,
        0.0f,    scale.y, 0.0f,    0.0f,
        0.0f,    0.0f,    scale.z, 0.0f,
        0.0f,    0.0f,    0.0f,    1.0f
)

fun mat4_scale(scale: Float) = mat4_scale(vec3(scale, scale, scale))

fun mat4_rotation(theta: Float, axis: vec3 = vec3(0.0f, 1.0f, 0.0f)): mat4 {
    val sin = Math.sin(theta.toDouble()).toFloat()
    val cos = Math.cos(theta.toDouble()).toFloat()
    val ux = axis.x
    val uy = axis.y
    val uz = axis.z
    val ux2 = ux * ux
    val uy2 = uy * uy
    val uz2 = uz * uz

    return mat4(
            cos + ux2 * (1 - cos), ux * uy * (1 - cos) - uz * sin, ux * uz * (1 - cos) + uy * sin, 0f,
            uy * ux * (1 - cos) + uz * sin, cos + uy2 * (1 - cos), uy * uz * (1 - cos) - ux * sin, 0f,
            uz * ux * (1 - cos) - uy * sin, uz * uy * (1 - cos) + ux * sin, cos + uz2 * (1 - cos), 0f,
            0f, 0f, 0f, 1f
    )
}

fun mat4_look(facing: vec3, up: vec3): mat4 {
    val zaxis = vec3(facing.x, facing.y, -facing.z) // no idea why the Z needs to be negated
    val xaxis = up.cross(zaxis).normalise()
    val yaxis = zaxis.cross(xaxis).normalise()

    return mat4(
            xaxis.x, xaxis.y, xaxis.z, 0f,
            yaxis.x, yaxis.y, yaxis.z, 0f,
            zaxis.x, zaxis.y, zaxis.z, 0f,
            0f, 0f, 0f, 1f
    )
}
