package peter.mitchell.tododaily

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.new_notification.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SettingsActivity : AppCompatActivity() {

    /*
// ----- Settings -----
    // --- overall settings ---
var settingsRead = false
var darkMode = true
    // --- home ---
var homeColumns = 2
var homeTextSize = 18
var startOfWeek : DayOfWeek = DayOfWeek.MONDAY
var exportLabelLine = true
var exportOrderDefault = "nvit"
var exportCustomDefault = ""
    // --- to-do ---
var todoColumns = 3
var todoTextSize = 18
    // --- notes ---
var notesColumns = 2
var notesTextSize = 18
var listsColumns = 2
var listsTextSize = 18
    // --- notifs ---
var oneTimeNotifsColumns = 2
var oneTimeNotifsTextSize = 18
var dailyNotifsColumns = 2
var dailyNotifsTextSize = 18
var notificationsFullNameMode = true
var snoozeTime = 360
    * */

    var settingsNameList = arrayOf(
        // -- All --
        "Dark Mode",
        // button colors?

        // -- Home --
        "Home Columns",
        "Home Text Size",
        "Start of Week",
        "Export Line Label",
        // export location?

        // -- To-do --
        "Todo Columns",
        "Todo Text Size",

        // -- Notes --
        "Notes Columns",
        "Notes Text Size",
        "Lists Columns",
        "Lists Text Size",

        // -- Notifs --
        "One Time Columns",
        "One Text Size",
        "Daily Columns",
        "Daily Text Size",
        "Daily View Mode",
        "Snooze time (seconds)",
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)

        //oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)



    }

}