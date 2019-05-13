package util

object Logging
private val enabled = HashSet<LogType>()

fun Logging.enable(logType: LogType) {
    enabled.add(logType)
}

fun Logging.disable(logType: LogType) {
    enabled.remove(logType)
}

fun Logging.log(logType: LogType = LogType.INFO, log: () -> String) {
    if (!enabled.contains(logType)) return
    println(log())
}

enum class LogType {
    INFO,
    SHADER_UNIFORM,
    SHADER_COMPILE,
    WARNING,
    ERROR
}
