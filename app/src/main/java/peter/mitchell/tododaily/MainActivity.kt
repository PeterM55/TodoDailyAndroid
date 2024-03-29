package peter.mitchell.tododaily

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import peter.mitchell.tododaily.HelperClasses.NotesList
import peter.mitchell.tododaily.HelperClasses.NotifWorker
import peter.mitchell.tododaily.databinding.ActivityMainBinding
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.HelperClasses.TodoLists
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import peter.mitchell.tododaily.ui.notifications.channelID
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

var saveInformation : SaveInformation = SaveInformation()
lateinit var dailyNotifications : DailyNotifications
var todoLists : TodoLists? = null // only used in to-do section, but kept in memory to not have to read every time
var notesList : NotesList? = null

val internalDataPath = "${Environment.getDataDirectory().absolutePath}/data/peter.mitchell.tododaily/files/"
val exportPath = "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DOWNLOADS}/"

val dailyInformationFile = File("${internalDataPath}dailyInformation.txt")
val tempFile = File("${internalDataPath}tempDailyInformation.txt")
val tempFile2 = File("${internalDataPath}tempDailyInformation2.txt")
val notificationsFile = File("${internalDataPath}dailyNotifications.txt")
val nextNotificationIntentFile = File("${internalDataPath}nextNotificationIntent.txt")
val settingsFile = File("${internalDataPath}settings.txt")
val settingsBackupFile = File("${internalDataPath}settingsBackup.txt")
val todosFile = File("${internalDataPath}todos.txt")
val exportFileName = "${exportPath}dailyInformationExport.csv"
//val importFileName = "${exportPath}todoDailyImport.txt"

var notifFragment : Fragment? = null
enum class fragments { home, todo, notes, notifs }
var currentFragment : fragments = fragments.home

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
var exportOrderDefault = "v"
var exportCustomDefault = ""
var checkedString = "1"
var unCheckedString = "0"
// --- to-do ---
var todoShown = true
var todoConfirmDelete = true
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
var mainQuickTimerTime = 15
var notificationsFullNameMode = true
var snoozeTime = 5

var navigationView : BottomNavigationView? = null

var mainBinding: ActivityMainBinding? = null

lateinit var debugContext : Context;

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("tdd-MainActivity", "onCreate run in MainActivity")
        super.onCreate(savedInstanceState)

        /*WorkManager.getInstance(this).cancelAllWork()

        debugContext = this

        val testWorkTag = "111111111111111111111111"
        val notificationWork : OneTimeWorkRequest = OneTimeWorkRequest.Builder(NotifWorker::class.java)
            //.setInitialDelay(1, TimeUnit.SECONDS)
            .addTag(testWorkTag)
            .build()

        var operation = WorkManager.getInstance(this).enqueue(notificationWork)

        //Log.i("tdd-enqueue", "Work enqueued: ${operation.state.observe()}")
        //Log.i("tdd-enqueue", "Work enqueued: ${WorkManager.getInstance(this).getWorkInfosByTag(testWorkTag).get()[0].tags.contains(testWorkTag) }")
        //Log.i("tdd-enqueue", "Work enqueued: ${WorkManager.getInstance(this).getWorkInfosByTag(testWorkTag).isDone}")

        Toast.makeText(this, "Work enqueued 1", Toast.LENGTH_SHORT).show()
        Log.i("tdd-enqueue", "Work enqueued for 1 seconds from now")*/

        //deleteFile(dailyInformationFile.name)
        /*dailyInformationFile.writeText("")
*/

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

            val notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(
                mainQuickTimerTime.toLong()
            )

            //Toast.makeText(this, "main quick timer: ${mainQuickTimerTime.toLong()}", Toast.LENGTH_SHORT).show()

            notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "Main Quick Timer",
                0,
                notificationDateTime,
                "Main Quick Timer",
                "Your Main quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(this)
            saveNotifications()

            notifFragment?.onResume()
        }

        mainBinding!!.helpButton.setOnClickListener {
            val intent = Intent(this as Context, HelpActivity::class.java)
            startActivity(intent)
        }

        mainBinding!!.optionsButton.setOnClickListener {
            val intent = Intent(this as Context, SettingsActivity::class.java)
            startActivity(intent)
        }

    } // end of on-create

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
        Log.e("Saving Daily: ", "Failed to save, permission denied")
        tempFile.delete()
        return
    }

    tempFile.renameTo(dailyInformationFile)
}

/** Read the daily information for today
 * this sets the SaveInformation variable
 */
