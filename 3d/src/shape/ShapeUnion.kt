package shape

class ShapeUnion(private vararg val children: Shape): ShapeContainer() {
    override fun getDistanceFunctionHeader(): String?
            = null

    override fun getDistanceFunction(): String
            = (2 .. children.size).fold("\$1") { acc, i -> "min($acc, \$$i)" }

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

    override fun getChildren(): List<Shape>
            = children.toList()
}
