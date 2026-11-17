package com.listo.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * Links for rating the app and sending feedback. Nothing here sends data on
 * its own: the user lands on the Play listing, or in their own mail app with a
 * pre-filled message they can read, edit or discard.
 */
object SupportLinks {

    fun playStoreAppUri(packageName: String): String = "market://details?id=$packageName"

    fun playStoreWebUrl(packageName: String): String =
        "https://play.google.com/store/apps/details?id=$packageName"

    /** A mailto: URI; spaces are %20 (URLEncoder's "+" shows up literally in some mail apps). */
    fun feedbackMailto(email: String, subject: String, body: String): String =
        "mailto:$email?subject=${encode(subject)}&body=${encode(body)}"

    private fun encode(text: String): String =
        URLEncoder.encode(text, Charsets.UTF_8.name()).replace("+", "%20")
}

/** Opens Listo's Play listing in the Play app, or in the browser if Play is missing. */
fun Context.openPlayStoreListing(packageName: String) {
    if (!tryStart(Intent(Intent.ACTION_VIEW, Uri.parse(SupportLinks.playStoreAppUri(packageName))))) {
        tryStart(Intent(Intent.ACTION_VIEW, Uri.parse(SupportLinks.playStoreWebUrl(packageName))))
    }
}

/** Opens the user's mail app with a feedback message addressed to [email]. */
fun Context.composeFeedbackEmail(email: String, subject: String, body: String) {
    tryStart(Intent(Intent.ACTION_SENDTO, Uri.parse(SupportLinks.feedbackMailto(email, subject, body))))
}

/** Starts [intent]; false if no app on the device can handle it. */
private fun Context.tryStart(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: ActivityNotFoundException) {
    false
}
