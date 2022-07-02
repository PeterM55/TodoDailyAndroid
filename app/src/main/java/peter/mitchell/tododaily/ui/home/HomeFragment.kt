package peter.mitchell.tododaily.ui.home

import android.Manifest
import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.MainActivity
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.HelperClasses.TextGridLayout
import peter.mitchell.tododaily.databinding.FragmentHomeBinding
import java.io.File
import java.time.LocalDate


class HomeFragment : Fragment() {

    private lateinit var _binding: FragmentHomeBinding
    private var addingNew = false
    private lateinit var imm: InputMethodManager
    private var dateList : ArrayList<String> = ArrayList()
    private var currentDate = LocalDate.now()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding

    private var initialSetupDone = false

    class SelectedViewValue(i : Int, name : String, type : SaveInformation.InformationFormat) {
        var valueIndex : Int = i
        var valueName : String = name
        var valueType : SaveInformation.InformationFormat = type
    }
    var selectedViewValue : SelectedViewValue? = null

    var selectedValueDates : ArrayList<LocalDate> = ArrayList()
    var selectedValueValues : ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("tdd-HomeFragment", "onCreateView run in HomeFragment")
        /*val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //fragmentLabel.text = "Home"
        mainBinding?.fragmentLabel?.setText("Home")

        readSettings()
        updateBottomNavVisibilities()

        if (saveInformation.length == 0) {
            readTodaysDailyInformationFile()
        }

        // --- Dynamic view width (height handled elsewhere) ---
        _binding.newReminderName.maxWidth =
            resources.displayMetrics.widthPixels - (230f * resources.displayMetrics.density).toInt()

        /*binding.mainReminders.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (saveInformation.formats[position] == SaveInformation.InformationFormat.checkBox) {
                    saveInformation.toggleBox(position)
                    saveDailyInformationFile()
                    reloadMainReminders()
                } else {
                    showInputDialog(position, saveInformation.names[position] + ": ")
                }
            }*/

