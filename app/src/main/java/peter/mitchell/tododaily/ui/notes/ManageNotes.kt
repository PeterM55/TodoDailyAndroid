package peter.mitchell.tododaily.ui.notes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.databinding.HelpScreenBinding
import peter.mitchell.tododaily.databinding.ManageDailyInformationBinding
import peter.mitchell.tododaily.databinding.ManageNotesBinding
import java.io.File
import java.lang.NumberFormatException
import java.time.LocalDate

/** The manage notes activity allows the user to re-arrange and export the notes.
 *
 */
class ManageNotes : AppCompatActivity() {

    private lateinit var imm: InputMethodManager

    private lateinit var binding: ManageNotesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ManageNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (darkMode)
            binding.mainBackgroundManageNotes.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            binding.mainBackgroundManageNotes.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        binding.manageNotesTextGrids.setupTitles(arrayListOf(
                "Rearrange Notes", "Rearrange Lists", "Export Note", "Export List",
            )
        )

        reloadManageNotes()

        binding.exportAllNotesButton.setOnClickListener {

            for (i in 0 until notesList!!.notesFiles.size) {
                if (i == 0 && !attemptExport(exportPath+notesList!!.notesFiles[i]+".txt", notesList!!.readNote(i))) {
                    Toast.makeText(this, "Export failed.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else
                    attemptExport(exportPath+notesList!!.notesFiles[i]+".txt", notesList!!.readNote(i))
            }

            for (i in 0 until notesList!!.listsFiles.size) {
                attemptExport(exportPath+notesList!!.listsFiles[i]+".txt", notesList!!.readList(i))
            }

        }

        // end of onCreateView
    }

    private fun reloadManageNotes() {
        binding.manageNotesTextGrids.sectionGrids[0].setCustomColumnCount(notesColumns)
        binding.manageNotesTextGrids.sectionGrids[1].setCustomColumnCount(listsColumns)
        binding.manageNotesTextGrids.sectionGrids[2].setCustomColumnCount(notesColumns)
        binding.manageNotesTextGrids.sectionGrids[3].setCustomColumnCount(listsColumns)

        var manageNotesContent = ArrayList<ArrayList<String>>(4)
        manageNotesContent.add(ArrayList<String>())
        manageNotesContent.add(ArrayList<String>())
        manageNotesContent.add(ArrayList<String>())
        manageNotesContent.add(ArrayList<String>())

        for (i in 0 until notesList!!.notesFiles.size) {
            manageNotesContent[0].add(notesList!!.notesFiles[i])
            manageNotesContent[2].add(notesList!!.notesFiles[i])
        }

        for (i in 0 until notesList!!.listsFiles.size) {
            manageNotesContent[1].add(notesList!!.listsFiles[i])
            manageNotesContent[3].add(notesList!!.listsFiles[i])
        }

        binding.manageNotesTextGrids.setupContent(manageNotesContent)

        for (i in 0 until manageNotesContent[0].size) {

            // rearrange
            binding.manageNotesTextGrids.sectionGrids[0].textGrid[i].setOnClickListener {
                rearrangeNoteDialog(i)
            }

            // export
            binding.manageNotesTextGrids.sectionGrids[2].textGrid[i].setOnClickListener {
                exportNoteDialog(i)
            }
        }

        for (i in 0 until manageNotesContent[1].size) {
            // rearrange
            binding.manageNotesTextGrids.sectionGrids[1].textGrid[i].setOnClickListener {
                rearrangeListDialog(i)
            }

            // export
            binding.manageNotesTextGrids.sectionGrids[3].textGrid[i].setOnClickListener {
                exportListDialog(i)
            }
        }
    }

    /** Allows a note to be moved from one index to another, a dialogue will be shown to take the
     * input, then the moveFrom function will be called in notesList
     *
     * @param i the index to move
     */
    private fun rearrangeNoteDialog(i : Int) {

        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Change the index of ${notesList!!.notesFiles[i]}: ")

        val input = EditText(this)
        input.hint = "Enter the new index (1-${notesList!!.notesFiles.size})"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)

            try {
                notesList!!.moveNoteFrom(i, input.text.toString().toInt()-1)
            } catch (e : Exception) {
                Toast.makeText(this, "Out of range.", Toast.LENGTH_SHORT).show()
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        input.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }

    /** Allows a list to be moved from one index to another, a dialogue will be shown to take the
     * input, then the moveFrom function will be called in notesList
     *
     * @param i the index to move
     */
    private fun rearrangeListDialog(i : Int) {

        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Change the index of ${notesList!!.listsFiles[i]}: ")

        val input = EditText(this)
        input.hint = "Enter the new index (1-${notesList!!.listsFiles.size})"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)

            try {
                notesList!!.moveListFrom(i, input.text.toString().toInt()-1)

            } catch (e : Exception) {
                Toast.makeText(this, "Out of range.", Toast.LENGTH_SHORT).show()
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            dialog.cancel()
        })

        builder.show()

        input.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }

    /** Will ask for confirmation of the export, then exports the pressed note to
     * exportPath/notesFilesName.txt
     *
     * @param i the index to export
     */
    private fun exportNoteDialog(i : Int) {

        MaterialAlertDialogBuilder(this).setTitle("Export?")
            .setMessage("This will export to your external downloads folder.")
            .setNegativeButton("Cancel") { dialog, which ->

            }.setPositiveButton("Export") { dialog, which ->

                if (!attemptExport(exportPath+notesList!!.notesFiles[i]+".txt", notesList!!.readNote(i)))
                    Toast.makeText(this, "Export failed.", Toast.LENGTH_SHORT).show()

            }.show()

    }

    /** Will ask for confirmation of the export, then exports the pressed list to
     * exportPath/notesFilesName.txt
     *
     * @param i the index to export
     */
    private fun exportListDialog(i : Int) {
        MaterialAlertDialogBuilder(this).setTitle("Export?")
            .setMessage("This will export to your external downloads folder.")
            .setNegativeButton("Cancel") { dialog, which ->

            }.setPositiveButton("Export") { dialog, which ->

                if (!attemptExport(exportPath+notesList!!.listsFiles[i]+".txt", notesList!!.readList(i)))
                    Toast.makeText(this, "Export failed.", Toast.LENGTH_SHORT).show()

            }.show()
    }

    /** Attempts to export the string given to the path specified
     *
     * @param fullPath the full path to the export
     * @param text the text to put in the file
     * @return whether it worked
     */
    private fun attemptExport(fullPath : String, text : String) : Boolean {
        if (!canExport(this, this))
            return false

        var exportFile : File? = getExportFile(fullPath)

        if (exportFile == null)
            return false

        exportFile.writeText(text)
        return true
    }

}