package peter.mitchell.tododaily.ui.notes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.edit_notes.*
import peter.mitchell.tododaily.R

class EditNotes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_notes)

        editNoteBackButton.setOnClickListener {
            saveNote()
            finish()
        }

    }

    fun saveNote() {

    }

}