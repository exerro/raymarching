import shape.ShaderCompiler
import shape.Shape
import shape.container.ShapeUnion
import shape.loadUniformNameLookup
import shape.primitive.Box
import shape.primitive.Sphere
import util.vec3

object ShaderCompilation {
    @JvmStatic
    fun main(args: Array<String>) {
        val compiler = ShaderCompiler()

        var stuff = arrayOf<Shape>()
        (1 .. 10).map { a -> (1 .. 10).map { b -> Pair(a, b) } }.flatten().map { (a, b) ->
            stuff = arrayOf(*stuff, Sphere(0.4f).setTranslation(vec3(a.toFloat(), b.toFloat(), 0f)))
        }

        val shape = ShapeUnion(*stuff)

        loadUniformNameLookup(shape, compiler.lookup)

        compiler.generateFragmentShaderStart()
        compiler.generateFragmentShaderMain()
        compiler.generateFragmentShaderUniforms(shape)
        compiler.generateDistanceFunctionHeaders(shape)
        compiler.generateMaterialFunctionHeaders(shape)
        compiler.generateFunctionDefinitions(shape)

        println(compiler.getText())
    }
}
