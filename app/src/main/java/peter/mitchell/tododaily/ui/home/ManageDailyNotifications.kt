package peter.mitchell.tododaily.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.manage_daily_information.*
import peter.mitchell.tododaily.*
import java.lang.NumberFormatException

class ManageDailyNotifications : AppCompatActivity() {

    //var allTitles : ArrayList<String> = ArrayList()
    var datesList : ArrayList<String> = ArrayList()
    var deleteMultipleMode : Boolean = false
    private lateinit var imm: InputMethodManager

    var selectedSortIndex = 0

    var currentTitlesVisible : Boolean = true
    var rearrangeTitlesVisible : Boolean = false
    var manageDatesVisible : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_daily_information)
        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        reloadDynamicView()

        manageMainReminders.isVisible = currentTitlesVisible
        manageAllTitles.isVisible = rearrangeTitlesVisible
        manageDatesList.isVisible = manageDatesVisible

        if (dailyInformationFile.exists()) {
            dailyInformationFile.forEachLine {
                datesList.add(it.split(",")[0]) //LocalDate.parse
            }
        }

        reloadCurrentTitles()
        setupDates()

        toggleMainReminders.setOnClickListener {
            currentTitlesVisible = !currentTitlesVisible
            manageMainReminders.isVisible = currentTitlesVisible
            if (currentTitlesVisible) {
                toggleMainReminders.setText("▼")
            } else {
                toggleMainReminders.setText("◀")
            }
            reloadDynamicView()
        }
        toggleRearrangeTitles.setOnClickListener {
            rearrangeTitlesVisible = !rearrangeTitlesVisible
            manageAllTitles.isVisible = rearrangeTitlesVisible
            if (rearrangeTitlesVisible) {
                toggleRearrangeTitles.setText("▼")
            } else {
                toggleRearrangeTitles.setText("◀")
            }
            reloadDynamicView()
        }
        toggleManageDatesVisibilityButton.setOnClickListener {
            manageDatesVisible = !manageDatesVisible
            manageDatesList.isVisible = manageDatesVisible
            if (manageDatesVisible) {
                toggleManageDatesVisibilityButton.setText("▼")
            } else {
                toggleManageDatesVisibilityButton.setText("◀")
            }
            reloadDynamicView()
        }

        deleteAllMainRemindersButton.setOnClickListener {

            manageMainReminders.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->

                    MaterialAlertDialogBuilder(this).setTitle("delete all titles?")
                        .setMessage("Would you like to delete all of today's items? (this will not remove the titles from any date but today)")
                        .setNegativeButton("Cancel") { dialog, which ->

                        }.setPositiveButton("Delete") { dialog, which ->
                            saveInformation.resetData()
                            reloadCurrentTitles()
                        }.show()
                }

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
            manageMainReminders.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->

                    MaterialAlertDialogBuilder(this).setTitle("delete all data?")
                        .setMessage("This will delete all saved data, except today.")
                        .setNegativeButton("Cancel") { dialog, which ->

                        }.setPositiveButton("Delete") { dialog, which ->
                            deleteAllDates()
                            setupDates()
                        }.show()
                }
        }
    }

    private fun reloadDynamicView() {
        // --- Dynamic view ---
        val extraSpace = 10
        val displayHeight = (resources.displayMetrics.heightPixels - (androidBarsSize+toolBarSize + 35 + 35 + 42 + 18 + extraSpace) * resources.displayMetrics.density).toInt()

        manageMainReminders.isVisible = currentTitlesVisible
        manageAllTitles.isVisible = rearrangeTitlesVisible
        manageDatesList.isVisible = manageDatesVisible

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

        if (currentTitlesVisible) {
            var params: ViewGroup.LayoutParams = manageAllTitles.layoutParams
            params.height = displayHeight/displaysVisible
            manageAllTitles.layoutParams = params
        }

        if (currentTitlesVisible) {
            var params: ViewGroup.LayoutParams = manageDatesList.layoutParams
            params.height = displayHeight/displaysVisible
            manageDatesList.layoutParams = params
        }

        /*var params: ViewGroup.LayoutParams = mainReminders.layoutParams
        params.height = displayHeight
        _binding.mainReminders.layoutParams = params*/
    }

    private fun reloadCurrentTitles() {

        var informationViewList = ArrayList<String>()

        for (i in 0 until saveInformation.length) {
            informationViewList.add("$i: "+saveInformation.names[i])
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, informationViewList)
        manageMainReminders.adapter = adapter
        manageAllTitles.adapter = adapter

        manageMainReminders.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->

                MaterialAlertDialogBuilder(this).setTitle("delete entry from today?")
                    .setMessage("Would you like to delete this item? (this will not remove the title from any date but today)")
                    .setNegativeButton("Cancel") { dialog, which ->

                    }.setPositiveButton("Delete") { dialog, which ->
                        saveInformation.deleteValue(position)
                        reloadCurrentTitles()
                    }.show()
            }

        manageAllTitles.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                showSortInputDialog(position)
            }

    }

    private fun setupDates() {

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datesList)
        manageDatesList.adapter = adapter

        manageDatesList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->

                val title : String
                val desc : String
                if (position == 0) {
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
                        if (position == 0) {
                            saveInformation.resetData()
                            reloadCurrentTitles()
                            setupDates()
                            saveDailyInformationFile()
                        } else {

                            deleteDate(position, deleteMultipleMode)

                            setupDates()
                        }
                    }.show()
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

    private fun backToHome() {
        finish()
    }

    override fun onBackPressed() {
        backToHome()
    }
}