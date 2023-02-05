package cc.polyfrost.polyui.renderer.impl

import cc.polyfrost.polyui.PolyUI
import cc.polyfrost.polyui.renderer.Window
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform

class GLWindow(title: String, width: Int, height: Int, resizeable: Boolean = true, decorated: Boolean = true) :
    Window(title, width, height) {
    val handle: Long
    var fps: Int = 0
    lateinit var polyUI: PolyUI

    init {
        GLFWErrorCallback.createPrint().set()
        if (!glfwInit()) throw RuntimeException("Failed to init GLFW")
        if (Platform.get() === Platform.MACOSX) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        }
        if (!resizeable) glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
        if (!decorated) glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE)

        handle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (handle == MemoryUtil.NULL) {
            glfwTerminate()
            throw RuntimeException("Failed to create the window.")
        }

        glfwMakeContextCurrent(handle)
        createCapabilities()
        glfwSetTime(0.0)
        glfwSwapInterval(0)
    }

    override fun createCallbacks() {
        // Add some callbacks for window resizing and content scale
        glfwSetFramebufferSizeCallback(handle) { _, width, height ->
            polyUI.onResize(width, height)
            // decreases the wierd effects
            polyUI.render()
        }

        glfwSetMouseButtonCallback(handle) { _, button, action, _ ->
            if (action == GLFW_PRESS) {
                polyUI.eventManager.onMousePressed(button)
            } else if (action == GLFW_RELEASE) {
                polyUI.eventManager.onMouseReleased(button)
            }
        }

        glfwSetCursorPosCallback(handle) { _, x, y ->
            polyUI.eventManager.setMousePosAndUpdate(x.toFloat(), y.toFloat())
        }

        glfwSetKeyCallback(handle) { _, key, _, action, _ ->
            if (action == GLFW_PRESS) {
                polyUI.eventManager.onKeyPressed(key)
            } else if (action == GLFW_RELEASE) {
                polyUI.eventManager.onKeyReleased(key)
            }
        }

        glfwSetScrollCallback(handle) { _, _, yoffset ->
            polyUI.eventManager.onMouseScrolled(yoffset.toInt())
        }
    }

    override fun open(polyUI: PolyUI): Window {
        this.polyUI = polyUI
        var frames = 0
        var lastSecond = System.currentTimeMillis()

        createCallbacks()
        while (!glfwWindowShouldClose(handle)) {

            glViewport(0, 0, width, height)
            glClearColor(0.1f, 0.1f, 0.1f, 0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

            this.polyUI.render()


            glfwPollEvents()
            glfwSwapBuffers(handle)

            if (lastSecond + 1000 < System.currentTimeMillis()) {
                lastSecond = System.currentTimeMillis()
                fps = frames
                frames = 0
                println("FPS: $fps")
            } else frames++
        }

        polyUI.cleanup()
        GL.setCapabilities(null)
        Callbacks.glfwFreeCallbacks(handle)
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
        return this
    }

    override fun closeWindow() {
        TODO("Not yet implemented")
    }

    override fun setIcon(icon: String) {
        TODO("Not yet implemented")
    }

    override fun renameWindow(title: String) {
        glfwSetWindowTitle(handle, title)
    }

    override fun fullscreen() {
        glfwGetVideoMode(glfwGetPrimaryMonitor())?.let {
            glfwSetWindowSize(handle, it.width(), it.height())
        }
    }

}