package com.listo.app.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.listo.app.LisToApplication
import com.listo.app.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Task Reminder"
        val message = intent.getStringExtra("message") ?: "You have a task due soon"
        val id = intent.getIntExtra("notifId", 0)
        showNotification(context, id, title, message)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-schedule alarms after reboot if needed
    }
}

fun showNotification(context: Context, id: Int, title: String, message: String) {
    val notification = NotificationCompat.Builder(context, LisToApplication.CHANNEL_TASK_REMINDER)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(id, notification)
}

fun scheduleTaskReminder(context: Context, taskId: Long, title: String, triggerAtMillis: Long) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", "Task Reminder")
        putExtra("message", title)
        putExtra("notifId", taskId.toInt())
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
}

fun cancelTaskReminder(context: Context, taskId: Long) {
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    alarmManager.cancel(pendingIntent)
}
