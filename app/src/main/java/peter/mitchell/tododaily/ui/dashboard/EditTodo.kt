package peter.mitchell.tododaily.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.databinding.EditTodoBinding
import peter.mitchell.tododaily.databinding.HelpScreenBinding
import peter.mitchell.tododaily.databinding.SettingsScreenBinding
import java.time.LocalDate

/** Edit the to-do element at the index provided by the intent */
class EditTodo : AppCompatActivity() {

    // Section index
    var myIndexI : Int = -1 // -1 = error
    // to do index IN section
    var myIndexJ : Int = -1 // -1 = new to do, -2 = editing section

    var confirmedDelete = false

    private lateinit var binding: EditTodoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditTodoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //setContentView(R.layout.edit_todo)

        if (darkMode)
            binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        myIndexI = intent.getIntExtra("indexi", -1)
        myIndexJ = intent.getIntExtra("indexj", -1)
        if (myIndexI == -1) finish()

        val arrayListAdapter = todoLists!!.getSectionTitles().clone() as ArrayList<String>
        if (myIndexJ == -2) arrayListAdapter.add("End")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListAdapter)
        binding.todoSectionInput.adapter = adapter
        binding.todoSectionInput.setSelection(myIndexI)
        if (myIndexJ != -2) {
            binding.todoSectionInput.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    reloadIndexSpinner()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        if (myIndexJ == -2) {

            binding.todoSection.setText("Section Comes Before: ")
            binding.notificationNameTitle.setText("Section Title: ")
            binding.todoNameInput.setText(todoLists!!.getSectionTitle(myIndexI))
            //extraInfoLayout.isVisible = false

        } else if (myIndexJ != -1)
            binding.todoNameInput.setText(todoLists!!.getTodo(myIndexI,myIndexJ))

        reloadIndexSpinner()

        binding.editTodoSubmitButton.setOnClickListener {

            binding.todoNameInput.setText(binding.todoNameInput.text.toString().replace("\n", " "))

            if (myIndexJ == -2) {
                if (binding.todoNameInput.text.toString() != "")
                    todoLists!!.setSectionTitle(myIndexI, binding.todoNameInput.text.toString())
                else
                    todoLists!!.removeSection(myIndexI)

                todoLists!!.moveSectionFrom(myIndexI, binding.todoSectionInput.selectedItemPosition)
            } else if (myIndexJ == -1) {
                if (binding.todoNameInput.text.toString() != "")
                    todoLists!!.addTodo(myIndexI, binding.todoNameInput.text.toString())

                if (myIndexJ != -2 && myIndexI != binding.todoSectionInput.selectedItemPosition)
                    todoLists!!.moveFromSection(myIndexI, binding.todoSectionInput.selectedItemPosition, myIndexJ, binding.todoPositionInput.selectedItemPosition)
                else
                    todoLists!!.moveFrom(myIndexI, todoLists!!.getSize(myIndexI)-1, binding.todoPositionInput.selectedItemPosition)
            } else {
                if (binding.todoNameInput.text.toString() != "")
                    todoLists!!.setTodo(myIndexI, myIndexJ, binding.todoNameInput.text.toString())
                else
                    todoLists!!.removeTodo(myIndexI, myIndexJ)

                if (myIndexJ != -2 && myIndexI != binding.todoSectionInput.selectedItemPosition)
                    todoLists!!.moveFromSection(myIndexI, binding.todoSectionInput.selectedItemPosition, myIndexJ, binding.todoPositionInput.selectedItemPosition)
                else
                    todoLists!!.moveFrom(myIndexI, myIndexJ, binding.todoPositionInput.selectedItemPosition)
            }
            finish()
        }

        binding.deleteTodoButton.setOnClickListener {
            deleteTodo()
        }

    }

    private fun deleteTodo() {
        if (!confirmedDelete && todoConfirmDelete) {

            MaterialAlertDialogBuilder(this).setTitle("Delete?")
                .setMessage("Are you sure?")
                .setNegativeButton("Cancel") { dialog, which ->
                }.setPositiveButton("Delete") { dialog, which ->
                    confirmedDelete = true
                    deleteTodo()
                }.show()

        } else {
            if (myIndexJ == -2) {
                todoLists!!.removeSection(myIndexI)
            } else if (myIndexJ != -1) {
                todoLists!!.removeTodo(myIndexI, myIndexJ)
            }
            finish()
        }
    }

    private fun reloadIndexSpinner() {

        if (myIndexJ != -2) {
            var positionArrayList = ArrayList<String>()
            for (i in 0 until todoLists!!.getSize(binding.todoSectionInput.selectedItemPosition)) {
                positionArrayList.add("${i + 1}")
            }
            if (myIndexJ == -1 || myIndexI != binding.todoSectionInput.selectedItemPosition) {
                positionArrayList.add("${positionArrayList.size+1}")
            }
            binding.todoPositionInput.adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, positionArrayList)

            if (myIndexJ == -1 || myIndexI != binding.todoSectionInput.selectedItemPosition)
                binding.todoPositionInput.setSelection(positionArrayList.size-1)
            else
                binding.todoPositionInput.setSelection(myIndexJ)

        } else
            binding.todoPositionLayout.isVisible = false

    }

}