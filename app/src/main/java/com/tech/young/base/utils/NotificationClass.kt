package com.tech.young.base.utils
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tech.young.R
import com.tech.young.ui.home.HomeActivity

class NotificationClass : FirebaseMessagingService() {
    var title = ""
    var body = ""
    override fun onNewToken(token: String) {
        Log.d("token", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            Log.i("eriotpuerioutoirto", "onMessageReceived: ${remoteMessage.notification?.body}")
            remoteMessage.notification!!.body?.let { sendNotification(it) }
        }
        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data["body"]?.let { sendNotification(it) }
        } else {
            Log.d("MyFirebase", "none_data")
        }
    }
    private fun sendNotification(msg: String) {
        val intent = Intent(this, HomeActivity::class.java)
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
}