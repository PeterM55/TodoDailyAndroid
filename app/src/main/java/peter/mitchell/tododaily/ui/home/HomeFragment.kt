package peter.mitchell.tododaily.ui.home

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import peter.mitchell.tododaily.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var _binding: FragmentHomeBinding
    private var saveInformation : SaveInformation = SaveInformation()
    private var addingNew = false
    private lateinit var imm : InputMethodManager

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

        // FIXME: Remove this debugging code
        saveInformation.addValue("input 1", "value 1", SaveInformation.InformationFormat.text)
        saveInformation.addValue("input 2", "value 2", SaveInformation.InformationFormat.text)
        saveInformation.addValue("input 3", "5.843", SaveInformation.InformationFormat.decimal)

        reloadReminderInput()

        reloadMainReminders()
        binding.mainReminders.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            showInputDialog(position, saveInformation.names[position]+": ")
        }

        _binding.newReminderInput.adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, saveInformation.informationFormatStrings)

        _binding.newReminderButton.setOnClickListener {
            addingNew = true
            reloadReminderInput()
        }
        _binding.cancelReminderButton.setOnClickListener {
            addingNew = false
            reloadReminderInput()
        }
        _binding.confirmReminderButton.setOnClickListener {

        }

        //ShowInputDialog("hi")

        /*val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        return root
    }

    private fun reloadMainReminders() {
        var informationViewList = ArrayList<String>()

        for (i in 0 until saveInformation.length) {
            informationViewList.add(saveInformation.names[i]+": "+saveInformation.getValue(i))
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, informationViewList)
        _binding.mainReminders.adapter = adapter
    }

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
        }
    }

    private fun showInputDialog(i : Int, inputText : String) {
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(inputText)

        val input = EditText(requireContext())
        input.hint = "Enter the value"
        // TODO: make option for -1 for check box, shouldn't show dialog, just check it
        input.inputType = saveInformation.getInputType(i)
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            saveInformation.values[i] = input.text.toString()
            reloadMainReminders()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}