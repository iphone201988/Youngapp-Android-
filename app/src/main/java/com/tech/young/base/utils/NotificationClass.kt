package com.tech.young.base.utils
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tech.young.R
import com.tech.young.data.model.FcmPayload
import com.tech.young.ui.home.HomeActivity

class NotificationClass : FirebaseMessagingService() {
    var title = ""
    var body = ""
    override fun onNewToken(token: String) {
        Log.d("dc ", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "Notification: ${remoteMessage.notification}")
        Log.d("FCM", "Data: ${remoteMessage.data}")

        // Notification body text (if available)
        val message = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "New notification"

        // Convert data payload into model
        val payload = parsePayload(remoteMessage.data)

        sendNotification(message, payload)
    }

    private fun sendNotification(msg: String, payload: FcmPayload) {
        val bundle = Bundle().apply {
            putParcelable("notificationData", payload)
        }
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtras(bundle) // 👈 attach bundle to intent
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId = "my_channel_id"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(msg)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "app", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }


    fun parsePayload(data: Map<String, String>): FcmPayload {
        return FcmPayload(
            type = data["type"],
            postId = data["postId"],
            userId = data["userId"],
            username = data["username"],
            profileImage = data["profileImage"],
            role = data["role"],
            chatId = data["chatId"]
        )
    }
}