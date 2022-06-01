package peter.mitchell.tododaily.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.edit_todo.*
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.todoLists

class EditTodo : AppCompatActivity() {

    var myIndexI : Int = -1 // -1 = error
    var myIndexJ : Int = -1 // -1 = new to do, -2 = editing section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_todo)

        myIndexI = intent.getIntExtra("indexi", -1)
        myIndexJ = intent.getIntExtra("indexj", -1)
        if (myIndexI == -1) finish()

        val arrayListAdapter = todoLists!!.getSectionTitles().clone() as ArrayList<String>
        if (myIndexJ == -2) arrayListAdapter.add("End")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListAdapter)
        todoSectionInput.adapter = adapter
        todoSectionInput.setSelection(myIndexI)
        if (myIndexJ == -2) {

            todoSection.setText("Section Comes Before: ")
            notificationNameTitle.setText("Section Title: ")
            todoNameInput.setText(todoLists!!.getSectionTitle(myIndexI))
            extraInfoLayout.isVisible = false

        } else if (myIndexJ != -1)
            todoNameInput.setText(todoLists!!.getTodo(myIndexI,myIndexJ))

        editTodoSubmitButton.setOnClickListener {
            if (myIndexJ == -2) {
                if (todoNameInput.text.toString() != "")
                    todoLists!!.setSectionTitle(myIndexI, todoNameInput.text.toString())
                else
                    todoLists!!.removeSection(myIndexI)
            } else if (myIndexJ == -1) {
                if (todoNameInput.text.toString() != "")
                    todoLists!!.addTodo(myIndexI, todoNameInput.text.toString())
            } else {
                if (todoNameInput.text.toString() != "")
                    todoLists!!.setTodo(myIndexI, myIndexJ, todoNameInput.text.toString())
                else
                    todoLists!!.removeTodo(myIndexI, myIndexJ)
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

}