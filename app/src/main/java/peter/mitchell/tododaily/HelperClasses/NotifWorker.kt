package peter.mitchell.tododaily.HelperClasses

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import peter.mitchell.tododaily.ui.notifications.channelID
import java.time.LocalDateTime

/** This class is simply used as the end to a timer to send a notification
 * @see DailyNotifications
 * @see TodoDailyNotification (handles snooze)
 */
class NotifWorker(contextIn: Context, workerParams: WorkerParameters) : Worker(contextIn, workerParams) {

    var context : Context = contextIn

    override fun doWork(): Result { // DO !!!!!NOT!!!!! CALL TOAST FROM THIS METHOD

        Log.i("tdd-doWork", "Do work called from notifworker")
        //Toast.makeText(context, "AAAAAAAAAAAAAAAAAAAAAAAAAAA", Toast.LENGTH_LONG).show()

        // --- setup the notifications class to be used ---
        var dailyNotificationsTemp : DailyNotifications = DailyNotifications(context)

        if (!notificationsFile.exists()) {
            return Result.success()
        } else {
            dailyNotificationsTemp.fromString(notificationsFile.bufferedReader().readText())
        }

        // --- Get the currently scheduled notification, to post the notification for it ---
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

        if (oneTimeNotification == null)
            return Result.success()

        val notificationTitle : String
        val notificationDesc : String

        if (oneTimeNotification) {
            notificationTitle = dailyNotificationsTemp.oneTimeNotificationTitles[notificationIndex]
            notificationDesc = dailyNotificationsTemp.oneTimeNotificationDescriptions[notificationIndex]
        } else {
            notificationTitle = dailyNotificationsTemp.dailyNotificationTitles[notificationIndex]
            notificationDesc = dailyNotificationsTemp.dailyNotificationDescriptions[notificationIndex]
        }

        // --- Setup the action when the snooze button is pressed ---
        val snoozeIntent = Intent(context, TodoDailyNotification::class.java)
        snoozeIntent.putExtra("snooze?", true)
        snoozeIntent.putExtra("snoozeIndex", notificationIndex)
        snoozeIntent.putExtra("snoozeIsOneTime", oneTimeNotification)
        if (oneTimeNotification) {
            snoozeIntent.putExtra("snoozeName", dailyNotificationsTemp.oneTimeNotificationNames[notificationIndex])
        } else {
            snoozeIntent.putExtra("snoozeName", dailyNotificationsTemp.dailyNotificationNames[notificationIndex])
        }
        snoozeIntent.putExtra("snoozeTitle", notificationTitle)
        snoozeIntent.putExtra("snoozeDesc", notificationDesc)

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            253,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // --- send the notification ---
        var builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDesc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_home_black_24dp, "Snooze", snoozePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder) //dailyNotifications.nextNotificationID++

        // --- setup the next notification pending thing ---
        dailyNotificationsTemp.refreshNotifications(context)

        // if the app is currently open, refresh the notification view
        CoroutineScope(Dispatchers.Main).launch {
            notifFragment?.onResume()
        }

        return Result.success()
    }

}