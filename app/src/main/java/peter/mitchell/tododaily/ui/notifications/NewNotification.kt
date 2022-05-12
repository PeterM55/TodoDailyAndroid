package peter.mitchell.tododaily.ui.notifications

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.new_notification.*
import peter.mitchell.tododaily.R

class NewNotification : AppCompatActivity() {

    var oneTimeNotification : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_notification)

        oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)

        if (!oneTimeNotification)
            datePicker.isVisible = false

        notificationRepeatInput.isChecked = !oneTimeNotification
        notificationRepeatInput.setOnClickListener {
            notificationRepeatInput.isChecked = !oneTimeNotification
            datePicker.isVisible = oneTimeNotification
        }

        newNotificationSubmitButton.setOnClickListener {

        }

    }


}