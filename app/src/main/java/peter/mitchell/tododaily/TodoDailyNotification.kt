package peter.mitchell.tododaily

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import peter.mitchell.tododaily.ui.notifications.channelID


//val notificationChannel : NotificationChannel;


class TodoDailyNotification : BroadcastReceiver() {

    init {
    }

    lateinit var copyDailyNotifications : DailyNotifications

    override fun onReceive(context : Context, intent : Intent?) {

        var builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("this is a test notification")
            .setContentText("this is lots of text: ${copyDailyNotifications.testString}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(55, builder) //dailyNotifications.nextNotificationID++

        /*with(NotificationManagerCompat.from(context)) {
            createNotificationChannel(notificationChannel)
            notify(1, builder.build())
        }*/

    }

}