        _binding.newReminderInput.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_list_item_1,
            saveInformation.informationFormatStrings
        )

        _binding.newReminderTime.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_list_item_1,
            arrayOf<String>("Daily", "Weekly", "Monthly", "Yearly", "Never")
        )

        _binding.newReminderButton.setOnClickListener {
            addingNew = true
            reloadReminderInput()
        }
        _binding.cancelReminderButton.setOnClickListener {
            addingNew = false
            reloadReminderInput()
        }
        _binding.confirmReminderButton.setOnClickListener {
            saveInformation.addValue(
                _binding.newReminderName.text.toString(),
                saveInformation.informationFormatStringToEnum(_binding.newReminderInput.selectedItem.toString()),
                saveInformation.repeatTimeStringToEnum(_binding.newReminderTime.selectedItem.toString())
            )
            addingNew = false
            saveDailyInformationFile()
            reloadMainReminders()
            reloadReminderInput()
        }

        _binding.manageMainRemindersButton.setOnClickListener {
            val intent = Intent(activity as Context, ManageDailyNotifications::class.java)
            startActivity(intent)
        }

        _binding.exportMainRemindersButton.setOnClickListener {
            showExportDialog()
        }

        _binding.selectPastDateSpinner.isVisible = false
        _binding.selectPastDateButton.setOnClickListener {
            _binding.selectPastDateButton.isVisible = false
            _binding.selectPastDateSpinner.isVisible = true
            reloadDateSpinner()
        }

        _binding.viewValueButton.setOnClickListener {

            if (!dailyInformationFile.exists())
                return@setOnClickListener

            val i = viewValueSelect.selectedItemPosition
            selectedViewValue = SelectedViewValue(i, saveInformation.names[i], saveInformation.formats[i])

            dailyInformationFile.forEachLine {
                if (it.isNullOrEmpty())
                    return@forEachLine

                val lineInfo : SaveInformation = SaveInformation()
                if (!lineInfo.fromString(it))
                    return@forEachLine

                var newI = lineInfo.getValueIndex(i, selectedViewValue!!.valueName, selectedViewValue!!.valueType)

                if (newI != null) {
                    selectedValueDates.add(lineInfo.date)
                    selectedValueValues.add(lineInfo.getDisplayValue(newI))
                }

            }

            reloadMainReminders()
        }

        // this is done in onResume, which runs after onCreate
        /*reloadReminderInput()
        reloadMainReminders()*/

        return root
    }

    /** Reloads the main reminders grid */
    private fun reloadMainReminders() {

        viewValueSelect.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, saveInformation.names)



            _binding.dailyInformationGrid.setupTitles(
                arrayListOf("Daily", "Weekly", "Monthly", "Yearly", "Never",)
            )

            _binding.dailyInformationGrid.setCustomColumnCount(homeColumns)
            _binding.dailyInformationGrid.setTextSize(homeTextSize)
            _binding.dailyInformationGrid.setShowAddButtons(false)

            for (i in 0 until _binding.dailyInformationGrid.sectionGrids.size) {
                _binding.dailyInformationGrid.sectionGrids[i].reset()
            }

            var eachCount: Array<Int> = arrayOf(0, 0, 0, 0, 0)
            for (i in 0 until saveInformation.length) {

                var textGridLayout: TextGridLayout
                var currentIndex: Int = 0

                if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Daily) {
                    textGridLayout = _binding.dailyInformationGrid.sectionGrids[0]
                    currentIndex = eachCount[0]++
                } else if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Weekly) {
                    textGridLayout = _binding.dailyInformationGrid.sectionGrids[1]
                    currentIndex = eachCount[1]++
                } else if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Monthly) {
                    textGridLayout = _binding.dailyInformationGrid.sectionGrids[2]
                    currentIndex = eachCount[2]++
                } else if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Yearly) {
                    textGridLayout = _binding.dailyInformationGrid.sectionGrids[3]
                    currentIndex = eachCount[3]++
                } else { // (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Never)
                    textGridLayout = _binding.dailyInformationGrid.sectionGrids[4]
                    currentIndex = eachCount[4]++
                }

                textGridLayout.addString(
                    requireContext(),
                    saveInformation.names[i] + ": " + saveInformation.getDisplayValue(i)
                )

                textGridLayout.textGrid[currentIndex].setOnClickListener {
                    if (saveInformation.formats[i] == SaveInformation.InformationFormat.checkBox) {
                        saveInformation.toggleBox(i)
                        saveDailyInformationFile()
                        reloadMainReminders()
                    } else {
                        showInputDialog(i, saveInformation.names[i] + ": ")
                    }
                }
            }

            for (i in 0 until _binding.dailyInformationGrid.sectionGrids.size) {
                _binding.dailyInformationGrid.setSectionVisible(i, eachCount[i] > 0)
            }

            currentDateText.setText("Selected date: ${saveInformation.date.toString()}")
        if (selectedViewValue != null) {

            _binding.viewValueGrid.reset()
            _binding.viewValueGrid.setCustomColumnCount(maxOf(2, homeColumns- homeColumns%2))
            _binding.viewValueGrid.setTextSize(homeTextSize)

            for (i in 0 until selectedValueDates.size) {
                _binding.viewValueGrid.addString(requireContext(), selectedValueDates[i].toString())
                _binding.viewValueGrid.addString(requireContext(), selectedValueValues[i])
            }

        }



    }

    /** Reloads the reminders input section */
    private fun reloadReminderInput() {
        if (addingNew) {
            _binding.newReminderName.visibility = View.VISIBLE
            _binding.newReminderInput.visibility = View.VISIBLE
            _binding.cancelReminderButton.visibility = View.VISIBLE
            _binding.confirmReminderButton.visibility = View.VISIBLE
            _binding.newReminderTime.visibility = View.VISIBLE
            _binding.newReminderButton.visibility = View.GONE
        } else {
            _binding.newReminderName.visibility = View.GONE
            _binding.newReminderInput.visibility = View.GONE
            _binding.cancelReminderButton.visibility = View.GONE
            _binding.confirmReminderButton.visibility = View.GONE
            _binding.newReminderTime.visibility = View.GONE
            _binding.newReminderButton.visibility = View.VISIBLE

            _binding.newReminderName.setText("")
        }
    }

    private fun reloadDateSpinner() {

        if (!dailyInformationFile.exists()) {
            return
        } else {

            dateList = ArrayList()

            var checkContainsFirstLine = true

            dailyInformationFile.forEachLine {
                if (it.isNullOrEmpty())
                    return@forEachLine

                val latestDate: LocalDate = LocalDate.parse(it.split(",")[0])
                if (checkContainsFirstLine) {
                    if (latestDate != LocalDate.now()) {
                        dateList.add(LocalDate.now().toString())
                    }
                    checkContainsFirstLine = false
                }
                dateList.add(latestDate.toString())

            }
        }

        if (dateList.size == 0)
            dateList.add(LocalDate.now().toString())

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dateList)
        _binding.selectPastDateSpinner.adapter = adapter

        _binding.selectPastDateSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (dateList.size == 0) {
                    return
                }

                if (!dailyInformationFile.exists()) {
                    return
                } else {

                    var i : Int = 0
                    var linePosition = position
                    var stop = false
                    dailyInformationFile.forEachLine {

                        if (!stop) {

                            if (i == 0) {
                                if (LocalDate.parse(it.split(",")[0]) != LocalDate.now()) {
                                    if (position == 0) {
                                        readTodaysDailyInformationFile()
                                        reloadMainReminders()
                                        stop = true
                                    } else {
                                        linePosition--
                                        //Log.i("------", "LinePosition: $linePosition")
                                    }
                                }
                            }

                            if (!stop && i == linePosition) {
                                saveInformation.fromString(it)
                                reloadMainReminders()
                                stop = true
                            }
                            i++
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /*readDailyInformationFile()
                        reloadMainReminders()*/
            }
        }
    }

    /** Shows the dialog to input the value
     *
     * @param i the index of the value
     * @param inputText the text to ask the user
     */
    private fun showInputDialog(i: Int, inputText: String) {
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(inputText)

        val input = EditText(requireContext())
        input.hint = "Enter the value"
        input.inputType = saveInformation.getInputType(i)
        if (selectHoldText)
            input.setText(saveInformation.getRawValue(i))
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            saveInformation.setValue(i, input.text.toString())
            saveDailyInformationFile()
            reloadMainReminders()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        input.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun showExportDialog() {

        if (!dailyInformationFile.exists()) {
            Toast.makeText(requireContext(),"No information to export.", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext()).setTitle("Export?")
            .setMessage("This will export to your external downloads folder.\nThis uses the default export option (manage)")
            .setNegativeButton("Cancel") { dialog, which ->

            }.setPositiveButton("Export") { dialog, which ->

                /*if (!canExport(requireActivity(), requireContext()))
                    return@setPositiveButton

                var exportFile : File? = getExportFile(exportFileName)

                if (exportFile == null)
                    return@setPositiveButton

                dailyInformationFile.forEachLine {
                    exportFile.appendText(it+"\n")
                }*/

                exportDailyInformation(requireActivity(), requireContext())

            }.show()
    }

    override fun onResume() {
        super.onResume()

        _binding.selectPastDateButton.isVisible = true
        _binding.selectPastDateSpinner.isVisible = false

        reloadReminderInput()

        // if the app is open overnight, and opened the next day
        if (currentDate != LocalDate.now()) {
            reloadDateSpinner()
            if (!_binding.selectPastDateSpinner.isVisible) {
                readTodaysDailyInformationFile()
            }
        }

        reloadMainReminders()
        if (!initialSetupDone) {
            homeScrollView.smoothScrollTo(0, 0)
            initialSetupDone = true
        }

        updateBottomNavVisibilities()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}