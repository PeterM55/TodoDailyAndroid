package peter.mitchell.tododaily.ui.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.NotifWorker
import peter.mitchell.tododaily.HelperClasses.TodoDailyNotification
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

const val channelID = "todoDailyNotificationChannel"

/** Daily notifications handles all of the notifications for the application, allowing for the
 * creation, management, and deletion of one-time and daily notifications. System notifications can
 * also be created, but those are just one time notifications the user cannot create.
 */
class DailyNotifications(context : Context) {

    var alarmManager : AlarmManager

    private val channelName = "dailyNotificationChannel"
    private val channelDescription = "The channel for sending daily notifications"

    init {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = channelDescription
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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

    var isSystemNotification : ArrayList<Boolean> = ArrayList()

    /** Adds a daily notification with the information given.
     * If another notification is at the same time it is not created and it returns false
     *
     * @param name the name of the notification
     * @param time the time of the notification
     * @param title the title of the notification, shown in the notification
     * @param desc the description of the notification, shown in the notification
     * @param swappingIndex removes the one time notification at that index, so a one-time can be
     * moved to daily
     * @return whether it worked
     */
    fun addDailyNotification(name : String, time : LocalTime, title : String, desc : String, swappingIndex : Int = -1) : Boolean {
        for (i in 0 until dailyNotificationsLength) {
            if (time == dailyNotificationTimes[i]) {
                return false
            }
        }
        for (i in 0 until oneTimeNotificationsLength) {
            if (swappingIndex == i) continue
            if (time == oneTimeNotificationTimes[i].toLocalTime()) {
                return false
            }
        }
        dailyNotificationNames.add(name)
        dailyNotificationTimes.add(time)
        dailyNotificationTitles.add(title)
        dailyNotificationDescriptions.add(desc)
        dailyNotificationsLength++

        if (swappingIndex != -1) {
            removeOneTimeNotification(swappingIndex)
        }

        return true
    }

    /** Adds a one-time notification with the information given.
     * If another notification is at the same time it is not created and it returns false
     *
     * @param name the name of the notification
     * @param time the date and time of the notification
     * @param title the title of the notification, shown in the notification
     * @param desc the description of the notification, shown in the notification
     * @param swappingIndex removes the daily notification at that index, so a daily can be
     * moved to one-time
     * @return whether it worked
     */
    fun addOneTimeNotification(name : String, time : LocalDateTime, title : String, desc : String, swappingIndex : Int = -1) : Boolean {
        for (i in 0 until oneTimeNotificationsLength) {
            if (time == oneTimeNotificationTimes[i]) {
                return false
            }
        }
        for (i in 0 until dailyNotificationsLength) {
            if (swappingIndex == i) continue
            if (time.toLocalTime() == dailyNotificationTimes[i]) {
                return false
            }
        }
        oneTimeNotificationNames.add(name)
        oneTimeNotificationTimes.add(time)
        oneTimeNotificationTitles.add(title)
        oneTimeNotificationDescriptions.add(desc)
        isSystemNotification.add(false)
        oneTimeNotificationsLength++

        if (swappingIndex != -1) {
            removeDailyNotification(swappingIndex)
        }

        return true
    }

    /** Sets the daily notification at that index using the information given.
     * If another notification is at the same time it will not be changed and it returns false
     *
     * @param iIn the index to change
     * @param name the name of the notification
     * @param time the time of the notification
     * @param title the title of the notification, shown in the notification
     * @param desc the description of the notification, shown in the notification
     * @return whether it worked
     */
    fun setDailyNotification(iIn : Int, name : String, time : LocalTime, title : String, desc : String) : Boolean {
        for (i in 0 until dailyNotificationsLength) {
            if (iIn == i) continue
            if (time == dailyNotificationTimes[i]) {
                return false
            }
        }
        for (i in 0 until oneTimeNotificationsLength) {
            if (time == oneTimeNotificationTimes[i].toLocalTime()) {
                return false
            }
        }
        dailyNotificationNames[iIn] = (name)
        dailyNotificationTimes[iIn] = (time)
        dailyNotificationTitles[iIn] = (title)
        dailyNotificationDescriptions[iIn] = (desc)
        return true
    }

    /** Sets the one-time notification at that index using the information given.
     * If another notification is at the same time it will not be changed and it returns false
     *
     * @param iIn the index to change
     * @param name the name of the notification
     * @param time the date and time of the notification
     * @param title the title of the notification, shown in the notification
     * @param desc the description of the notification, shown in the notification
     * @return whether it worked
     */
    fun setOneTimeNotification(iIn : Int, name : String, time : LocalDateTime, title : String, desc : String) : Boolean {
        for (i in 0 until oneTimeNotificationsLength) {
            if (iIn == i) continue
            if (time == oneTimeNotificationTimes[i]) {
                return false
            }
        }
        for (i in 0 until dailyNotificationsLength) {
            if (time.toLocalTime() == dailyNotificationTimes[i]) {
                return false
            }
        }
        oneTimeNotificationNames[iIn] = (name)
        oneTimeNotificationTimes[iIn] = (time)
        oneTimeNotificationTitles[iIn] = (title)
        oneTimeNotificationDescriptions[iIn] = (desc)
        return true
    }

    /** Gets the date and time of the next notification, if none returns null
     *
     * @return the date and time of the next notification, if none returns null
     */
    fun getNextNotificationTime() : LocalDateTime? {

        var nextNotification : LocalDateTime? = null

        for (i in 0 until oneTimeNotificationsLength) {

            if (nextNotification == null || nextNotification.isAfter(oneTimeNotificationTimes[i])) {
                nextNotification = oneTimeNotificationTimes[i]
            }

        }

        for (i in 0 until dailyNotificationsLength) {

            var tempDateTime : LocalDateTime = LocalDateTime.of(LocalDate.now(), dailyNotificationTimes[i])
            if (tempDateTime.isBefore(LocalDateTime.now())) {
                tempDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), dailyNotificationTimes[i])
            }

            if (nextNotification == null || nextNotification.isAfter(tempDateTime)) {
                nextNotification = tempDateTime
            }

        }

