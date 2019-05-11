package display

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import gl.GLResource

class Display : GLResource {
    val window: Long
    val width: Int
    val height: Int

    /**
     * Creates a new display, using the standard GLFW init code found at https://www.lwjgl.org/guide
     */
    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height

        GLFWErrorCallback.createPrint(System.err).set()

        if(!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")

        // configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // create the window
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL)

        if (window == NULL)
            throw RuntimeException("Failed to create the GLFW window")

        glfwSetWindowSize(window, width, height)

        // get the thread stack and push a new frame
        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)

            // get the resolution of the primary monitor
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

            // center the window
            if (videoMode != null)
                glfwSetWindowPos(
                        window,
                        (videoMode.width() - pWidth.get(0)) / 2,
                        (videoMode.height() - pHeight.get(0)) / 2
                )
        }

        // make the OpenGL context current
        glfwMakeContextCurrent(window)
        // enable v-sync
        glfwSwapInterval(1)

        // make the window visible
        glfwShowWindow(window)

        // "This line is critical for LWJGL's inter-operation with GLFW's
        //  OpenGL context, or any context that is managed externally.
        //  LWJGL detects the context that is current in the current thread,
        //  creates the GLCapabilities instance and makes the OpenGL
        //  bindings available for use."
        GL.createCapabilities()
    }

    fun isKeyDown(key: Int): Boolean = glfwGetKey(window, key) == GLFW_PRESS

    fun loop(render: () -> Unit) {
        // set the clear colour to black
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glViewport(0, 0, width, height)

        // run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key
        while (!glfwWindowShouldClose(window)) {
            // clear the framebuffer
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // call the render callback
            render()

            // swap the color buffers to present the content to the screen
            glfwSwapBuffers(window)

            // poll for window events
            // he key callback above will only be invoked during this call
            glfwPollEvents()
        }
    }

    fun close() {
        // terminate GLFW and free the error callback
        destroy()
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    override fun destroy() {
        // free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
    }
}