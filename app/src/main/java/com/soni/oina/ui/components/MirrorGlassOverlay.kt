package com.soni.oina.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Purely cosmetic overlay drawn on top of the camera preview so OINA reads as
 * a real mirror rather than a raw camera feed: a soft dark vignette at the
 * edges plus a faint cool silver-blue tint, the way light behaves on actual
 * mirror glass. Draws only — no gesture handling — so it never blocks
 * pinch-to-zoom or taps on the layers beneath it.
 */
@Composable
fun MirrorGlassOverlay(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val longSideDp = if (maxWidth > maxHeight) maxWidth else maxHeight
        val radiusPx = with(density) { (longSideDp * 1.15f).toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Subtle dark vignette toward the edges/corners
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.30f)
                        ),
                        radius = radiusPx
                    )
                )
                // Faint cool silver-blue mirror tint, stronger at top and bottom
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCFEFFF).copy(alpha = 0.08f),
                            Color.Transparent,
                            Color(0xFF7DD3FC).copy(alpha = 0.06f)
                        )
                    )
                )
        )
    }
}
