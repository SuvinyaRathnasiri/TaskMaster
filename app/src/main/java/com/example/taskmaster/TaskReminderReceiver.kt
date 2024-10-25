package com.example.taskmaster

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.example.taskmaster.R
import com.example.taskmaster.home

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("taskName")
        val taskDescription = intent.getStringExtra("taskDescription")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, home::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "task_reminder_channel")
            .setSmallIcon(R.drawable.noti)
            .setContentTitle("Task Reminder: $taskName")
            .setContentText(taskDescription)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
