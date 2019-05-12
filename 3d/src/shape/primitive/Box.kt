package shape.primitive

import shape.*
import util.vec3

class Box(size: vec3, material: Material = default_material()) : MaterialShape(material) {
    private val sizeUniform = Vec3ShapeUniformValue(size)

    fun getSize(size: vec3) = sizeUniform.data

    fun setSize(size: vec3) {
        sizeUniform.data = size
    }

    override fun getHeader(): String?
            = "float sdBox( vec3 p, vec3 b ) {\n" +
                "\tvec3 d = abs(p) - b;\n" +
                "\treturn length(max(d,0.0))\n" +
                "\t       + min(max(d.x,max(d.y,d.z)),0.0); // remove this line for an only partially signed sdf \n" +
            "}"

    override fun getDistanceFunction(): String
            = "sdBox(\$ray_position.xyz, (1 * vec4(\$size, 0)).xyz)"

    override fun getUniforms(): Map<String, ShapeUniformValue> = mapOf(
            "size" to sizeUniform
    )

}