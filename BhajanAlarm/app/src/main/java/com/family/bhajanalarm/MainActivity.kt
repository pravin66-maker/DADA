package com.family.bhajanalarm

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.family.bhajanalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ScheduleAdapter(
            this,
            mutableListOf(),
            onEdit = { schedule ->
                val intent = Intent(this, AddEditScheduleActivity::class.java)
                intent.putExtra(AddEditScheduleActivity.EXTRA_SCHEDULE_ID, schedule.id)
                startActivity(intent)
            },
            onDelete = { schedule ->
                ScheduleStore.delete(this, schedule.id)
                AlarmScheduler.cancelAll(this, schedule.id)
                refreshList()
            },
            onToggle = { schedule, enabled ->
                val updated = schedule.copy(enabled = enabled)
                ScheduleStore.update(this, updated)
                AlarmScheduler.reschedule(this, updated)
            }
        )
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.scheduleRecyclerView.adapter = adapter

        binding.btnAddSchedule.setOnClickListener {
            startActivity(Intent(this, AddEditScheduleActivity::class.java))
        }

        binding.btnLangHi.setOnClickListener { setAppLanguage("hi") }
        binding.btnLangGu.setOnClickListener { setAppLanguage("gu") }

        // Reflect current language in the toggle group
        val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentLang.startsWith("gu")) {
            binding.langToggleGroup.check(binding.btnLangGu.id)
        } else {
            binding.langToggleGroup.check(binding.btnLangHi.id)
        }

        requestNotificationPermissionIfNeeded()
        checkExactAlarmPermission()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val schedules = ScheduleStore.getAll(this)
        adapter.updateData(schedules)
        binding.emptyText.visibility = if (schedules.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.scheduleRecyclerView.visibility = if (schedules.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun setAppLanguage(langCode: String) {
        val locales = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100
                )
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.exact_alarm_permission_title)
                    .setMessage(R.string.exact_alarm_permission_message)
                    .setPositiveButton(R.string.open_settings) { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }
}
