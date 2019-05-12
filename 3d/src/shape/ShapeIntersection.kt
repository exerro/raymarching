package shape

class ShapeIntersection(private vararg val children: Shape): ShapeContainer() {
    override fun getHeader(): String?
            = "DistanceData sdIntersection(DistanceData a, DistanceData b) {\n" +
            "\treturn a.distance > b.distance ? a : b;\n" +
            "}"

    override fun getFunction(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "sdIntersection($acc, \$$i)" }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = children.toList()
}
