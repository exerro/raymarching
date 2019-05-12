package gl

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import util.vec2

class Display(var width: Int, var height: Int, val title: String = "Display") : GLResource {
    private var running = false
    private val heldMouseButtons = HashSet<Int>()
    private var lastMousePosition = vec2(0f, 0f)
    private var firstMousePosition = vec2(0f, 0f)
    private var onSetup: MutableList<() -> Unit> = ArrayList()
    private var setup = false
    private var window: Long = 0
    var onResizedCallback: ((Int, Int) -> Unit)? = null
    var onMouseDownCallback: ((vec2, Int) -> Unit)? = null
    var onMouseUpCallback: ((vec2, Int) -> Unit)? = null
    var onMouseMoveCallback: ((vec2, vec2) -> Unit)? = null
    var onMouseDragCallback: ((vec2, vec2, vec2, Set<Int>) -> Unit)? = null
    var onKeyPressedCallback: ((Int, Int) -> Unit)? = null
    var onKeyReleasedCallback: ((Int, Int) -> Unit)? = null
    var onTextInputCallback: ((String) -> Unit)? = null
    var onUpdateCallback: ((Float) -> Unit)? = null
    var onDrawCallback: (() -> Unit)? = null
    var onLoadCallback: (() -> Unit)? = null

    fun setMouseLocked(locked: Boolean) {
        whenSetup {
            glfwSetInputMode(window, GLFW_CURSOR, if (locked) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL)
        }
    }

    /**
     * Return true if the key given is currently held
     */
    fun isKeyDown(key: Int): Boolean = glfwGetKey(window, key) == GLFW_PRESS

    /**
     * Return true if the mouse button given is currently held
     */
    fun isMouseDown(button: Int): Boolean = glfwGetMouseButton(window, button) == GLFW_PRESS

    /**
     * Return true if any mouse button is currently held
     */
    fun isMouseDown(): Boolean = heldMouseButtons.isNotEmpty()

    /**
     * Gets the mouse position
     */
    fun getMousePosition(): vec2 {
        val w = DoubleArray(1)
        val h = DoubleArray(1)
        glfwGetCursorPos(window, w, h)
        return vec2(w[0].toFloat(), h[0].toFloat())
    }

    /**
     * Gets the size of the window
     */
    fun getWindowSize(): vec2 {
        val w = IntArray(1)
        val h = IntArray(1)
        glfwGetWindowSize(window, w, h)
        return vec2(w[0].toFloat(), h[0].toFloat())
    }

    /**
     * Closes the display
     */
    fun close() {
        running = false
    }

    fun run() {
        var lastUpdate = System.currentTimeMillis()

        setup()
        running = true

        // run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key
        while (running && !glfwWindowShouldClose(window)) {
            // set the clear colour to black and clear the framebuffer
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val t = System.currentTimeMillis()
            val dt = t - lastUpdate
            lastUpdate = t

            // call the update render callbacks
            onUpdateCallback?.invoke(dt / 1000f)
            onDrawCallback?.invoke()

            // swap the color buffers to present the content to the screen
            glfwSwapBuffers(window)

            // poll for window events
            // the key callback above will only be invoked during this call
            glfwPollEvents()
        }

        destroy()
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    override fun destroy() {
        // free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
    }

    /**
     * Creates a new display, using the standard GLFW init code found at https://www.lwjgl.org/guide
     */
    private fun setup() {
        GLFWErrorCallback.createPrint(System.err).set()

        // initialise GLFW
        if(!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE) // the window will focus when shown
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_CENTER_CURSOR , GLFW_TRUE) // the window will have the cursor centred

        window = glfwCreateWindow(width, height, title, NULL, NULL) // create the window
        if (window == NULL) throw RuntimeException("Failed to create the GLFW window")

        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        if (videoMode != null)
            glfwSetWindowPos(
                    window,
                    (videoMode.width() - getWindowSize().x.toInt()) / 2,
                    (videoMode.height() - getWindowSize().y.toInt()) / 2
            )

        glfwMakeContextCurrent(window) // make the OpenGL context current
        glfwSwapInterval(1) // enable v-sync
        glfwShowWindow(window) // make the window visible

        GL.createCapabilities() // makes OpenGL bindings available to use from LWJGL

        setCallbacks() // sets the GLFW callbacks to use the custom callbacks above
        updateGLViewport() // update the GL viewport to set it up initially

        onLoadCallback?.invoke() // call the loaded callback, if set
        onSetup.map { it() }
        setup = true
    }

    private fun setCallbacks() {
        // on window resize, update the GL viewport and call a resized callback, if set
        glfwSetWindowSizeCallback(window) { _, _, _ ->
            updateGLViewport()
            onResizedCallback?.invoke(width, height)
        }

        glfwSetKeyCallback(window) { _, key, _, action, mods ->
            if (action == GLFW_PRESS) onKeyPressedCallback?.invoke(key, mods)
            if (action == GLFW_RELEASE) onKeyReleasedCallback?.invoke(key, mods)
        }

        glfwSetCharCallback(window) { _, codepoint ->
            onTextInputCallback?.invoke(Character.toChars(codepoint).toString())
        }

        glfwSetCursorPosCallback(window) { _, _, _ ->
            val pos = getMousePosition()
            if (heldMouseButtons.isEmpty()) onMouseMoveCallback?.invoke(pos, lastMousePosition)
            else onMouseDragCallback?.invoke(pos, lastMousePosition, firstMousePosition, heldMouseButtons)
            lastMousePosition = pos
        }

        glfwSetMouseButtonCallback(window) { _, button, action, mods ->
            val pos = getMousePosition()

            if (action == GLFW_PRESS) {
                if (heldMouseButtons.isEmpty()) firstMousePosition = pos
                heldMouseButtons.add(button)
                onMouseDownCallback?.invoke(pos, mods)
            }
            else if (action == GLFW_RELEASE) {
                heldMouseButtons.remove(button)
                onMouseUpCallback?.invoke(pos, mods)
            }
        }
    }

    private fun updateGLViewport() {
        val width = IntArray(1)
        val height = IntArray(1)
        glfwGetFramebufferSize(window, width, height)
        glViewport(0, 0, width[0], height[0])
    }

    private fun whenSetup(func: () -> Unit) {
        if (setup) func()
        else onSetup.add(func)
    }
}