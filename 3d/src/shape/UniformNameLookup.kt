package shape

class UniformNameLookup {
    val valueNames = HashMap<ShapeUniformValue, String>()
    val materialNames = HashMap<MaterialShape, String>()
    val transformationNames = HashMap<MaterialShape, String>()
    val shapeNames = HashMap<Shape, String>()

    fun generateShapeName(shape: Shape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (shapeNames.values.contains("${name}_$i")) ++i
        shapeNames[shape] = "${name}_$i"
        return "${name}_$i"
    }

    fun generateValueUniformName(name: String, uniform: ShapeUniformValue): String {
        var i = 1
        while (valueNames.values.contains("${name}_$i")) ++i
        valueNames[uniform] = "${name}_$i"
        return "${name}_$i"
    }

    fun generateMaterialUniformName(shape: MaterialShape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (materialNames.values.contains("${name}_${i}_material")) ++i
        materialNames[shape] = "${name}_${i}_material"
        return "${name}_${i}_material"
    }

    fun generateTransformationUniformName(shape: MaterialShape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (transformationNames.values.contains("${name}_${i}_transformation")) ++i
        transformationNames[shape] = "${name}_${i}_transformation"
        return "${name}_${i}_transformation"
    }
}

fun loadUniformNameLookup(shape: Shape, lookup: UniformNameLookup = UniformNameLookup()): UniformNameLookup {
    shape.getUniforms().map { (name, uniform) -> name to lookup.generateValueUniformName(name, uniform) } .toMap()
    lookup.generateShapeName(shape)

    if (shape is ShapeContainer) {
        shape.getChildren().map { loadUniformNameLookup(it, lookup) }
    }
    else if (shape is MaterialShape) {
        lookup.generateTransformationUniformName(shape)
        lookup.generateMaterialUniformName(shape)
    }

    return lookup
}
