package shape.primitive

import shape.*

var torusInverted = false

class Torus(radius: Float, rradius: Float, material: Material = default_material()) : MaterialShape(material) {
    override fun getDistanceFunction(): String
            = "((\$inverted <= 0 ? 1 : -1) * (length(vec2(length((\$position).xz) - \$radius, (\$position).y)) - \$rradius))"

    private val radiusUniform = FloatShapeUniformValue(radius)
    private val rradiusUniform = FloatShapeUniformValue(rradius)
    private val inverted = FloatShapeUniformValue(-1f)

    init {
        inverted.setDynamic(true)
    }

    fun getRadius(): Float = radiusUniform.getValue()
    fun setRadius(radius: Float) {
        radiusUniform.setValue(radius)
    }

    fun getRRadius(): Float = rradiusUniform.getValue()
    fun setRRadius(radius: Float) {
        rradiusUniform.setValue(radius)
    }

    fun getInverted(): Float = inverted.getValue()
    fun setInverted(inverted: Float) {
        this.inverted.setValue(inverted)
    }

    override fun getUniforms(): Map<String, ShaderData<*>> = mapOf(
            "radius" to radiusUniform,
            "rradius" to rradiusUniform,
            "inverted" to inverted
    )

}