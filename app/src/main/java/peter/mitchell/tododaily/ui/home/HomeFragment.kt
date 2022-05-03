package peter.mitchell.tododaily.ui.home

import android.R
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import peter.mitchell.tododaily.databinding.FragmentHomeBinding
import java.io.File
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment : Fragment() {

    private lateinit var _binding: FragmentHomeBinding
    private var saveInformation : SaveInformation = SaveInformation()
    private var addingNew = false
    private lateinit var imm : InputMethodManager

    //val dailyInformationFile = File("${requireContext().filesDir.path}/dailyInformation.txt")
    //val tempFile = File("${requireContext().filesDir.path}/tempDailyInformation.txt")
    val dailyInformationFile = File("/data/data/peter.mitchell.tododaily/files/dailyInformation.txt")
    val tempFile = File("/data/data/peter.mitchell.tododaily/files/tempDailyInformation.txt")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding

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

        if (saveInformation.length == 0) {
            readDailyInformationFile()
        }

        reloadReminderInput()

        reloadMainReminders()
        binding.mainReminders.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (saveInformation.formats[position] == SaveInformation.InformationFormat.checkBox) {
                saveInformation.toggleBox(position)
                reloadMainReminders()
            } else {
                showInputDialog(position, saveInformation.names[position]+": ")
            }
        }

        _binding.newReminderInput.adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, saveInformation.informationFormatStrings)

        _binding.newReminderButton.setOnClickListener {
            addingNew   = true
            reloadReminderInput()
        }
        _binding.cancelReminderButton.setOnClickListener {
            addingNew = false
            reloadReminderInput()
        }
        _binding.confirmReminderButton.setOnClickListener {
            saveInformation.addValue(_binding.newReminderName.text.toString(), saveInformation.informationFormatStringToEnum(_binding.newReminderInput.selectedItem.toString()))
            addingNew = false
            reloadMainReminders()
            reloadReminderInput()
        }

        /*val notificationChannel =
            NotificationChannel("test_channel_id_55", "My Channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Sends Alarms"
            }
        Log.i("CUSTOM: ", "started build")
        var builder = NotificationCompat.Builder(requireContext(), "test_channel_id_55")
            .setSmallIcon(R.drawable.arrow_up_float)
            .setContentTitle("this is a test notification")
            .setContentText("this is lots of text, what happens if I do this\n hahahah")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        Log.i("CUSTOM: ", "finished build")
        with(NotificationManagerCompat.from(requireContext())) {
            createNotificationChannel(notificationChannel)
            notify(1, builder.build())
        }
        Log.i("CUSTOM: ", "sent? build")*/


        return root
    }

    /** Reloads the main reminders grid */
    private fun reloadMainReminders() {
        var informationViewList = ArrayList<String>()

        for (i in 0 until saveInformation.length) {
            informationViewList.add(saveInformation.names[i]+": "+saveInformation.getDisplayValue(i))
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, informationViewList)
        _binding.mainReminders.adapter = adapter

        saveDailyInformationFile()
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

    /** Shows the dialog to input the value
     *
     * @param i the index of the value
     * @param inputText the text to ask the user
     */
    private fun showInputDialog(i : Int, inputText : String) {
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(inputText)

        val input = EditText(requireContext())
        input.hint = "Enter the value"
        input.inputType = saveInformation.getInputType(i)
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            saveInformation.setValue(i, input.text.toString())
            reloadMainReminders()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun readDailyInformationFile() {
        if (!dailyInformationFile.exists()) {
            return
        } else {
            var latestLine : String = dailyInformationFile.inputStream().bufferedReader().readLine()
            if (latestLine.isNullOrEmpty())
                return
            var latestDate : LocalDate = LocalDate.parse(latestLine.split(", ")[0])

            if (latestDate != LocalDate.now()) {
                saveInformation.copySetup(dailyInformationFile.inputStream().bufferedReader().readLine())
            } else {
                saveInformation.fromString(dailyInformationFile.inputStream().bufferedReader().readLine())
            }

        }
    }

    /** Save the daily information
     * note: the order is from latest to oldest because the priority of this application is fast
     * opening and recording. So the save time is less important than read time.
     */
    private fun saveDailyInformationFile() {

        if (!dailyInformationFile.exists()) {
            dailyInformationFile.parentFile!!.mkdirs()
            dailyInformationFile.createNewFile()
            dailyInformationFile.writeText(saveInformation.toString())
            return
        }

        if (!tempFile.exists()) {
            tempFile.parentFile!!.mkdirs()
            tempFile.createNewFile()
        }

        var currentWritten : Boolean = false;
        dailyInformationFile.forEachLine {
            if (!currentWritten) {
                var lineDate : LocalDate = LocalDate.parse(it.split(", ")[0])

                if (LocalDate.now() > lineDate) {
                    tempFile.appendText(saveInformation.toString()+"\n")
                    tempFile.appendText(it+"\n")
                    currentWritten = true
                } else if (LocalDate.now() == lineDate) {
                    tempFile.appendText(saveInformation.toString()+"\n")
                    currentWritten = true
                } else {
                    tempFile.appendText(it+"\n")
                }

            } else {
                tempFile.appendText(it+"\n")
            }

        }


        if (!dailyInformationFile.delete()) {
            Toast.makeText(requireContext(),"Could not save. Permission denied.",Toast.LENGTH_SHORT).show()
            tempFile.delete()
            return
        }

        tempFile.renameTo(dailyInformationFile)

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}