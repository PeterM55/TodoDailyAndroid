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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_notifications.*
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

        // temporary code
        /*var setTime = LocalTime.now().plusSeconds(10)

        val testTime : LocalDateTime = setTime.atDate(LocalDate.now())
        val timeToTimer : Long = testTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000
        Toast.makeText(context, "Next alarm in: ${(timeToTimer-System.currentTimeMillis())/1000} seconds", Toast.LENGTH_SHORT).show()

        //Log.i("Notif Fragment 55: ","Time set to now + 15s, but set to: ${timeToTimer/1000} and current is: ${(System.currentTimeMillis())/1000}")
        //Log.i("Notif Fragment 55: ","Difference is: ${(timeToTimer-System.currentTimeMillis())/1000}")

        dailyNotifications.createNotification(requireContext(), setTime)*/

        // ----- Setup Main View -----
        if (dailyNotifications.totalLength() == 0) {
            readNotifications()
        }

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
        }

        return root
    }

    private fun reloadOneTimeNotifications() {
        _binding.oneTimeNotificationsGrid.reset()
        for (i in 0 until dailyNotifications.oneTimeNotificationsLength) {
            _binding.oneTimeNotificationsGrid.addString(requireContext(), dailyNotifications.getOneTimeString(i))

            _binding.oneTimeNotificationsGrid.textGrid[i].setOnClickListener {
                Toast.makeText(requireContext(), "hi: $i", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reloadDailyNotifications() {
        _binding.dailyNotificationsGrid.reset()
        for (i in 0 until dailyNotifications.dailyNotificationsLength) {
            if (notificationsFullNameMode)
                _binding.dailyNotificationsGrid.addString(requireContext(), "${dailyNotifications.dailyNotificationTimes[i].toString()}: ${dailyNotifications.dailyNotificationNames[i]}")
            else
            _binding.dailyNotificationsGrid.addString(requireContext(), "${dailyNotifications.dailyNotificationTimes[i].toString()}")
        }
    }

    override fun onResume() {
        super.onResume()

        dailyNotifications.refreshNotifications(requireContext())

        reloadOneTimeNotifications()
        reloadDailyNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}