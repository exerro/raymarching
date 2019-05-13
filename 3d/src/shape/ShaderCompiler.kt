package shape

import util.RootBuilder
import util.*
import kotlin.math.max

class ShaderCompiler: RootBuilder() {
    private val MAX_ITERATIONS = 500
    private val MAX_DISTANCE= 500
    private val INTERSECTION_DISTANCE= 0.01f
    private val EPSILON = 0.0001f
    val lookup = UniformNameLookup()

    fun generateFragmentShaderStart(): ShaderCompiler
            =appendLine("#version 440 core")
            .appendLine()
            .appendConstant("vec3", "LIGHT_DIRECTION", "normalize(-vec3(2, 3, 1))")
            .appendLine()
            .appendVertexParameter("vec2", "uv")
            .appendLine()
            .appendUniform("vec3", "cameraPosition")
            .appendUniform("mat3", "transform")
            .appendUniform("float", "FOV")
            .appendUniform("float", "aspectRatio")
            .appendLine()
            .appendStruct("Material") { block -> block
                    .appendDeclaration("vec3", "colour")
            }
            .appendLine()
            .appendStruct("MaterialDistance") { block -> block
                    .appendDeclaration("Material", "material")
                    .appendDeclaration("float", "dist")
            }
            .appendLine()
            .appendFunctionDeclaration("float", "distanceFunction", Pair("vec3", "p"))
            .appendFunctionDeclaration("MaterialDistance", "materialFunction", Pair("vec3", "p"))
            .appendLine()
            // raymarch returns a vector: (distance to object, minimum distance during march, maximum distance during march)
            .appendFunction("vec4", "raymarch", Pair("vec3", "position"), Pair("vec3", "direction")) { block -> block
                    .appendDefinition("int", "iterations", 0)
                    .appendDefinition("float", "total_distance", 0)
                    .appendDefinition("float", "min_distance", 1000000)
                    .appendDefinition("float", "max_distance", 0)
                    .appendDeclaration("float", "distance")
                    .appendLine()
                    .appendWhile("iterations < $MAX_ITERATIONS && total_distance < $MAX_DISTANCE") { wblock -> wblock
                            .appendStatement("distance = distanceFunction(position)")
                            .appendStatement("total_distance += distance")
                            .appendStatement("position += direction * distance")
                            .appendLine()
                            .appendLine("if (distance < min_distance) min_distance = distance;")
                            .appendLine("if (distance > max_distance) max_distance = distance;")
                            .appendLine("if (abs(distance) < $INTERSECTION_DISTANCE) break;")
                            .appendLine()
                            .appendStatement("++iterations")
                    }
                    .appendLine()
                    .appendReturn("vec4(total_distance, min_distance, max_distance, distance)")
//                    .appendReturn("position")
            }
            .appendLine()
            // getMaterialAt returns the material at a point in 3D space
            .appendFunction("Material", "getMaterialAt", Pair("vec3", "position")) { block -> block
                    .appendReturn("materialFunction(position).material")
            }
            .appendLine()
            // estimateNormal estimates... you guessed it... the normal (distance gradient at a point in 3D space)
            .appendFunction("vec3", "estimateNormal", Pair("vec3", "p")) { block -> block
                    .appendReturn("normalize(vec3(" +
                            "${distanceFunctionCall("vec3(p.x + $EPSILON, p.y, p.z)")} - ${distanceFunctionCall("vec3(p.x - $EPSILON, p.y, p.z)")}, " +
                            "${distanceFunctionCall("vec3(p.x, p.y + $EPSILON, p.z)")} - ${distanceFunctionCall("vec3(p.x, p.y - $EPSILON, p.z)")}, " +
                            "${distanceFunctionCall("vec3(p.x, p.y, p.z + $EPSILON)")} - ${distanceFunctionCall("vec3(p.x, p.y, p.z - $EPSILON)")}))")
            }
            .appendLine()
            // calculateLightingAt determines the scalar lighting value of a point in 3D space using its position and normal
            .appendFunction("float", "calculateLightingAt", Pair("vec3", "position"), Pair("vec3", "normal")) { block -> block
                    .appendDefinition("vec3", "reflection", "normalize(reflect(position - cameraPosition, normal))")
                    .appendDefinition("float", "ambient", "0.3")
                    .appendDefinition("float", "diffuse", "max(0, dot(normal, -LIGHT_DIRECTION)) * 0.7")
                    .appendDefinition("float", "specular", "pow(max(0, dot(reflection, -LIGHT_DIRECTION)), 30) * 0.5")
                    .appendReturn("ambient + diffuse + specular")
            }
            .appendLine()
            // calculatePointColour determines the colour of a point in 3D space using the material information and lighting
            .appendFunction("vec3", "calculatePointColour", Pair("vec3", "position")) { block -> block
                    .appendDefinition("vec3", "normal", "estimateNormal(position)")
                    .appendDefinition("float", "lighting", "calculateLightingAt(position, normal)")
                    .appendDefinition("Material", "material", "getMaterialAt(position)")
                    .appendReturn("material.colour * lighting")
            }
            .appendLine()

