package com.listo.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * The feedback link. It sends nothing on its own: the user lands in their own
 * mail app with a pre-filled message they can read, edit or discard.
 */
object SupportLinks {

    /** A mailto: URI; spaces are %20 (URLEncoder's "+" shows up literally in some mail apps). */
    fun feedbackMailto(email: String, subject: String, body: String): String =
        "mailto:$email?subject=${encode(subject)}&body=${encode(body)}"

    private fun encode(text: String): String =
        URLEncoder.encode(text, Charsets.UTF_8.name()).replace("+", "%20")
}

/** Opens the user's mail app with a feedback message addressed to [email]; ignored without one. */
fun Context.composeFeedbackEmail(email: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(SupportLinks.feedbackMailto(email, subject, body)))
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        // No mail app on the device — nothing sensible to fall back to.
    }
}