fun readTodaysDailyInformationFile() {

    if (!dailyInformationFile.exists()) {
        return
    } else {

        var testBuffer = dailyInformationFile.inputStream().bufferedReader()
        if (!testBuffer.ready()) return
        var latestLine: String = testBuffer.readLine()

        if (latestLine.isNullOrEmpty())
            return
        var latestDate: LocalDate = LocalDate.parse(latestLine.split(",")[0])

        if (latestDate != LocalDate.now()) {
            saveInformation.copySetup(
                latestLine
            )
        } else {
            saveInformation.fromString(
                latestLine
            )
        }

    }
}

/** Export the information, if they are null the defaults (in settings) will be used
 * @param activity used to check permissions
 * @param context used to check permissions
 * @param lineLabelIn whether the label line should be created
 * @param exportOrderIn the order text to export (won't be used if exportCustomIn is not "")
 * @param exportCustomIn the custom text to export
 * @param setAsDefault whether to set the export as the default
 */
fun exportDailyInformation(activity: Activity, context: Context,
                           lineLabelIn : Boolean? = null, exportOrderIn : String? = null,
                           exportCustomIn : String? = null, setAsDefault : Boolean = false
) {
    if (!dailyInformationFile.exists() || !canExport(activity, context))
        return

    val exportFile : File? = getExportFile(exportFileName)

    if (exportFile == null)
        return

    var lineLabel = true
    var exportOrder = ""
    var exportCustom = ""

    if (lineLabelIn == null) lineLabel = exportLabelLine
    else lineLabel = lineLabelIn

    if (exportOrderIn == null) exportOrder = exportOrderDefault
    else exportOrder = exportOrderIn

    if (exportCustomIn == null) exportCustom = exportCustomDefault
    else exportCustom = exportCustomIn

    // -- do the actual export --
    if (lineLabel) {
        var colInfo : ArrayList<SaveInformation.ValueInfo> = ArrayList()
        var tempSaveInformation : SaveInformation = SaveInformation()

        exportFile.writeText("")

        if (exportCustom.isEmpty()) {
            colInfo = tempSaveInformation.setupColInfoForOrder()
            exportFile.appendText("Date,")
            for (i in 0 until colInfo.size) {
                exportFile.appendText("${colInfo[i].name},")
            }
            exportFile.appendText("\n")
        }

        dailyInformationFile.forEachLine {
            tempSaveInformation.fromString(it)

            if (exportCustom.isNotEmpty()) {
                exportFile.appendText(tempSaveInformation.exportToCustomByNames(exportCustom, colInfo)+"\n")
            } else {
                exportFile.appendText(tempSaveInformation.exportToOrderByNames(exportOrder, colInfo)+"\n")
            }
        }

    } else {
        var tempSaveInformation : SaveInformation = SaveInformation()

        exportFile.writeText("")

        dailyInformationFile.forEachLine {
            tempSaveInformation.fromString(it)

            if (exportCustom.isNotEmpty()) {
                exportFile.appendText(tempSaveInformation.exportToCustomString(exportCustom) + "\n")
            } else {
                exportFile.appendText(tempSaveInformation.exportToCustomOrder(exportOrder) + "\n")
            }
        }
    }

    if (setAsDefault) {
        setDefaultExport(lineLabel, exportOrder, exportCustom)
    }

}

/** sets the default export settings
 */
fun setDefaultExport(lineLabel : Boolean, exportOrder : String, exportCustom : String) {
    exportLabelLine = lineLabel
    if (exportCustomDefault.isNotEmpty()) {
        exportOrderDefault = ""
        exportCustomDefault = exportCustom.replace('\n',' ')
    } else {
        exportOrderDefault = exportOrder.replace('\n',' ')
        exportCustomDefault = ""
    }
    saveSettings()
}

/**
 * Reads the data of the notification file, and saves them in dailyNotifications
 * (Just calls fromString in daily notifications)
 */
fun readNotifications() {
    if (!notificationsFile.exists()) {
        return
    } else {
        dailyNotifications.fromString(notificationsFile.bufferedReader().readText())
    }
}

/** Saves the provided DailyNotifications object to the notificationsFile
 * (creates the directories if needed)
 */
fun saveNotifications(notificationSource : DailyNotifications = dailyNotifications) {
    if (!notificationsFile.exists()) {
        notificationsFile.parentFile!!.mkdirs()
        notificationsFile.createNewFile()
    }

    notificationsFile.writeText(notificationSource.toString())
}

