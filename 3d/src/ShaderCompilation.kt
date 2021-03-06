import lwaf_core.vec3
import raymarch.RaymarchShaderCompiler
import shape.Shape
import shape.container.ShapeUnion
import raymarch.loadUniformNameLookup
import shape.primitive.Sphere
import shape.setTranslation

object ShaderCompilation {
    @JvmStatic
    fun main(args: Array<String>) {
        val compiler = RaymarchShaderCompiler()

        var stuff = arrayOf<Shape>()
        (1 .. 10).map { a -> (1 .. 10).map { b -> Pair(a, b) } }.flatten().map { (a, b) ->
            stuff = arrayOf(*stuff, Sphere(0.4f).setTranslation(vec3(a.toFloat(), b.toFloat(), 0f)))
        }

        val shape = ShapeUnion(*stuff)

        loadUniformNameLookup(shape, compiler.lookup)

        compiler.generateFragmentShaderStart()
        compiler.generateDefaultFragmentShaderMain()
        compiler.generateFragmentShaderUniforms(shape)
        compiler.generateDistanceFunctionHeaders(shape)
        compiler.generateMaterialFunctionHeaders(shape)
        compiler.generateFunctionDefinitions(shape)

        println(compiler.getText())
    }
}
