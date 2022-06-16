package peter.mitchell.tododaily.HelperClasses

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import peter.mitchell.tododaily.ui.notifications.channelID
import java.time.LocalDateTime


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

        // --- handle snooze ---
        var snooze : Boolean? = intent?.getBooleanExtra("snooze?", false)
        if (snooze == true && intent != null) {
            // Snooze by setting up a one time notification
            dailyNotificationsTemp.addOneTimeNotification(intent.getStringExtra("snoozeName")!!, LocalDateTime.now().plusSeconds(
                snoozeTime.toLong()
            ), intent.getStringExtra("snoozeTitle")!!, intent.getStringExtra("snoozeDesc")!!)
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

        if (notificationIndex < 0 || oneTimeNotification == null || snooze == true) {
            dailyNotificationsTemp.refreshNotifications(context)
            notifFragment?.onResume()
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

        val snoozeIntent = Intent(context, TodoDailyNotification::class.java)
        snoozeIntent.putExtra("snooze?", true)
        if (oneTimeNotification) {
            snoozeIntent.putExtra("snoozeName", dailyNotificationsTemp.oneTimeNotificationNames[notificationIndex]+"-Snoozed")
            snoozeIntent.putExtra("snoozeTitle", dailyNotificationsTemp.oneTimeNotificationTitles[notificationIndex])
            snoozeIntent.putExtra("snoozeDesc", dailyNotificationsTemp.oneTimeNotificationDescriptions[notificationIndex])
        } else {
            snoozeIntent.putExtra("snoozeName", dailyNotificationsTemp.dailyNotificationNames[notificationIndex]+"-Snoozed")
            snoozeIntent.putExtra("snoozeTitle", dailyNotificationsTemp.dailyNotificationTitles[notificationIndex])
            snoozeIntent.putExtra("snoozeDesc", dailyNotificationsTemp.dailyNotificationDescriptions[notificationIndex])
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            253,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        var builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDesc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_home_black_24dp, "Snooze", snoozePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder) //dailyNotifications.nextNotificationID++

        dailyNotificationsTemp.refreshNotifications(context)

        notifFragment?.onResume()
    }

}