package peter.mitchell.tododaily.HelperClasses

import peter.mitchell.tododaily.notificationsFullNameMode
import peter.mitchell.tododaily.settingsFile
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

    fun readNotesList() {
        notesFiles.clear()
        listsFiles.clear()

        if (!notesListFile.exists()) {
            return
        } else {
            var splitLines = settingsFile.readText().split("\n")
            var notesSplit = splitLines[0].split(" ")
            var listsSplit = splitLines[1].split(" ")

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
            if (notesFiles[i].isNotEmpty())
                stringBuilder.append(notesFiles[i])
        }
        stringBuilder.append("\n")
        for (i in 0 until listsFiles.size) {
            if (listsFiles[i].isNotEmpty())
                stringBuilder.append(listsFiles[i])
        }

        notesListFile.writeText(stringBuilder.toString())

    }

    private fun createFile(fileTitle : String) : Boolean {

        for (i in 0 until fileTitle.length) {
            if (!fileTitle[i].isLetterOrDigit() && fileTitle[i] != ' ') {
                return false
            }
        }

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

}