package peter.mitchell.tododaily.ui.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import peter.mitchell.tododaily.TodoDailyNotification
import peter.mitchell.tododaily.dailyNotifications
import java.lang.StringBuilder
import java.time.*

const val channelID = "todoDailyNotificationChannel"

class DailyNotifications(context : Context) {

    lateinit var alarmManager : AlarmManager

    var nextNotificationID = 0

    var notificationIndex = 0
    var notificationTimes : ArrayList<LocalTime> = ArrayList()
    var notificationTitles : ArrayList<String> = ArrayList()
    var notificationDescriptions : ArrayList<String> = ArrayList()

    private val channelName = "dailyNotificationChannel"
    private val channelDescription = "The channel for sending daily notifications"

    public val testString = "daily notifications string"

    init {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = channelDescription
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (alarmManager.nextAlarmClock != null)
            Toast.makeText(context, "Next alarm in: ${(alarmManager.nextAlarmClock.triggerTime-System.currentTimeMillis())/1000} seconds", Toast.LENGTH_SHORT).show()
    }

    public fun createNotification(context : Context, time : LocalTime) {

        // this is the intent that is run
        val intent = Intent(context, TodoDailyNotification::class.java)

            // this sets a pending intent?
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationIndex++,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set an alarm to start the intent
        //val time = System.currentTimeMillis()+3000;
        //val timeToTimer : Long = time.toEpochSecond(LocalDate.now(), ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val testTime : LocalDateTime = time.atDate(LocalDate.now())
        val timeToTimer : Long = testTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000


        //val timeToTimer = System.currentTimeMillis()+3000;
        alarmManager.setAndAllowWhileIdle(//setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeToTimer,
            pendingIntent
        )
    }



    var dailyNotificationsLength = 0
    var dailyNotificationNames : ArrayList<String> = ArrayList()
    var dailyNotificationTimes : ArrayList<LocalTime> = ArrayList()
    var dailyNotificationTitles : ArrayList<String> = ArrayList()
    var dailyNotificationDescriptions : ArrayList<String> = ArrayList()

    var oneTimeNotificationsLength = 0
    var oneTimeNotificationNames : ArrayList<String> = ArrayList()
    var oneTimeNotificationTimes : ArrayList<LocalDateTime> = ArrayList()
    var oneTimeNotificationTitles : ArrayList<String> = ArrayList()
    var oneTimeNotificationDescriptions : ArrayList<String> = ArrayList()

    fun addDailyNotification(name : String, time : LocalTime, title : String, desc : String) {
        dailyNotificationNames.add(name)
        dailyNotificationTimes.add(time)
        dailyNotificationTitles.add(title)
        dailyNotificationDescriptions.add(desc)
        dailyNotificationsLength++
    }

    fun addOneTimeNotification(name : String, time : LocalDateTime, title : String, desc : String) {
        oneTimeNotificationNames.add(name)
        oneTimeNotificationTimes.add(time)
        oneTimeNotificationTitles.add(title)
        oneTimeNotificationDescriptions.add(desc)
        oneTimeNotificationsLength++
    }

    fun totalLength() : Int {
        return dailyNotificationsLength + oneTimeNotificationsLength
    }

    fun resetData() {
        dailyNotificationsLength = 0
        dailyNotificationNames = ArrayList()
        dailyNotificationTimes = ArrayList()
        dailyNotificationTitles = ArrayList()
        dailyNotificationDescriptions = ArrayList()

        oneTimeNotificationsLength = 0
        oneTimeNotificationNames = ArrayList()
        oneTimeNotificationTimes = ArrayList()
        oneTimeNotificationTitles = ArrayList()
        oneTimeNotificationDescriptions = ArrayList()
    }

    public fun fromString(str : String) {
        resetData()
        Log.i("DailyNitifications.fromString", str)

        var i : Int = 0
        var j : Int = 0
        var lineNum : Int = 0
        var readingString = false
        var currentString = StringBuilder()

        while (i < str.length) {

            if (str[i] == '\n') {
                lineNum++
            } else if (str[i] != ',' || readingString) {

                if (str[i] == '\"' && currentString.isEmpty() && i+1 < str.length) {
                    i++
                    readingString = true
                }

                if (str[i] == '\"' && i+1 < str.length && str[i+1] == ',') {
                    readingString = false
                } else {
                    currentString.append(str[i])
                }

            } else {
                // The string was read, add it

                if (lineNum == 0) {

                    if (j%4 == 0) {
                        dailyNotificationNames.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%4 == 1) {
                        dailyNotificationTimes.add(LocalTime.parse(currentString.toString()))
                    } else if (j%4 == 2) {
                        dailyNotificationTitles.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%4 == 3) {
                        dailyNotificationDescriptions.add(currentString.toString().replace("\"\"", "\""))
                        dailyNotificationsLength++
                        j++
                    }

                } else {

                    if (j%4 == 0) {
                        oneTimeNotificationNames.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%4 == 1) {
                        oneTimeNotificationTimes.add(LocalDateTime.parse(currentString.toString()))
                    } else if (j%4 == 2) {
                        oneTimeNotificationTitles.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%4 == 3) {
                        oneTimeNotificationDescriptions.add(currentString.toString().replace("\"\"", "\""))
                        oneTimeNotificationsLength++
                        j++
                    }

                }

                j++
                currentString.clear()
            }

            i++
        }
    }

    public override fun toString() : String {
        var returnString : StringBuilder = StringBuilder()

        for (i in 0 until dailyNotificationsLength) {
            returnString.append("\"${dailyNotificationNames[i]}\",${dailyNotificationTimes[i].toString()},\"${dailyNotificationTitles[i]}\",\"${dailyNotificationDescriptions[i]}\",")
        }
        returnString.append("\n")
        for (i in 0 until oneTimeNotificationsLength) {
            returnString.append("\"${oneTimeNotificationNames[i]}\",${oneTimeNotificationTimes[i].toString()},\"${oneTimeNotificationTitles[i]}\",\"${oneTimeNotificationDescriptions[i]}\",")
        }

        return returnString.toString()
    }

}