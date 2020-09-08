package me.pitok.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.pitok.coroutines.Dispatcher
import me.pitok.firebase.di.builder.FcmComponentBuilder
import me.pitok.firebase.repository.FcmTokenRefreshable
import javax.inject.Inject

@Suppress("UNREACHABLE_CODE")
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var fcmTokenRefresher: FcmTokenRefreshable
    @Inject lateinit var dispatcher: Dispatcher

    init {
        FcmComponentBuilder.getComponent().inject(this)
    }

    companion object{
        const val TYPE_KEY = "type"
        const val NOTIFY_TYPE_VALUE = "notification"
        const val NOTIFICATION_CHANNEL_ID_KEY = "notification_channel_id"

        //shift to right bitCount
        const val LONG_USHR = 32
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data[TYPE_KEY] == NOTIFY_TYPE_VALUE){
            handleNotification(remoteMessage)
        }
    }

    private fun handleNotification(remoteMessage: RemoteMessage) {
        val clickIntent = Intent(Intent.ACTION_VIEW).apply {
            `package` = packageName
//            data = getString(R.string.deeplink_start).toUri()
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val clickPendingIntent = PendingIntent.getActivity(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = remoteMessage.notification?.channelId?: return
        val defaultSongUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder= NotificationCompat.Builder(this,channelId ).run {
//            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(remoteMessage.notification?.title?: "")
            setContentText(remoteMessage.notification?.body?: "")
            setAutoCancel(true)
            setSound(defaultSongUri)
            setContentIntent(clickPendingIntent)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.notificationChannels.find {channel -> channel.id == channelId}
                ?: run {
                    val channel = NotificationChannel(
                        channelId,
                        remoteMessage.data[NOTIFICATION_CHANNEL_ID_KEY],
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }
        }

        val currentTime = System.currentTimeMillis()
        val notificationId = (currentTime xor currentTime ushr LONG_USHR).toInt()

        notificationManager.notify(notificationId, notificationBuilder.build())
        TODO("add small icon to notification")
        TODO("fix content click intent destination")
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        GlobalScope.launch(dispatcher.io) {
            fcmTokenRefresher.write(newToken)
        }
    }
}