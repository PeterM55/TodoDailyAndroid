package peter.mitchell.tododaily.HelperClasses

import android.util.Log
import peter.mitchell.tododaily.internalDataPath
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

/** Reads and stores a list of text files sorted into notes files and list files
 *
 */
class NotesList {

    private val notesListFile : File = File("${internalDataPath}notesList.txt")
    private val notesPath : String = "${internalDataPath}/notes/"

    var notesFiles : ArrayList<String> = ArrayList()
    var listsFiles : ArrayList<String> = ArrayList()

    init {
        readNotesList()
    }

    /** Delete a note
     * @param i The index of the note
     * @param leaveIndex (default false), whether to remove the index after deleting the note
     */
    fun deleteNote(i : Int, leaveIndex : Boolean = false) {
        if (i == -1) return

        val delFile : File = File("$notesPath${notesFiles[i]}.txt")

        if (!delFile.exists()) {
            if (leaveIndex)
                notesFiles[i] = ""
            else
                notesFiles.removeAt(i)
            saveNotesList()
            return
        }

        delFile.delete()
        if (leaveIndex)
            notesFiles[i] = ""
        else
            notesFiles.removeAt(i)

        saveNotesList()
    }

    /** Delete a list
     * @param i The index of the list
     * @param leaveIndex (default false), whether to remove the index after deleting the note
     */
    fun deleteList(i : Int, leaveIndex : Boolean = false) {
        if (i == -1) return

        val delFile : File = File("$notesPath${listsFiles[i]}.txt")

        if (!delFile.exists()) {
            if (leaveIndex)
                listsFiles[i] = ""
            else
                listsFiles.removeAt(i)
            saveNotesList()
            return
        }

        delFile.delete()
        if (leaveIndex)
            listsFiles[i] = ""
        else
            listsFiles.removeAt(i)

        saveNotesList()
    }

    /** Read the note list
     * Reads the note list from the notesListFile, which includes the titles of the notes and lists
     * Called from init
     */
    private fun readNotesList() {
        notesFiles.clear()
        listsFiles.clear()

        if (!notesListFile.exists()) {
            return
        } else {
            val splitLines = notesListFile.readText().split("\n")
            val notesSplit = splitLines[0].split("|")
            val listsSplit = splitLines[1].split("|")

            for (i in notesSplit.indices) {
                if (notesSplit[i].isNotEmpty())
                    notesFiles.add(notesSplit[i])
            }

            for (i in listsSplit.indices) {
                if (listsSplit[i].isNotEmpty())
                    listsFiles.add(listsSplit[i])
            }

        }
    }

    /** Save the notes list
     * Saves the list of titles of notes and lists
     */
    private fun saveNotesList() {

        val stringBuilder = StringBuilder()

        if (!notesListFile.exists()) {
            notesListFile.parentFile!!.mkdirs()
            notesListFile.createNewFile()
        }

        for (i in 0 until notesFiles.size) {
            if (notesFiles[i].isNotEmpty())
                stringBuilder.append(notesFiles[i]+"|")
        }
        stringBuilder.append("\n")
        for (i in 0 until listsFiles.size) {
            if (listsFiles[i].isNotEmpty())
                stringBuilder.append(listsFiles[i]+"|")
        }

        notesListFile.writeText(stringBuilder.toString())

    }

    /** Creates a file in the notes folder under the name given
     * @param fileTitle The title of the file
     * @return whether the file was created/exists
     */
    private fun createFile(fileTitle : String) : Boolean {

        if (!fileNameValid(fileTitle)) return false

        val finalPath : File = File("$notesPath$fileTitle.txt")

        if (finalPath.exists()) {

            if (!notesFiles.contains(fileTitle) && !listsFiles.contains(fileTitle)) {
                return true
            }

            return false
        } else {
            try {
                finalPath.parentFile!!.mkdirs()
                finalPath.createNewFile()
            } catch (e : Exception) {
                return false
            }
        }

        if (!finalPath.exists())
            return false

        return true
    }

    /** Reads the note at the given index, returning the contents
     * @param i the index of the note
     * @return The contents of the file
     */
    fun readNote(i : Int) : String {
        if (i == -1) return ""
        val saveFile : File = File("$notesPath${notesFiles[i]}.txt")
        if (!saveFile.exists())
            return ""

        return saveFile.readText()
    }

