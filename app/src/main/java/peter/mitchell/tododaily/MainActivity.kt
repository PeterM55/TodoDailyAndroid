package peter.mitchell.tododaily

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.new_notification.*
import peter.mitchell.tododaily.HelperClasses.NotesList
import peter.mitchell.tododaily.databinding.ActivityMainBinding
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.HelperClasses.TodoLists
import peter.mitchell.tododaily.ui.home.ManageDailyNotifications
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

var saveInformation : SaveInformation = SaveInformation()
lateinit var dailyNotifications : DailyNotifications
var todoLists : TodoLists? = null // only used in to-do section, but kept in memory to not have to read every time
var notesList : NotesList? = null

val internalDataPath = "${Environment.getDataDirectory().absolutePath}/data/peter.mitchell.tododaily/files/"
val exportPath = "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DOWNLOADS}/"

val dailyInformationFile = File("${internalDataPath}dailyInformation.txt")
val tempFile = File("${internalDataPath}tempDailyInformation.txt")
val notificationsFile = File("${internalDataPath}dailyNotifications.txt")
val nextNotificationIntentFile = File("${internalDataPath}nextNotificationIntent.txt")
val settingsFile = File("${internalDataPath}settings.txt")
val settingsBackupFile = File("${internalDataPath}settingsBackup.txt")
val todosFile = File("${internalDataPath}todos.txt")
//val exportFileName = "/storage/emulated/0/Download/dailyInformationExport.txt"
val exportFileName = "${exportPath}dailyInformationExport.csv"

var notifFragment : Fragment? = null
enum class fragments { home, todo, notes, notifs }
var currentFragment : fragments = fragments.home

const val androidBarsSize : Int = 20+41
const val toolBarSize : Int = 47
//const val bottomBarSize : Int =

// ----- Settings -----
// --- overall settings ---
var settingsRead = false
var darkMode = true
// --- home ---
var homeColumns = 2
var homeTextSize = 18f
var startOfWeek : DayOfWeek = DayOfWeek.MONDAY
var selectHoldText = true
var exportLabelLine = true
var exportOrderDefault = "nvit"
var exportCustomDefault = ""
// --- to-do ---
var todoShown = true
var todoColumns = 3
var todoTextSize = 18f
// --- notes ---
var notesShown = true
var notesColumns = 2
var notesTextSize = 18f
var listsColumns = 2
var listsTextSize = 18f
// --- notifs ---
var notificationsShown = true
var oneTimeNotifsColumns = 2
var oneTimeNotifsTextSize = 18f
var dailyNotifsColumns = 2
var dailyNotifsTextSize = 18f
var mainQuickTimerTime = 20
var notificationsFullNameMode = true
var snoozeTime = 5

var navigationView : BottomNavigationView? = null
//var quickTimerButtonCopy :

