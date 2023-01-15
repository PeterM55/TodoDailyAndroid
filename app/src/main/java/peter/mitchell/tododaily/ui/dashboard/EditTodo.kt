package peter.mitchell.tododaily.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.edit_todo.*
import peter.mitchell.tododaily.*
import java.time.LocalDate

/** Edit the to-do element at the index provided by the intent */
class EditTodo : AppCompatActivity() {

    // Section index
    var myIndexI : Int = -1 // -1 = error
    // to do index IN section
    var myIndexJ : Int = -1 // -1 = new to do, -2 = editing section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_todo)

        if (darkMode)
            mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        myIndexI = intent.getIntExtra("indexi", -1)
        myIndexJ = intent.getIntExtra("indexj", -1)
        if (myIndexI == -1) finish()

        val arrayListAdapter = todoLists!!.getSectionTitles().clone() as ArrayList<String>
        if (myIndexJ == -2) arrayListAdapter.add("End")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListAdapter)
        todoSectionInput.adapter = adapter
        todoSectionInput.setSelection(myIndexI)
        if (myIndexJ != -2) {
            todoSectionInput.onItemSelectedListener = object :
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

            todoSection.setText("Section Comes Before: ")
            notificationNameTitle.setText("Section Title: ")
            todoNameInput.setText(todoLists!!.getSectionTitle(myIndexI))
            //extraInfoLayout.isVisible = false

        } else if (myIndexJ != -1)
            todoNameInput.setText(todoLists!!.getTodo(myIndexI,myIndexJ))

        reloadIndexSpinner()

        editTodoSubmitButton.setOnClickListener {

            todoNameInput.setText(todoNameInput.text.toString().replace("\n", " "))

            if (myIndexJ == -2) {
                if (todoNameInput.text.toString() != "")
                    todoLists!!.setSectionTitle(myIndexI, todoNameInput.text.toString())
                else
                    todoLists!!.removeSection(myIndexI)

                todoLists!!.moveSectionFrom(myIndexI, todoSectionInput.selectedItemPosition)
            } else if (myIndexJ == -1) {
                if (todoNameInput.text.toString() != "")
                    todoLists!!.addTodo(myIndexI, todoNameInput.text.toString())

                if (myIndexJ != -2 && myIndexI != todoSectionInput.selectedItemPosition)
                    todoLists!!.moveFromSection(myIndexI, todoSectionInput.selectedItemPosition, myIndexJ, todoPositionInput.selectedItemPosition)
                else
                    todoLists!!.moveFrom(myIndexI, todoLists!!.getSize(myIndexI)-1, todoPositionInput.selectedItemPosition)
            } else {
                if (todoNameInput.text.toString() != "")
                    todoLists!!.setTodo(myIndexI, myIndexJ, todoNameInput.text.toString())
                else
                    todoLists!!.removeTodo(myIndexI, myIndexJ)

                if (myIndexJ != -2 && myIndexI != todoSectionInput.selectedItemPosition)
                    todoLists!!.moveFromSection(myIndexI, todoSectionInput.selectedItemPosition, myIndexJ, todoPositionInput.selectedItemPosition)
                else
                    todoLists!!.moveFrom(myIndexI, myIndexJ, todoPositionInput.selectedItemPosition)
            }
            finish()
        }

        deleteTodoButton.setOnClickListener {
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
            for (i in 0 until todoLists!!.getSize(todoSectionInput.selectedItemPosition)) {
                positionArrayList.add("${i + 1}")
            }
            if (myIndexJ == -1 || myIndexI != todoSectionInput.selectedItemPosition) {
                positionArrayList.add("${positionArrayList.size+1}")
            }
            todoPositionInput.adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, positionArrayList)

            if (myIndexJ == -1 || myIndexI != todoSectionInput.selectedItemPosition)
                todoPositionInput.setSelection(positionArrayList.size-1)
            else
                todoPositionInput.setSelection(myIndexJ)

        } else
            todoPositionLayout.isVisible = false

    }

}