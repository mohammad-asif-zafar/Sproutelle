package com.hathway.sproutelle.presentation.games

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Custom arched cloud banner shape with a gentle upward semi-circle curve
 * and deeply rounded pill ends matching the Creative Studio header.
 */
class SemiCircleCloudShape(
    private val archHeightRatio: Float = 0.24f,   // Controls how steep the upward semi-circle arch is
    private val bottomArchRatio: Float = 0.14f    // Controls the matching gentle upward curve on the bottom
) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val topArch = h * archHeightRatio
        val bottomArch = h * bottomArchRatio
        val cornerR = (h - topArch) * 0.48f

        val path = Path().apply {
            // Start at left side just below top-left corner curve
            moveTo(0f, topArch + cornerR)

            // Top-left rounded corner into arched top edge
            quadraticBezierTo(
                x1 = 0f, y1 = topArch, x2 = cornerR, y2 = topArch * 0.85f
            )

            // Upward semi-circle arch across the top
            quadraticBezierTo(
                x1 = w * 0.5f, y1 = -topArch * 0.85f, x2 = w - cornerR, y2 = topArch * 0.85f
            )

            // Top-right rounded corner
            quadraticBezierTo(
                x1 = w, y1 = topArch, x2 = w, y2 = topArch + cornerR
            )

            // Right vertical edge down to bottom-right corner
            lineTo(w, h - cornerR)

            // Bottom-right rounded corner
            quadraticBezierTo(
                x1 = w, y1 = h, x2 = w - cornerR, y2 = h - (bottomArch * 0.15f)
            )

            // Gentle upward curve along the bottom edge
            quadraticBezierTo(
                x1 = w * 0.5f,
                y1 = h - (bottomArch * 1.8f),
                x2 = cornerR,
                y2 = h - (bottomArch * 0.15f)
            )

            // Bottom-left rounded corner
            quadraticBezierTo(
                x1 = 0f, y1 = h, x2 = 0f, y2 = h - cornerR
            )

            close()
        }

        return Outline.Generic(path)
    }
}