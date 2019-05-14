package raymarch

import shape.ShaderData
import shape.Shape
import shape.ShapeContainer

class UniformNameLookup {
    val valueNames = HashMap<ShaderData<*>, String>()
    val shapeNames = HashMap<Shape, String>()

    fun generateShapeName(shape: Shape): String {
        var i = 1
        val name = shape.javaClass.simpleName
        while (shapeNames.values.contains("${name}_$i")) ++i
        shapeNames[shape] = "${name}_$i"
        return "${name}_$i"
    }

    fun generateValueUniformName(name: String, uniform: ShaderData<*>): String {
        var i = 1
        while (valueNames.values.contains("${name}_$i")) ++i
        valueNames[uniform] = "${name}_$i"
        return "${name}_$i"
    }
}

fun loadUniformNameLookup(shape: Shape, lookup: UniformNameLookup = UniformNameLookup()): UniformNameLookup {
    shape.getUniforms() .filter { it.value.isDynamic() } .map { (name, uniform) -> name to lookup.generateValueUniformName(name, uniform) } .toMap()
    lookup.generateShapeName(shape)

    if (shape is ShapeContainer) {
        shape.getChildren().map { loadUniformNameLookup(it, lookup) }
    }

    return lookup
}
