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
import kotlinx.android.synthetic.main.fragment_home.*
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.databinding.FragmentHomeBinding
import java.io.File
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
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
                saveInformation.informationFormatStringToEnum(_binding.newReminderInput.selectedItem.toString())
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
        for (i in 0 until saveInformation.length) {

            mainGridLayout.addString(requireContext(), saveInformation.names[i] + ": " + saveInformation.getDisplayValue(i))

            mainGridLayout.textGrid[i].setOnClickListener {
                if (saveInformation.formats[i] == SaveInformation.InformationFormat.checkBox) {
                    saveInformation.toggleBox(i)
                    saveDailyInformationFile()
                    reloadMainReminders()
                } else {
                    showInputDialog(i, saveInformation.names[i] + ": ")
                }
            }
        }
    }

    /** Reloads the reminders input section */
    private fun reloadReminderInput() {
        if (addingNew) {
            _binding.newReminderName.visibility = View.VISIBLE
            _binding.newReminderInput.visibility = View.VISIBLE
            _binding.confirmReminderButton.visibility = View.VISIBLE
            _binding.cancelReminderButton.visibility = View.VISIBLE
            _binding.newReminderButton.visibility = View.GONE
        } else {
            _binding.newReminderName.visibility = View.GONE
            _binding.newReminderInput.visibility = View.GONE
            _binding.confirmReminderButton.visibility = View.GONE
            _binding.cancelReminderButton.visibility = View.GONE
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

                if (!hasWriteStoragePermission() || !hasReadStoragePermission()) {
                    Toast.makeText(requireContext(),"No permission.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

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
                    return@setPositiveButton

                dailyInformationFile.forEachLine {
                    exportFile.appendText(it+"\n")
                }

            }.show()
    }

    private fun hasWriteStoragePermission(): Boolean {

        if (!(ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
        }
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadStoragePermission(): Boolean {

        if (!(ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                102
            )
        }
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
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