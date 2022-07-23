package peter.mitchell.tododaily.HelperClasses

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.ui.notifications.DailyNotifications

/** When the snooze button on a notification is pressed, onReceive of this class is called
 * Snooze will call snoozeTimer from DailyNotifications
 * @see DailyNotifications
 * @see NotifWorker (calls the initial notification)
 */
class TodoDailyNotification : BroadcastReceiver() {

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
            dailyNotificationsTemp.snoozeTimer(
                intent.getIntExtra("snoozeIndex", -1),
                intent.getBooleanExtra("snoozeIsOneTime", false),
                intent.getStringExtra("snoozeName")!!,
                intent.getStringExtra("snoozeTitle")!!,
                intent.getStringExtra("snoozeDesc")!!
            )

        } else {
            return
        }

        dailyNotificationsTemp.refreshNotifications(context)
        notifFragment?.onResume()
    }

}