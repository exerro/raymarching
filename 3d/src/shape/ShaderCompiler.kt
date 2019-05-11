package shape

class ShaderCompiler {
    private val headers = HashSet<String>()
    private val uniformValueLookups = HashMap<ShapeUniformValue, String>()
    private val uniforms = HashMap<String, String>()

    private fun getName(name: String, uniform: ShapeUniformValue): String {
        var i = 1
        while (uniformValueLookups.values.contains("${name}_$i")) ++i
        uniformValueLookups[uniform] = "${name}_$i"
        return "${name}_$i"
    }

    private fun compileShapeDistanceFunction(shape: Shape): String {
        val header = shape.getDistanceFunctionHeader()
        if (header != null) headers.add(header)
        val nameLookup = shape.getUniforms().map { (name, uniform) -> name to getName(name, uniform) } .toMap()
        val func = nameLookup.keys.toList().fold(shape.getDistanceFunction()) { acc, it ->
            acc.replace("\$$it", nameLookup[it]!!)
        }

        shape.getUniforms().map { (name, uniform) ->
            uniforms[nameLookup[name]!!] = uniform.getGLSLType()
        }

        return if (shape is ShapeContainer) {
            val children = shape.getChildren().map { compileShapeDistanceFunction(it) }
            (1 .. children.size).fold(func) { acc, it ->
                acc.replace("\$$it", children[it - 1])
            }
        }
        else {
            func
        }
    }

    private fun buildHeader(): String
            = headers.joinToString("\n\n") + "\n\n" + uniforms.map { (name, type) -> "uniform $type $name;" } .joinToString("\n")

    fun buildFragmentShader(shape: Shape): String {
        val content = compileShapeDistanceFunction(shape)
        val str = StringBuilder()
        str.append("#version 440 core\n\n")
        str.append(buildHeader())
        str.append("\n\nvoid main(void) {\n")
        // str.append("\t" + content)
        str.append("\tgl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);")
        str.append("\n}")
        return str.toString()
    }

    fun buildVertexShader(): String {
        val str = StringBuilder()
        str.append("#version 440 core\n\n")
        str.append("layout (location=0) in vec3 vertex;\n")
        str.append("layout (location=1) in vec2 vertex_uv;\n")
        str.append("\n")
        str.append("void main(void) {\n")
        str.append("\tgl_Position = vec4(vertex, 1);\n")
        str.append("\t\n")
        str.append("\t\n")
        str.append("}")
        return str.toString()
    }
}
