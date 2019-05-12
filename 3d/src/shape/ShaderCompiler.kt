package shape

import java.nio.file.Files
import java.nio.file.Paths

class ShaderCompiler {
    private val headers = HashSet<String>()
    private val uniforms = HashMap<String, String>()
    val lookup = UniformNameLookup()

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
            val transformationUniform = lookup.getTransformationUniformName(shape)
            func
                    .replace("\$transformation", transformationUniform)
                    .replace("\$ray_position", "($transformationUniform * ray_position)")
                    .replace("\$material", lookup.getMaterialUniformName(shape))
                    .replace("\$transformation_scale", "${transformationUniform}_scale")
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
              lookup.materialNames.map { (_, name) -> "uniform Material $name;" } .joinToString("\n") +
              "\n\n" +
              lookup.transformationNames.map { (_, name) -> "uniform mat4 $name; uniform float ${name}_scale;" } .joinToString("\n")

    fun buildBufferedFragmentShader(shape: Shape): String {
        val distance_function = compileShapeFunction(shape)
        return String(Files.readAllBytes(Paths.get("src/glsl/fragment-buffer.glsl")))
                .replace("/*\$header*/", buildHeader())
                .replace("/*\$distance_function*/", distance_function)
    }

    fun buildVertexShader(): String {
        return String(Files.readAllBytes(Paths.get("src/glsl/vertex.glsl")))
    }

    fun buildFragmentShader(shape: Shape): String {
        val distance_function = compileShapeFunction(shape)
        return String(Files.readAllBytes(Paths.get("src/glsl/fragment-screen.glsl")))
                .replace("/*\$header*/", buildHeader())
                .replace("/*\$distance_function*/", distance_function)
    }
}

class UniformNameLookup {
    val valueNames = HashMap<ShapeUniformValue, String>()
    val materialNames = HashMap<MaterialShape, String>()
    val transformationNames = HashMap<MaterialShape, String>()

    fun getValueUniformName(name: String, uniform: ShapeUniformValue): String {
        var i = 1
        while (valueNames.values.contains("${name}_$i")) ++i
        valueNames[uniform] = "${name}_$i"
        return "${name}_$i"
    }

    fun getMaterialUniformName(shape: MaterialShape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (materialNames.values.contains("${name}_${i}_material")) ++i
        materialNames[shape] = "${name}_${i}_material"
        return "${name}_${i}_material"
    }

    fun getTransformationUniformName(shape: MaterialShape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (transformationNames.values.contains("${name}_${i}_transformation")) ++i
        transformationNames[shape] = "${name}_${i}_transformation"
        return "${name}_${i}_transformation"
    }
}
