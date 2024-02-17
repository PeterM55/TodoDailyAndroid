package peter.mitchell.tododaily.ui.notifications

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.databinding.EditNotesBinding
import peter.mitchell.tododaily.databinding.HelpScreenBinding
import peter.mitchell.tododaily.databinding.NewNotificationBinding
import java.lang.StringBuilder
import java.time.*
import java.time.temporal.TemporalField

/** Edit notification is an activity that allows the user to create or edit notifications. They show
 * up the same way but when editing the index and whether it is a one time must be provided in the
 * intent, and the fields will be filled automatically.
 */
class EditNotification : AppCompatActivity() {

    var oneTimeNotification : Boolean = false
    var editNotificationIndex : Int = -1
    var systemOffsetIndex : Int = -1
    var editingSystemNotification : Boolean = false

    private lateinit var binding: NewNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewNotificationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (darkMode)
            binding.mainBackgroundNewNotifs.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            binding.mainBackgroundNewNotifs.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        oneTimeNotification = intent.getBooleanExtra("oneTimeNotification", false)
        binding.notificationRepeatWeeklyInput.isChecked = intent.getBooleanExtra("weeklyNotification", false)
        editNotificationIndex = intent.getIntExtra("index", -1)

        binding.datePicker.minDate = LocalDateTime.now().toEpochSecond(ZoneId.systemDefault().rules.getOffset(
            Instant.now()))

        if (!oneTimeNotification)
            binding.datePicker.isVisible = false

        binding.notificationRepeatInput.isChecked = !oneTimeNotification
        binding.notificationRepeatInput.setOnClickListener {
            binding.datePicker.isVisible = !binding.notificationRepeatInput.isChecked
            binding.notificationRepeatWeeklyInput.isChecked = false
        }

        if (editNotificationIndex != -1 && oneTimeNotification)
            binding.notificationRepeatWeeklyInput.isChecked = dailyNotifications.datedNotificationDaysBetween[editNotificationIndex] == 7
        binding.notificationRepeatWeeklyInput.setOnClickListener {
            binding.datePicker.isVisible = binding.notificationRepeatWeeklyInput.isChecked || !binding.notificationRepeatInput.isChecked
            binding.notificationRepeatInput.isChecked = false
        }

