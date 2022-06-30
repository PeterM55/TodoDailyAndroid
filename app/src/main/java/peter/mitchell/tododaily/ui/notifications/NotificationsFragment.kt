package peter.mitchell.tododaily.ui.notifications

import android.R
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.new_notification.*
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.SaveInformation
import peter.mitchell.tododaily.databinding.FragmentNotificationsBinding
import peter.mitchell.tododaily.ui.home.ManageDailyNotifications
import java.time.*

@RequiresApi(Build.VERSION_CODES.O)
class NotificationsFragment : Fragment() {

    private lateinit var _binding: FragmentNotificationsBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)*/

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mainBinding?.fragmentLabel?.setText("Notifications")

        // ----- Setup Buttons -----
        _binding.oneTimeNotificationsButton.setOnClickListener {
            val intent = Intent(activity as Context, NewNotification::class.java)
            intent.putExtra("oneTimeNotification", true)
            startActivity(intent)
        }
        _binding.dailyNotificationsButton.setOnClickListener {
            val intent = Intent(activity as Context, NewNotification::class.java)
            startActivity(intent)
        }

        _binding.dailyNotificationsToggle.setOnClickListener {
            notificationsFullNameMode = !notificationsFullNameMode
            saveSettings()

            reloadDailyNotifications()
        }

        setupQuickTimers()

        notifFragment = this
        return root
    }

    private fun reloadNextNotification() {

        var nextNotification = dailyNotifications.getNextNotificationTime()

        if (nextNotification == null) {
            nextNotificationText.text = "No notifications found"
        } else {
            nextNotificationText.text = "Next Notification: ${nextNotification.toLocalDate().toString()} at ${nextNotification.toLocalTime().toString()}"
        }

    }

    private fun reloadOneTimeNotifications() {
        _binding.oneTimeNotificationsGrid.setCustomColumnCount(oneTimeNotifsColumns)
        _binding.oneTimeNotificationsGrid.setTextSize(oneTimeNotifsTextSize)
        _binding.systemNotificationsGrid.setTextSize(oneTimeNotifsTextSize)

        _binding.oneTimeNotificationsGrid.reset()
        _binding.systemNotificationsGrid.reset()
        for (i in 0 until dailyNotifications.oneTimeNotificationsLength) {
            if (dailyNotifications.isSystemNotification[i]) {
                _binding.systemNotificationsGrid.addString(requireContext(), dailyNotifications.getOneTimeString(i))

                _binding.systemNotificationsGrid.textGrid[i].setOnClickListener {
                    val intent = Intent(activity as Context, NewNotification::class.java)
                    intent.putExtra("oneTimeNotification", true)
                    intent.putExtra("index", i)
                    startActivity(intent)
                }
            } else {
                _binding.oneTimeNotificationsGrid.addString(requireContext(), dailyNotifications.getOneTimeString(i))

                _binding.oneTimeNotificationsGrid.textGrid[i].setOnClickListener {
                    val intent = Intent(activity as Context, NewNotification::class.java)
                    intent.putExtra("oneTimeNotification", true)
                    intent.putExtra("index", i)
                    startActivity(intent)
                }
            }
        }
    }

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
                val intent = Intent(activity as Context, NewNotification::class.java)
                intent.putExtra("oneTimeNotification", false)
                intent.putExtra("index", i)
                startActivity(intent)
            }
        }
    }

    private fun setupQuickTimers() {
        _binding.quickTimerButton5m.setOnClickListener {
            var notificationDateTime : LocalDateTime = LocalDateTime.now().plusMinutes(5)
            notificationDateTime = notificationDateTime.minusSeconds(notificationDateTime.second.toLong())
            notificationDateTime = notificationDateTime.minusNanos(notificationDateTime.nano.toLong())
            dailyNotifications.addOneTimeNotification(
                "5m Quick Timer",
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
                notificationDateTime,
                "60m Quick Timer",
                "Your 16m quick timer has expired"
            )
            dailyNotifications.setSystemNotification(dailyNotifications.oneTimeNotificationsLength-1)

            dailyNotifications.refreshNotifications(requireContext())

            reloadNextNotification()
            reloadOneTimeNotifications()
            saveNotifications()

        }
    }

    override fun onResume() {
        super.onResume()

        if (!notificationsShown) {
            updateBottomNavVisibilities()

            WorkManager.getInstance(requireContext()).cancelAllWork()

            val action = NotificationsFragmentDirections.actionNavigationNotificationsToNavigationHome()
            view?.findNavController()?.navigate(action)
        }

        Log.i("tdd-tempDebug=====","size1: ${dailyNotifications.oneTimeNotificationsLength}")
        readNotifications()

        dailyNotifications.refreshNotifications(requireContext())

        reloadNextNotification()
        reloadOneTimeNotifications()
        reloadDailyNotifications()

        updateBottomNavVisibilities()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}