/** Reads the settings file
 * goes line by line reading the settings, if they are malformed it calls readBackupSettings
 * (lines are not labeled to save time reading, I know it isn't really necessary, but I did anyway)
 * readBackupSettings does use labels, which is important if more settings need to be added to keep
 * the old ones
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
            checkedString = splitSettings[inputNum++]
            unCheckedString = splitSettings[inputNum++]
            // -- To-do --
            todoShown = splitSettings[inputNum++].toBoolean()
            todoConfirmDelete = splitSettings[inputNum++].toBoolean()
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
            mainQuickTimerTime = splitSettings[inputNum++].toInt()
            notificationsFullNameMode = splitSettings[inputNum++].toBoolean()
            snoozeTime = splitSettings[inputNum++].toInt()
        } catch (e : Exception) {
            e.printStackTrace()
            readBackupSettings()
        }
    }

    settingsRead = true
}

/** Saves the settings to the settings file
 * simply goes line by line writing the text
 */
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
    settingsFile.appendText("$checkedString\n")
    settingsFile.appendText("$unCheckedString\n")
    // -- To-do --
    settingsFile.appendText("$todoShown\n")
    settingsFile.appendText("$todoConfirmDelete\n")
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
    settingsFile.appendText("$mainQuickTimerTime\n")
    settingsFile.appendText("$notificationsFullNameMode\n")
    settingsFile.appendText("$snoozeTime\n")

    saveBackupSettings()
}

/** Reads the backup settings file
 * goes line by line, checking the string against each option, saving it to the one it matches.
 */
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
                else if (splitTitle == "checkedString") checkedString = splitValue
                else if (splitTitle == "unCheckedString") unCheckedString = splitValue
                // -- To-do --
                else if (splitTitle == "todoShown") todoShown = splitValue.toBoolean()
                else if (splitTitle == "todoConfirmDelete") todoConfirmDelete = splitValue.toBoolean()
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
                else if (splitTitle == "mainQuickTimerTime") mainQuickTimerTime = splitValue.toInt()
                else if (splitTitle == "notificationsFullNameMode") notificationsFullNameMode = splitValue.toBoolean()
                else if (splitTitle == "snoozeTime") snoozeTime = splitValue.toInt()
            }

        } catch (e : Exception) {
            e.printStackTrace()
        }
    }
    settingsRead = true

}

/** Saves the backup settings file
 * applies a name to each line so they can be identified when reading
 */
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
    settingsBackupFile.appendText("checkedString $checkedString\n")
    settingsBackupFile.appendText("unCheckedString $unCheckedString\n")
    // -- To-do --
    settingsBackupFile.appendText("todoShown $todoShown\n")
    settingsBackupFile.appendText("todoConfirmDelete $todoConfirmDelete\n")
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
    settingsBackupFile.appendText("mainQuickTimerTime $mainQuickTimerTime\n")
    settingsBackupFile.appendText("notificationsFullNameMode $notificationsFullNameMode\n")
    settingsBackupFile.appendText("snoozeTime $snoozeTime\n")
}

/** Shows/Hides the bottom nav menu items
 */
fun updateBottomNavVisibilities() {
    if (navigationView != null && settingsRead) {
        navigationView!!.menu.findItem(R.id.navigation_dashboard).isVisible = todoShown
        navigationView!!.menu.findItem(R.id.navigation_notes).isVisible = notesShown

        navigationView!!.menu.findItem(R.id.navigation_notifications).isVisible = notificationsShown
        mainBinding!!.quickTimerButton.isVisible = notificationsShown
    }
}

/** Checks for permissions to export, asking if false
 * Checks for both read and write permissions for exporting/importing files
 *
 * @param activity needed for permissions check
 * @param context needed for permissions check
 * @return whether the permission has been granted
 */
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

/** Part of canExport, checks/asks for write permissions
 *
 * @param activity needed for permissions check
 * @return whether the permission has been granted
 */
private fun hasWriteStoragePermission(activity : Activity): Boolean {

    if (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.i("tdd-hasWriteStoragePermission", "Write permission requested")
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

/** Part of canExport, checks/asks for read permissions
 *
 * @param activity needed for permissions check
 * @return whether the permission has been granted
 */
private fun hasReadStoragePermission(activity: Activity): Boolean {

    if (!(ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    ) {
        Log.i("tdd-hasReadStoragePermission", "Read permission requested")
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

/** uses the fileName to create an absolute path for the export file
 * gets the name that can work for the export file, if it exists it counts the number in brackets
 * like when windows creates a copy.
 *
 * @param fileName the name of the file, do not include the path
 * @return the final path to be used
 */
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
