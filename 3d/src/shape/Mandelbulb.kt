package shape

// TODO: WIP

val mandelbulb = "const float Power = 1.0;" +
        "float DE(vec3 pos) {\n" +
        "\tvec3 z = pos;\n" +
        "\tfloat dr = 1.0;\n" +
        "\tfloat r = 0.0;\n" +
        "\tfor (int i = 0; i < 150 ; i++) {\n" +
        "\t\tr = length(z);\n" +
        "\t\tif (r>200) break;\n" +
        "\t\t\n" +
        "\t\t// convert to polar coordinates\n" +
        "\t\tfloat theta = acos(z.z/r);\n" +
        "\t\tfloat phi = atan(z.y,z.x);\n" +
        "\t\tdr =  pow( r, Power-1.0)*Power*dr + 1.0;\n" +
        "\t\t\n" +
        "\t\t// scale and rotate the point\n" +
        "\t\tfloat zr = pow( r,Power);\n" +
        "\t\ttheta = theta*Power;\n" +
        "\t\tphi = phi*Power;\n" +
        "\t\t\n" +
        "\t\t// convert back to cartesian coordinates\n" +
        "\t\tz = zr*vec3(sin(theta)*cos(phi), sin(phi)*sin(theta), cos(theta));\n" +
        "\t\tz+=pos;\n" +
        "\t}\n" +
        "\treturn 0.5*log(r)*r/dr;\n" +
        "}"

class Mandelbulb : Shape() {
    override fun getDistanceFunctionHeader(): String?
            = mandelbulb

    override fun getDistanceFunction(): String
            = "DE(\$ray_position.xyz)"

    override fun getUniforms(): Map<String, ShapeUniformValue>
            = mapOf()

}