        return nextNotification
    }

    /** Changes the one time notification at the given index to be a system notification.
     * This should never be done by the user.
     *
     * @param i the index to set to a system notification
     */
    fun setSystemNotification(i : Int) {
        isSystemNotification[i] = true
    }

    /** Remove the daily notification at the index and decrement the length
     *
     * @param i the daily index to remove
     */
    fun removeDailyNotification(i : Int) {
        dailyNotificationNames.removeAt(i)
        dailyNotificationTimes.removeAt(i)
        dailyNotificationTitles.removeAt(i)
        dailyNotificationDescriptions.removeAt(i)
        dailyNotificationsLength--
    }

    /** Remove the daily notification at the index and decrement the length
     *
     * @param i the daily index to remove
     */
    fun removeOneTimeNotification(i : Int) {
        oneTimeNotificationNames.removeAt(i)
        oneTimeNotificationTimes.removeAt(i)
        oneTimeNotificationTitles.removeAt(i)
        oneTimeNotificationDescriptions.removeAt(i)
        isSystemNotification.removeAt(i)
        oneTimeNotificationsLength--
    }

    /** Moves the daily notification from the first index to the second
     *
     * @param i the first index, moved *from*
     * @param to the second index, moved *to*
     */
    fun dailyMoveFrom(i : Int, to : Int) {

        if (i == to || i >= dailyNotificationsLength || to >= dailyNotificationsLength) return

        var tempName = dailyNotificationNames[i]
        var tempTime = dailyNotificationTimes[i]
        var tempTitle = dailyNotificationTitles[i]
        var tempDesc = dailyNotificationDescriptions[i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    dailyNotificationNames[j] = dailyNotificationNames[j+1]
                    dailyNotificationTimes[j] = dailyNotificationTimes[j+1]
                    dailyNotificationTitles[j] = dailyNotificationTitles[j+1]
                    dailyNotificationDescriptions[j] = dailyNotificationDescriptions[j+1]
                } else if (j == to) {
                    dailyNotificationNames[j] = tempName
                    dailyNotificationTimes[j] = tempTime
                    dailyNotificationTitles[j] = tempTitle
                    dailyNotificationDescriptions[j] = tempDesc
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    dailyNotificationNames[j] = dailyNotificationNames[j-1]
                    dailyNotificationTimes[j] = dailyNotificationTimes[j-1]
                    dailyNotificationTitles[j] = dailyNotificationTitles[j-1]
                    dailyNotificationDescriptions[j] = dailyNotificationDescriptions[j-1]
                } else if (j == to) {
                    dailyNotificationNames[j] = tempName
                    dailyNotificationTimes[j] = tempTime
                    dailyNotificationTitles[j] = tempTitle
                    dailyNotificationDescriptions[j] = tempDesc
                }
            }
        }

    }

    /** Moves the one-time notification from the first index to the second
     *
     * @param i the first index, moved *from*
     * @param to the second index, moved *to*
     */
    fun oneTimeMoveFrom(i : Int, to : Int) {

        if (i == to || i >= oneTimeNotificationsLength || to >= oneTimeNotificationsLength) return

        var tempName = oneTimeNotificationNames[i]
        var tempTime = oneTimeNotificationTimes[i]
        var tempTitle = oneTimeNotificationTitles[i]
        var tempDesc = oneTimeNotificationDescriptions[i]
        var tempIsSystem = isSystemNotification[i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    oneTimeNotificationNames[j] = oneTimeNotificationNames[j+1]
                    oneTimeNotificationTimes[j] = oneTimeNotificationTimes[j+1]
                    oneTimeNotificationTitles[j] = oneTimeNotificationTitles[j+1]
                    oneTimeNotificationDescriptions[j] = oneTimeNotificationDescriptions[j+1]
                    isSystemNotification[j] = isSystemNotification[j+1]
                } else if (j == to) {
                    oneTimeNotificationNames[j] = tempName
                    oneTimeNotificationTimes[j] = tempTime
                    oneTimeNotificationTitles[j] = tempTitle
                    oneTimeNotificationDescriptions[j] = tempDesc
                    isSystemNotification[j] = tempIsSystem
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    oneTimeNotificationNames[j] = oneTimeNotificationNames[j-1]
                    oneTimeNotificationTimes[j] = oneTimeNotificationTimes[j-1]
                    oneTimeNotificationTitles[j] = oneTimeNotificationTitles[j-1]
                    oneTimeNotificationDescriptions[j] = oneTimeNotificationDescriptions[j-1]
                    isSystemNotification[j] = isSystemNotification[j-1]
                } else if (j == to) {
                    oneTimeNotificationNames[j] = tempName
                    oneTimeNotificationTimes[j] = tempTime
                    oneTimeNotificationTitles[j] = tempTitle
                    oneTimeNotificationDescriptions[j] = tempDesc
                    isSystemNotification[j] = tempIsSystem
                }
            }
        }

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
        isSystemNotification = ArrayList()
    }

    /** Gets the formatted string for a one time notification (name: date - time)
     *
     * @param i the index to get
     * @return the formatted string (name: date - time)
     */
    public fun getOneTimeString(i : Int) : String {
        return "${oneTimeNotificationNames[i]}: ${oneTimeNotificationTimes[i].toLocalDate().toString()} - ${oneTimeNotificationTimes[i].toLocalTime().toString()}"
    }

    /** Takes in the notification information from the string given,resetting the object first
     *
     * @param str the string to read
     */
    public fun fromString(str : String) {
        resetData()
        Log.i("tdd.DailyNitifications.fromString", str)

        var i : Int = 0
        var j : Int = 0
        var lineNum : Int = 0
        var readingString = false
        val currentString = StringBuilder()

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
                        j = -1
                    }

                } else {

                    if (j%5 == 0) {
                        oneTimeNotificationNames.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%5 == 1) {
                        oneTimeNotificationTimes.add(LocalDateTime.parse(currentString.toString()))
                    } else if (j%5 == 2) {
                        oneTimeNotificationTitles.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%5 == 3) {
                        oneTimeNotificationDescriptions.add(currentString.toString().replace("\"\"", "\""))
                    } else if (j%5 == 4) {
                        isSystemNotification.add(currentString.toString().toBoolean())
                        oneTimeNotificationsLength++
                        j = -1
                    }

                }

                j++
                currentString.clear()
            }

            i++
        }
    }

    /** outputs the notification content to a string, with a format that fromString can use
     *
     * @return the string representation of the object
     */
    public override fun toString() : String {
        var returnString : StringBuilder = StringBuilder()

        for (i in 0 until dailyNotificationsLength) {
            returnString.append("\"${dailyNotificationNames[i]}\",${dailyNotificationTimes[i].toString()},\"${dailyNotificationTitles[i]}\",\"${dailyNotificationDescriptions[i]}\",")
        }
        returnString.append("\n")
        for (i in 0 until oneTimeNotificationsLength) {
            returnString.append("\"${oneTimeNotificationNames[i]}\",${oneTimeNotificationTimes[i].toString()},\"${oneTimeNotificationTitles[i]}\",\"${oneTimeNotificationDescriptions[i]}\",${isSystemNotification[i]},")
        }

        return returnString.toString()
    }

    /** Snoozes the timer for the snooze time in the settings
     *
     * @param i the index to snooze
     * @param isOneTime whether it is a one-time notification
     * @param snoozeName the name of the snoozed notification
     * @param snoozeTitle the title of the notification to be displayed in the notification
     * @param snoozeDesc the description of the notification to be displayed in the notification
     */
    public fun snoozeTimer(i : Int, isOneTime : Boolean, snoozeName : String, snoozeTitle : String, snoozeDesc : String) {
        if (i == -1) return

        val newName = "$i-$snoozeName-Snoozed"

        var tempSnoozeTime = -1

        if (settingsBackupFile.exists()) {
            settingsBackupFile.forEachLine {
                val splitTitle = it.split(" ")[0]
                val splitValue = it.split(" ")[1]

                if (splitTitle == "snoozeTime") tempSnoozeTime = splitValue.toInt()
            }
        }

        if (tempSnoozeTime == -1) tempSnoozeTime = snoozeTime

        if (isOneTime) {
            addOneTimeNotification(newName, LocalDateTime.now().plusMinutes(snoozeTime.toLong()), snoozeTitle, snoozeDesc)
        } else {
            addOneTimeNotification(newName, LocalDateTime.now().plusMinutes(snoozeTime.toLong()), snoozeTitle, snoozeDesc)
        }

        setSystemNotification(oneTimeNotificationsLength-1)

        // if the app is currently open, refresh the notification view
        CoroutineScope(Dispatchers.Main).launch {
            notifFragment?.onResume()
        }
    }

    /**
     * This should be the ONLY FUNCTION creating and destroying alarms (notifications)
     * All others should queue ones, then call this one.
     */
    public fun refreshNotifications(context : Context) {

        if (dailyNotificationsLength == 0 && oneTimeNotificationsLength == 0)
            return

        deletePastOneTimeAlarms()

        // --- set a couple constants ---

        // this is the intent that is run
        val notificationIntent = Intent(context, TodoDailyNotification::class.java)

        // --- Get the currently scheduled notification, and delete it ---
        WorkManager.getInstance(context).cancelAllWork()

        // --- Get the next notification to schedule ---
        var nextNotification : LocalDateTime? = null
        var notificationIndex : Int = 0

        for (i in 0 until oneTimeNotificationsLength) {

            if (nextNotification == null || nextNotification.isAfter(oneTimeNotificationTimes[i])) {
                nextNotification = oneTimeNotificationTimes[i]
                notificationIndex = i+dailyNotificationsLength
            }

        }

        for (i in 0 until dailyNotificationsLength) {

            var tempDateTime : LocalDateTime = LocalDateTime.of(LocalDate.now(), dailyNotificationTimes[i])
            if (tempDateTime.isBefore(LocalDateTime.now())) {
                tempDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), dailyNotificationTimes[i])
            }

            if (nextNotification == null || nextNotification.isAfter(tempDateTime)) {
                nextNotification = tempDateTime
                notificationIndex = i
            }

        }

        // --- Schedule the next notification ---
        if (nextNotification == null) return

        //var timeOfTimer : Long = nextNotification.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000
        var secondsToTimer : Long = ChronoUnit.SECONDS.between(LocalDateTime.now(),nextNotification)

        val testWorkTag = "notificationDailyTag"

        val notificationWork : OneTimeWorkRequest = OneTimeWorkRequest.Builder(NotifWorker::class.java)
            .setInitialDelay(secondsToTimer, TimeUnit.SECONDS)
            .addTag(testWorkTag)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWork)

        if (!dailyInformationFile.exists()) {
            dailyInformationFile.parentFile!!.mkdirs()
            dailyInformationFile.createNewFile()
        }
        nextNotificationIntentFile.writeText(notificationIndex.toString())

        saveNotifications(this)
    }

    /** Goes through the one-time notifications and deletes all notifications in the past */
    private fun deletePastOneTimeAlarms() {

        var i : Int = 0

        while(i < oneTimeNotificationsLength) {

            if (!oneTimeNotificationTimes[i].isAfter(LocalDateTime.now())) {
                Log.i("tdd.deletePastOneTimes", "Deleting: ${oneTimeNotificationTimes[i]}")
                removeOneTimeNotification(i)
            }

            i++
        }
    }

}