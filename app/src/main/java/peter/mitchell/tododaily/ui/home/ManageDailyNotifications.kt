package peter.mitchell.tododaily.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.databinding.EditTodoBinding
import peter.mitchell.tododaily.databinding.HelpScreenBinding
import peter.mitchell.tododaily.databinding.ManageDailyInformationBinding
import java.io.File
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
        ExportPresetFormat("Raw Data", false, false, "ncvit"),
    )
    var defaultPreset : Int = 1
    var selectedPreset : Int = 0

    private lateinit var binding: ManageDailyInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ManageDailyInformationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (darkMode)
            binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        binding.manageMainReminders.setCustomColumnCount(homeColumns)
        binding.manageMainReminders.setTextSize(homeTextSize)
        binding.manageAllTitles.setCustomColumnCount(homeColumns)
        binding.manageAllTitles.setTextSize(homeTextSize)

        binding.manageDatesList.setCustomColumnCount(3)

        binding.manageMainReminders.isVisible = currentTitlesVisible
        binding.manageAllTitles.isVisible = rearrangeTitlesVisible
        binding.manageDatesList.isVisible = manageDatesVisible
        binding.toggleManageDatesText.isVisible = manageDatesVisible

        readExportPresets()

        reloadCurrentTitles()
        setupDates()

        binding.toggleMainReminders.setOnClickListener {
            currentTitlesVisible = !currentTitlesVisible
            reloadVisibilities()
        }
        binding.toggleRearrangeTitles.setOnClickListener {
            rearrangeTitlesVisible = !rearrangeTitlesVisible
            reloadVisibilities()
        }
        binding.toggleManageDatesVisibilityButton.setOnClickListener {
            manageDatesVisible = !manageDatesVisible
            reloadVisibilities()
        }
        binding.addDateVisibilityButton.setOnClickListener {
            addDatesVisible = !addDatesVisible

            if (addDatesVisible) {
                currentTitlesVisible = false
                rearrangeTitlesVisible = false
                manageDatesVisible = false
            }
            reloadVisibilities()
        }


        binding.deleteAllMainRemindersButton.setOnClickListener {

            MaterialAlertDialogBuilder(this).setTitle("delete all titles?")
                .setMessage("Would you like to delete all of today's items? (this will not remove the titles from any date but today)")
                .setNegativeButton("Cancel") { dialog, which ->

                }.setPositiveButton("Delete") { dialog, which ->
                    saveInformation.resetData()
                    saveDailyInformationFile()
                    reloadCurrentTitles()
                }.show()
        }

        binding.toggleManageDatesButton.setOnClickListener {
            deleteMultipleMode = !deleteMultipleMode
            if (deleteMultipleMode) {
                binding.toggleManageDatesText.setText("Delete all before date")
            } else {
                binding.toggleManageDatesText.setText("Single delete")
            }
        }

        binding.deleteAllDatesButton.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("delete all data?")
                .setMessage("This will delete all saved data, except today.")
                .setNegativeButton("Cancel") { dialog, which ->

                }.setPositiveButton("Delete") { dialog, which ->
                    deleteAllDates()
                    setupDates()
                    saveDailyInformationFile()
                }.show()
        }

        binding.addDateButton.setOnClickListener {

            if (!dailyInformationFile.exists()) {
                dailyInformationFile.parentFile!!.mkdirs()
                dailyInformationFile.createNewFile()
            }

            var newDate = LocalDate.of(binding.addDateInput.year,binding.addDateInput.month+1,binding.addDateInput.dayOfMonth)

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

        binding.exportOptionsExpandButton.setOnClickListener {
            exportVisible = !exportVisible
            reloadVisibilities()
            setExportPresetsInputPosition(selectedPreset)
        }

        reloadExportPresetsInput()

        binding.importOptionsExpandButton.setOnClickListener {
            importVisible = !importVisible
            reloadVisibilities()
        }
        binding.importButton.setOnClickListener {
            importSubmit()
        }

        binding.exportPresetsInput.onItemSelectedListener = object :
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

        binding.labelExportCheck.setOnClickListener {
            exportLabelLine = !exportLabelLine
            saveSettings()
        }
        binding.labelExportCheck.isChecked = exportLabelLine

        binding.customExportInput.addTextChangedListener {
            if (binding.customExportInput.text.isNotEmpty()) {
                //exportExplanation1.isVisible = false
                binding.customOrderTitle.isVisible = false
                binding.customOrderInput.isVisible = false
            } else {
                //exportExplanation1.isVisible = true
                binding.customOrderTitle.isVisible = true
                binding.customOrderInput.isVisible = true
            }
        }
        binding.customOrderInput.setText(exportOrderDefault)
        binding.customExportInput.setText(exportCustomDefault)

        binding.deleteExportPreset.setOnClickListener {
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

        binding.saveExportPreset.setOnClickListener {
            saveExportPresetPressed()
        }

        binding.exportButton.setOnClickListener { customExportSubmit() }
        // end of onCreateView
    }

    /** Reloads the current titles sections for deleting and rearranging the main information */
    private fun reloadCurrentTitles() {

        var informationViewList = ArrayList<String>()

        for (i in 0 until saveInformation.length) {
            informationViewList.add("$i: "+saveInformation.names[i])

        }

        binding.manageMainReminders.setAdapter(this, informationViewList)
        binding.manageAllTitles.setAdapter(this, informationViewList)

        for (i in 0 until informationViewList.size) {
            binding.manageMainReminders.textGrid[i].setOnClickListener {
                MaterialAlertDialogBuilder(this).setTitle("delete entry from today?")
                    .setMessage("Would you like to delete this item? (this will not remove the title from any date but today)")
                    .setNegativeButton("Cancel") { dialog, which ->

                    }.setPositiveButton("Delete") { dialog, which ->
                        saveInformation.deleteValue(i)
                        saveDailyInformationFile()
                        reloadCurrentTitles()
                    }.show()
            }

            binding.manageAllTitles.textGrid[i].setOnClickListener {
                showSortInputDialog(i)
            }
        }
    }

    /** Reloads what is visible and what is not, hiding closed sections */
    private fun reloadVisibilities() {

        if (addDatesVisible && (currentTitlesVisible || rearrangeTitlesVisible || manageDatesVisible)) {
            addDatesVisible = false
        }

        binding.manageMainReminders.isVisible = currentTitlesVisible
        binding.manageAllTitles.isVisible = rearrangeTitlesVisible
        binding.manageDatesList.isVisible = manageDatesVisible
        binding.toggleManageDatesText.isVisible = manageDatesVisible
        binding.addDateInput.isVisible = addDatesVisible
        binding.addDateButton.isVisible = addDatesVisible

        binding.importText.isVisible = importVisible
        binding.importButton.isVisible = importVisible

        binding.exportPresetsTitle.isVisible = exportVisible
        binding.exportPresetsInput.isVisible = exportVisible
        val exportPresetNew = binding.exportPresetsInput.selectedItemPosition == 0 || binding.exportPresetsInput.selectedItemPosition > 2

        binding.labelExportCheck.isVisible = exportVisible
        binding.exportExplanation1.isVisible = exportVisible && exportPresetNew
        if (exportPresetNew && exportVisible && binding.customExportInput.text.isEmpty()) {
            binding.customOrderTitle.isVisible = true
            binding.customOrderInput.isVisible = true
        } else {
            binding.customOrderTitle.isVisible = false
            binding.customOrderInput.isVisible = false
        }
        binding.customExportTitle.isVisible = exportVisible && exportPresetNew
        binding.customExportInput.isVisible = exportVisible && exportPresetNew
        binding.deleteExportPreset.isVisible = exportVisible && selectedPreset != 1 && selectedPreset != 2
        binding.saveExportPreset.isVisible = exportVisible && selectedPreset != 1 && selectedPreset != 2
        binding.defaultExportCheck.isVisible = exportVisible
        binding.exportButton.isVisible =  exportVisible

        if (currentTitlesVisible)
            binding.toggleMainReminders.setText("▼")
        else
            binding.toggleMainReminders.setText("◀")

        if (rearrangeTitlesVisible)
            binding.toggleRearrangeTitles.setText("▼")
        else
            binding.toggleRearrangeTitles.setText("◀")

        if (manageDatesVisible)
            binding.toggleManageDatesVisibilityButton.setText("▼")
        else
            binding.toggleManageDatesVisibilityButton.setText("◀")

        if (addDatesVisible)
            binding.addDateVisibilityButton.setText("▼")
        else
            binding.addDateVisibilityButton.setText("◀")

        if (importVisible)
            binding.importOptionsExpandButton.setText("▼")
        else
            binding.importOptionsExpandButton.setText("◀")

        if (exportVisible)
            binding.exportOptionsExpandButton.setText("▼")
        else
            binding.exportOptionsExpandButton.setText("◀")
    }

    /** Uses the dateslist to setup the manage dates section to delete dates*/
    private fun setupDates() {
        datesList.clear()
        if (dailyInformationFile.exists()) {
            dailyInformationFile.forEachLine {
                datesList.add(it.split(",")[0]) //LocalDate.parse
            }
        }

        binding.manageDatesList.reset()
        binding.manageDatesList.setAdapter(this,datesList)

        for (i in 0 until datesList.size) {
            binding.manageDatesList.textGrid[i].setOnClickListener {
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
        setupDates()
    }

    /** Deletes all dates */
    private fun deleteAllDates() {
        if (dailyInformationFile.exists()) {
            dailyInformationFile.writeText(saveInformation.toString()+"\n")
        }
        setupDates()
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

        binding.exportPresetsInput.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exportPresetNames)
    }

    /** Sets the position of the export preset spinner and reloads the other visibilities
     *
     * @param position the new position (index of the spinner)
     */
    private fun setExportPresetsInputPosition(position : Int) {
        if (position < 0) return
        if (position >= exportPresets.size) return

        binding.labelExportCheck.isChecked = exportPresets[position].lineLabels
        if (exportPresets[position].custom) {
            binding.customOrderInput.setText("")
            binding.customExportInput.setText(exportPresets[position].exportString)
        } else {
            binding.customOrderInput.setText(exportPresets[position].exportString)
            binding.customExportInput.setText("")
        }
        binding.defaultExportCheck.isChecked = position == defaultPreset

        selectedPreset = position
        binding.exportPresetsInput.setSelection(position)
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

    // spaghetti code due to android updating way after I made all this
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null || data.data == null) return

        Log.i("tdd-onActivityResult", "On activity result called: $requestCode $resultCode ${data.data}}")

        val inputStream = contentResolver.openInputStream(data.data!!)
        var text = String(inputStream!!.readBytes())
        inputStream.close()

        saveInformation.importDataSelected(this, text)

        setupDates()
    }

    /** Exports the data using the preset specified, most of the work is done in the
     * exportDailyInformation in MainActivity. This also saves the preset is specified.
     */
    private fun customExportSubmit() {
        exportDailyInformation(
            this, this,
            binding.labelExportCheck.isChecked, binding.customOrderInput.text.toString(),
            binding.customExportInput.text.toString(), binding.defaultExportCheck.isChecked
        )

        // set to default if all worked
        if (binding.defaultExportCheck.isChecked) {
            // default is done in exportDailyInformation, presets need to be done here
            saveExportPresetPressed()
            defaultPreset = binding.exportPresetsInput.selectedItemPosition
            saveSettings()
        }
    }

    /** Save an export preset, add it to the list, save it, and reload the view */
    private fun saveExportPresetPressed() {
        if (selectedPreset == 0) {

            if (binding.customExportInput.text.toString().isNotEmpty()) {
                exportPresets.add(
                    ExportPresetFormat(
                        "User Preset ${exportPresets.size - 2}",
                        binding.labelExportCheck.isChecked,
                        true,
                        binding.customExportInput.text.toString(),
                    )
                )
            } else {
                exportPresets.add(
                    ExportPresetFormat(
                        "User Preset ${exportPresets.size - 2}",
                        binding.labelExportCheck.isChecked,
                        false,
                        binding.customOrderInput.text.toString(),
                    )
                )
            }

            if (binding.defaultExportCheck.isChecked) {
                setDefaultExport(binding.labelExportCheck.isChecked, binding.customOrderInput.text.toString(), binding.customExportInput.text.toString())
                saveSettings()

                defaultPreset = exportPresets.size-1
            }

            reloadExportPresetsInput()
            setExportPresetsInputPosition(exportPresets.size - 1)
            saveExportPresets()

        } else if (selectedPreset > 2) {

            exportPresets[selectedPreset].lineLabels = binding.labelExportCheck.isChecked
            if (binding.customExportInput.text.toString().isNotEmpty()) {
                exportPresets[selectedPreset].custom = true
                exportPresets[selectedPreset].exportString = binding.customExportInput.text.toString()
            } else {
                exportPresets[selectedPreset].custom = false
                exportPresets[selectedPreset].exportString = binding.customOrderInput.text.toString()
            }

            if (binding.defaultExportCheck.isChecked) {
                setDefaultExport(binding.labelExportCheck.isChecked, binding.customOrderInput.text.toString(), binding.customExportInput.text.toString())
                defaultPreset = binding.exportPresetsInput.selectedItemPosition
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