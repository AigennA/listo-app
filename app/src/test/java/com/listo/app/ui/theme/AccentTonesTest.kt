package com.listo.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.listo.app.data.models.AccentColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccentTonesTest {

    @Test
    fun `every accent reads at AA on the light background`() = assertAllReadableOn(LisWhite)

    @Test
    fun `every accent reads at AA on the dark background`() = assertAllReadableOn(LisDarkBg)

    @Test
    fun `an accent that already passes keeps its exact color`() {
        assertEquals(AccentColor.TEAL.primary.toInt(), AccentColor.TEAL.primaryOn(LisDarkBg).toArgb())
    }

    private fun assertAllReadableOn(background: Color) {
        AccentColor.entries.forEach { accent ->
            val ratio = ColorContrast.ratio(accent.primaryOn(background).toArgb(), background.toArgb())
            assertTrue("$accent is $ratio:1", ratio >= ColorContrast.MIN_TEXT_RATIO)
        }
    }
}
