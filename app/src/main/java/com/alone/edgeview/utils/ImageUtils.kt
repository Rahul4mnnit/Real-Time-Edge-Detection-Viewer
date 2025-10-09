package com.alone.edgeview.utils

import android.media.Image
import java.nio.ByteBuffer

object ImageUtils {

    // Converts YUV_420_888 Image to NV21 byte array
    fun imageToNV21(image: Image): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y goes first
        yBuffer.get(nv21, 0, ySize)

        // NV21 format: V then U
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        return nv21
    }
}