    /** Reads the list at the given index, returning the contents
     * @param i the index of the list
     * @return The contents of the file
     */
    fun readList(i : Int) : String {
        if (i == -1) return ""
        val saveFile : File = File("$notesPath${listsFiles[i]}.txt")
        if (!saveFile.exists())
            return ""

        return saveFile.readText()
    }

    /** Save the given note, using the parameters given
     * @param i the index of the note
     * @param title the title of the note
     * @param text the text to put in the file
     * @return the new index of the note, -1 if it didn't work
     */
    fun saveNote(i : Int, title : String, text : String) : Int {

        var newIndex : Int = i

        // --- delete the old one ---
        if (i == -1) {
            if (!createFile(title))
                return -1
            notesFiles.add(title)
            newIndex = notesFiles.size-1
            saveNotesList()
        } else if (title != notesFiles[i]) {
            if (!createFile(title))
                return -1
            deleteNote(i, true)
            notesFiles[i] = title
            saveNotesList()
    }

        // --- write the new string ---
        val saveFile : File = File("$notesPath$title.txt")

        if (!saveFile.exists())
            return -1

        saveFile.writeText(text)

        return newIndex
    }

    /** Save the given list, using the parameters given
     * @param i the index of the list
     * @param title the title of the list
     * @param text the text to put in the file
     * @return the new index of the note, -1 if it didn't work
     */
    fun saveList(i : Int, title : String, text : String) : Int {

        var newIndex : Int = i

        // --- delete the old one ---
        if (i == -1) {
            if (!createFile(title))
                return -1
            listsFiles.add(title)
            newIndex = listsFiles.size-1
            saveNotesList()
        } else if (title != listsFiles[i]) {
            if (!createFile(title))
                return -1
            deleteList(i, true)
            listsFiles[i] = title
            saveNotesList()
    }

        // --- write the new string ---
        val saveFile : File = File("$notesPath$title.txt")

        if (!saveFile.exists())
            return -1

        saveFile.writeText(text)

        return newIndex
    }

    /** Check if the file name is valid
     * @param fileTitle the proposed file name
     * @return whether it is valid
     */
    private fun fileNameValid(fileTitle : String) : Boolean {

        val invalidList = arrayOf("CON", "PRN", "AUX", "NUL")

        for (element in invalidList) {
            if (fileTitle == element) {
                return false
            }
        }

        if (fileTitle.length < 5) {
            val illegalStarts = arrayOf("COM, LPT")
            for (element in illegalStarts) {
                if (fileTitle.contains(element, true)) {
                    return false
                }
            }
        }

        val illegalCharacters = arrayOf("<", ">", ":", "\"", "/", "\\", "|", "?", "*", "\n")
        for (element in illegalCharacters) {
            if (fileTitle.contains(element)) {
                return false
            }
        }

        if (fileTitle.endsWith(" ") || fileTitle.endsWith(".") || fileTitle.startsWith("."))
            return false

        for (element in fileTitle) {
            if (element < 32.toChar())
                return false
        }

        return true
    }

    /** Move a note to a different index
     * @param i The starting index, the thing to move
     * @param to The index to move it to
     */
    fun moveNoteFrom(i : Int, to : Int) {
        // move the note, then save

        if (i == to || i >= notesFiles.size || to >= notesFiles.size) return

        val tempName = notesFiles[i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    notesFiles[j] = notesFiles[j+1]
                } else if (j == to) {
                    notesFiles[j] = tempName
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    notesFiles[j] = notesFiles[j-1]
                } else if (j == to) {
                    notesFiles[j] = tempName
                }
            }
        }

        saveNotesList()
    }

    /** Move a list to a different index
     * @param i The starting index, the thing to move
     * @param to The index to move it to
     */
    fun moveListFrom(i : Int, to : Int) {
        // move the note, then save

        if (i == to || i >= listsFiles.size || to >= listsFiles.size) return

        val tempName = listsFiles[i]

        if (i < to) {
            for (j in i .. to) {
                if (j < to) {
                    listsFiles[j] = listsFiles[j+1]
                } else if (j == to) {
                    listsFiles[j] = tempName
                }
            }
        } else if (to < i) {
            for (j in i downTo to) {
                if (j > to) {
                    listsFiles[j] = listsFiles[j-1]
                } else if (j == to) {
                    listsFiles[j] = tempName
                }
            }
        }

        saveNotesList()
    }

}