import lang.StringTokenStream

object ParseTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val stream = StringTokenStream("Hello world\n this is a thing")

        stream.readToken()
        println(stream.readToken().position.to(stream.readToken().position).getErrorString("Oh noes!", StringTokenStream("Hello world\n this is a thing")))
    }
}
