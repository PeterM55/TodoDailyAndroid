package peter.mitchell.tododaily

import android.Manifest
import android.app.Activity
import android.content.Context
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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import peter.mitchell.tododaily.databinding.ActivityMainBinding
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.HelperClasses.TodoLists
import peter.mitchell.tododaily.ui.notifications.DailyNotifications
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

var saveInformation : SaveInformation = SaveInformation()
lateinit var dailyNotifications : DailyNotifications
var todoLists : TodoLists? = null // only used in to-do section, but kept in memory to not have to read every time

//FIXME
//val dailyInformationFile = File("${requireContext().filesDir.path}/dailyInformation.txt")
//val tempFile = File("${requireContext().filesDir.path}/tempDailyInformation.txt")
val dailyInformationFile = File("/data/data/peter.mitchell.tododaily/files/dailyInformation.txt")
val tempFile = File("/data/data/peter.mitchell.tododaily/files/tempDailyInformation.txt")
val notificationsFile = File("/data/data/peter.mitchell.tododaily/files/dailyNotifications.txt")
val nextNotificationIntentFile = File("/data/data/peter.mitchell.tododaily/files/nextNotificationIntent.txt")
val settingsFile = File("/data/data/peter.mitchell.tododaily/files/settings.txt")
val todosFile = File("/data/data/peter.mitchell.tododaily/files/todos.txt")
//val exportFileName = "/storage/emulated/0/Download/dailyInformationExport.txt"
val exportFileName = "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DOWNLOADS}/dailyInformationExport.csv"

const val androidBarsSize : Int = 20+41
const val toolBarSize : Int = 47
//const val bottomBarSize : Int =

// ----- Settings -----
var settingsRead = false
var notificationsFullNameMode = true
var startOfWeek : DayOfWeek = DayOfWeek.MONDAY
var darkMode = true
var exportLabelLine = true
var exportOrderDefault = "nvit"
var exportCustomDefault = ""

var mainBinding: ActivityMainBinding? = null

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding!!.root)

        val navView: BottomNavigationView = mainBinding!!.navView

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

        dailyNotifications = DailyNotifications(this)
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
    if (!settingsFile.exists()) {
        return
    } else {
        var splitSettings = settingsFile.readText().split(" ")

        notificationsFullNameMode = splitSettings[0].toBoolean()
    }
}

fun saveSettings() {
    if (!settingsFile.exists()) {
        settingsFile.parentFile!!.mkdirs()
        settingsFile.createNewFile()
    }

    settingsFile.writeText("$notificationsFullNameMode ")
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

fun getExportFile() : File? {
    var exportFile = File(exportFileName)

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
