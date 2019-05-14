package raymarch

data class RenderOptions(
        private var shadowsEnabled: Boolean = false
) {
    fun shadowsEnabled(): Boolean = shadowsEnabled

    fun enableShadows(): RenderOptions {
        shadowsEnabled = true
        return this
    }
}
