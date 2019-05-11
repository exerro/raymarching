package shape

import util.vec3
import util.vec4

class Box(center: vec4, size: vec3) : Shape() {
    private val centerUniform = Vec4ShapeUniformValue(center)
    private val sizeUniform = Vec3ShapeUniformValue(size)

    override fun getDistanceFunctionHeader(): String?
            = "float sdBox( vec3 p, vec3 b ) {\n" +
                "\tvec3 d = abs(p) - b;\n" +
                "\treturn length(max(d,0.0))\n" +
                "\t       + min(max(d.x,max(d.y,d.z)),0.0); // remove this line for an only partially signed sdf \n" +
            "}"

    override fun getDistanceFunction(): String
            = "sdBox((\$ray_position - \$center).xyz, \$size)"

    override fun getUniforms(): Map<String, ShapeUniformValue> = mapOf(
            "center" to centerUniform,
            "size" to sizeUniform
    )

}