package raymarch

data class RenderOptions(
        private var shadowsEnabled: Boolean = false,
        private var maxReflectionCount: Int = 0
) {
    fun shadowsEnabled(): Boolean = shadowsEnabled
    fun maxReflectionCount(): Int = maxReflectionCount

    fun enableShadows(): RenderOptions {
        shadowsEnabled = true
        return this
    }

    fun disableShadows(): RenderOptions {
        shadowsEnabled = false
        return this
    }

    fun enableReflections(count: Int = 1): RenderOptions {
        maxReflectionCount = count
        return this
    }

    fun disableReflections(): RenderOptions {
        maxReflectionCount = 0
        return this
    }
}
