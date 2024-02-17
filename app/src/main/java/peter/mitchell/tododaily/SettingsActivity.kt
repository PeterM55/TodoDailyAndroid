package peter.mitchell.tododaily

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import peter.mitchell.tododaily.databinding.HelpScreenBinding
import peter.mitchell.tododaily.databinding.SettingsScreenBinding
import java.time.DayOfWeek

class SettingsActivity : AppCompatActivity() {

    /*
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
var notificationsFullNameMode = true
var snoozeTime = 5
    * */

    enum class SettingType {
        Title,
        Toggle,
        ColumnCount,
        TextSize,
        DayOfWeek,
        Integer,
        String,
    }

    var settingsNameList = arrayOf(
        // -- All --
        //"All",
        //"Dark Mode",
        // button colors?

        // -- Home --
        "Home",
        "Home Columns",
        "Home Text Size",
        "Start of Week",
        "Select Hold Text",
        "Export Line Label",
        "Checked",
        "Un-Checked",
        // export location?

        // -- To-do --
        "Todo",
        "Todo Shown",
        "Todo Confirm Delete",
        "Todo Columns",
        "Todo Text Size",

        // -- Notes --
        "Notes",
        "Notes Shown",
        "Notes Columns",
        "Notes Text Size",
        "Lists Columns",
        "Lists Text Size",

        // -- Notifs --
        "Notifs",
        "Notifs Shown",
        "One Time Columns",
        "One Text Size",
        "Daily Columns",
        "Daily Text Size",
        "Main Quick Timer Time",
        "Daily View Mode",
        "Snooze Time (minutes)",
    )
    var settingsTypeList = arrayOf(
        // -- All --
        //SettingType.Title,
        //SettingType.Toggle,
        // button colors?

        // -- Home --
        SettingType.Title,
        SettingType.ColumnCount,
        SettingType.TextSize,
        SettingType.DayOfWeek,
        SettingType.Toggle,
        SettingType.Toggle,
        SettingType.String,
        SettingType.String,
        // export location?

        // -- To-do --
        SettingType.Title,
        SettingType.Toggle,
        SettingType.Toggle,
        SettingType.ColumnCount,
        SettingType.TextSize,

        // -- Notes --
        SettingType.Title,
        SettingType.Toggle,
        SettingType.ColumnCount,
        SettingType.TextSize,
        SettingType.ColumnCount,
        SettingType.TextSize,

        // -- Notifs --
        SettingType.Title,
        SettingType.Toggle,
        SettingType.ColumnCount,
        SettingType.TextSize,
        SettingType.ColumnCount,
        SettingType.TextSize,
        SettingType.Integer,
        SettingType.Toggle,
        SettingType.Integer,
    )


    private var settingsList : ArrayList<View> = ArrayList(settingsTypeList.size)


