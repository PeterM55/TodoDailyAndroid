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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //fragmentLabel.text = "Home"
        mainBinding?.fragmentLabel?.setText("Home")

        if (!settingsRead) {
            settingsRead = true
            readSettings()
        }

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

        // this is done in onResume, which runs after onCreate
        /*reloadReminderInput()
        reloadMainReminders()*/

        Log.i("-----", "button size: ${_binding.bottomSpacingHome.height} ${_binding.bottomSpacingHome.height*resources.displayMetrics.density}")

        return root
    }

    /** Reloads the main reminders grid */
    private fun reloadMainReminders() {
        mainGridLayout.reset()
        weeklyGridLayout.reset()
        monthlyGridLayout.reset()
        yearlyGridLayout.reset()
        neverGridLayout.reset()

        var eachCount : Array<Int> = arrayOf(0,0,0,0,0)
        for (i in 0 until saveInformation.length) {

            var textGridLayout : TextGridLayout
            var currentIndex : Int = 0

            if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Daily) {
                textGridLayout = mainGridLayout
                currentIndex = eachCount[0]++
            } else if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Weekly) {
                textGridLayout = weeklyGridLayout
                currentIndex = eachCount[1]++
            } else if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Monthly) {
                textGridLayout = monthlyGridLayout
                currentIndex = eachCount[2]++
            } else if (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Yearly) {
                textGridLayout = yearlyGridLayout
                currentIndex = eachCount[3]++
            } else { // (saveInformation.repeatTime[i] == SaveInformation.RepeatFormat.Never)
                textGridLayout = neverGridLayout
                currentIndex = eachCount[4]++
            }

            textGridLayout.addString(requireContext(), saveInformation.names[i] + ": " + saveInformation.getDisplayValue(i))

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

        dailyInformationTitle.isVisible = (eachCount[0] > 0)
        mainGridLayout.isVisible = (eachCount[0] > 0)

        weeklyInformationTitle.isVisible = (eachCount[1] > 0)
        weeklyGridLayout.isVisible = (eachCount[1] > 0)

        monthlyInformationTitle.isVisible = (eachCount[2] > 0)
        monthlyGridLayout.isVisible = (eachCount[2] > 0)

        yearlyInformationTitle.isVisible = (eachCount[3] > 0)
        yearlyGridLayout.isVisible = (eachCount[3] > 0)

        neverInformationTitle.isVisible = (eachCount[4] > 0)
        neverGridLayout.isVisible = (eachCount[4] > 0)

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

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun showExportDialog() {

        if (!dailyInformationFile.exists()) {
            Toast.makeText(requireContext(),"No information to export.", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext()).setTitle("Export?")
            .setMessage("This will export to your external downloads folder.")
            .setNegativeButton("Cancel") { dialog, which ->

            }.setPositiveButton("Export") { dialog, which ->

                if (!canExport(requireActivity(), requireContext()))
                    return@setPositiveButton

                var exportFile : File? = getExportFile()

                if (exportFile == null)
                    return@setPositiveButton

                dailyInformationFile.forEachLine {
                    exportFile.appendText(it+"\n")
                }

            }.show()
    }

    override fun onResume() {
        super.onResume()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}