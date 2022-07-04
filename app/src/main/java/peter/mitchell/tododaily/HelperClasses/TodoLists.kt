package peter.mitchell.tododaily.HelperClasses

import android.util.Log
import peter.mitchell.tododaily.settingsFile
import peter.mitchell.tododaily.todosFile
import java.lang.StringBuilder

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

    fun getSize(i : Int) : Int {
        return sectionTodos[i].size
    }

    public fun moveFrom(sectionNum : Int, i : Int, to : Int) {

        if (i == to || i >= sectionTodos[sectionNum].size || to >= sectionTodos[sectionNum].size) return

        var tempName = sectionTodos[sectionNum][i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    sectionTodos[sectionNum][j] = sectionTodos[sectionNum][j+1]
                } else if (j == to) {
                    sectionTodos[sectionNum][j] = tempName
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    sectionTodos[sectionNum][j] = sectionTodos[sectionNum][j-1]
                } else if (j == to) {
                    sectionTodos[sectionNum][j] = tempName
                }
            }
        }

        saveTodos()
    }

    public fun moveSectionFrom(i : Int, to : Int) {

        if (i == to || i >= sectionTitles.size || to >= sectionTitles.size) return

        var tempName = sectionTitles[i]
        var tempArr = sectionTodos[i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    sectionTitles[j] = sectionTitles[j+1]
                    sectionTodos[j] = sectionTodos[j+1]
                } else if (j == to) {
                    sectionTitles[j] = tempName
                    sectionTodos[j] = tempArr
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    sectionTitles[j] = sectionTitles[j-1]
                    sectionTodos[j] = sectionTodos[j-1]
                } else if (j == to) {
                    sectionTitles[j] = tempName
                    sectionTodos[j] = tempArr
                }
            }
        }

        saveTodos()
    }

    private fun readTodos() {
        if (!todosFile.exists()) {
            return
        } else {
            sectionTitles.clear()
            sectionTodos.clear()

            val todoLines = todosFile.readText().lines()
            for (line in 0 until todoLines.size) {
                if (todoLines[line].length <= 2) return
                sectionTodos.add(ArrayList(10))

                var i : Int = 1 // skip first "
                var previousWasQuote : Boolean = false
                var savedTitle : Boolean = false
                var currentString = StringBuilder()

                while (i < todoLines[line].length) {

                    if (!previousWasQuote && todoLines[line][i] == '\"' && todoLines[line][i+1] == ',') {

                        if (savedTitle)
                            sectionTodos[sectionTodos.size-1].add(currentString.toString())
                        else {
                            sectionTitles.add(currentString.toString())
                            savedTitle = true
                        }
                        currentString.clear()
                        i+= 2 // adds 3 (because of i++), starting on the char after " or exiting while
                        previousWasQuote = false
                    } else {

                        if (todoLines[line][i] == '\"') {

                            if (!previousWasQuote)
                                currentString.append(todoLines[line][i])

                            previousWasQuote = !previousWasQuote
                        } else {
                            currentString.append(todoLines[line][i])
                        }

                    }

                    i++
                }




                /*val splitLine = todoLines[line].split(",")
                if (splitLine.size <= 1) return

                var splitIndex = 0

                sectionTitles.add(splitLine[splitIndex++])
                sectionTodos.add(ArrayList(10))

                for (todoNum in splitIndex until splitLine.size) {
                    if (splitLine[todoNum].isNullOrEmpty()) continue

                    if (todoNum != 1 && splitLine[todoNum-1].get(splitLine[todoNum-1].length-1) == '\\') {
                        sectionTodos[sectionTodos.size-1][sectionTitles.size-1] += ","+splitLine[splitIndex++]
                    } else
                        sectionTodos[sectionTodos.size-1].add(splitLine[todoNum])
                }*/
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
            todosFile.appendText("\""+sectionTitles[sectionNum].replace("\"","\"\"").replace("\n"," ")+"\",")
            for (todoNum in 0 until sectionTodos[sectionNum].size) {
                todosFile.appendText("\""+sectionTodos[sectionNum][todoNum].replace("\"","\"\"").replace("\n"," ")+"\",")
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