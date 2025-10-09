package com.alone.edgeview

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.alone.edgeview.gl.GLESRenderer
import com.alone.edgeview.utils.ImageUtils
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import android.util.Base64
import com.alone.edgeview.net.MyWebSocketServer
import org.java_websocket.server.WebSocketServer

class MainActivity : AppCompatActivity() {
    private lateinit var wsServer: MyWebSocketServer
    private lateinit var textureView: TextureView
    private lateinit var renderer: GLESRenderer
    private lateinit var imageReader: ImageReader
    private val REQUEST_CAMERA = 1

    init {
        System.loadLibrary("c++_shared")
        System.loadLibrary("opencv_java4")
        System.loadLibrary("native-lib")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textureView = TextureView(this)
        setContentView(textureView)

        // Start WebSocket server on port 8080
        wsServer = MyWebSocketServer(8080)
        wsServer.start()
        Log.d("WS", "✅ WebSocket server started on port 8080")


        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        renderer = GLESRenderer(this)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                Log.d("CAMERA_DEBUG", "SurfaceTexture available")
                startCamera(st)
            }

            override fun onSurfaceTextureDestroyed(st: SurfaceTexture) = true
            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            textureView.surfaceTexture?.let { startCamera(it) }
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera(surfaceTexture: SurfaceTexture) {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.first()
        val size = Size(640, 480)

        imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val nv21 = ImageUtils.imageToNV21(image)
            image.close()

            val rgba = nativeProcessFrame(nv21, size.width, size.height)
            renderer.queueFrame(rgba, size.width, size.height)

            // ✅ Send to WebSocket Clients (Browser)
            try {
                val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba))

                val output = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
                val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)

                wsServer.sendFrame(base64)
            } catch (e: Exception) {
                Log.e("STREAM_DEBUG", "Error sending frame to WebSocket: ${e.message}")
            }

        }, null)

        val previewSurface = Surface(surfaceTexture)
        val surfaces = listOf(previewSurface, imageReader.surface)

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.d("CAMERA_DEBUG", "Camera opened successfully!")
                val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                builder.addTarget(previewSurface)
                builder.addTarget(imageReader.surface)
                camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(builder.build(), null, null)
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e("CAMERA_DEBUG", "Session config failed")
                    }
                }, null)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        }, null)
    }

    private external fun nativeProcessFrame(nv21: ByteArray, w: Int, h: Int): ByteArray
}
