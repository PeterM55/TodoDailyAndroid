package peter.mitchell.tododaily.ui.dashboard

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_dashboard.*
import peter.mitchell.tododaily.HelperClasses.TodoLists
import peter.mitchell.tododaily.databinding.FragmentDashboardBinding
import peter.mitchell.tododaily.saveDailyInformationFile
import peter.mitchell.tododaily.saveInformation
import peter.mitchell.tododaily.todoLists
import peter.mitchell.tododaily.ui.home.ManageDailyNotifications

class DashboardFragment : Fragment() {

    private lateinit var _binding: FragmentDashboardBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var imm: InputMethodManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (todoLists == null)
            todoLists = TodoLists()

        _binding.addNewTodoButton.setOnClickListener {
            showInputDialog("Please enter the new section name")
        }

        return root
    }

    private fun reloadTodoList() {

        /*var tempTodoList : ArrayList<String> = ArrayList<String>()
        tempTodoList.add("test1")
        tempTodoList.add("test2")
        tempTodoList.add("test3")
        tempTodoList.add("test4")*/
        _binding.todoListsVisual.setupTitles(todoLists!!.getSectionTitles())

        /*var tempTodoContent : ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
        tempTodoContent.add(ArrayList())
        tempTodoContent.add(ArrayList())
        tempTodoContent.add(ArrayList())
        tempTodoContent.add(ArrayList())
        tempTodoContent[0].add("test11")
        tempTodoContent[0].add("test12")
        tempTodoContent[0].add("test13")
        tempTodoContent[0].add("test14")
        tempTodoContent[0].add("test15")
        tempTodoContent[1].add("test21")
        tempTodoContent[1].add("test22")
        tempTodoContent[1].add("test23")
        tempTodoContent[1].add("test24")
        tempTodoContent[2].add("test31")*/
        _binding.todoListsVisual.setupContent(todoLists!!.getTodos())

        for (i in 0 until _binding.todoListsVisual.listContent.size) {
            _binding.todoListsVisual.sectionAddButtons[i].setOnClickListener {
                openEditTodo(i,-1)
            }
            _binding.todoListsVisual.titleText[i].setOnClickListener {
                openEditTodo(i,-2)
            }
            for (j in 0 until _binding.todoListsVisual.listContent[i].size) {
                _binding.todoListsVisual.listContent[i][j].setOnClickListener {
                    openEditTodo(i, j)
                }
            }
        }

    }

    private fun openEditTodo(i : Int, j : Int) {
        val intent = Intent(activity as Context, EditTodo::class.java)
        intent.putExtra("indexi", i)
        intent.putExtra("indexj", j)
        startActivity(intent)
    }

    /** Shows the dialog to input the value
     *
     * @param inputText the text to ask the user
     */
    private fun showInputDialog(inputText: String) {
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(inputText)

        val input = EditText(requireContext())
        input.hint = "Enter the value"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            todoLists!!.addSection(input.text.toString())
            reloadTodoList()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    override fun onResume() {
        reloadTodoList()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}