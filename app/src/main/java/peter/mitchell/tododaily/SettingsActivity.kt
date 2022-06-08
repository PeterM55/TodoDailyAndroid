package peter.mitchell.tododaily

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.new_notification.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)

        //oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)



    }

}