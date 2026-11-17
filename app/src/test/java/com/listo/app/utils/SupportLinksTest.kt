package com.listo.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportLinksTest {

    private val pkg = "com.example.app"

    @Test
    fun `play store app link opens the listing in the Play app`() {
        assertEquals("market://details?id=com.example.app", SupportLinks.playStoreAppUri(pkg))
    }

    @Test
    fun `play store web link is the fallback without the Play app`() {
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.example.app",
            SupportLinks.playStoreWebUrl(pkg)
        )
    }

    @Test
    fun `feedback mail goes to the support address with an encoded subject`() {
        val uri = SupportLinks.feedbackMailto("help@example.com", "Listo feedback (1.0.0)", "")
        assertTrue(uri, uri.startsWith("mailto:help@example.com?subject=Listo%20feedback%20%281.0.0%29"))
    }

    @Test
    fun `spaces are encoded as percent-20, never as plus signs`() {
        val uri = SupportLinks.feedbackMailto("help@example.com", "a b", "c d")
        assertFalse(uri, uri.contains('+'))
        assertTrue(uri, uri.endsWith("&body=c%20d"))
    }

    @Test
    fun `non-ascii text survives encoding`() {
        val uri = SupportLinks.feedbackMailto("help@example.com", "Gillar du Listo? Åäö", "")
        assertTrue(uri, uri.contains("%C3%85%C3%A4%C3%B6"))
    }
}
