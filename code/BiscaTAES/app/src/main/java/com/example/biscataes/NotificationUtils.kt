package com.example.biscataes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationUtils {

    private const val CHANNEL_ID = "global_ranking_channel"
    private const val CHANNEL_NAME = "Global Ranking"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Changed to HIGH
            ).apply {
                description = "Notifications for global ranking updates"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRankingNotification(context: Context, rank: Int, playerName: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Changed to a standard mipmap icon
            .setContentTitle("Global Ranking Update")
            .setContentText("$playerName, you are currently ranked #$rank!")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Changed to HIGH

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build())
    }
}