    private val LinearLayoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    )

    private lateinit var binding: SettingsScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide();

        if (darkMode)
            binding.mainScrollView.setBackgroundColor(resources.getColor(R.color.backgroundDark))
        else
            binding.mainScrollView.setBackgroundColor(resources.getColor(R.color.backgroundLight))

        binding.settingsBackButton.setOnClickListener {
            saveSettingsScreen()
            finish()
        }

        binding.saveSettingsButton.setOnClickListener {
            saveSettingsScreen()
        }

        for (i in 0 until settingsNameList.size) {

            var linearLayout : LinearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.id = View.generateViewId()
            linearLayout.layoutParams = LinearLayoutParams

            var settingTitle : TextView = TextView(this)
            settingTitle.setText(settingsNameList[i])
            settingTitle.textSize = 22f
            if (darkMode)
                settingTitle.setTextColor(resources.getColor(R.color.textDark))
            else
                settingTitle.setTextColor(resources.getColor(R.color.textLight))
            settingTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            settingTitle.minHeight = 30
            settingTitle.id = View.generateViewId()

            val textLayoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )

            if (settingsTypeList[i] == SettingType.Title)
                textLayoutParams.topMargin = (20 * resources.displayMetrics.density).toInt()

            textLayoutParams.weight = 1f
            settingTitle.layoutParams = textLayoutParams

            linearLayout.addView(settingTitle)

            if (settingsTypeList[i] == SettingType.Title) {
                // do nothing
            } else if (settingsTypeList[i] == SettingType.Toggle) {
                var settingInput : TextView = TextView(this)
                settingInput.setText("Enabled")
                if (darkMode)
                    settingInput.setTextColor(resources.getColor(R.color.textDark))
                else
                    settingInput.setTextColor(resources.getColor(R.color.textLight))
                settingInput.id = View.generateViewId()
                settingInput.minWidth = (100 * resources.displayMetrics.density).toInt()

                val inputLayoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                settingInput.textSize = 18f

                settingInput.setOnClickListener {
                    if (settingInput.text.toString() == "Enabled") {
                        settingInput.setText("Disabled")
                    } else {
                        settingInput.setText("Enabled")
                    }
                }

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            } else if (settingsTypeList[i] == SettingType.ColumnCount) {
                var settingInput: Spinner = Spinner(this)
                settingInput.id = View.generateViewId()
                settingInput.minimumHeight = 0
                settingInput.minimumWidth = (100 * resources.displayMetrics.density).toInt()

                val spinnerList = arrayOf("1", "2", "3", "4", "5", "6")
                settingInput.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    spinnerList
                )

                val inputLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            } else if (settingsTypeList[i] == SettingType.TextSize) {
                var settingInput = EditText(this)
                settingInput.id = View.generateViewId()
                settingInput.minWidth = (100 * resources.displayMetrics.density).toInt()
                if (darkMode)
                    settingInput.setTextColor(resources.getColor(R.color.textDark))
                else
                    settingInput.setTextColor(resources.getColor(R.color.textLight))

                settingInput.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL

                val inputLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            } else if (settingsTypeList[i] == SettingType.DayOfWeek) {
                var settingInput: Spinner = Spinner(this)
                settingInput.id = View.generateViewId()
                settingInput.minimumHeight = 0
                settingInput.minimumWidth = (100 * resources.displayMetrics.density).toInt()

                settingInput.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    daysOfTheWeek
                )

                val inputLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            } else if (settingsTypeList[i] == SettingType.Integer) {
                var settingInput = EditText(this)
                settingInput.id = View.generateViewId()
                settingInput.minWidth = (100 * resources.displayMetrics.density).toInt()
                if (darkMode)
                    settingInput.setTextColor(resources.getColor(R.color.textDark))
                else
                    settingInput.setTextColor(resources.getColor(R.color.textLight))

                settingInput.inputType = InputType.TYPE_CLASS_NUMBER

                val inputLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            } else if (settingsTypeList[i] == SettingType.String) {
                var settingInput = EditText(this)
                settingInput.id = View.generateViewId()
                settingInput.minWidth = (100 * resources.displayMetrics.density).toInt()
                if (darkMode)
                    settingInput.setTextColor(resources.getColor(R.color.textDark))
                else
                    settingInput.setTextColor(resources.getColor(R.color.textLight))

                settingInput.inputType = InputType.TYPE_CLASS_TEXT

                val inputLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            } else {
                var settingInput : Button = Button(this)
                settingInput.setText("---")
                settingInput.id = View.generateViewId()

                settingInput.minHeight = 0
                settingInput.minWidth = (100 * resources.displayMetrics.density).toInt()

                val inputLayoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (43 * resources.displayMetrics.density).toInt(),
                )
                settingInput.layoutParams = inputLayoutParams

                linearLayout.addView(settingInput)
                settingsList.add(settingInput)
            }

            binding.overallLinearLayout.addView(linearLayout)

        }

        var countingIndex : Int = 0

        // "All",
        // "Dark Mode",
        //(settingsList[countingIndex++] as TextView).text = getBooleanString(darkMode)
        // button colors?

        // -- Home --
        // "Home",
        // "Home Columns",
        (settingsList[countingIndex++] as Spinner).setSelection(homeColumns-1)
        // "Home Text Size",
        (settingsList[countingIndex++] as EditText).setText(homeTextSize.toString())
        // "Start of Week",
        (settingsList[countingIndex++] as Spinner).setSelection(getDayIndex(dayObjectToString(startOfWeek)))
        // "Select Hold Text",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(selectHoldText)
        // "Export Line Label",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(exportLabelLine)
        // "Checked",
        (settingsList[countingIndex++] as TextView).text = checkedString
        // "UnChecked",
        (settingsList[countingIndex++] as TextView).text = unCheckedString
        // export location?

        // -- To-do --
        // "To-do",
        //"To-do Shown",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(todoShown)
        //"To-do Confirm Delete",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(todoConfirmDelete)
        // "To-do Columns",
        (settingsList[countingIndex++] as Spinner).setSelection(todoColumns-1)
        // "To-do Text Size",
        (settingsList[countingIndex++] as EditText).setText(todoTextSize.toString())

        // -- Notes --
        //"Notes",
        //"Notes Shown",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(notesShown)
        //"Notes Columns",
        (settingsList[countingIndex++] as Spinner).setSelection(notesColumns-1)
        //"Notes Text Size",
        (settingsList[countingIndex++] as EditText).setText(notesTextSize.toString())
        //"Lists Columns",
        (settingsList[countingIndex++] as Spinner).setSelection(listsColumns-1)
        //"Lists Text Size",
        (settingsList[countingIndex++] as EditText).setText(listsTextSize.toString())

        // -- Notifs --
        //"Notifs",
        //"Notifs Shown",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(notificationsShown)
        //"One Time Columns",
        (settingsList[countingIndex++] as Spinner).setSelection(oneTimeNotifsColumns-1)
        //"One Text Size",
        (settingsList[countingIndex++] as EditText).setText(oneTimeNotifsTextSize.toString())
        //"Daily Columns",
        (settingsList[countingIndex++] as Spinner).setSelection(dailyNotifsColumns-1)
        //"Daily Text Size",
        (settingsList[countingIndex++] as EditText).setText(dailyNotifsTextSize.toString())
        //"mainQuickTimerTime",
        (settingsList[countingIndex++] as EditText).setText(mainQuickTimerTime.toString())
        //"Daily View Mode",
        (settingsList[countingIndex++] as TextView).text = getBooleanString(notificationsFullNameMode)
        //"Snooze time (minutes)",
        (settingsList[countingIndex++] as EditText).setText(snoozeTime.toString())

    }

    fun saveSettingsScreen() {
        var countingIndex : Int = 0

        // "All",
        // "Dark Mode",
        //darkMode = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        // button colors?

        // -- Home --
        // "Home",
        // "Home Columns",
        homeColumns = (settingsList[countingIndex++] as Spinner).selectedItem.toString().toInt()
        // "Home Text Size",
        homeTextSize = (settingsList[countingIndex++] as EditText).text.toString().toFloat()
        // "Start of Week",
        startOfWeek = dayIndexToObject((settingsList[countingIndex++] as Spinner).selectedItemPosition)
        // "Select Hold Text",
        selectHoldText = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        // "Export Line Label",
        exportLabelLine = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        // "Checked",
        checkedString = (settingsList[countingIndex++] as TextView).text.toString().replace("\n","")
        // "UnChecked",
        unCheckedString = (settingsList[countingIndex++] as TextView).text.toString().replace("\n","")
        // export location?

        // -- To-do --
        // "To-do",
        //"To-do Shown",
        todoShown = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        //"To-do Confirm Delete",
        todoConfirmDelete = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        // "To-do Columns",
        todoColumns = (settingsList[countingIndex++] as Spinner).selectedItem.toString().toInt()
        // "To-do Text Size",
        todoTextSize = (settingsList[countingIndex++] as EditText).text.toString().toFloat()

        // -- Notes --
        //"Notes",
        //"Notes Shown",
        notesShown = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        //"Notes Columns",
        notesColumns = (settingsList[countingIndex++] as Spinner).selectedItem.toString().toInt()
        //"Notes Text Size",
        notesTextSize = (settingsList[countingIndex++] as EditText).text.toString().toFloat()
        //"Lists Columns",
        listsColumns = (settingsList[countingIndex++] as Spinner).selectedItem.toString().toInt()
        //"Lists Text Size",
        listsTextSize = (settingsList[countingIndex++] as EditText).text.toString().toFloat()

        // -- Notifs --
        //"Notifs",
        //"Notifs Shown",

        // Special case for notifications shown, cancel pending notifications to stop it from activating while disabled
        if (notificationsShown && !getStringBoolean((settingsList[countingIndex] as TextView).text.toString())) {
            WorkManager.getInstance(this).cancelAllWork()
        }

        notificationsShown = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        //"One Time Columns",
        oneTimeNotifsColumns = (settingsList[countingIndex++] as Spinner).selectedItem.toString().toInt()
        //"One Text Size",
        oneTimeNotifsTextSize = (settingsList[countingIndex++] as EditText).text.toString().toFloat()
        //"Daily Columns",
        dailyNotifsColumns = (settingsList[countingIndex++] as Spinner).selectedItem.toString().toInt()
        //"Daily Text Size",
        dailyNotifsTextSize = (settingsList[countingIndex++] as EditText).text.toString().toFloat()
        //"mainQuickTimerTime",
        mainQuickTimerTime = (settingsList[countingIndex++] as EditText).text.toString().toInt()
        //"Daily View Mode",
        notificationsFullNameMode = getStringBoolean((settingsList[countingIndex++] as TextView).text.toString())
        //"Snooze time (minutes)",
        snoozeTime = (settingsList[countingIndex++] as EditText).text.toString().toInt()

        saveSettings()

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
    }

    fun getBooleanString(bool : Boolean) : String {
        return if (bool) "Enabled"
        else "Disabled"
    }
    fun getStringBoolean(str : String) : Boolean{
        return str == "Enabled"
    }

    val daysOfTheWeek = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    fun getDayIndex(day : String) : Int {
        for (i in 0 until daysOfTheWeek.size) {
            if (day == daysOfTheWeek[i])
                return i
        }
        return -1
    }
    fun dayObjectToString(day : DayOfWeek) : String {
        if (day == DayOfWeek.MONDAY) {
            return daysOfTheWeek[0]
        } else if (day == DayOfWeek.TUESDAY) {
            return daysOfTheWeek[1]
        } else if (day == DayOfWeek.WEDNESDAY) {
            return daysOfTheWeek[2]
        } else if (day == DayOfWeek.THURSDAY) {
            return daysOfTheWeek[3]
        } else if (day == DayOfWeek.FRIDAY) {
            return daysOfTheWeek[4]
        } else if (day == DayOfWeek.SATURDAY) {
            return daysOfTheWeek[5]
        } else if (day == DayOfWeek.SUNDAY) {
            return daysOfTheWeek[6]
        }
        return daysOfTheWeek[0]
    }

    fun dayIndexToObject(day : Int) : DayOfWeek {
        if (day == 0) {
            return DayOfWeek.MONDAY
        } else if (day == 1) {
            return DayOfWeek.TUESDAY
        } else if (day == 2) {
            return DayOfWeek.WEDNESDAY
        } else if (day == 3) {
            return DayOfWeek.THURSDAY
        } else if (day == 4) {
            return DayOfWeek.FRIDAY
        } else if (day == 5) {
            return DayOfWeek.SATURDAY
        } else if (day == 6) {
            return DayOfWeek.SUNDAY
        }
        return DayOfWeek.MONDAY
    }


    override fun onBackPressed() {
        super.onBackPressed()
        saveSettingsScreen()
    }
}