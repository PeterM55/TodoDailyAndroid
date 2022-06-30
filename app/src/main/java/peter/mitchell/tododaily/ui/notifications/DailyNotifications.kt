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
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.NotifWorker
import peter.mitchell.tododaily.HelperClasses.TodoDailyNotification
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

const val channelID = "todoDailyNotificationChannel"

class DailyNotifications(context : Context) {

    lateinit var alarmManager : AlarmManager

    private val channelName = "dailyNotificationChannel"
    private val channelDescription = "The channel for sending daily notifications"

    private var scheduledNotificationIntent : PendingIntent? = null

    init {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = channelDescription
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        /*if (alarmManager.nextAlarmClock != null) {
            Toast.makeText(context, "Next alarm in: ${(alarmManager.nextAlarmClock.triggerTime-System.currentTimeMillis())/1000} seconds", Toast.LENGTH_SHORT).show()
            Log.i("DailyNotifications", "Next alarm at: ${(LocalDateTime.ofEpochSecond(alarmManager.nextAlarmClock.triggerTime/1000,
                (alarmManager.nextAlarmClock.triggerTime%1000).toInt(), ZoneId.systemDefault().rules.getOffset(Instant.now())).toString())}")
        } else {
            Log.i("DailyNotifications", "no alarm scheduled")
        }*/
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

    fun setSystemNotification(i : Int) {
        isSystemNotification[i] = true
    }

    fun removeDailyNotification(i : Int) {
        dailyNotificationNames.removeAt(i)
        dailyNotificationTimes.removeAt(i)
        dailyNotificationTitles.removeAt(i)
        dailyNotificationDescriptions.removeAt(i)
        dailyNotificationsLength--
    }

    fun removeOneTimeNotification(i : Int) {
        oneTimeNotificationNames.removeAt(i)
        oneTimeNotificationTimes.removeAt(i)
        oneTimeNotificationTitles.removeAt(i)
        oneTimeNotificationDescriptions.removeAt(i)
        isSystemNotification.removeAt(i)
        oneTimeNotificationsLength--
    }

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
        isSystemNotification = ArrayList()
    }

    public fun getOneTimeString(i : Int) : String {
        return "${oneTimeNotificationNames[i]}: ${oneTimeNotificationTimes[i].toLocalDate().toString()} - ${oneTimeNotificationTimes[i].toLocalTime().toString()}"
    }

    public fun fromString(str : String) {
        resetData()
        Log.i("tdd.DailyNitifications.fromString", str)

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

    public override fun toString() : String {
        var returnString : StringBuilder = StringBuilder()

        for (i in 0 until dailyNotificationsLength) {
            returnString.append("\"${dailyNotificationNames[i]}\",${dailyNotificationTimes[i].toString()},\"${dailyNotificationTitles[i]}\",\"${dailyNotificationDescriptions[i]}\",")
        }
        returnString.append("\n")
        for (i in 0 until oneTimeNotificationsLength) {
            returnString.append("\"${oneTimeNotificationNames[i]}\",${oneTimeNotificationTimes[i].toString()},\"${oneTimeNotificationTitles[i]}\",\"${oneTimeNotificationDescriptions[i]}\",${isSystemNotification[i]},")
        }

        //Log.i("tdd.DailyNitifications.toString", returnString.toString())

        return returnString.toString()
    }

    public fun snoozeTimer(i : Int, isOneTime : Boolean, snoozeName : String, snoozeTitle : String, snoozeDesc : String) {
        if (i == -1) return

        var newName = ""
        if (isOneTime) {
            newName = "$i-"+oneTimeNotificationNames[i]+"-Snoozed"
        } else {
            newName = "$i-"+dailyNotificationNames[i]+"-Snoozed"
        }

        for (i in 0 until oneTimeNotificationsLength) {
            if (isSystemNotification[i] && oneTimeNotificationNames[i] == newName) {
                // extend the time because it already exists
                oneTimeNotificationTimes[i] = LocalDateTime.now().plusMinutes(snoozeTime.toLong())
                return
            }
        }

        if (isOneTime) {
            addOneTimeNotification("$i-"+oneTimeNotificationNames[i]+"-Snoozed", LocalDateTime.now().plusMinutes(snoozeTime.toLong()), oneTimeNotificationTitles[i], oneTimeNotificationDescriptions[i])
        } else {
            addOneTimeNotification("$i-"+dailyNotificationNames[i]+"-Snoozed", LocalDateTime.now().plusMinutes(snoozeTime.toLong()), dailyNotificationTitles[i], dailyNotificationDescriptions[i])
        }

        setSystemNotification(oneTimeNotificationsLength-1)

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
        //Toast.makeText(context, "${nextNotification.toString()} is in $secondsToTimer seconds", Toast.LENGTH_SHORT).show()

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

        Log.i("tdd-refreshNotifs", "Next alarm (${nextNotification.toLocalTime().toString()}) in: ${secondsToTimer} seconds")
        //Toast.makeText(context, "Next alarm in: ${(timeToTimer-System.currentTimeMillis())/1000} seconds", Toast.LENGTH_SHORT).show()

        saveNotifications(this)
    }

    fun deletePastOneTimeAlarms() {

        Log.i("tdd.deletePastOneTimes", "Deleting past one times")

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


/*
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
 */