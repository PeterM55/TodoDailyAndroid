package peter.mitchell.tododaily.ui.notifications

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.new_notification.*
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.dailyNotifications
import peter.mitchell.tododaily.saveNotifications
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

            var notificationTime : LocalTime = LocalTime.of(timePicker.hour, timePicker.minute)

            if (oneTimeNotification) {

                //val notificationDate : LocalDate = LocalDate.of(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                val notificationDateTime : LocalDateTime = LocalDateTime.of(LocalDate.of(datePicker.year, datePicker.month+1, datePicker.dayOfMonth),notificationTime)

                if (notificationDateTime.isBefore(LocalDateTime.now())) {
                    Toast.makeText(this, "Date/time is in the past", Toast.LENGTH_SHORT).show()
                    Log.i("newNotificationSubmitButton", "${notificationDateTime} is before: ${LocalDateTime.now()}")
                    return@setOnClickListener
                }

                if (!dailyNotifications.addOneTimeNotification(
                    notificationNameInput.text.toString(),
                    notificationDateTime,
                    notificationTitleInput.text.toString(),
                    notificationDescInput.text.toString()
                )) {
                    Toast.makeText(this, "Date/time already exists", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                if (!dailyNotifications.addDailyNotification(
                    notificationNameInput.text.toString(),
                    notificationTime,
                    notificationTitleInput.text.toString(),
                    notificationDescInput.text.toString()
                )) {
                    Toast.makeText(this, "Time already exists", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            saveNotifications()

            finish()

        }

    }


}