package com.alone.edgeview.gl

import android.content.Context
import android.opengl.GLES20
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.Size
import android.os.Handler
import android.os.HandlerThread
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLESRenderer(val ctx: Context) : GLSurfaceView.Renderer {
    private val glThread = HandlerThread("GLThread").apply { start() }
    val handler = Handler(glThread.looper)
    private var textureId = 0
    private var program = 0
    private var width = 640
    private var height = 480
    private var pendingFrame: ByteBuffer? = null

    // Quad vertices (X, Y, U, V)
    private val vertexData = floatArrayOf(
        -1f, -1f, 0f, 0f,  // bottom left
        1f, -1f, 1f, 0f,  // bottom right
        -1f,  1f, 0f, 1f,  // top left
        1f,  1f, 1f, 1f   // top right
    )

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertexData); position(0) }

    fun start(surfaceTexture: SurfaceTexture, size: Size) {
        // Here you can set up your OpenGL surface
        // Example: initialize EGL or your shader program
        // For now, just log or debug-print to confirm
        println("Renderer started with size ${size.width}x${size.height}")
    }



    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = createProgram(vertexShaderSource, fragmentShaderSource)
        textureId = createTexture()
        GLES20.glClearColor(0f, 0f, 0f, 1f)
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        width = w
        height = h
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        synchronized(this) {
            pendingFrame?.let { buf ->
                buf.position(0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
                )
                pendingFrame = null
            }
        }

        drawQuad(program, textureId)
    }

    fun queueFrame(rgba: ByteArray, w: Int, h: Int) {
        val bb = ByteBuffer.allocateDirect(rgba.size)
        bb.put(rgba)
        bb.position(0)
        synchronized(this) { pendingFrame = bb }
    }

    // ✅ Create OpenGL texture
    private fun createTexture(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        return tex[0]
    }

    // ✅ Create shader program
    private fun createProgram(vsSource: String, fsSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vsSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fsSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    // ✅ Shader loader
    private fun loadShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        return shader
    }

    // ✅ Shader sources
    private val vertexShaderSource = """
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        void main() {
            gl_Position = a_Position;
            v_TexCoord = a_TexCoord;
        }
    """.trimIndent()

    private val fragmentShaderSource = """
        precision mediump float;
        varying vec2 v_TexCoord;
        uniform sampler2D u_Texture;
        void main() {
            gl_FragColor = texture2D(u_Texture, v_TexCoord);
        }
    """.trimIndent()

    // ✅ Draw textured quad
    private fun drawQuad(program: Int, textureId: Int) {
        GLES20.glUseProgram(program)
        val posLoc = GLES20.glGetAttribLocation(program, "a_Position")
        val texLoc = GLES20.glGetAttribLocation(program, "a_TexCoord")
        val samplerLoc = GLES20.glGetUniformLocation(program, "u_Texture")

        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

        vertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(texLoc)
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(samplerLoc, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(texLoc)
    }
}
