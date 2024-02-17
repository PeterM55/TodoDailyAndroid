package peter.mitchell.tododaily.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.util.Log
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.databinding.FragmentNotificationsBinding
import java.time.*

/** The notifications fragment handles the viewing, creation, deletion, and management of
 * notifications. Though creation, deletion, and management are handled in the Edit Notification
 * activity, this object allows the user to open the activity using the index pressed.
 */
class NotificationsFragment : Fragment() {

    private lateinit var _binding: FragmentNotificationsBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private enum class ReadyToDelete { daily, oneTime, system }
    private var readyToDelete : ReadyToDelete? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mainBinding?.fragmentLabel?.setText("Notifications")

        if (darkMode)
            _binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            _binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        // ----- Setup Buttons -----
        _binding.oneTimeNotificationsButton.setOnClickListener {
            val intent = Intent(activity as Context, EditNotification::class.java)
            intent.putExtra("oneTimeNotification", true)
            startActivity(intent)
        }
        _binding.deleteAllOneTimesButton.setOnClickListener {
            readyToDelete = ReadyToDelete.oneTime
            showDeleteAllDialog()
        }

        _binding.dailyNotificationsButton.setOnClickListener {
            val intent = Intent(activity as Context, EditNotification::class.java)
            startActivity(intent)
        }
        _binding.deleteAllDailyButton.setOnClickListener {
            readyToDelete = ReadyToDelete.daily
            showDeleteAllDialog()
        }

        _binding.dailyNotificationsToggle.setOnClickListener {
            notificationsFullNameMode = !notificationsFullNameMode
            saveSettings()

            reloadDailyNotifications()
        }
        _binding.deleteAllSystemButton.setOnClickListener {
            readyToDelete = ReadyToDelete.system
            showDeleteAllDialog()
        }

        setupQuickTimers()

