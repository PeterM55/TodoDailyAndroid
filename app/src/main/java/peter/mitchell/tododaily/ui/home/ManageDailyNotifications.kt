package peter.mitchell.tododaily.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import java.io.File
import java.lang.NumberFormatException
import java.time.LocalDate

class ManageDailyNotifications : AppCompatActivity() {

    //var allTitles : ArrayList<String> = ArrayList()
    var datesList : ArrayList<String> = ArrayList()
    var deleteMultipleMode : Boolean = false
    private lateinit var imm: InputMethodManager

    var selectedSortIndex = 0

    var currentTitlesVisible : Boolean = false
    var rearrangeTitlesVisible : Boolean = false
    var manageDatesVisible : Boolean = false
    var addDatesVisible : Boolean = false
    var exportVisible : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_daily_information)
        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        reloadDynamicView()

        manageMainReminders.isVisible = currentTitlesVisible
        manageAllTitles.isVisible = rearrangeTitlesVisible
        manageDatesList.isVisible = manageDatesVisible
        toggleManageDatesText.isVisible = manageDatesVisible

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
        }

        labelExportCheck.setOnClickListener {
            exportLabelLine = !exportLabelLine
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

        exportButton.setOnClickListener { customExportSubmit() }
        // end of onCreateView
    }

    private fun reloadDynamicView() {
        return
        // --- Dynamic view ---
        /*val extraSpace = 10
        var displayHeight = (resources.displayMetrics.heightPixels - (androidBarsSize+toolBarSize + 35 + 35 + 42 + 18 + extraSpace) * resources.displayMetrics.density).toInt()

        if (addDatesVisible) displayHeight = 0

        var displaysVisible = 0
        if (currentTitlesVisible) displaysVisible++
        if (rearrangeTitlesVisible) displaysVisible++
        if (manageDatesVisible) displaysVisible++

        if (displaysVisible == 0) return

        if (currentTitlesVisible) {
            var params: ViewGroup.LayoutParams = manageMainReminders.layoutParams
            params.height = displayHeight/displaysVisible
            manageMainReminders.layoutParams = params
        }

        if (rearrangeTitlesVisible) {
            var params: ViewGroup.LayoutParams = manageAllTitles.layoutParams
            params.height = displayHeight/displaysVisible
            manageAllTitles.layoutParams = params
        }

        if (manageDatesVisible) {
            var params: ViewGroup.LayoutParams = manageDatesList.layoutParams
            params.height = displayHeight/displaysVisible
            manageDatesList.layoutParams = params
        }*/

        /*var params: ViewGroup.LayoutParams = mainReminders.layoutParams
        params.height = displayHeight
        _binding.mainReminders.layoutParams = params*/
    }

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
                        reloadCurrentTitles()
                    }.show()
            }

            manageAllTitles.textGrid[i].setOnClickListener {
                showSortInputDialog(i)
            }
        }
    }

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

        customExportTitle.isVisible = exportVisible
        customExportInput.isVisible = exportVisible
        exportExplanation1.isVisible = exportVisible
        labelExportCheck.isVisible = exportVisible
        if (exportVisible && customExportInput.text.isEmpty()) {
            customOrderTitle.isVisible = true
            customOrderInput.isVisible = true
            defaultExportCheck.isVisible = true
            exportButton.isVisible =  true
        } else {
            customOrderTitle.isVisible = false
            customOrderInput.isVisible = false
            defaultExportCheck.isVisible = false
            exportButton.isVisible =  false
        }

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

        if (exportVisible)
            exportOptionsExpandButton.setText("▼")
        else
            exportOptionsExpandButton.setText("◀")

        reloadDynamicView()
    }

    private fun setupDates() {

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datesList)
        manageDatesList.setAdapter(this,datesList)

        for (i in 0 until datesList.size) {
            manageDatesList.setOnClickListener {
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

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun customExportSubmit() {

        if (!canExport(this, this))
            return

        var exportFile : File? = getExportFile()

        if (exportFile == null)
            return

        var putLabel = !exportLabelLine

        var tempSaveInformation : SaveInformation = SaveInformation()

        dailyInformationFile.forEachLine {
            tempSaveInformation.fromString(it)

            if (customExportInput.text.isNotEmpty()) {
                exportFile.appendText(tempSaveInformation.exportToCustomString(customExportInput.text.toString(), !putLabel)+"\n")
            } else {
                exportFile.appendText(tempSaveInformation.exportToCustomOrder(customOrderInput.text.toString(), !putLabel)+"\n")
            }

            if (!putLabel) {
                putLabel = true
            }
        }

        // set to default if all worked
        if (defaultExportCheck.isChecked) {
            if (customExportInput.text.toString().isNotEmpty()) {
                exportCustomDefault = customExportInput.text.toString()
            } else {
                exportOrderDefault = customOrderInput.text.toString()
                exportCustomDefault = ""
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reloadVisibilities()
    }

    private fun backToHome() {
        finish()
    }

    override fun onBackPressed() {
        backToHome()
    }
}