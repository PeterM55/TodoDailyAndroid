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

        var snooze : Boolean? = intent?.getBooleanExtra("snooze?", false)
        if (snooze == true && intent != null) {
            // Snooze by setting up a one time notification
            dailyNotificationsTemp.addOneTimeNotification(intent.getStringExtra("snoozeName")!!, LocalDateTime.now().plusMinutes(
                snoozeTime.toLong()
            ), intent.getStringExtra("snoozeTitle")!!, intent.getStringExtra("snoozeDesc")!!)
        } else {
            return
        }

        dailyNotificationsTemp.refreshNotifications(context)
        notifFragment?.onResume()
    }

}