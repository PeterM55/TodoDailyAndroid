package peter.mitchell.tododaily.HelperClasses

import android.util.Log
import peter.mitchell.tododaily.settingsFile
import peter.mitchell.tododaily.todosFile

class TodoLists {

    private var sectionTitles : ArrayList<String> = ArrayList(5)
    private var sectionTodos : ArrayList<ArrayList<String>> = ArrayList(5)

    init {
        readTodos()
    }

    fun addSection(title : String) {
        sectionTitles.add(title)
        sectionTodos.add(ArrayList(10))
        saveTodos()
    }

    fun addTodo(i : Int, todo : String) {
        sectionTodos[i].add(todo)
        saveTodos()
    }

    fun removeTodo(i : Int, j : Int) {
        sectionTodos[i].removeAt(j)
        saveTodos()
    }

    fun setTodo(i : Int, j : Int, todo : String) {
        sectionTodos[i][j] = todo
        saveTodos()
    }

    fun getSectionTitle(i : Int) : String {
        return sectionTitles[i]
    }

    fun setSectionTitle(i : Int, str : String) {
        sectionTitles[i]  = str
        saveTodos()
    }

    fun removeSection(i : Int) {
        sectionTitles.removeAt(i)
        sectionTodos.removeAt(i)
        saveTodos()
    }

    fun getSectionTitles() : ArrayList<String> {
        return sectionTitles
    }

    fun getTodo(i : Int, j : Int) : String {
        return sectionTodos[i][j]
    }

    fun getTodo(i : Int) : ArrayList<String> {
        return sectionTodos[i]
    }

    fun getTodos() : ArrayList<ArrayList<String>> {
        return sectionTodos
    }

    fun getSize() : Int {
        return sectionTitles.size
    }

    private fun readTodos() {
        if (!todosFile.exists()) {
            return
        } else {
            val todoLines = todosFile.readText().lines()
            for (line in 0 until todoLines.size) {
                val splitLine = todoLines[line].split(",")
                if (splitLine.size <= 1) return

                sectionTitles.add(splitLine[0])
                sectionTodos.add(ArrayList(10))

                for (todoNum in 1 until splitLine.size) {
                    if (splitLine[todoNum].isNotEmpty())
                        sectionTodos[line].add(splitLine[todoNum])
                }
            }
        }
    }

    fun saveTodos() {
        if (sectionTitles.size != sectionTodos.size) {
            Log.e("TodoLists", "Sizes are not the same, aborting save")
            return
        }

        if (!todosFile.exists()) {
            todosFile.parentFile!!.mkdirs()
            todosFile.createNewFile()
        }

        todosFile.writeText("")
        for (sectionNum in 0 until sectionTitles.size) {
            todosFile.appendText(sectionTitles[sectionNum]+",")
            for (todoNum in 0 until sectionTodos[sectionNum].size) {
                todosFile.appendText(sectionTodos[sectionNum][todoNum]+",")
            }
            todosFile.appendText("\n")
        }
    }

    fun debugTodos() {
        for (sectionNum in 0 until sectionTitles.size) {
            Log.i("TodoListsSave-$sectionNum", sectionTitles[sectionNum]+",")
            for (todoNum in 0 until sectionTodos[sectionNum].size) {
                Log.i("TodoListsSave-$sectionNum-$todoNum", sectionTodos[sectionNum][todoNum]+",")
            }
        }
    }

}