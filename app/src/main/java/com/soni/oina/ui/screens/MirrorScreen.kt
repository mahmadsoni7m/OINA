package com.soni.oina.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soni.oina.R
import com.soni.oina.camera.CameraBindResult
import com.soni.oina.camera.CameraController
import com.soni.oina.settings.OinaSettings
import com.soni.oina.ui.components.CameraPreviewView
import com.soni.oina.ui.components.ControlBar
import com.soni.oina.ui.components.PermissionRationale
import com.soni.oina.ui.components.ZoomControl
import com.soni.oina.utils.ImageSaver
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput


private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MirrorScreen(
    settings: OinaSettings,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var requestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        requestedOnce = true
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        val permanentlyDenied = requestedOnce && activity != null &&
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)

        PermissionRationale(
            permanentlyDenied = permanentlyDenied,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
        return
    }

    MirrorContent(settings = settings, onOpenSettings = onOpenSettings)
}

@Composable
private fun MirrorContent(
    settings: OinaSettings,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember { CameraController(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraReady by remember { mutableStateOf(false) }
    var cameraUnavailable by remember { mutableStateOf(false) }

    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var minZoom by remember { mutableFloatStateOf(1f) }
    var maxZoom by remember { mutableFloatStateOf(1f) }
    var screenLightOn by remember { mutableStateOf(false) }

    // Bind / rebind camera whenever the preview surface is ready or the
    // "default camera" preference changes.
    LaunchedEffect(previewView, settings.defaultCameraFront) {
        val view = previewView ?: return@LaunchedEffect
        cameraController.bind(
            lifecycleOwner = lifecycleOwner,
            previewView = view,
            preferFront = settings.defaultCameraFront
        ) { result ->
            when (result) {
                is CameraBindResult.Success -> {
                    cameraReady = true
                    cameraUnavailable = false
                    minZoom = cameraController.minZoomRatio()
                    maxZoom = cameraController.maxZoomRatio()
                    zoomRatio = minZoom
                    cameraController.setZoomRatio(zoomRatio)
                }
                is CameraBindResult.NoCameraAvailable -> {
                    cameraUnavailable = true
                }
                is CameraBindResult.Failure -> {
                    cameraUnavailable = true
                }
            }
        }
    }

    // Apply keep-screen-on + screen light + saved brightness to the window.
    DisposableEffect(activity, settings.keepScreenOn, screenLightOn, settings.screenBrightness) {
        val window = activity?.window
        if (window != null) {
            if (settings.keepScreenOn) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            val layoutParams = window.attributes
            layoutParams.screenBrightness = if (screenLightOn) {
                1.0f
            } else {
                settings.screenBrightness
            }
            window.attributes = layoutParams
        }
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val layoutParams = window?.attributes
            if (layoutParams != null) {
                layoutParams.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = layoutParams
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraController.unbind() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!cameraUnavailable) {
            CameraPreviewView(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(minZoom, maxZoom) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            if (maxZoom > minZoom) {
                                val next = (zoomRatio * gestureZoom).coerceIn(minZoom, maxZoom)
                                zoomRatio = next
                                cameraController.setZoomRatio(next)
                            }
                        }
                    },
                mirror = settings.mirrorPreview && cameraController.isFrontCamera(),
                onViewCreated = { view -> previewView = view }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.camera_unavailable),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color.White
            )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cameraReady && maxZoom > minZoom) {
                ZoomControl(
                    zoomRatio = zoomRatio,
                    minZoom = minZoom,
                    maxZoom = maxZoom,
                    onZoomChange = {
                        zoomRatio = it
                        cameraController.setZoomRatio(it)
                    }
                )
            }

            ControlBar(
                screenLightOn = screenLightOn,
                onToggleScreenLight = { screenLightOn = !screenLightOn },
                onSwitchCamera = {
                    val view = previewView ?: return@ControlBar
                    cameraController.switchLens(lifecycleOwner, view) { result ->
                        if (result is CameraBindResult.Success) {
                            minZoom = cameraController.minZoomRatio()
                            maxZoom = cameraController.maxZoomRatio()
                            zoomRatio = minZoom
                        }
                    }
                },
                onCapture = {
                    if (!cameraReady) return@ControlBar
                    val shouldMirrorSave = settings.saveMirroredPhoto && cameraController.isFrontCamera()
                    cameraController.takePicture(
                        onCaptured = { image ->
                            ImageSaver.saveJpeg(
                                context = context,
                                image = image,
                                mirrorHorizontally = shouldMirrorSave,
                                onSuccess = {
                                    Toast.makeText(context, context.getString(R.string.photo_saved), Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, context.getString(R.string.photo_save_failed), Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onError = {
                            Toast.makeText(context, context.getString(R.string.photo_save_failed), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}
