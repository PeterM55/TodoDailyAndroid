package peter.mitchell.tododaily.HelperClasses

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.nextNotificationIntentFile
import peter.mitchell.tododaily.notificationsFile
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import peter.mitchell.tododaily.ui.notifications.channelID


//val notificationChannel : NotificationChannel;


class TodoDailyNotification : BroadcastReceiver() {

    init {
    }

    //lateinit var copyDailyNotifications : DailyNotifications

    override fun onReceive(context : Context, intent : Intent?) {

        // --- setup the notifications class to be used ---
        var dailyNotificationsTemp : DailyNotifications = DailyNotifications(context)

        if (!notificationsFile.exists()) {
            return
        } else {
            dailyNotificationsTemp.fromString(notificationsFile.bufferedReader().readText())
        }

        // --- Get the currently scheduled notification, and delete it ---
        var oneTimeNotification : Boolean? = null
        var notificationIndex : Int = -1
        var notificationId : Int = 0
        if (nextNotificationIntentFile.exists()) {
            val fileText = nextNotificationIntentFile.readText()
            if (fileText.isNotEmpty()) {

                notificationId = fileText.toInt()

                if (fileText.toInt() >= dailyNotificationsTemp.dailyNotificationsLength) {
                    notificationIndex = fileText.toInt()-dailyNotificationsTemp.dailyNotificationsLength
                    oneTimeNotification = true
                } else {
                    notificationIndex = fileText.toInt()
                    oneTimeNotification = false
                }

            }
        }

        if (notificationIndex < 0 || oneTimeNotification == null) {
            dailyNotificationsTemp.refreshNotifications(context)
            return
        }

        val notificationTitle : String
        val notificationDesc : String

        if (oneTimeNotification) {
            notificationTitle = dailyNotificationsTemp.oneTimeNotificationTitles[notificationIndex]
            notificationDesc = dailyNotificationsTemp.oneTimeNotificationDescriptions[notificationIndex]
        } else {
            notificationTitle = dailyNotificationsTemp.dailyNotificationTitles[notificationIndex]
            notificationDesc = dailyNotificationsTemp.dailyNotificationDescriptions[notificationIndex]
        }

        var builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDesc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder) //dailyNotifications.nextNotificationID++

        dailyNotificationsTemp.refreshNotifications(context)

        /*if (LocalTime.now().isAfter(LocalTime.of(12+6,15,0))) {
            return
        }

        var setTime = LocalTime.now().plusSeconds(20)

        dailyNotifications.createNotification(context, setTime)*/

        /*with(NotificationManagerCompat.from(context)) {
            createNotificationChannel(notificationChannel)
            notify(1, builder.build())
        }*/

    }

}