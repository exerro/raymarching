package shape

import java.nio.file.Files
import java.nio.file.Paths

class ShaderCompiler {
    private val headers = HashSet<String>()
    private val uniforms = HashMap<String, String>()
    val lookup = NameLookup()

    private fun compileShapeFunction(shape: Shape): String {
        val header = shape.getHeader()
        if (header != null) headers.add(header)
        val nameLookup = shape.getUniforms().map { (name, uniform) -> name to lookup.getValueUniformName(name, uniform) } .toMap()
        val func = nameLookup.keys.toList().fold(shape.getFunction()) { acc, it ->
            acc.replace("\$$it", nameLookup[it]!!)
        }

        shape.getUniforms().map { (name, uniform) ->
            uniforms[nameLookup[name]!!] = uniform.getGLSLType()
        }

        return if (shape is ShapeContainer) {
            val children = shape.getChildren().map { compileShapeFunction(it) }
            (1 .. children.size).fold(func) { acc, it ->
                acc.replace("\$$it", children[it - 1])
            }
        }
        else if (shape is MaterialShape) {
            func.replace("\$material", lookup.getMaterialUniformName(shape))
        }
        else {
            func
        }
    }

    private fun buildHeader(): String
            = headers.joinToString("\n\n") +
              "\n\n" +
              uniforms.map { (name, type) -> "uniform $type $name;" } .joinToString("\n") +
              "\n\n" +
              lookup.materials.map { (_, name) -> "uniform Material $name;" } .joinToString("\n")

    fun buildFragmentShader(shape: Shape): String {
        val distance_function = compileShapeFunction(shape).replace("\$ray_position", "ray_position")
        return String(Files.readAllBytes(Paths.get("src/glsl/fragment.glsl")))
                .replace("/*\$header*/", buildHeader())
                .replace("0/*\$distance_function*/", distance_function)
    }

    fun buildVertexShader(): String {
        return String(Files.readAllBytes(Paths.get("src/glsl/vertex.glsl")))
    }
}

class NameLookup {
    val uniformValues = HashMap<ShapeUniformValue, String>()
    val materials = HashMap<MaterialShape, String>()

    fun getValueUniformName(name: String, uniform: ShapeUniformValue): String {
        var i = 1
        while (uniformValues.values.contains("${name}_$i")) ++i
        uniformValues[uniform] = "${name}_$i"
        return "${name}_$i"
    }

    fun getMaterialUniformName(shape: MaterialShape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (materials.values.contains("${name}_${i}_material")) ++i
        materials[shape] = "${name}_${i}_material"
        return "${name}_${i}_material"
    }
}
