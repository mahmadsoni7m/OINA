package com.soni.oina.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture

sealed class CameraBindResult {
    data object Success : CameraBindResult()
    data object NoCameraAvailable : CameraBindResult()
    data class Failure(val throwable: Throwable) : CameraBindResult()
}

/**
 * Thin, lifecycle-aware wrapper around CameraX. Owns the ProcessCameraProvider,
 * the active Preview + ImageCapture use cases, and exposes simple imperative
 * controls (switch lens, zoom, capture) for the Compose UI layer.
 *
 * Fully local: CameraX talks directly to the device camera HAL. No network calls.
 */
class CameraController(private val context: Context) {

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null

    var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
        private set

    fun hasFrontCamera(): Boolean = try {
        val provider = cameraProviderFuture?.get()
        provider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: true
    } catch (t: Throwable) {
        true
    }

    fun bind(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        preferFront: Boolean,
        onResult: (CameraBindResult) -> Unit
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture = providerFuture
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()

                val wantFront = preferFront &&
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                val wantBack = !preferFront &&
                    provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

                if (!wantFront && !wantBack) {
                    // Fall back to whichever camera actually exists.
                    val hasFront = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    val hasBack = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    if (!hasFront && !hasBack) {
                        onResult(CameraBindResult.NoCameraAvailable)
                        return@addListener
                    }
                    lensFacing = if (hasFront) CameraSelector.LENS_FACING_FRONT
                    else CameraSelector.LENS_FACING_BACK
                } else {
                    lensFacing = if (preferFront) CameraSelector.LENS_FACING_FRONT
                    else CameraSelector.LENS_FACING_BACK
                }

                bindUseCases(provider, lifecycleOwner, previewView)
                onResult(CameraBindResult.Success)
            } catch (t: Throwable) {
                Log.e(TAG, "Camera bind failed", t)
                onResult(CameraBindResult.Failure(t))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindUseCases(
        provider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
    it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
        imageCapture = capture
    }

    fun switchLens(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onResult: (CameraBindResult) -> Unit
    ) {
        val nextPreferFront = lensFacing != CameraSelector.LENS_FACING_FRONT
        bind(lifecycleOwner, previewView, preferFront = nextPreferFront, onResult = onResult)
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun maxZoomRatio(): Float =
        camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f

    fun minZoomRatio(): Float =
        camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f

    fun isFrontCamera(): Boolean = lensFacing == CameraSelector.LENS_FACING_FRONT

    fun takePicture(
        onCaptured: (androidx.camera.core.ImageProxy) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val capture = imageCapture ?: return
        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    onCaptured(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun unbind() {
        cameraProviderFuture?.get()?.unbindAll()
    }

    companion object {
        private const val TAG = "OinaCameraController"
    }
}
