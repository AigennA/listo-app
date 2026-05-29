package com.listo.app.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.listo.app.LisToApplication
import com.listo.app.MainActivity
import com.listo.app.R
import com.listo.app.data.repository.ShoppingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val DAILY_REMINDER_NOTIF_ID = 2001
private const val DAILY_REMINDER_REQUEST = 7001

/** Schedules / cancels the once-a-day reminder alarm. */
object DailyReminderScheduler {

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTrigger(hour, minute),
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(pendingIntent(context))
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}

/** Fires daily; shows how many items are still unchecked across active lists. */
@AndroidEntryPoint
class DailyReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: ShoppingRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = repository.getActivePendingItemCount()
                showDailyReminder(context, count)
            } finally {
                pending.finish()
            }
        }
    }
}

/** Re-arms the daily reminder after a reboot (alarms are cleared on boot). */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (prefs.isDailyReminderEnabled.first()) {
                    DailyReminderScheduler.schedule(
                        context,
                        prefs.dailyReminderHour.first(),
                        prefs.dailyReminderMinute.first()
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}

private fun showDailyReminder(context: Context, pendingCount: Int) {
    // Use the in-app chosen language for the notification text.
    val ctx = LocaleHelper.wrap(context)
    val text = if (pendingCount > 0) {
        ctx.getString(R.string.daily_reminder_text, pendingCount)
    } else {
        ctx.getString(R.string.daily_reminder_text_empty)
    }

    val contentIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, LisToApplication.CHANNEL_DAILY_REMINDER)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(ctx.getString(R.string.app_name))
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(contentIntent)
        .build()

    // On Android 13+ this is a no-op unless POST_NOTIFICATIONS was granted.
    context.getSystemService(NotificationManager::class.java).notify(DAILY_REMINDER_NOTIF_ID, notification)
}
