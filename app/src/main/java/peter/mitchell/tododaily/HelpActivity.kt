package peter.mitchell.tododaily

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.help_screen.*

class HelpActivity : AppCompatActivity() {

    val helpString : String =
"""

""".trimIndent()



    val todoString : String = ""



    val notesString : String = ""



    val notifsString : String = ""





    var selectedHelpFragment = currentFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_screen)

        //oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)

        homeHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.home
        }
        todoHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.todo
        }
        notesHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.notes
        }
        notifsHelpButton.setOnClickListener {
            selectedHelpFragment = fragments.notifs
        }

        reloadText()
    }

    /** Reloads the text to be displayed */
    fun reloadText() {
        if (selectedHelpFragment == fragments.home) {
            mainInformation.setText(helpString)
        } else if (selectedHelpFragment == fragments.todo) {
            mainInformation.setText(todoString)
        } else if (selectedHelpFragment == fragments.notes) {
            mainInformation.setText(notesString)
        } else if (selectedHelpFragment == fragments.notifs) {
            mainInformation.setText(notifsString)
        }
    }

}