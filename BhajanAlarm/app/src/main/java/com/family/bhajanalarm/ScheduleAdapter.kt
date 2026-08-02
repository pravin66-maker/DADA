package com.family.bhajanalarm

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.family.bhajanalarm.databinding.ItemScheduleBinding
import java.util.Calendar
import java.util.Locale

class ScheduleAdapter(
    private val context: Context,
    private var items: MutableList<Schedule>,
    private val onEdit: (Schedule) -> Unit,
    private val onDelete: (Schedule) -> Unit,
    private val onToggle: (Schedule, Boolean) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    fun updateData(newItems: List<Schedule>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = items[position]
        holder.binding.songNameText.text = schedule.songName

        val orderedDays = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        val dayLabels = orderedDays.filter { schedule.days.contains(it) }
            .joinToString(", ") { Schedule.dayLabel(context, it) }

        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", schedule.hour, schedule.minute)
        holder.binding.daysTimeText.text = "$dayLabels  •  $timeStr"

        holder.binding.enabledSwitch.setOnCheckedChangeListener(null)
        holder.binding.enabledSwitch.isChecked = schedule.enabled
        holder.binding.enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            onToggle(schedule, isChecked)
        }

        holder.binding.editButton.setOnClickListener { onEdit(schedule) }
        holder.binding.deleteButton.setOnClickListener { onDelete(schedule) }
    }

    override fun getItemCount(): Int = items.size
}
