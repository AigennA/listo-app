package com.listo.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawWithCache

/** Design tokens for the fresh / organic-grocery brand moments. */
object BrandColors {
    val Cream = Color(0xFFFBF7EF)
    val CreamDeep = Color(0xFFF1E9D8)
    val Forest = Color(0xFF1B4332)
    val Leaf = Color(0xFF2D6A4F)
    val Sprout = Color(0xFF40916C)
    val Mint = Color(0xFFB7E4C7)
    val Amber = Color(0xFFF4A259)
    val Coral = Color(0xFFE76F51)
    val Ink = Color(0xFF1B2D26)
}

private data class Blob(
    val xFraction: Float,
    val yFraction: Float,
    val radiusFraction: Float,
    val color: Color,
    val alpha: Float
)

private val brandBlobs = listOf(
    Blob(0.88f, 0.10f, 0.85f, BrandColors.Mint, 0.65f),
    Blob(0.08f, 0.26f, 0.70f, BrandColors.Amber, 0.16f),
    Blob(0.95f, 0.92f, 0.95f, BrandColors.Coral, 0.12f),
    Blob(0.12f, 0.96f, 0.80f, BrandColors.Sprout, 0.22f)
)

private fun DrawScope.drawBlob(blob: Blob) {
    val center = Offset(size.width * blob.xFraction, size.height * blob.yFraction)
    val radius = size.minDimension * blob.radiusFraction
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(blob.color.copy(alpha = blob.alpha), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

/**
 * Atmospheric cream backdrop with soft layered "produce" blobs — a gradient
 * mesh that reads as fresh/organic and works on every API level (no blur).
 */
@Composable
fun OrganicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val base = Brush.verticalGradient(
                    listOf(BrandColors.Cream, BrandColors.CreamDeep)
                )
                onDrawBehind {
                    drawRect(base)
                    brandBlobs.forEach { drawBlob(it) }
                }
            },
        content = content
    )
}

/**
 * Theme-aware version for in-app screens: soft accent blobs over the current
 * Material background, so it follows light/dark mode and the chosen accent.
 */
@Composable
fun ThemedMeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    drawRect(background)
                    drawBlob(Blob(0.88f, 0.05f, 0.75f, primary, 0.18f))
                    drawBlob(Blob(0.04f, 0.40f, 0.60f, secondary, 0.10f))
                    drawBlob(Blob(0.96f, 0.85f, 0.85f, primary, 0.10f))
                }
            },
        content = content
    )
}