        notifFragment = this
        return root
    }

    /** Reload the next notification view field, using dailyNotifications.getNextNotificationTime */
    private fun reloadNextNotification() {

        val nextNotification = dailyNotifications.getNextNotificationTime()

        if (nextNotification == null) {
            _binding.nextNotificationText.text = "No notifications found"
        } else {
            _binding.nextNotificationText.text = "Next Notification: ${nextNotification.toLocalDate().toString()} at ${nextNotification.toLocalTime().toString()}"
        }

    }

    /** Reloads the one time and system notifications views */
    private fun reloadOneTimeNotifications() {
        _binding.oneTimeNotificationsGrid.setCustomColumnCount(oneTimeNotifsColumns)
        _binding.oneTimeNotificationsGrid.setTextSize(oneTimeNotifsTextSize)
        _binding.systemNotificationsGrid.setTextSize(oneTimeNotifsTextSize)

        _binding.oneTimeNotificationsGrid.reset()
        _binding.systemNotificationsGrid.reset()
        var systemI = 0
        var oneTimeI = 0
        for (i in 0 until dailyNotifications.oneTimeNotificationsLength) {
            if (dailyNotifications.isSystemNotification[i]) {
                _binding.systemNotificationsGrid.addString(requireContext(), dailyNotifications.getOneTimeString(i))

                _binding.systemNotificationsGrid.textGrid[systemI].setOnClickListener {
                    val intent = Intent(activity as Context, EditNotification::class.java)
                    intent.putExtra("oneTimeNotification", true)
                    intent.putExtra("index", i)
                    startActivity(intent)
                }
                systemI++
            } else {
                _binding.oneTimeNotificationsGrid.addString(requireContext(), dailyNotifications.getOneTimeString(i))

                _binding.oneTimeNotificationsGrid.textGrid[oneTimeI].setOnClickListener {
                    val intent = Intent(activity as Context, EditNotification::class.java)
                    intent.putExtra("oneTimeNotification", true)
                    intent.putExtra("index", i)
                    startActivity(intent)
                }
                oneTimeI++
            }
        }
    }

    /** Reloads the daily notifications view */
    private fun reloadDailyNotifications() {
        _binding.dailyNotificationsGrid.setCustomColumnCount(dailyNotifsColumns)
        _binding.dailyNotificationsGrid.setTextSize(dailyNotifsTextSize)

        _binding.dailyNotificationsGrid.reset()
        for (i in 0 until dailyNotifications.dailyNotificationsLength) {
            if (notificationsFullNameMode)
                _binding.dailyNotificationsGrid.addString(requireContext(), "${dailyNotifications.dailyNotificationTimes[i].toString()}: ${dailyNotifications.dailyNotificationNames[i]}")
            else
            _binding.dailyNotificationsGrid.addString(requireContext(), "${dailyNotifications.dailyNotificationTimes[i].toString()}")

            _binding.dailyNotificationsGrid.textGrid[i].setOnClickListener {
                val intent = Intent(activity as Context, EditNotification::class.java)
                intent.putExtra("oneTimeNotification", false)
                intent.putExtra("index", i)
                startActivity(intent)
            }
        }
    }

    /** Shows the delete all dialog for confirming the deletions, readyToDelete determines which
     * section is planning to be deleted
     */
    private fun showDeleteAllDialog() {

        MaterialAlertDialogBuilder(requireContext()).setTitle("Are you sure?")
            .setMessage("This will delete all of your notifications in the selected category!")
            .setNegativeButton("Cancel") { dialog, which ->
                readyToDelete = null
            }.setPositiveButton("Delete") { dialog, which ->

                if (readyToDelete == ReadyToDelete.daily) {
                    for (i in 0 until dailyNotifications.dailyNotificationsLength) {
                        dailyNotifications.removeDailyNotification(i)
                    }
                    saveNotifications()
                } else if (readyToDelete == ReadyToDelete.oneTime) {
                    var i = 0
                    while (i < dailyNotifications.oneTimeNotificationsLength) {
                        if (!dailyNotifications.isSystemNotification[i]) {
                            dailyNotifications.removeOneTimeNotification(i)
                        } else
                            i++
                    }
                } else if (readyToDelete == ReadyToDelete.system) {
                    var i = 0
                    while (i < dailyNotifications.oneTimeNotificationsLength) {
                        if (dailyNotifications.isSystemNotification[i]) {
                            dailyNotifications.removeOneTimeNotification(i)
                        } else
                            i++
                    }

                }

                readyToDelete = null
                reloadNextNotification()
                reloadDailyNotifications()
                reloadOneTimeNotifications()
                saveNotifications()

            }.show()
    }

    /** sets up the quick timers at the bottom of the fragment, most of the function is setting up
     * the on click listeners
     */
    private fun setupQuickTimers() {
        _binding.quickTimerButton5m.setOnClickListener {
            var notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(5)
            notificationDateTime = notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime = notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "5m Quick Timer",
                0,
                notificationDateTime,
                "5m Quick Timer",
                "Your 5m quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            saveNotifications()

        }

        _binding.quickTimerButton10m.setOnClickListener {
            var notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(10)
            notificationDateTime = notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime = notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "10m Quick Timer",
                0,
                notificationDateTime,
                "10m Quick Timer",
                "Your 10m quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            saveNotifications()

        }

        _binding.quickTimerButton20m.setOnClickListener {
            var notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(20)
            notificationDateTime = notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime = notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "20m Quick Timer",
                0,
                notificationDateTime,
                "20m Quick Timer",
                "Your 20m quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            saveNotifications()

        }

        _binding.quickTimerButton30m.setOnClickListener {
            var notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(30)
            notificationDateTime = notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime = notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "30m Quick Timer",
                0,
                notificationDateTime,
                "30m Quick Timer",
                "Your 30m quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            saveNotifications()

        }

        _binding.quickTimerButton60m.setOnClickListener {
            var notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(60)
            notificationDateTime = notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime = notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "60m Quick Timer",
                0,
                notificationDateTime,
                "60m Quick Timer",
                "Your 60m quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            saveNotifications()

        }
    }

    override fun onResume() {
        try {
            super.onResume()

            if (!notificationsShown) {
                updateBottomNavVisibilities()

                WorkManager.getInstance(requireContext()).cancelAllWork()

                val action =
                    NotificationsFragmentDirections.actionNavigationNotificationsToNavigationHome()
                view?.findNavController()?.navigate(action)
            }

            readNotifications()

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            reloadDailyNotifications()

            updateBottomNavVisibilities()
        } catch (e : Exception) {
            Log.i("tdd-notif on resume: ", "on resume error fount. non-error if done through quick timer.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}