    fun generateFragmentShaderMain(): ShaderCompiler
            =appendFunction("void", "main") { block -> block
            .appendDefinition("vec3", "direction", "transform * normalize(vec3((2 * uv - vec2(1, 1)) * vec2(aspectRatio, 1), -1/tan(FOV/2)))")
            .appendDefinition("vec4", "result", "raymarch(cameraPosition, direction)")
            .appendLine()
//            .appendStatement("gl_FragColor = vec4(direction, 1)") // TODO
//            .appendStatement("return")
            .appendLine()
            .appendIf("abs(result.w) <= $INTERSECTION_DISTANCE") { iblock -> iblock
                    .appendStatement("gl_FragColor = vec4(calculatePointColour(cameraPosition + direction * result.x), 1)")
                    .appendStatement("return")
            }
            .appendLine()
            .appendStatement("gl_FragColor = vec4(0, 0, 0, 0)")
    }

    fun generateFragmentShaderUniforms(shape: Shape): ShaderCompiler
            = generateFragmentShaderUniforms(shape, TransformInfo(shape.transform))

    private fun generateFragmentShaderUniforms(shape: Shape, ti: TransformInfo): ShaderCompiler
            =appendLine("// uniforms for ${lookup.shapeNames[shape]!!}")
            .foreach(shape.getUniforms().values.filter { it.isDynamic() }) { block, uniform -> block
                    .appendUniform(uniform.getGLSLType(), lookup.valueNames[uniform]!!)
            }
            .conditional(shape is ShapeContainer) { block -> block
                    .foreach((shape as ShapeContainer).getChildren()) { _, child ->
                            generateFragmentShaderUniforms(child, TransformInfo(child.transform, ti))
                    }
            }
            .conditional(shape is MaterialShape) { block -> block
                    .conditional((shape as MaterialShape).getMaterial().colour.isDynamic()) { mblock -> mblock
                            .appendUniform("Material", "${lookup.shapeNames[shape]!!}_material")
                    }
                    .conditional(ti.dynamicOrRotated) { tblock -> tblock
                                .appendUniform("mat4", "${lookup.shapeNames[shape]!!}_transform")
                                .conditional(ti.dynamicScale) { sblock -> sblock
                                    .appendUniform("float", "${lookup.shapeNames[shape]!!}_divisor")
                                }
                    }
            }

    fun generateDistanceFunctionHeaders(shape: Shape): ShaderCompiler
            =(shape.compileDistanceFunctionHeader(this) ?: this)
            .conditional(shape is ShapeContainer) { builder: ShaderCompiler -> builder
                    .foreach((shape as ShapeContainer).getChildren()) { b, it -> it.compileDistanceFunctionHeader(b)?.appendLine() ?: b }
            }

