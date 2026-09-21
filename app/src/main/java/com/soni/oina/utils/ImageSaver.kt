package com.soni.oina.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object ImageSaver {

    /**
     * Converts a captured [ImageProxy] to a JPEG [Bitmap], optionally mirroring it
     * horizontally so the saved photo matches exactly what the user saw on screen,
     * then writes it to the public Pictures/OINA collection via MediaStore.
     *
     * Fully local — MediaStore is on-device storage, no upload of any kind.
     */
    fun saveJpeg(
        context: Context,
        image: ImageProxy,
        mirrorHorizontally: Boolean,
        onSuccess: (android.net.Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val bytes = imageProxyToJpegBytes(image)
            image.close()

            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Decode failed")

            if (mirrorHorizontally) {
                val matrix = Matrix().apply { preScale(-1f, 1f) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            val filename = "OINA_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date()) + ".jpg"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OINA")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("MediaStore insert failed")

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            } ?: throw IllegalStateException("Unable to open output stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            bitmap.recycle()
            onSuccess(uri)
        } catch (t: Throwable) {
            runCatching { image.close() }
            onError(t)
        }
    }

    private fun imageProxyToJpegBytes(image: ImageProxy): ByteArray {
        // ImageCapture.OnImageCapturedCallback with default JPEG output format
        // delivers a single JPEG plane we can copy directly.
        val plane = image.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val rotation = image.imageInfo.rotationDegrees
        if (rotation == 0) return bytes

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val stream = ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        bitmap.recycle()
        rotated.recycle()
        return stream.toByteArray()
    }
}
