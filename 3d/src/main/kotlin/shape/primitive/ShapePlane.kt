package shape.primitive

import lwaf_core.vec3
import shape.*

class ShapePlane(point: vec3, normal: vec3, material: Material = default_material()): MaterialShape(material) {
    private val pointUniform = Vec3ShapeUniformValue(point)
    private val normalUniform = Vec3ShapeUniformValue(normal)

    override fun getDistanceFunction(): String = "dot(\$normal, \$position - \$point)"
    override fun getMaterialFunction(): String = "MaterialDistance(Material(vec3(0.5 + 0.5 * abs(1 - (step(5, mod((\$position).x, 10)) + (1 - step(5, mod((\$position).z, 10)))))), \$material.reflectivity), \$distance)"

    override fun getUniforms(): Map<String, ShaderData<*>> = mapOf(
            "point" to pointUniform,
            "normal" to normalUniform
    )
}
