package raymarch

import shape.*
import util.RootBuilder
import util.*
import kotlin.math.max

class RaymarchShaderCompiler: RootBuilder() {
    private val MAX_ITERATIONS = 500
    private val MAX_DISTANCE= 5000
    private val INTERSECTION_DISTANCE= 0.01f
    private val EPSILON = 0.001f
    private lateinit var options: RenderOptions
    private val compiledShapeClassDistanceHeaders = HashSet<String>()
    private val compiledShapeClassMaterialHeaders = HashSet<String>()
    val lookup = UniformNameLookup()

    fun setOptions(options: RenderOptions): RaymarchShaderCompiler {
        this.options = options
        return this
    }

    fun generateFragmentShaderStart(): RaymarchShaderCompiler
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
                    .appendDeclaration("float", "reflectivity")
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
                    .appendWhile("iterations < $MAX_ITERATIONS && total_distance < $MAX_DISTANCE") {
                            this.appendStatement("distance = distanceFunction(position)")
                            .appendStatement("total_distance += distance")
                            .appendStatement("position += direction * distance")
                            .appendLine()
                            .appendLine("if (distance < -$INTERSECTION_DISTANCE) return vec4(total_distance - distance, min_distance, max_distance, 0);")
//                            .appendLine("if (distance < min_distance) min_distance = distance;")
//                            .appendLine("if (distance > max_distance) max_distance = distance;")
                            .appendLine("if (distance < $INTERSECTION_DISTANCE) break;")
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
            // estimateNormal estimates... you guessed it... the normal (distance gradient at a point in 3D space)
            .appendFunction("float", "calculateShadowFactor", Pair("vec3", "position"), Pair("vec3", "normal")) { block -> block
                    .conditional(options.shadowsEnabled()) { sblock -> sblock
                            .appendIf("dot(normal, -LIGHT_DIRECTION) > 0") { iblock -> iblock
                                    .appendDefinition("vec4", "trace", "raymarch(position - LIGHT_DIRECTION * ${INTERSECTION_DISTANCE * 10}, -LIGHT_DIRECTION)")
                                    .appendReturn("step(${MAX_DISTANCE - 1}, trace.x)")
                            }
                            .appendReturn(0)
                        // motherfucking shadows fuck everything aaaagh
//                            .appendReturn("distanceFunction(position + normal * 0.1) / 0.1")
//                            .appendDefinition("vec4", "trace_fine1", "raymarch(position - LIGHT_DIRECTION / dot(-LIGHT_DIRECTION, normal), -LIGHT_DIRECTION)")
//                            .appendReturn("distanceFunction(position + normal * 0.1) / 0.1")
//                            .appendDefinition("vec4", "trace_fine", "raymarch(position - LIGHT_DIRECTION / dot(-LIGHT_DIRECTION, normal), -LIGHT_DIRECTION)")
//                            .appendReturn("pow(clamp(trace_fine.y, 0, 1), 3)")
                    }
                    .conditional(!options.shadowsEnabled()) { sblock -> sblock
                            .appendReturn("1")
                    }
            }
            .appendLine()
            // calculateLightingAt determines the scalar lighting value of a point in 3D space using its position and normal
            .appendFunction("float", "calculateLightingAt", Pair("vec3", "position"), Pair("vec3", "normal")) { block -> block
                    .appendDefinition("vec3", "reflection", "normalize(reflect(position - cameraPosition, normal))")
                    .appendDefinition("float", "ambient", "0.3")
                    .appendDefinition("float", "diffuse", "max(0, dot(normal, -LIGHT_DIRECTION)) * 0.7")
                    .appendDefinition("float", "shadowFactor", "calculateShadowFactor(position, normal)")
                    .appendReturn("ambient + shadowFactor * diffuse")
            }
            .appendLine()
            // calculatePointColour determines the colour of a point in 3D space using the material information and lighting
            .appendFunction("vec3", "calculatePointColour", Pair("vec3", "position"), Pair("Material", "material"), Pair("vec3", "normal")) { block -> block
                    .appendDefinition("float", "lighting", "calculateLightingAt(position, normal)")
                    .appendReturn("material.colour * lighting")
            }
            .appendLine()

    fun generateReflectionFragmentShaderMain(): RaymarchShaderCompiler
            =appendFunction("void", "main") { block -> block
            .appendDefinition("vec3", "position", "cameraPosition")
            .appendDefinition("vec3", "direction", "transform * normalize(vec3((2 * uv - vec2(1, 1)) * vec2(aspectRatio, 1), -1/tan(FOV/2)))")
            .appendDefinition("vec4", "computedColour", "vec4(0)")
            .appendDefinition("int", "reflectionBounces", "0")
            .appendLine()
            .appendWhile("computedColour.w < 1 && reflectionBounces <= ${options.maxReflectionCount()}") {
                    this.appendDefinition("vec4", "result", "raymarch(position, direction)")
                    .appendLine()
                    .appendLine()
                    .appendIf("abs(result.w) <= $INTERSECTION_DISTANCE") { iblock -> iblock
                            .appendStatement("position += direction * result.x")
                            .appendDefinition("Material", "material", "materialFunction(position).material")
                            .appendDefinition("vec3", "normal", "estimateNormal(position)")
                            .appendDefinition("float", "reflectivity", "material.reflectivity")
                            .appendDefinition("float", "lighting", "calculateLightingAt(position, normal)")
                            .appendDefinition("vec3", "materialColour", "material.colour * lighting")
                            .appendStatement("computedColour += vec4(materialColour, 1) * (1 - reflectivity) * (1 - computedColour.w)")
                            .appendStatement("++reflectionBounces")
                            .appendStatement("direction = reflect(direction, normal)")
                            .appendStatement("position += direction * ${INTERSECTION_DISTANCE * 10}")
                            .appendStatement("continue")
                    }
                    .appendStatement("computedColour += vec4(0.7, 0.8, 0.9, 1) * (direction.y + 1) * 0.5 * (1 - computedColour.w)") // ~skybox
                    .appendStatement("break")
            }
            .appendLine()
            .appendIf("computedColour.w == 0") { iblock -> iblock
                    .appendStatement("computedColour == vec4(0.7, 0.8, 0.9, 1)")
            }
            .appendLine()
            .appendStatement("gl_FragColor = vec4(computedColour.xyz, 1)")
            .appendStatement("gl_FragColor += vec4(vec3(pow(1 - computedColour.w, 0.2)), 0) * pow(max(0, dot(-LIGHT_DIRECTION, direction)), 400)")
    }

    fun generateDefaultFragmentShaderMain(): RaymarchShaderCompiler
            =appendFunction("void", "main") { block -> block
            .appendDefinition("vec3", "direction", "transform * normalize(vec3((2 * uv - vec2(1, 1)) * vec2(aspectRatio, 1), -1/tan(FOV/2)))")
            .appendDefinition("vec4", "result", "raymarch(cameraPosition, direction)")
            .appendLine()
            .appendLine()
            .appendIf("abs(result.w) <= $INTERSECTION_DISTANCE") { iblock -> iblock
                    .appendDefinition("vec3", "position", "cameraPosition + direction * result.x")
                    .appendDefinition("vec3", "normal", "estimateNormal(position)")
                    .appendDefinition("Material", "material", "getMaterialAt(position)")
                    .appendStatement("gl_FragColor = vec4(calculatePointColour(position, material, normal), 1)")
                    .appendStatement("return")
            }
            .appendLine()
            .appendStatement("gl_FragColor = vec4(0.7, 0.8, 0.9, 1) * max(0, (direction.y + 0.2) * 0.8)")
    }

    fun generateMinDistanceFragmentShaderMain(): RaymarchShaderCompiler
            =appendFunction("void", "main") { block -> block
            .appendDefinition("vec3", "direction", "transform * normalize(vec3((2 * uv - vec2(1, 1)) * vec2(aspectRatio, 1), -1/tan(FOV/2)))")
            .appendDefinition("vec4", "result", "raymarch(cameraPosition, direction)")
            .appendLine()
//            .appendStatement("gl_FragColor = vec4(direction, 1)") // TODO
//            .appendStatement("return")
            .appendLine()
            .appendStatement("gl_FragColor = vec4(vec3(result.y), 1)")
    }

    fun generateFragmentShaderUniforms(shape: Shape): RaymarchShaderCompiler
            = generateFragmentShaderUniforms(shape, TransformInfo(shape.transform))

    private fun generateFragmentShaderUniforms(shape: Shape, ti: TransformInfo): RaymarchShaderCompiler
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

    fun generateDistanceFunctionHeaders(shape: Shape): RaymarchShaderCompiler
            =conditional(!compiledShapeClassDistanceHeaders.contains(shape.javaClass.name)) { builder ->
                    compiledShapeClassDistanceHeaders.add(shape.javaClass.name)
                    shape.compileDistanceFunctionHeader(builder) ?: builder
            }
            .conditional(shape is ShapeContainer) { this
                    .foreach((shape as ShapeContainer).getChildren()) { _, it -> generateDistanceFunctionHeaders(it) }
            }

    fun generateMaterialFunctionHeaders(shape: Shape): RaymarchShaderCompiler
            =conditional(!compiledShapeClassMaterialHeaders.contains(shape.javaClass.name)) { builder ->
                    compiledShapeClassMaterialHeaders.add(shape.javaClass.name)
                    (shape.compileMaterialFunctionHeader(builder) ?: builder).appendLine()
            }
            .conditional(shape is ShapeContainer) { this
                    .foreach((shape as ShapeContainer).getChildren()) { _, it -> generateMaterialFunctionHeaders(it) }
            }

    fun generateFunctionDefinitions(shape: Shape): RaymarchShaderCompiler
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
        val propertiesRemapped = (if (uniformsRemapped.contains("\$distance")) uniformsRemapped.replace("\$distance", generateDistanceFunction(shape, ti)) else uniformsRemapped)
                .replace("\$position", transformPosition(shape, ti))

        return if (shape is ShapeContainer) {
            val children = shape.getChildren().map { child -> buildChild(child, TransformInfo(child.transform, ti)) }

            (children.size downTo 1).fold(propertiesRemapped) { acc, it ->
                acc.replace("\$$it", children[it - 1])
            }
        }
        else if (shape is MaterialShape) {
            propertiesRemapped
                    .replace("\$material", if (shape.getMaterial().isDynamic()) { shape.getMaterial().colour.changeHandled(); "${lookup.shapeNames[shape]!!}_material" } else shape.getMaterial().getGLSLValue())
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
            ti.scaled -> "($value * ${max(max(ti.getScale().x, ti.getScale().y), ti.getScale().z)})"
            else -> value
        }
    }

    private fun distanceFunctionCall(point: String): String
            = "distanceFunction($point)"
}

