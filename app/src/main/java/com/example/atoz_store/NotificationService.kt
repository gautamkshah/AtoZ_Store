package com.example.atoz_store

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.atoz_store.activity.UsersMainActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val channelId="AtoZStore"
        val channel=NotificationChannel(channelId,"AtoZStore", NotificationManager.IMPORTANCE_HIGH).apply {
            description="AtoZStore"
            enableLights(true)
        }
        val manager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val pendingIntent=PendingIntent.getActivity(this,0, Intent(this, UsersMainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT)
        val notification=NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.app_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(Random.nextInt(),notification)
    }

}