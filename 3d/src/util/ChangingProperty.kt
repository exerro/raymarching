package util

open class ChangingProperty {
    private var changed = true

    fun notifyChanged() { changed = true }
    fun changeHandled() { changed = false }
    fun hasChanged(): Boolean = changed
}