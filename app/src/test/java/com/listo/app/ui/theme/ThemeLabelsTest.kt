package com.listo.app.ui.theme

import com.listo.app.R
import com.listo.app.data.models.AccentColor
import com.listo.app.data.models.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeLabelsTest {

    @Test
    fun `each accent shows the string named after it`() {
        AccentColor.entries.forEach { accent ->
            assertEquals(accent.name, stringId("accent_${accent.name.lowercase()}"), accent.labelRes())
        }
    }

    @Test
    fun `each theme mode shows the string named after it`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode.name, stringId("theme_${mode.name.lowercase()}"), mode.labelRes())
        }
    }

    /** Looks the id up by resource name, so a swapped mapping fails even though every id is distinct. */
    private fun stringId(name: String): Int = R.string::class.java.getField(name).getInt(null)
}