    fun generateMaterialFunctionHeaders(shape: Shape): ShaderCompiler
            =(shape.compileMaterialFunctionHeader(this) ?: this)
            .conditional(shape is ShapeContainer) { builder -> builder
                    .foreach((shape as ShapeContainer).getChildren()) { b, it -> it.compileMaterialFunctionHeader(b)?.appendLine() ?: b }
            }

    fun generateFunctionDefinitions(shape: Shape): ShaderCompiler
            =appendFunction("float", "distanceFunction", Pair("vec3", "position")) { block -> block.appendReturn(generateDistanceFunction(shape, TransformInfo(shape.transform))) }
            .appendLine()
            .appendFunction("MaterialDistance", "materialFunction", Pair("vec3", "position")) { block -> block.appendReturn(generateMaterialFunction(shape, TransformInfo(shape.transform))) }

    private fun generateDistanceFunction(shape: Shape, ti: TransformInfo): String
            = if (shape is MaterialShape) {
                transformDivisor(formatFunctionString(shape, shape.getDistanceFunction(), ti, ::generateDistanceFunction), shape, ti)
            } else {
                formatFunctionString(shape, shape.getDistanceFunction(), ti, ::generateDistanceFunction)
            }

    private fun generateMaterialFunction(shape: Shape, ti: TransformInfo): String
            =formatFunctionString(shape, shape.getMaterialFunction(), ti, ::generateMaterialFunction)

    private fun formatFunctionString(shape: Shape, text: String, ti: TransformInfo, buildChild: (Shape, TransformInfo) -> String): String {
        val uniformsRemapped = shape.getUniforms().toList().fold(text) { acc, (name, value) ->
            acc.replace("\$$name", if (value.isDynamic()) lookup.valueNames[value]!! else value.getGLSLValue())
        }
        val propertiesRemapped = uniformsRemapped
                .replace("\$position", transformPosition(shape, ti))

        return if (shape is ShapeContainer) {
            val children = shape.getChildren().map { child -> buildChild(child, TransformInfo(child.transform, ti)) }

            (children.size downTo 1).fold(propertiesRemapped) { acc, it ->
                acc.replace("\$$it", children[it - 1])
            }
        }
        else if (shape is MaterialShape) {
            val materialRemapped = propertiesRemapped
                    .replace("\$material", if (shape.getMaterial().colour.isDynamic()) { shape.getMaterial().colour.changeHandled(); "${lookup.shapeNames[shape]!!}_material" } else shape.getMaterial().colour.getGLSLValue())

            if (materialRemapped.contains("\$distance")) {
                materialRemapped.replace("\$distance", generateDistanceFunction(shape, ti))
            }
            else {
                materialRemapped
            }
        }
        else {
            "wtff"
        }
    }

    private fun transformPosition(shape: Shape, ti: TransformInfo): String {
        return if (ti.dynamicOrRotated) {
            "(${lookup.shapeNames[shape]!!}_transform * vec4(position, 1)).xyz"
        }
        else if (ti.scaled && ti.translated) {
            val pos = ti.getTranslation()
            val scale = ti.getScale()
            "((position " +
                    "- vec3(${pos.x}, ${pos.y}, ${pos.z})) " +
                    "* vec3(${1/scale.x}, ${1/scale.y}, ${1/scale.z}))"
        }
        else if (ti.scaled) {
            val scale = ti.getScale()
            "(position " +
                    "* vec3(${1/scale.x}, ${1/scale.y}, ${1/scale.z}))"
        }
        else if (ti.translated) {
            val pos = ti.getTranslation()
            "(position " +
                    "- vec3(${pos.x}, ${pos.y}, ${pos.z})) "
        }
        else {
            "position"
        }
    }

    private fun transformDivisor(value: String, shape: MaterialShape, ti: TransformInfo): String {
        return when {
            ti.dynamicScale -> "($value / ${lookup.shapeNames[shape]!!}_divisor)"
            ti.scaled -> "($value * ${1/max(max(1/shape.transform.scale.x, 1/shape.transform.scale.y), 1/shape.transform.scale.z)})"
            else -> value
        }
    }

    private fun distanceFunctionCall(point: String): String
            = "distanceFunction($point)"
}

