package peter.mitchell.tododaily.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.manage_daily_information.*
import kotlinx.coroutines.selects.select
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.ui.notifications.NotificationsFragment
import java.io.File
import java.lang.NumberFormatException
import java.time.LocalDate

/** Manage daily notifications is an activity that handles managing the information displayed on the
 * home page. This includes rearranging, deleting, importing, and exporting.
 *
 */
class ManageDailyNotifications : AppCompatActivity() {

    var datesList : ArrayList<String> = ArrayList()
    var deleteMultipleMode : Boolean = false
    private lateinit var imm: InputMethodManager

    var selectedSortIndex = 0

    var currentTitlesVisible : Boolean = false
    var rearrangeTitlesVisible : Boolean = false
    var manageDatesVisible : Boolean = false
    var addDatesVisible : Boolean = false
    var importVisible : Boolean = false
    var exportVisible : Boolean = false

    private val exportPresetsFile = File("${internalDataPath}exportPresets.txt")
    class ExportPresetFormat(nameIn : String, lineLabelsIn : Boolean, customIn : Boolean, exportStringIn : String) {
        var name : String = nameIn;
        var lineLabels : Boolean = lineLabelsIn;
        var custom : Boolean = customIn
        var exportString : String = exportStringIn;
    };
    var exportPresets = ArrayList<ExportPresetFormat>()
    var defaultExportPresets : ArrayList<ExportPresetFormat> = arrayListOf(
        ExportPresetFormat("New", true, false, ""),
        ExportPresetFormat("Standard", true, false, "v"),
        ExportPresetFormat("Raw Data", false, false, "nvit"),
    )
    var defaultPreset : Int = 1
    var selectedPreset : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_daily_information)
        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (darkMode)
            mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        manageMainReminders.setCustomColumnCount(homeColumns)
        manageMainReminders.setTextSize(homeTextSize)
        manageAllTitles.setCustomColumnCount(homeColumns)
        manageAllTitles.setTextSize(homeTextSize)

        manageDatesList.setCustomColumnCount(3)

        manageMainReminders.isVisible = currentTitlesVisible
        manageAllTitles.isVisible = rearrangeTitlesVisible
        manageDatesList.isVisible = manageDatesVisible
        toggleManageDatesText.isVisible = manageDatesVisible

        readExportPresets()
        if (dailyInformationFile.exists()) {
            dailyInformationFile.forEachLine {
                datesList.add(it.split(",")[0]) //LocalDate.parse
            }
        }

        reloadCurrentTitles()
        setupDates()

        toggleMainReminders.setOnClickListener {
            currentTitlesVisible = !currentTitlesVisible
            reloadVisibilities()
        }
        toggleRearrangeTitles.setOnClickListener {
            rearrangeTitlesVisible = !rearrangeTitlesVisible
            reloadVisibilities()
        }
        toggleManageDatesVisibilityButton.setOnClickListener {
            manageDatesVisible = !manageDatesVisible
            reloadVisibilities()
        }
        addDateVisibilityButton.setOnClickListener {
            addDatesVisible = !addDatesVisible

            if (addDatesVisible) {
                currentTitlesVisible = false
                rearrangeTitlesVisible = false
                manageDatesVisible = false
            }
            reloadVisibilities()
        }


        deleteAllMainRemindersButton.setOnClickListener {

            MaterialAlertDialogBuilder(this).setTitle("delete all titles?")
                .setMessage("Would you like to delete all of today's items? (this will not remove the titles from any date but today)")
                .setNegativeButton("Cancel") { dialog, which ->

                }.setPositiveButton("Delete") { dialog, which ->
                    saveInformation.resetData()
                    saveDailyInformationFile()
                    reloadCurrentTitles()
                }.show()
        }

        toggleManageDatesButton.setOnClickListener {
            deleteMultipleMode = !deleteMultipleMode
            if (deleteMultipleMode) {
                toggleManageDatesText.setText("Delete all before date")
            } else {
                toggleManageDatesText.setText("Single delete")
            }
        }

        deleteAllDatesButton.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("delete all data?")
                .setMessage("This will delete all saved data, except today.")
                .setNegativeButton("Cancel") { dialog, which ->

                }.setPositiveButton("Delete") { dialog, which ->
                    deleteAllDates()
                    setupDates()
                    saveDailyInformationFile()
                }.show()
        }

        addDateButton.setOnClickListener {

            if (!dailyInformationFile.exists()) {
                dailyInformationFile.parentFile!!.mkdirs()
                dailyInformationFile.createNewFile()
            }

            var newDate = LocalDate.of(addDateInput.year,addDateInput.month+1,addDateInput.dayOfMonth)

            var done = false
            dailyInformationFile.forEachLine {
                var lineDate: LocalDate = LocalDate.parse(it.split(",")[0])
                if (newDate == lineDate) {
                    saveInformation.fromString(it)
                    done = true
                }
            }

            if (!done) {
                saveInformation.date = newDate
                saveInformation.clearValues()
                saveDailyInformationFile()
            }
        }

        exportOptionsExpandButton.setOnClickListener {
            exportVisible = !exportVisible
            reloadVisibilities()
            setExportPresetsInputPosition(selectedPreset)
        }

        reloadExportPresetsInput()

        importOptionsExpandButton.setOnClickListener {
            importVisible = !importVisible
            reloadVisibilities()
        }
        importButton.setOnClickListener {
            importSubmit()
        }

        exportPresetsInput.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setExportPresetsInputPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /*readDailyInformationFile()
                        reloadMainReminders()*/
            }
        }

        labelExportCheck.setOnClickListener {
            exportLabelLine = !exportLabelLine
            saveSettings()
        }
        labelExportCheck.isChecked = exportLabelLine

        customExportInput.addTextChangedListener {
            if (customExportInput.text.isNotEmpty()) {
                //exportExplanation1.isVisible = false
                customOrderTitle.isVisible = false
                customOrderInput.isVisible = false
            } else {
                //exportExplanation1.isVisible = true
                customOrderTitle.isVisible = true
                customOrderInput.isVisible = true
            }
        }
        customOrderInput.setText(exportOrderDefault)
        customExportInput.setText(exportCustomDefault)

        deleteExportPreset.setOnClickListener {
            if (selectedPreset > 2) {
                exportPresets.removeAt(selectedPreset)
                if (selectedPreset == defaultPreset)
                    defaultPreset = 1

                saveExportPresets()
                reloadVisibilities()
                setExportPresetsInputPosition(selectedPreset-1)
                reloadExportPresetsInput()
            }
        }

        saveExportPreset.setOnClickListener {
            saveExportPresetPressed()
        }

        exportButton.setOnClickListener { customExportSubmit() }
        // end of onCreateView
    }

    /** Reloads the current titles sections for deleting and rearranging the main information */
    private fun reloadCurrentTitles() {

        var informationViewList = ArrayList<String>()

        for (i in 0 until saveInformation.length) {
            informationViewList.add("$i: "+saveInformation.names[i])

        }

        manageMainReminders.setAdapter(this, informationViewList)
        manageAllTitles.setAdapter(this, informationViewList)

        for (i in 0 until informationViewList.size) {
            manageMainReminders.textGrid[i].setOnClickListener {
                MaterialAlertDialogBuilder(this).setTitle("delete entry from today?")
                    .setMessage("Would you like to delete this item? (this will not remove the title from any date but today)")
                    .setNegativeButton("Cancel") { dialog, which ->

                    }.setPositiveButton("Delete") { dialog, which ->
                        saveInformation.deleteValue(i)
                        saveDailyInformationFile()
                        reloadCurrentTitles()
                    }.show()
            }

            manageAllTitles.textGrid[i].setOnClickListener {
                showSortInputDialog(i)
            }
        }
    }

    /** Reloads what is visible and what is not, hiding closed sections */
    private fun reloadVisibilities() {

        if (addDatesVisible && (currentTitlesVisible || rearrangeTitlesVisible || manageDatesVisible)) {
            addDatesVisible = false
        }

        manageMainReminders.isVisible = currentTitlesVisible
        manageAllTitles.isVisible = rearrangeTitlesVisible
        manageDatesList.isVisible = manageDatesVisible
        toggleManageDatesText.isVisible = manageDatesVisible
        addDateInput.isVisible = addDatesVisible
        addDateButton.isVisible = addDatesVisible

        importText.isVisible = importVisible
        importButton.isVisible = importVisible

        exportPresetsTitle.isVisible = exportVisible
        exportPresetsInput.isVisible = exportVisible
        val exportPresetNew = exportPresetsInput.selectedItemPosition == 0 || exportPresetsInput.selectedItemPosition > 2

        labelExportCheck.isVisible = exportVisible
        exportExplanation1.isVisible = exportVisible && exportPresetNew
        if (exportPresetNew && exportVisible && customExportInput.text.isEmpty()) {
            customOrderTitle.isVisible = true
            customOrderInput.isVisible = true
        } else {
            customOrderTitle.isVisible = false
            customOrderInput.isVisible = false
        }
        customExportTitle.isVisible = exportVisible && exportPresetNew
        customExportInput.isVisible = exportVisible && exportPresetNew
        deleteExportPreset.isVisible = exportVisible && selectedPreset != 1 && selectedPreset != 2
        saveExportPreset.isVisible = exportVisible && selectedPreset != 1 && selectedPreset != 2
        defaultExportCheck.isVisible = exportVisible
        exportButton.isVisible =  exportVisible

        if (currentTitlesVisible)
            toggleMainReminders.setText("▼")
        else
            toggleMainReminders.setText("◀")

        if (rearrangeTitlesVisible)
            toggleRearrangeTitles.setText("▼")
        else
            toggleRearrangeTitles.setText("◀")

        if (manageDatesVisible)
            toggleManageDatesVisibilityButton.setText("▼")
        else
            toggleManageDatesVisibilityButton.setText("◀")

        if (addDatesVisible)
            addDateVisibilityButton.setText("▼")
        else
            addDateVisibilityButton.setText("◀")

        if (importVisible)
            importOptionsExpandButton.setText("▼")
        else
            importOptionsExpandButton.setText("◀")

        if (exportVisible)
            exportOptionsExpandButton.setText("▼")
        else
            exportOptionsExpandButton.setText("◀")
    }

    /** Uses the dateslist to setup the manage dates section to delete dates*/
    private fun setupDates() {
        manageDatesList.setAdapter(this,datesList)

        for (i in 0 until datesList.size) {
            manageDatesList.textGrid[i].setOnClickListener {
                val title : String
                val desc : String
                if (i == 0) {
                    title = "Are you sure?"
                    desc = "Delete will clear all of today's values and titles."
                } else {
                    title = "Are you sure?"
                    desc = "Delete will delete that day from memory."
                }


                MaterialAlertDialogBuilder(this).setTitle(title)
                    .setMessage(desc)
                    .setNegativeButton("Cancel") { dialog, which ->

                    }.setPositiveButton("Delete") { dialog, which ->
                        if (i == 0) {
                            saveInformation.resetData()
                            reloadCurrentTitles()
                            setupDates()
                            saveDailyInformationFile()
                        } else {

                            deleteDate(i, deleteMultipleMode)

                            setupDates()
                        }
                    }.show()
            }
        }
    }

    /** Deletes the date specified, and if specified all after that date will be deleted too
     *
     * @param dateIndex the index to delete
     * @param andAllAfter whether to delete the dates before this date (further in the past, or
     * after in the list)
     */
    private fun deleteDate(dateIndex : Int, andAllAfter : Boolean) {

        if (tempFile.exists()) {
            tempFile.writeText("")
        } else {
            tempFile.parentFile!!.mkdirs()
            tempFile.createNewFile()
        }
        var lineNum : Int = 0
        if (dailyInformationFile.exists()) {
            dailyInformationFile.forEachLine {
                if (lineNum != dateIndex) {
                    tempFile.appendText(it+"\n")
                } else if (lineNum == dateIndex && andAllAfter) {
                    return@forEachLine
                }
                lineNum++
            }
        }

        for (i in dateIndex until datesList.size) {
            datesList.removeAt(dateIndex)
        }
        //datesList = datesList.subList(0,dateIndex) as ArrayList<String>

        if (dailyInformationFile.exists() && !dailyInformationFile.delete()) {
            Toast.makeText(this,"Could not save. Permission denied.", Toast.LENGTH_SHORT).show()
            tempFile.delete()
            return
        }

        tempFile.renameTo(dailyInformationFile)

    }

    /** Deletes all dates */
    private fun deleteAllDates() {
        if (dailyInformationFile.exists()) {
            dailyInformationFile.writeText(saveInformation.toString()+"\n")
        }
    }

    /** Shows the dialog to input the value
     *
     * @param i the index of the value
     */
    private fun showSortInputDialog(i: Int) {
        selectedSortIndex = i
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Change the index of ${saveInformation.names[i]}: ")

        val input = EditText(this)
        input.hint = "Enter the new index (0-${saveInformation.length})"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)

            try {
                if (input.text.toString().toInt() < saveInformation.length) {
                    saveInformation.moveFrom(selectedSortIndex, input.text.toString().toInt())
                } else if (input.text.toString().toInt() == saveInformation.length) {
                    saveInformation.moveToEnd(selectedSortIndex)
                }
            } catch (e : NumberFormatException) {
                Toast.makeText(this, "Out of range.", Toast.LENGTH_SHORT).show()
            }

            saveDailyInformationFile()
            reloadCurrentTitles()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        input.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /** Reloads the export presets input, collecting all of the export preset names and adding * to
     * the default
     */
    private fun reloadExportPresetsInput() {
        var exportPresetNames = ArrayList<String>()
        for (i in 0 until exportPresets.size) {
            if (i == defaultPreset)
                exportPresetNames.add("*"+exportPresets[i].name)
            else
                exportPresetNames.add(exportPresets[i].name)
        }

        exportPresetsInput.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exportPresetNames)
    }

    /** Sets the position of the export preset spinner and reloads the other visibilities
     *
     * @param position the new position (index of the spinner)
     */
    private fun setExportPresetsInputPosition(position : Int) {
        if (position < 0) return
        if (position >= exportPresets.size) return

        labelExportCheck.isChecked = exportPresets[position].lineLabels
        if (exportPresets[position].custom) {
            customOrderInput.setText("")
            customExportInput.setText(exportPresets[position].exportString)
        } else {
            customOrderInput.setText(exportPresets[position].exportString)
            customExportInput.setText("")
        }
        defaultExportCheck.isChecked = position == defaultPreset

        selectedPreset = position
        exportPresetsInput.setSelection(position)
        reloadVisibilities()

    }

    /** Asks for confirmation to import, then calls importData in SaveInformation */
    private  fun importSubmit() {

        MaterialAlertDialogBuilder(this).setTitle("Are you sure?")
            .setMessage("This will import all dates in the file todoDailyImport.txt in the downloads file.\nDates that exist will COMBINE")
            .setNegativeButton("Cancel") { dialog, which ->
            }.setPositiveButton("Import") { dialog, which ->
                saveInformation.importData(this, this)
            }.show()
    }

    /** Exports the data using the preset specified, most of the work is done in the
     * exportDailyInformation in MainActivity. This also saves the preset is specified.
     */
    private fun customExportSubmit() {
        exportDailyInformation(
            this, this,
            labelExportCheck.isChecked, customOrderInput.text.toString(),
            customExportInput.text.toString(), defaultExportCheck.isChecked
        )

        // set to default if all worked
        if (defaultExportCheck.isChecked) {
            // default is done in exportDailyInformation, presets need to be done here
            saveExportPresetPressed()
            defaultPreset = exportPresetsInput.selectedItemPosition
            saveSettings()
        }
    }

    /** Save an export preset, add it to the list, save it, and reload the view */
    private fun saveExportPresetPressed() {
        if (selectedPreset == 0) {

            if (customExportInput.text.toString().isNotEmpty()) {
                exportPresets.add(
                    ExportPresetFormat(
                        "User Preset ${exportPresets.size - 2}",
                        labelExportCheck.isChecked,
                        true,
                        customExportInput.text.toString(),
                    )
                )
            } else {
                exportPresets.add(
                    ExportPresetFormat(
                        "User Preset ${exportPresets.size - 2}",
                        labelExportCheck.isChecked,
                        false,
                        customOrderInput.text.toString(),
                    )
                )
            }

            if (defaultExportCheck.isChecked) {
                setDefaultExport(labelExportCheck.isChecked, customOrderInput.text.toString(), customExportInput.text.toString())
                saveSettings()

                defaultPreset = exportPresets.size-1
            }

            reloadExportPresetsInput()
            setExportPresetsInputPosition(exportPresets.size - 1)
            saveExportPresets()

        } else if (selectedPreset > 2) {

            exportPresets[selectedPreset].lineLabels = labelExportCheck.isChecked
            if (customExportInput.text.toString().isNotEmpty()) {
                exportPresets[selectedPreset].custom = true
                exportPresets[selectedPreset].exportString = customExportInput.text.toString()
            } else {
                exportPresets[selectedPreset].custom = false
                exportPresets[selectedPreset].exportString = customOrderInput.text.toString()
            }

            if (defaultExportCheck.isChecked) {
                setDefaultExport(labelExportCheck.isChecked, customOrderInput.text.toString(), customExportInput.text.toString())
                defaultPreset = exportPresetsInput.selectedItemPosition
                saveSettings()
            }

            saveExportPresets()
        }
    }

    /** Reads the list of export presets from the file exportPresetsFile, overwriting the current
     * ones
     */
    private fun readExportPresets() {
        exportPresets = defaultExportPresets.clone() as ArrayList<ExportPresetFormat>

        if (!exportPresetsFile.exists()) {
            defaultPreset = 1
            selectedPreset = defaultPreset
            reloadExportPresetsInput()
            setExportPresetsInputPosition(selectedPreset)
            return
        } else {

            var latestLine = exportPresetsFile.inputStream().bufferedReader().readLines()
            defaultPreset = latestLine[0].toInt()

            for (i in 1 until latestLine.size) {
                val splitLine = latestLine[i].split(",")
                if (splitLine.size < 4) continue

                exportPresets.add(
                    ExportPresetFormat(
                        splitLine[0],
                        splitLine[1].toBoolean(),
                        splitLine[2].toBoolean(),
                        splitLine[3],
                    )
                )

                for (j in 4 until splitLine.size) {
                    exportPresets[i+2].exportString += ","+splitLine[j]
                }

            }

        }

        reloadExportPresetsInput()
        setExportPresetsInputPosition(defaultPreset)

    }

    /** Saves the export presets to the file exportPresetsFile (doesn't save the defaults) */
    private fun saveExportPresets() {
        if (!exportPresetsFile.exists()) {
            exportPresetsFile.parentFile!!.mkdirs()
            exportPresetsFile.createNewFile()
        }
        exportPresetsFile.writeText("$defaultPreset\n")
        for (i in 3 until exportPresets.size) {
            exportPresetsFile.appendText("${exportPresets[i].name},${exportPresets[i].lineLabels},${exportPresets[i].custom},${exportPresets[i].exportString}\n")
        }

    }

    override fun onResume() {
        super.onResume()
        reloadVisibilities()
    }

    override fun onBackPressed() {
        finish()
    }
}