var mainBinding: ActivityMainBinding? = null

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("tdd-MainActivity", "onCreate run in MainActivity")
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding!!.root)

        val navView: BottomNavigationView = mainBinding!!.navView
        //quickTimerButtonCopy =

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notes, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide();

        navigationView = navView
        updateBottomNavVisibilities()

        dailyNotifications = DailyNotifications(this)

        mainBinding!!.quickTimerButton.setOnClickListener {
            readNotifications()
            dailyNotifications.refreshNotifications(this)

            val notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(60)
            notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "Main Quick Timer",
                notificationDateTime,
                "Main Quick Timer",
                "Your Main quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(this)
            saveNotifications()
        }

        mainBinding!!.helpButton.setOnClickListener {
            val intent = Intent(this as Context, HelpActivity::class.java)
            startActivity(intent)
        }

        mainBinding!!.optionsButton.setOnClickListener {
            val intent = Intent(this as Context, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

}

/** Save the daily information
 * note: the order is from latest to oldest because the priority of this application is fast
 * opening and recording. So the save time is less important than read time.
 */
fun saveDailyInformationFile() {

    if (!dailyInformationFile.exists()) {
        dailyInformationFile.parentFile!!.mkdirs()
        dailyInformationFile.createNewFile()
        dailyInformationFile.writeText(saveInformation.toString())
        return
    }

    if (tempFile.exists()) {
        tempFile.writeText("")
    } else {
        tempFile.parentFile!!.mkdirs()
        tempFile.createNewFile()
    }

    // date protection checks if dates have already been written, and skips them.
    // SHOULD never be needed.
    var dateProtection : Boolean = true
    var dateProtectionArray : ArrayList<LocalDate> = ArrayList()

    var currentWritten : Boolean = false;
    dailyInformationFile.forEachLine {

        // used in date protection
        var doWrite = true
        if (dateProtection) {
            var lineDate: LocalDate = LocalDate.parse(it.split(",")[0])
            if (dateProtectionArray.contains(lineDate))
                doWrite = false

            if (dateProtection)
                dateProtectionArray.add(lineDate)

            if (dateProtection && currentWritten)
                dateProtectionArray.add(saveInformation.date)
        }

        if (doWrite) {
            if (!currentWritten) {
                var lineDate: LocalDate = LocalDate.parse(it.split(",")[0])

                if (saveInformation.date > lineDate) {
                    tempFile.appendText(saveInformation.toString() + "\n")
                    tempFile.appendText(it + "\n")
                    currentWritten = true
                } else if (saveInformation.date == lineDate) {
                    tempFile.appendText(saveInformation.toString() + "\n")
                    currentWritten = true
                } else {
                    tempFile.appendText(it + "\n")
                }

            } else {
                tempFile.appendText(it + "\n")
            }

        }
    }

    if (!currentWritten) {
        tempFile.appendText(saveInformation.toString() + "\n")
    }

    if (!dailyInformationFile.delete()) {
        //Toast.makeText(,"Could not save. Permission denied.", Toast.LENGTH_SHORT).show()
            Log.e("Saving Daily: ", "Failed to save, permission denied")
        tempFile.delete()
        return
    }

    tempFile.renameTo(dailyInformationFile)
}

fun readTodaysDailyInformationFile() {

    if (!dailyInformationFile.exists()) {
        return
    } else {

        var latestLine: String = dailyInformationFile.inputStream().bufferedReader().readLine()
        if (latestLine.isNullOrEmpty())
            return
        var latestDate: LocalDate = LocalDate.parse(latestLine.split(",")[0])

        if (latestDate != LocalDate.now()) {
            saveInformation.copySetup(
                dailyInformationFile.inputStream().bufferedReader().readLine()
            )
        } else {
            saveInformation.fromString(
                dailyInformationFile.inputStream().bufferedReader().readLine()
            )
        }

    }
}

/**
 * Reads the data of the notification file, and saves them in dailyNotifications
 *
 * file format:
 * first line: list of names,times,titles,descriptions with times in the format of 00:00:00
 * second line: same, but with localDateTime
 */
fun readNotifications() {
    if (!notificationsFile.exists()) {
        return
    } else {
        dailyNotifications.fromString(notificationsFile.bufferedReader().readText())
    }
}

fun saveNotifications(notificationSource : DailyNotifications = dailyNotifications) {
    if (!notificationsFile.exists()) {
        notificationsFile.parentFile!!.mkdirs()
        notificationsFile.createNewFile()
    }

    notificationsFile.writeText(notificationSource.toString())
}

/**
 * List:
 * notificationsFullNameMode
 */
fun readSettings() {
    if (settingsRead || !settingsFile.exists()) {
        return
    } else {
        var splitSettings = settingsFile.readText().split("\n")

        try {
            var inputNum = 0

            // -- All --
            darkMode = splitSettings[inputNum++].toBoolean()
            // -- home --
            homeColumns = splitSettings[inputNum++].toInt()
            homeTextSize = splitSettings[inputNum++].toFloat()
            startOfWeek = DayOfWeek.valueOf(splitSettings[inputNum++])
            selectHoldText = splitSettings[inputNum++].toBoolean()
            exportLabelLine = splitSettings[inputNum++].toBoolean()
            exportOrderDefault = splitSettings[inputNum++]
            exportCustomDefault = splitSettings[inputNum++]
            // -- To-do --
            todoShown = splitSettings[inputNum++].toBoolean()
            todoColumns = splitSettings[inputNum++].toInt()
            todoTextSize = splitSettings[inputNum++].toFloat()
            // -- Notes --
            notesShown = splitSettings[inputNum++].toBoolean()
            notesColumns = splitSettings[inputNum++].toInt()
            notesTextSize = splitSettings[inputNum++].toFloat()
            listsColumns = splitSettings[inputNum++].toInt()
            listsTextSize = splitSettings[inputNum++].toFloat()
            // -- Notifs --
            notificationsShown = splitSettings[inputNum++].toBoolean()
            oneTimeNotifsColumns = splitSettings[inputNum++].toInt()
            oneTimeNotifsTextSize = splitSettings[inputNum++].toFloat()
            dailyNotifsColumns = splitSettings[inputNum++].toInt()
            dailyNotifsTextSize = splitSettings[inputNum++].toFloat()
            notificationsFullNameMode = splitSettings[inputNum++].toBoolean()
            snoozeTime = splitSettings[inputNum++].toInt()
        } catch (e : Exception) {
            e.printStackTrace()
            readBackupSettings()
        }
    }

    settingsRead = true
}

fun saveSettings() {
    if (!settingsFile.exists()) {
        settingsFile.parentFile!!.mkdirs()
        settingsFile.createNewFile()
    }

    // -- All --
    settingsFile.writeText("$darkMode\n")
    // -- home --
    settingsFile.appendText("$homeColumns\n")
    settingsFile.appendText("$homeTextSize\n")
    settingsFile.appendText("${startOfWeek.toString()}\n")
    settingsFile.appendText("${selectHoldText}\n")
    settingsFile.appendText("$exportLabelLine\n")
    settingsFile.appendText("$exportOrderDefault\n")
    settingsFile.appendText("$exportCustomDefault\n")
    // -- To-do --
    settingsFile.appendText("$todoShown\n")
    settingsFile.appendText("$todoColumns\n")
    settingsFile.appendText("$todoTextSize\n")
    // -- Notes --
    settingsFile.appendText("$notesShown\n")
    settingsFile.appendText("$notesColumns\n")
    settingsFile.appendText("$notesTextSize\n")
    settingsFile.appendText("$listsColumns\n")
    settingsFile.appendText("$listsTextSize\n")
    // -- Notifs --
    settingsFile.appendText("$notificationsShown\n")
    settingsFile.appendText("$oneTimeNotifsColumns\n")
    settingsFile.appendText("$oneTimeNotifsTextSize\n")
    settingsFile.appendText("$dailyNotifsColumns\n")
    settingsFile.appendText("$dailyNotifsTextSize\n")
    settingsFile.appendText("$notificationsFullNameMode\n")
    settingsFile.appendText("$snoozeTime\n")

    saveBackupSettings()
}

fun readBackupSettings() {

    if (!settingsBackupFile.exists()) {
        return
    } else {
        var splitSettings = settingsBackupFile.readText().split("\n")

        try {

            for (i in 0 until splitSettings.size) {

                if (splitSettings[i].split(" ").size < 2) return

                val splitTitle = splitSettings[i].split(" ")[0]
                val splitValue = splitSettings[i].split(" ")[1]

                // -- All --
                if (splitTitle == "darkMode") darkMode = splitValue.toBoolean()
                // -- home --
                else if (splitTitle == "homeColumns") homeColumns = splitValue.toInt()
                else if (splitTitle == "homeTextSize") homeTextSize = splitValue.toFloat()
                else if (splitTitle == "startOfWeek") startOfWeek = DayOfWeek.valueOf(splitValue)
                else if (splitTitle == "selectHoldText") selectHoldText = splitValue.toBoolean()
                else if (splitTitle == "exportLabelLine") exportLabelLine = splitValue.toBoolean()
                else if (splitTitle == "exportOrderDefault") exportOrderDefault = splitValue
                else if (splitTitle == "exportCustomDefault") exportCustomDefault = splitValue
                // -- To-do --
                else if (splitTitle == "todoShown") todoShown = splitValue.toBoolean()
                else if (splitTitle == "todoColumns") todoColumns = splitValue.toInt()
                else if (splitTitle == "todoTextSize") todoTextSize = splitValue.toFloat()
                // -- Notes --
                else if (splitTitle == "notesShown") notesShown = splitValue.toBoolean()
                else if (splitTitle == "notesColumns") notesColumns = splitValue.toInt()
                else if (splitTitle == "notesTextSize") notesTextSize = splitValue.toFloat()
                else if (splitTitle == "listsColumns") listsColumns = splitValue.toInt()
                else if (splitTitle == "listsTextSize") listsTextSize = splitValue.toFloat()
                // -- Notifs --
                else if (splitTitle == "notificationsShown") notificationsShown = splitValue.toBoolean()
                else if (splitTitle == "oneTimeNotifsColumns") oneTimeNotifsColumns = splitValue.toInt()
                else if (splitTitle == "oneTimeNotifsTextSize") oneTimeNotifsTextSize = splitValue.toFloat()
                else if (splitTitle == "dailyNotifsColumns") dailyNotifsColumns = splitValue.toInt()
                else if (splitTitle == "dailyNotifsTextSize") dailyNotifsTextSize = splitValue.toFloat()
                else if (splitTitle == "notificationsFullNameMode") notificationsFullNameMode = splitValue.toBoolean()
                else if (splitTitle == "snoozeTime") snoozeTime = splitValue.toInt()
            }

        } catch (e : Exception) {
            e.printStackTrace()
        }
    }
    settingsRead = true

}

fun saveBackupSettings() {
    if (!settingsBackupFile.exists()) {
        settingsBackupFile.parentFile!!.mkdirs()
        settingsBackupFile.createNewFile()
    }

    // -- All --
    settingsBackupFile.writeText("darkMode $darkMode\n")
    // -- home --
    settingsBackupFile.appendText("homeColumns $homeColumns\n")
    settingsBackupFile.appendText("homeTextSize $homeTextSize\n")
    settingsBackupFile.appendText("startOfWeek ${startOfWeek.toString()}\n")
    settingsBackupFile.appendText("selectHoldText $selectHoldText\n")
    settingsBackupFile.appendText("exportLabelLine $exportLabelLine\n")
    settingsBackupFile.appendText("exportOrderDefault $exportOrderDefault\n")
    settingsBackupFile.appendText("exportCustomDefault $exportCustomDefault\n")
    // -- To-do --
    settingsBackupFile.appendText("todoShown $todoShown\n")
    settingsBackupFile.appendText("todoColumns $todoColumns\n")
    settingsBackupFile.appendText("todoTextSize $todoTextSize\n")
    // -- Notes --
    settingsBackupFile.appendText("notesShown $notesShown\n")
    settingsBackupFile.appendText("notesColumns $notesColumns\n")
    settingsBackupFile.appendText("notesTextSize $notesTextSize\n")
    settingsBackupFile.appendText("listsColumns $listsColumns\n")
    settingsBackupFile.appendText("listsTextSize $listsTextSize\n")
    // -- Notifs --
    settingsBackupFile.appendText("notificationsShown $notificationsShown\n")
    settingsBackupFile.appendText("oneTimeNotifsColumns $oneTimeNotifsColumns\n")
    settingsBackupFile.appendText("oneTimeNotifsTextSize $oneTimeNotifsTextSize\n")
    settingsBackupFile.appendText("dailyNotifsColumns $dailyNotifsColumns\n")
    settingsBackupFile.appendText("dailyNotifsTextSize $dailyNotifsTextSize\n")
    settingsBackupFile.appendText("notificationsFullNameMode $notificationsFullNameMode\n")
    settingsBackupFile.appendText("snoozeTime $snoozeTime\n")
}

fun updateBottomNavVisibilities() {
    if (navigationView != null && settingsRead) {
        navigationView!!.menu.findItem(R.id.navigation_dashboard).isVisible = todoShown
        navigationView!!.menu.findItem(R.id.navigation_notes).isVisible = notesShown

        navigationView!!.menu.findItem(R.id.navigation_notifications).isVisible = notificationsShown
        mainBinding!!.quickTimerButton.isVisible = notificationsShown
    }
}

fun canExport(activity: Activity, context: Context) : Boolean {
    if (!hasWriteStoragePermission(activity)) {
        Toast.makeText(context,"No write permission :( ", Toast.LENGTH_SHORT).show()
        return false
    }

    if (!hasReadStoragePermission(activity)) {
        Toast.makeText(context,"No read permission :( ", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

fun getExportFile(fileName : String) : File? {
    var exportFile = File(fileName)

    if (exportFile.exists()) {
        var copyNum = 1
        while (true) {
            var tempFileName : File
            if (exportFile.name.contains(".")) {
                val extension: String = exportFile.name.substring(exportFile.name.lastIndexOf("."))
                val filepathMinusExtension: String = exportFile.toString().substring(0, exportFile.toString().lastIndexOf("."))
                tempFileName = File(filepathMinusExtension+"($copyNum)"+extension)
            } else {
                tempFileName = File(exportFile.absolutePath+"($copyNum).txt")
            }

            if (!tempFileName.exists()) {
                exportFile = tempFileName
                break
            }
            copyNum++
        }
    }

    if (!exportFile.exists()) {
        exportFile.parentFile!!.mkdirs()
        exportFile.createNewFile()
    } else
        return null
    return exportFile
}

private fun hasWriteStoragePermission(activity : Activity): Boolean {

    if (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.i("##hasWriteStoragePermission##", "Write permission requested")
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            101
        )
    }
    return ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

private fun hasReadStoragePermission(activity: Activity): Boolean {

    if (!(ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    ) {
        Log.i("##hasReadStoragePermission##", "Read permission requested")
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            102
        )
    }
    return ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}
