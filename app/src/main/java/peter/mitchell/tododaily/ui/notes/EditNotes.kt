package peter.mitchell.tododaily.ui.notes

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.edit_notes.*
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.notesList

/** Edit note activity, this activity allows the user to edit the notes and the title of the note
 * An index and whether it is a list must be passed in with the intent
 */
class EditNotes : AppCompatActivity() {

    var isList : Boolean = true
    var fileIndex : Int = -1

    var confirmedDelete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_notes)

        supportActionBar?.hide();

        isList = intent.getBooleanExtra("isList", false)
        fileIndex = intent.getIntExtra("selectedFileIndex", -1)
        Log.i("tdd.editNote", "index: ${fileIndex}")

        if (fileIndex != -1) {
            if (isList)
                noteTitleTextBox.setText(notesList!!.listsFiles[fileIndex])
            else
                noteTitleTextBox.setText(notesList!!.notesFiles[fileIndex])
        }
        if (isList)
            noteTextBox.setText(notesList!!.readList(fileIndex))
        else
            noteTextBox.setText(notesList!!.readNote(fileIndex))

        noteTitleTextBox.setOnKeyListener { view, i, keyEvent ->
            if (noteTitleTextBox.text.contains("\n")) {
                noteTitleTextBox.setText(noteTitleTextBox.text.toString().replace('\n', ' ', true))
                return@setOnKeyListener true
            }
            false
        }

        editNoteBackButton.setOnClickListener {
            if (saveNote())
                finish()
        }

        saveNoteButton.setOnClickListener {
            saveNote()
        }

        deleteNoteButton.setOnClickListener {
            deleteNote()
        }

    }

    /** Save the note using notesList's savelist or savenote depending on what was passed in
     * @return whether it worked
     */
    fun saveNote() : Boolean {

        if (noteTitleTextBox.text.toString().isNullOrEmpty()) { // || noteTextBox.text.toString().isNullOrEmpty()
            if (fileIndex == -1)
                return true
            return false
        }

        var tempIndex : Int
        if (isList)
            tempIndex = notesList!!.saveList(fileIndex, noteTitleTextBox.text.toString(), noteTextBox.text.toString())
        else
            tempIndex = notesList!!.saveNote(fileIndex, noteTitleTextBox.text.toString(), noteTextBox.text.toString())


        if (tempIndex == -1) {
            Toast.makeText(this, "Unable to save file, file name invalid", Toast.LENGTH_SHORT)
                .show()
            return false
        } else
            fileIndex = tempIndex

        return true
    }

    /** Delete the note currently open *IF* confirmation is given in the dialogue */
    fun deleteNote() {
        if (!confirmedDelete) {

            MaterialAlertDialogBuilder(this).setTitle("Delete?")
                .setMessage("This will delete the file, which cannot be recovered.")
                .setNegativeButton("Cancel") { dialog, which ->
                }.setPositiveButton("Delete") { dialog, which ->
                    confirmedDelete = true
                    deleteNote()
                }.show()

        } else {
            if (isList)
                notesList!!.deleteList(fileIndex)
            else
                notesList!!.deleteNote(fileIndex)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveNote()
    }
}