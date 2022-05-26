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

    fun getSectionTitle(i : Int) : String {
        return sectionTitles[i]
    }

    fun getTodo(i : Int, j : Int) : String {
        return sectionTodos[i][j]
    }

    fun getTodo(i : Int) : ArrayList<String> {
        return sectionTodos[i]
    }

    fun getSize() : Int {
        return sectionTitles.size
    }

    fun readTodos() {
        if (!todosFile.exists()) {
            return
        } else {
            val todoLines = todosFile.readText().lines()
            for (line in 0 until todoLines.size) {
                val splitLine = todoLines[line].split(",")
                if (splitLine.isEmpty()) return

                sectionTitles.add(splitLine[0])
                sectionTodos.add(ArrayList(10))

                for (todoNum in 0 until splitLine.size) {
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

        for (sectionNum in 0 until sectionTitles.size) {
            todosFile.writeText(sectionTitles[sectionNum]+",")
            for (todoNum in 0 .. sectionTodos[sectionNum].size) {
                todosFile.writeText(sectionTodos[sectionNum][todoNum]+",")
            }
        }
    }

}