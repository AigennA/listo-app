package com.listo.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.listo.app.data.models.AccentColor

/**
 * The tone of this accent to use as `primary` on [background]: the chosen color,
 * nudged just enough that primary text on the background — and background-colored
 * text on primary — meets WCAG AA. The Settings swatches use it too, so what you
 * pick is what you get.
 */
fun AccentColor.primaryOn(background: Color): Color =
    Color(ColorContrast.ensure(primary.toInt(), background.toArgb(), ColorContrast.MIN_TEXT_RATIO))
