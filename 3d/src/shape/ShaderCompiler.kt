package shape

import java.nio.file.Files
import java.nio.file.Paths

class ShaderCompiler {
    private val headers = HashSet<String>()
    private val uniforms = HashMap<String, String>()
    val uniformValueLookups = HashMap<ShapeUniformValue, String>()

    private fun getName(name: String, uniform: ShapeUniformValue): String {
        var i = 1
        while (uniformValueLookups.values.contains("${name}_$i")) ++i
        uniformValueLookups[uniform] = "${name}_$i"
        println(uniformValueLookups)
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
        val distance_function = compileShapeDistanceFunction(shape).replace("\$ray_position", "rp")
        return String(Files.readAllBytes(Paths.get("src/glsl/fragment.glsl")))
                .replace("/*\$header*/", buildHeader())
                .replace("0/*\$distance_function*/", distance_function)
    }

    fun buildVertexShader(): String {
        return String(Files.readAllBytes(Paths.get("src/glsl/vertex.glsl")))
    }
}
