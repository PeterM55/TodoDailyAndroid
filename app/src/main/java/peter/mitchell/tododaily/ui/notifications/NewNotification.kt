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
import java.time.*
import java.time.temporal.TemporalField

class NewNotification : AppCompatActivity() {

    var oneTimeNotification : Boolean = false
    var editNotificationIndex : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_notification)

        oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)
        editNotificationIndex = intent.getIntExtra("index", -1)

        datePicker.minDate = LocalDateTime.now().toEpochSecond(ZoneId.systemDefault().rules.getOffset(
            Instant.now()))

        if (!oneTimeNotification)
            datePicker.isVisible = false

        notificationRepeatInput.isChecked = !oneTimeNotification
        notificationRepeatInput.setOnClickListener {
            notificationRepeatInput.isChecked = !oneTimeNotification
            datePicker.isVisible = oneTimeNotification
        }

        if (editNotificationIndex != -1) {
            if (oneTimeNotification) {
                notificationNameInput.setText(dailyNotifications.oneTimeNotificationNames[editNotificationIndex])
                notificationTitleInput.setText(dailyNotifications.oneTimeNotificationTitles[editNotificationIndex])
                notificationDescInput.setText(dailyNotifications.oneTimeNotificationDescriptions[editNotificationIndex])
                datePicker.updateDate(
                    dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].year,
                    dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].monthValue,
                    dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].dayOfMonth
                )
                timePicker.hour = dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].hour
                timePicker.minute = dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].minute
            } else {
                notificationNameInput.setText(dailyNotifications.dailyNotificationNames[editNotificationIndex])
                notificationTitleInput.setText(dailyNotifications.dailyNotificationTitles[editNotificationIndex])
                notificationDescInput.setText(dailyNotifications.dailyNotificationDescriptions[editNotificationIndex])
                timePicker.hour = dailyNotifications.dailyNotificationTimes[editNotificationIndex].hour
                timePicker.minute = dailyNotifications.dailyNotificationTimes[editNotificationIndex].minute
            }
        }

        newNotificationSubmitButton.setOnClickListener {
            submitButton()
        }

    }

    private fun submitButton() {

        var notificationTime : LocalTime = LocalTime.of(timePicker.hour, timePicker.minute)

        if (oneTimeNotification) {

            //val notificationDate : LocalDate = LocalDate.of(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val notificationDateTime : LocalDateTime = LocalDateTime.of(LocalDate.of(datePicker.year, datePicker.month+1, datePicker.dayOfMonth),notificationTime)

            if (notificationDateTime.isBefore(LocalDateTime.now())) {
                Toast.makeText(this, "Date/time is in the past", Toast.LENGTH_SHORT).show()
                Log.i("newNotificationSubmitButton", "${notificationDateTime} is before: ${LocalDateTime.now()}")
                return
            }

            if (editNotificationIndex == -1) {
                if (!dailyNotifications.addOneTimeNotification(
                        notificationNameInput.text.toString(),
                        notificationDateTime,
                        notificationTitleInput.text.toString(),
                        notificationDescInput.text.toString()
                    )) {
                    Toast.makeText(this, "Date/time already exists", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                if (!dailyNotifications.setOneTimeNotification(
                        editNotificationIndex,
                        notificationNameInput.text.toString(),
                        notificationDateTime,
                        notificationTitleInput.text.toString(),
                        notificationDescInput.text.toString()
                    )) {
                    Toast.makeText(this, "Date/time already exists", Toast.LENGTH_SHORT).show()
                    return
                }
            }

        } else {
            if (editNotificationIndex == -1) {
                if (!dailyNotifications.addDailyNotification(
                        notificationNameInput.text.toString(),
                        notificationTime,
                        notificationTitleInput.text.toString(),
                        notificationDescInput.text.toString()
                    )) {
                    Toast.makeText(this, "Time already exists", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                if (!dailyNotifications.setDailyNotification(
                        editNotificationIndex,
                        notificationNameInput.text.toString(),
                        notificationTime,
                        notificationTitleInput.text.toString(),
                        notificationDescInput.text.toString()
                    )) {
                    Toast.makeText(this, "Date/time already exists", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
        saveNotifications()

        finish()

    }

}