package com.soni.oina.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Hosts a CameraX [PreviewView]. Horizontal mirroring is applied purely at the
 * view layer (scaleX = -1) so the live preview stays smooth and low-latency —
 * no per-frame image processing is involved.
 */
@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    mirror: Boolean,
    onViewCreated: (PreviewView) -> Unit
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                onViewCreated(this)
            }
        },
        update = { view ->
            view.scaleX = if (mirror) -1f else 1f
        }
    )
}
