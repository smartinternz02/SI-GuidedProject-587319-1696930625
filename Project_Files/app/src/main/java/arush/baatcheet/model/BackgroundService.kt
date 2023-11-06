package arush.baatcheet.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.IBinder
import arush.baatcheet.R
import arush.baatcheet.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class BackgroundService: Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var count = 0
    private var isAppInForeground = false
    override fun onBind(p0: Intent?): IBinder? {
        count = 0
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        count = 0
        val channel = NotificationChannel("channelId", "New messages",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lightColor = Color.MAGENTA
        channel.enableLights(true)
        channel.enableVibration(true)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification = Notification.Builder(this, "channelId")
            .setContentTitle("New messages")
            .setContentText("You have new messages waiting for you")
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .build()

        notification.flags = Notification.FLAG_AUTO_CANCEL

        val appInForegroundFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                isAppInForeground = true
            }
        }
        registerReceiver(receiver, appInForegroundFilter)

        serviceScope.launch {
            DatabaseHandler().getMessagesList().collect{
                if(count>=1 && !isAppInForeground) {
                    manager.notify(0, notification)
                }
                count ++
            }
        }
        return START_STICKY
    }
}