package peter.mitchell.tododaily.ui.notifications

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.new_notification.*
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.dailyNotifications
import peter.mitchell.tododaily.saveInformation
import peter.mitchell.tododaily.saveNotifications
import java.lang.StringBuilder
import java.time.*
import java.time.temporal.TemporalField

class NewNotification : AppCompatActivity() {

    var oneTimeNotification : Boolean = false
    var editNotificationIndex : Int = -1
    var systemOffsetIndex : Int = -1
    var editingSystemNotification : Boolean = false

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
            //notificationRepeatInput.isChecked = !oneTimeNotification
            datePicker.isVisible = notificationRepeatInput.isChecked

            // update: notificationIndexInput
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
                editingSystemNotification = dailyNotifications.isSystemNotification[editNotificationIndex]
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

        newNotificationDeleteButton.setOnClickListener { deleteButton() }

        var indexArray : ArrayList<String> = ArrayList()
        if (oneTimeNotification) {
            for (i in 0 until dailyNotifications.oneTimeNotificationsLength) {
                if (!dailyNotifications.isSystemNotification[i] || (dailyNotifications.isSystemNotification[i] && editingSystemNotification) )
                    indexArray.add("$i: ${dailyNotifications.oneTimeNotificationNames[i]}")

                if (i == editNotificationIndex)
                    systemOffsetIndex = indexArray.size-1
            }
            if (editNotificationIndex == -1)
                indexArray.add("End: new")
        } else {
            for (i in 0 until dailyNotifications.dailyNotificationsLength) {
                indexArray.add("$i: ${dailyNotifications.dailyNotificationNames[i]}")
            }
            if (editNotificationIndex == -1)
                indexArray.add("End: new")
        }

        notificationIndexInput.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            indexArray.toArray()
        )

        if (systemOffsetIndex != -1)
            notificationIndexInput.setSelection(systemOffsetIndex)
        else if (editNotificationIndex != -1)
            notificationIndexInput.setSelection(editNotificationIndex)
        else
            notificationIndexInput.setSelection(indexArray.size-1)
    }

    private fun submitButton() {
        var notificationTime : LocalTime = LocalTime.of(timePicker.hour, timePicker.minute)

        if (!notificationRepeatInput.isChecked) {

            //val notificationDate : LocalDate = LocalDate.of(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val notificationDateTime : LocalDateTime = LocalDateTime.of(LocalDate.of(datePicker.year, datePicker.month+1, datePicker.dayOfMonth),notificationTime)

            if (notificationDateTime.isBefore(LocalDateTime.now())) {
                Toast.makeText(this, "Date/time is in the past", Toast.LENGTH_SHORT).show()
                Log.i("newNotificationSubmitButton", "${notificationDateTime} is before: ${LocalDateTime.now()}")
                return
            }

            if (editNotificationIndex == -1 || oneTimeNotification == notificationRepeatInput.isChecked) {
                if (!dailyNotifications.addOneTimeNotification(
                        notificationNameInput.text.toString(),
                        notificationDateTime,
                        notificationTitleInput.text.toString(),
                        notificationDescInput.text.toString(),
                        editNotificationIndex // is either -1, or needs to be used.
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
            if (editNotificationIndex == -1 || oneTimeNotification == notificationRepeatInput.isChecked) {
                if (!dailyNotifications.addDailyNotification(
                        notificationNameInput.text.toString(),
                        notificationTime,
                        notificationTitleInput.text.toString(),
                        notificationDescInput.text.toString(),
                        editNotificationIndex
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

        // --- delete notification if swapped locations --- MOVED INTO DAILYNOTIFICATIONS
        /*if (editNotificationIndex != -1 || oneTimeNotification == notificationRepeatInput.isChecked) {
            if (oneTimeNotification) {
                dailyNotifications.removeOneTimeNotification(editNotificationIndex)
            } else {
                dailyNotifications.removeDailyNotification(editNotificationIndex)
            }
        }*/

        // --- If notification index is set, then set the position ---


        if (!notificationRepeatInput.isChecked) {
            if (editNotificationIndex == -1)
                editNotificationIndex = dailyNotifications.oneTimeNotificationsLength-1

            var newIndex = notificationIndexInput.selectedItemPosition
            for (i in 0 until notificationIndexInput.selectedItemPosition+1) {
                if (i >= dailyNotifications.isSystemNotification.size) break

                if (editingSystemNotification != dailyNotifications.isSystemNotification[i]) {
                    newIndex++
                }
            }

            dailyNotifications.oneTimeMoveFrom(editNotificationIndex, newIndex)
        } else {
            if (editNotificationIndex == -1)
                editNotificationIndex = dailyNotifications.dailyNotificationsLength-1
            dailyNotifications.dailyMoveFrom(editNotificationIndex, notificationIndexInput.selectedItemPosition)
        }


        saveNotifications()
        finish()

    }

    private fun deleteButton() {
        if (oneTimeNotification) {
            dailyNotifications.removeOneTimeNotification(editNotificationIndex)
        } else {
            dailyNotifications.removeDailyNotification(editNotificationIndex)
        }
        saveNotifications()
        finish()
    }
}