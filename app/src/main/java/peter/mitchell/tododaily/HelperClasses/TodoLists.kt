package peter.mitchell.tododaily.HelperClasses

import android.util.Log
import peter.mitchell.tododaily.settingsFile
import peter.mitchell.tododaily.todosFile
import java.lang.StringBuilder

/** holds an array of titles, and the contents of each section of the to-do list. */
class TodoLists {

    private var sectionTitles : ArrayList<String> = ArrayList(5)
    private var sectionTodos : ArrayList<ArrayList<String>> = ArrayList(5)

    init {
        readTodos()
    }

    /** Adds a section with the provided name
     *
     * @param title the title of the section to add
     */
    fun addSection(title : String) {
        sectionTitles.add(title)
        sectionTodos.add(ArrayList(10))
        saveTodos()
    }

    /** Add the string to the section specified
     *
     * @param i the section index to it to
     * @param todo1 the to-do to add to the section
     */
    fun addTodo(i : Int, todo1 : String) {
        sectionTodos[i].add(todo1)
        saveTodos()
    }

    /** remove the to-do at the index specified
     *
     * @param i the section index
     * @param j the index in the section
     */
    fun removeTodo(i : Int, j : Int) {
        sectionTodos[i].removeAt(j)
        saveTodos()
    }

    /** Set the to-do at the index specify
     *
     * @param i the section index
     * @param j the index in the section
     * @param todo1 the new text
     */
    fun setTodo(i : Int, j : Int, todo1 : String) {
        sectionTodos[i][j] = todo1
        saveTodos()
    }

    /** get the title section at the index
     *
     * @param i the section index
     * @return the section title
     */
    fun getSectionTitle(i : Int) : String {
        return sectionTitles[i]
    }

    /** set the section title at the index
     *
     * @param i the section index
     * @param str the new section title
     */
    fun setSectionTitle(i : Int, str : String) {
        sectionTitles[i]  = str
        saveTodos()
    }

    /** Remove a section
     *
     * @param i the section index
     */
    fun removeSection(i : Int) {
        sectionTitles.removeAt(i)
        sectionTodos.removeAt(i)
        saveTodos()
    }

    /** get all section titles
     *
     * @return the arraylist of section titles
     */
    fun getSectionTitles() : ArrayList<String> {
        return sectionTitles
    }

    /** Get the to-do at the given index
     *
     * @param i the section index
     * @param j the index in the section
     * @return the to-do content
     */
    fun getTodo(i : Int, j : Int) : String {
        return sectionTodos[i][j]
    }

    /** Get the to-do arraylist at the given index
     *
     * @param i the section index
     * @return the to-do arraylist
     */
    fun getTodo(i : Int) : ArrayList<String> {
        return sectionTodos[i]
    }

    /** Get the full to-do arraylist of arraylists of strings
     *
     * @return the complete to-do arraylist
     */
    fun getTodos() : ArrayList<ArrayList<String>> {
        return sectionTodos
    }

    /** Get the number of sections
     *
     * @return the number of sections
     */
    fun getSize() : Int {
        return sectionTitles.size
    }

    /** Get the size of the section
     *
     * @return the number of to-dos in the section
     */
    fun getSize(i : Int) : Int {
        return sectionTodos[i].size
    }

    /** move a to-do from one index to another in the same section
     *
     * @param sectionNum the section to edit
     * @param i the index to be moved from
     * @param to the index to be moved to
     */
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

    /** Move a section
     *
     * @param i the index to be moved from
     * @param to the index to be moved to
     */
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

    /** Move a to-do from a section to another
     *
     * @param iSection the section to be moved from
     * @param toSection the section to be moved to
     * @param fromIndex the index to be moved from
     * @param toIndex the index to be moved to
     */
    public fun moveFromSection(iSection : Int, toSection : Int, fromIndex : Int, toIndex : Int) {

        if (iSection == toSection || fromIndex >= sectionTodos[iSection].size || toIndex > sectionTodos[toSection].size) return

        var tempName = sectionTodos[iSection][fromIndex]

        removeTodo(iSection, fromIndex)

        addTodo(toSection, tempName)

        moveFrom(toSection, sectionTodos[toSection].size-1, toIndex)
    }

    /** Reads all of the to-do information from the todosFile file */
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

            }
        }
    }

    /** Saves all of the to-do information to the todosFile file */
    private fun saveTodos() {
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

}