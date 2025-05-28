package com.example.weatherapp.ui.notification

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.weatherapp.databinding.FragmentNotificationBinding
import java.util.*

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmManager: AlarmManager
    private val alarmRequestCode1 = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        createNotificationChannel()
        setupAddAlarmButton()

        return binding.root
    }

    private fun setupAddAlarmButton() {
        binding.fabAdd.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                showTimePicker(selectedDate)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = now.timeInMillis  // Don't allow past dates
        datePicker.show()
    }

    private fun showTimePicker(selectedDate: Calendar) {
        val now = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                selectedDate.set(Calendar.SECOND, 0)
                selectedDate.set(Calendar.MILLISECOND, 0)

                val currentTime = System.currentTimeMillis()
                val alarmTime = selectedDate.timeInMillis

                if (alarmTime <= currentTime) {
                    Toast.makeText(requireContext(), "Selected time is in the past!", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                scheduleAlarmAt(alarmTime, alarmRequestCode1)

                // Show success Toast
                Toast.makeText(requireContext(), "Alarm set for ${selectedDate.time}", Toast.LENGTH_SHORT).show()

                // Navigate again to this fragment (refresh)
//                parentFragmentManager.beginTransaction()
//                    .replace(this.id, NotificationFragment())
//                    .commit()

            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun scheduleAlarmAt(timeInMillis: Long, requestCode: Int) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AlarmReceiver.CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm notifications"
                setSound(
                    android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI,
                    Notification.AUDIO_ATTRIBUTES_DEFAULT
                )
            }
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
