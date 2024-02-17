package peter.mitchell.tododaily.ui.notes

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.darkMode
import peter.mitchell.tododaily.databinding.EditNotesBinding
import peter.mitchell.tododaily.databinding.HelpScreenBinding
import peter.mitchell.tododaily.databinding.ManageNotesBinding
import peter.mitchell.tododaily.notesList
import java.time.LocalDateTime

/** Edit note activity, this activity allows the user to edit the notes and the title of the note
 * An index and whether it is a list must be passed in with the intent
 */
class EditNotes : AppCompatActivity() {

    var isList : Boolean = true
    var fileIndex : Int = -1

    var confirmedDelete = false

    /*val closeActivity = Thread {
        try {
            Thread.sleep(60000)
            saveNote()
        } catch (e: Exception) {
            e.localizedMessage
        }
    }*/

    private lateinit var binding: EditNotesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (darkMode)
            binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))

        supportActionBar?.hide();

        isList = intent.getBooleanExtra("isList", false)
        fileIndex = intent.getIntExtra("selectedFileIndex", -1)
        Log.i("tdd.editNote", "index: ${fileIndex}")

        if (fileIndex != -1) {
            if (isList)
                binding.noteTitleTextBox.setText(notesList!!.listsFiles[fileIndex])
            else
                binding.noteTitleTextBox.setText(notesList!!.notesFiles[fileIndex])
        }
        if (isList)
            binding.noteTextBox.setText(notesList!!.readList(fileIndex))
        else
            binding.noteTextBox.setText(notesList!!.readNote(fileIndex))

        // how to do bold etc: https://stackoverflow.com/questions/14371092/how-to-make-a-specific-text-on-textview-bold/50243835#50243835
        // how to add select text option: https://stackoverflow.com/questions/51632908/how-to-add-an-item-to-the-text-selection-popup-menu

        // noteTextBox.setText(  SpannableStringBuilder().bold { append("this is a test bold") }.append(noteTextBox.text)  )

        binding.noteTitleTextBox.setOnKeyListener { view, i, keyEvent ->
            if (binding.noteTitleTextBox.text.contains("\n")) {
                binding.noteTitleTextBox.setText(binding.noteTitleTextBox.text.toString().replace('\n', ' ', true))
                return@setOnKeyListener true
            }
            false
        }

        binding.editNoteBackButton.setOnClickListener {
            if (saveNote())
                finish()
        }

        binding.saveNoteButton.setOnClickListener {
            saveNote()
        }

        binding.deleteNoteButton.setOnClickListener {
            deleteNote()
        }

        binding.noteTextBox.addTextChangedListener {
            binding.saveNoteButton.text = "Save"
        }

    }



    /** Save the note using notesList's savelist or savenote depending on what was passed in
     * @return whether it worked
     */
    fun saveNote() : Boolean {

        var titleString = binding.noteTitleTextBox.text.toString()

        if (titleString.isNullOrEmpty()) { // || noteTextBox.text.toString().isNullOrEmpty()
            if (fileIndex == -1)
                return true
            return false
        }

        while (titleString.isNotEmpty() && titleString.last() == ' ') {
            titleString = titleString.substring(0, titleString.length-1)
        }

        var tempIndex : Int
        if (isList)
            tempIndex = notesList!!.saveList(fileIndex, titleString, binding.noteTextBox.text.toString())
        else
            tempIndex = notesList!!.saveNote(fileIndex, titleString, binding.noteTextBox.text.toString())

        if (tempIndex == -1) {
            Toast.makeText(this, "Unable to save file, file name invalid", Toast.LENGTH_SHORT)
                .show()
            return false
        } else
            fileIndex = tempIndex

        binding.saveNoteButton.text = "Saved"

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

    override fun onPause() {
        super.onPause()
        if (!confirmedDelete)
            saveNote()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!confirmedDelete)
            saveNote()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        if (saveNote()) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Unable to save file", Toast.LENGTH_SHORT).show()
        }
    }
}