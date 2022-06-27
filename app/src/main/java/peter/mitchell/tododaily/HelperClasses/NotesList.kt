package peter.mitchell.tododaily.HelperClasses

import android.util.Log
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

class NotesList {

    val notesListFile : File = File("/data/data/peter.mitchell.tododaily/files/notesList.txt")
    val notesPath : String = "/data/data/peter.mitchell.tododaily/files/notes/"

    var notesFiles : ArrayList<String> = ArrayList()
    var listsFiles : ArrayList<String> = ArrayList()

    init {
        readNotesList()
    }

    fun addNote(fileTitle : String) : Boolean {

        if (!createFile(fileTitle))
            return false

        notesFiles.add(fileTitle)
        return true
    }

    fun addList(fileTitle : String) : Boolean {

        if (!createFile(fileTitle))
            return false

        listsFiles.add(fileTitle)
        return true
    }

    fun deleteOldNote(i : Int, leaveIndex : Boolean = false) {
        if (i == -1) return

        var delFile : File = File("$notesPath${notesFiles[i]}.txt")

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

    fun deleteOldList(i : Int) {
        if (i == -1) return

        var delFile : File = File("$notesPath${listsFiles[i]}.txt")

        if (!delFile.exists()) {
            return
        }

        delFile.delete()
        listsFiles.removeAt(i)
        saveNotesList()
    }

    fun readNotesList() {
        notesFiles.clear()
        listsFiles.clear()

        if (!notesListFile.exists()) {
            return
        } else {
            var splitLines = notesListFile.readText().split("\n")
            var notesSplit = splitLines[0].split("|")
            var listsSplit = splitLines[1].split("|")

            for (i in 0 until notesSplit.size) {
                if (notesSplit[i].isNotEmpty())
                    notesFiles.add(notesSplit[i])
            }

            for (i in 0 until listsSplit.size) {
                if (listsSplit[i].isNotEmpty())
                    listsFiles.add(listsSplit[i])
            }

        }
    }

    fun saveNotesList() {

        var stringBuilder = StringBuilder()

        if (!notesListFile.exists()) {
            notesListFile.parentFile!!.mkdirs()
            notesListFile.createNewFile()
        }

        for (i in 0 until notesFiles.size) {
            Log.i("saveNotesList", "Saving: ${notesFiles[i]}")
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

    private fun createFile(fileTitle : String) : Boolean {

        if (!fileNameValid(fileTitle)) return false

        var finalPath : File = File("$notesPath$fileTitle.txt")

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

    fun readNote(i : Int) : String {
        if (i == -1) return ""
        val saveFile : File = File("$notesPath${notesFiles[i]}.txt")
        if (!saveFile.exists()) {
            return ""
        } else {
            return saveFile.readText()
        }
    }

    fun readList(i : Int) : String {
        if (i == -1) return ""
        val saveFile : File = File("$notesPath${listsFiles[i]}.txt")
        if (!saveFile.exists()) {
            return ""
        } else {
            return saveFile.readText()
        }
    }

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
            deleteOldNote(i, true)
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
            deleteOldNote(i, true)
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

    fun fileNameValid(fileTitle : String) : Boolean {

        var invalidList = arrayOf("CON", "PRN", "AUX", "NUL")

        for (i in 0 until invalidList.size) {
            if (fileTitle == invalidList[i]) {
                return false
            }
        }

        if (fileTitle.length < 5) {
            var illegalStarts = arrayOf("COM, LPT")
            for (i in 0 until illegalStarts.size) {
                if (fileTitle.contains(illegalStarts[i], true)) {
                    return false
                }
            }
        }

        var illegalCharacters = arrayOf("<", ">", ":", "\"", "/", "\\", "|", "?", "*", "\n")
        for (i in 0 until illegalCharacters.size) {
            if (fileTitle.contains(illegalCharacters[i])) {
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

    fun moveNoteFrom(i : Int, to : Int) {
        // move the note, then save

        if (i == to || i >= notesFiles.size || to >= notesFiles.size) return

        var tempName = notesFiles[i]

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

    fun moveListFrom(i : Int, to : Int) {
        // move the note, then save

        if (i == to || i >= listsFiles.size || to >= listsFiles.size) return

        var tempName = listsFiles[i]

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