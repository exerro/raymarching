package shape

import java.nio.file.Files
import java.nio.file.Paths

class ShaderCompiler {
    private val headers = HashSet<String>()
    private val uniforms = HashMap<String, String>()
    private var isShaderGBuffered = false
    val lookup = UniformNameLookup()

    fun useGBuffer(use: Boolean = true) {
        isShaderGBuffered = use
    }

    fun buildVertexShader(): String {
        return String(Files.readAllBytes(Paths.get("src/glsl/vertex.glsl")))
    }

    fun buildFragmentShader(shape: Shape): String {
        generateNames(shape)
        val distance_function = compileShapeDistanceFunction(shape)
        val data_function = compileShapeDataFunction(shape)
        val shaderPath = if (isShaderGBuffered) "src/glsl/fragment-buffer.glsl" else "src/glsl/fragment-screen.glsl"
        return String(Files.readAllBytes(Paths.get(shaderPath)))
                .replace("/*\$header*/", buildHeader())
                .replace("0/*\$distance_function*/", distance_function)
                .replace("/*\$data_function*/", data_function)
    }

    private fun generateNames(shape: Shape) {
        shape.getUniforms().map { (name, uniform) -> name to lookup.getValueUniformName(name, uniform) } .toMap()

        if (shape is ShapeContainer) {
            shape.getChildren().map { generateNames(it) }
        }
        else if (shape is MaterialShape) {
            lookup.getTransformationUniformName(shape)
            lookup.getMaterialUniformName(shape)
        }
    }

    private fun compileShapeDistanceFunction(shape: Shape): String {
        val header = shape.getHeader()
        if (header != null) headers.add(header)
        val nameLookup = shape.getUniforms().map { (name, uniform) -> name to lookup.valueNames[uniform] } .toMap()
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
        else if (shape is MaterialShape) {
            val transformationUniform = lookup.transformationNames[shape]!!
            func
                    .replace("\$transformation", transformationUniform)
                    .replace("\$ray_position", "($transformationUniform * ray_position)")
                    .replace("\$transformation_scale", "${transformationUniform}_scale")
        }
        else {
            func
        }
    }

    private fun compileShapeDataFunction(shape: Shape): String {
        val header = shape.getHeader()
        if (header != null) headers.add(header)
        val nameLookup = shape.getUniforms().map { (name, uniform) -> name to lookup.valueNames[uniform] } .toMap()
        val func = nameLookup.keys.toList().fold(shape.getFunction()) { acc, it ->
            acc.replace("\$$it", nameLookup[it]!!)
        }

        shape.getUniforms().map { (name, uniform) ->
            uniforms[nameLookup[name]!!] = uniform.getGLSLType()
        }

        return if (shape is ShapeContainer) {
            val children = shape.getChildren().map { compileShapeDataFunction(it) }
            (1 .. children.size).fold(func) { acc, it ->
                acc.replace("\$$it", children[it - 1])
            }
        }
        else if (shape is MaterialShape) {
            val transformationUniform = lookup.transformationNames[shape]!!
            func
                    .replace("\$transformation", transformationUniform)
                    .replace("\$ray_position", "($transformationUniform * rp)")
                    .replace("\$material", lookup.materialNames[shape]!!)
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