        if (editNotificationIndex != -1) {
            if (oneTimeNotification) {
                binding.notificationNameInput.setText(dailyNotifications.oneTimeNotificationNames[editNotificationIndex])
                binding.notificationTitleInput.setText(dailyNotifications.oneTimeNotificationTitles[editNotificationIndex])
                binding.notificationDescInput.setText(dailyNotifications.oneTimeNotificationDescriptions[editNotificationIndex])
                binding.datePicker.updateDate(
                    dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].year,
                    dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].monthValue-1,
                    dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].dayOfMonth
                )
                binding.timePicker.hour = dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].hour
                binding.timePicker.minute = dailyNotifications.oneTimeNotificationTimes[editNotificationIndex].minute
                editingSystemNotification = dailyNotifications.isSystemNotification[editNotificationIndex]
            } else {
                binding.notificationNameInput.setText(dailyNotifications.dailyNotificationNames[editNotificationIndex])
                binding.notificationTitleInput.setText(dailyNotifications.dailyNotificationTitles[editNotificationIndex])
                binding.notificationDescInput.setText(dailyNotifications.dailyNotificationDescriptions[editNotificationIndex])
                binding.timePicker.hour = dailyNotifications.dailyNotificationTimes[editNotificationIndex].hour
                binding.timePicker.minute = dailyNotifications.dailyNotificationTimes[editNotificationIndex].minute
            }
        }

        binding.newNotificationSubmitButton.setOnClickListener {
            submitButton()
        }

        binding.newNotificationDeleteButton.setOnClickListener { deleteButton() }

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

        binding.notificationIndexInput.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            indexArray.toArray()
        )

        if (systemOffsetIndex != -1)
            binding.notificationIndexInput.setSelection(systemOffsetIndex)
        else if (editNotificationIndex != -1)
            binding.notificationIndexInput.setSelection(editNotificationIndex)
        else
            binding.notificationIndexInput.setSelection(indexArray.size-1)

        binding.copyNotifNameButton.setOnClickListener {

            if (binding.notificationTitleInput.text.toString() != "") {
                MaterialAlertDialogBuilder(this).setTitle("Overwrite Name?")
                    .setMessage("The notification name field is not blank, this will overwrite it. Are you sure?")
                    .setNegativeButton("Cancel") { dialog, which ->
                    }.setPositiveButton("Overwrite") { dialog, which ->
                        binding.notificationTitleInput.text = binding.notificationNameInput.text
                    }.show()
            } else {
                binding.notificationTitleInput.text = binding.notificationNameInput.text
            }

        }
    }

    /** takes the information from the inputs and saves them, moving the element if needed */
    private fun submitButton() {

        // if NOT editing (== -1) or one time notification status has changed, then make a new one
        val new = editNotificationIndex == -1 || oneTimeNotification == binding.notificationRepeatInput.isChecked
        val datedNotification = binding.notificationRepeatWeeklyInput.isChecked || !binding.notificationRepeatInput.isChecked

        val notifName = binding.notificationNameInput.text.toString()
        var repeatTime = 0
        if (binding.notificationRepeatWeeklyInput.isChecked) repeatTime = 7;
        val notificationTime : LocalTime = LocalTime.of(binding.timePicker.hour, binding.timePicker.minute)

        val notifTitle = binding.notificationTitleInput.text.toString()
        val notifDesc = binding.notificationDescInput.text.toString()

        var succeeded = false

        if (datedNotification) {

            val notificationDateTime : LocalDateTime = LocalDateTime.of(LocalDate.of(binding.datePicker.year, binding.datePicker.month+1, binding.datePicker.dayOfMonth),notificationTime)
            if (notificationDateTime.isBefore(LocalDateTime.now())) {
                Toast.makeText(this, "Date/time is in the past", Toast.LENGTH_SHORT).show()
                Log.i("tdd.newNotificationSubmitButton", "${notificationDateTime} is before: ${LocalDateTime.now()}")
                return
            }

            if (new) {
                succeeded = dailyNotifications.addOneTimeNotification(
                    notifName, repeatTime, notificationDateTime, notifTitle, notifDesc,
                    editNotificationIndex // is either -1, or needs to be used.
                )
            } else {
                succeeded = dailyNotifications.setOneTimeNotification(
                    editNotificationIndex,
                    notifName, repeatTime, notificationDateTime, notifTitle, notifDesc
                )
            }
        } else {
            if (new) {
                succeeded = dailyNotifications.addDailyNotification(
                    notifName, notificationTime, notifTitle, notifDesc,
                    editNotificationIndex
                )
            } else {
                succeeded = dailyNotifications.setDailyNotification(
                    editNotificationIndex,
                    notifName, notificationTime, notifTitle, notifDesc
                )
            }
        }

        if (!succeeded) {
            Toast.makeText(this, "Date/time already exists", Toast.LENGTH_SHORT).show()
            return
        }

        // --- If notification index is set, then set the position ---
        if (!binding.notificationRepeatInput.isChecked) {
            if (editNotificationIndex == -1)
                editNotificationIndex = dailyNotifications.oneTimeNotificationsLength-1

            var newIndex = binding.notificationIndexInput.selectedItemPosition
            for (i in 0 until binding.notificationIndexInput.selectedItemPosition+1) {
                if (i >= dailyNotifications.isSystemNotification.size) break

                if (editingSystemNotification != dailyNotifications.isSystemNotification[i]) {
                    newIndex++
                }
            }

            dailyNotifications.oneTimeMoveFrom(editNotificationIndex, newIndex)
        } else {
            if (editNotificationIndex == -1)
                editNotificationIndex = dailyNotifications.dailyNotificationsLength-1
            dailyNotifications.dailyMoveFrom(editNotificationIndex, binding.notificationIndexInput.selectedItemPosition)
        }


        saveNotifications()
        finish()

    }

    /** Deletes the notification being edited, if not created yet it exits the activity */
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