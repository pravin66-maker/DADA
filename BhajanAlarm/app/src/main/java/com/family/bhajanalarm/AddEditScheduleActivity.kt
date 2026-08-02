package com.family.bhajanalarm

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.family.bhajanalarm.databinding.ActivityAddEditScheduleBinding
import java.util.Calendar
import java.util.Locale

class AddEditScheduleActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    }

    private lateinit var binding: ActivityAddEditScheduleBinding
    private var editingSchedule: Schedule? = null

    private var selectedSongUri: String? = null
    private var selectedSongName: String? = null
    private var selectedHour: Int = 6
    private var selectedMinute: Int = 0

    private val dayOrder = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
        Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    )
    private val dayChips = mutableMapOf<Int, Chip>()

    private val pickSongLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == RESULT_OK && uri != null) {
            // Keep permanent access to this file even after phone reboot.
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            selectedSongUri = uri.toString()
            selectedSongName = queryFileName(uri) ?: getString(R.string.select_song)
            binding.selectedSongText.text = selectedSongName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buildDayChips()
        updateTimeButtonText()

        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID)
        if (scheduleId != null) {
            editingSchedule = ScheduleStore.getAll(this).firstOrNull { it.id == scheduleId }
            editingSchedule?.let { prefill(it) }
        }

        binding.btnChooseSong.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/mpeg"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/mpeg", "audio/mp3", "audio/*"))
            }
            pickSongLauncher.launch(intent)
        }

        binding.btnPickTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    updateTimeButtonText()
                },
                selectedHour, selectedMinute, true
            ).show()
        }

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveSchedule() }
    }

    private fun buildDayChips() {
        binding.daysChipGroup.removeAllViews()
        dayChips.clear()
        dayOrder.forEach { day ->
            val chip = Chip(this)
            chip.text = Schedule.dayLabel(this, day)
            chip.isCheckable = true
            chip.isCheckedIconVisible = false
            binding.daysChipGroup.addView(chip)
            dayChips[day] = chip
        }
    }

    private fun prefill(schedule: Schedule) {
        selectedSongUri = schedule.songUri
        selectedSongName = schedule.songName
        binding.selectedSongText.text = schedule.songName
        selectedHour = schedule.hour
        selectedMinute = schedule.minute
        updateTimeButtonText()
        schedule.days.forEach { day -> dayChips[day]?.isChecked = true }
        binding.btnSave.setText(R.string.save)
    }

    private fun updateTimeButtonText() {
        binding.btnPickTime.text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
    }

    private fun saveSchedule() {
        val uri = selectedSongUri
        if (uri == null) {
            Toast.makeText(this, R.string.please_select_song_error, Toast.LENGTH_SHORT).show()
            return
        }
        val chosenDays = dayChips.filterValues { it.isChecked }.keys
        if (chosenDays.isEmpty()) {
            Toast.makeText(this, R.string.please_select_day_error, Toast.LENGTH_SHORT).show()
            return
        }

        val schedule = Schedule(
            id = editingSchedule?.id ?: ScheduleStore.newId(),
            songUri = uri,
            songName = selectedSongName ?: "",
            days = chosenDays,
            hour = selectedHour,
            minute = selectedMinute,
            enabled = true
        )

        if (editingSchedule != null) {
            ScheduleStore.update(this, schedule)
        } else {
            ScheduleStore.add(this, schedule)
        }
        AlarmScheduler.reschedule(this, schedule)
        finish()
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }
}
