package com.family.bhajanalarm

import android.content.Context
import org.json.JSONArray
import java.util.UUID

/**
 * Very simple JSON-in-SharedPreferences storage. No database needed for a
 * handful of schedules.
 */
object ScheduleStore {

    private const val PREFS = "bhajan_alarm_prefs"
    private const val KEY_SCHEDULES = "schedules"

    fun getAll(context: Context): List<Schedule> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCHEDULES, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<Schedule>()
        for (i in 0 until arr.length()) {
            list.add(Schedule.fromJson(arr.getJSONObject(i)))
        }
        return list
    }

    private fun saveAll(context: Context, schedules: List<Schedule>) {
        val arr = JSONArray()
        schedules.forEach { arr.put(it.toJson()) }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCHEDULES, arr.toString()).apply()
    }

    fun add(context: Context, schedule: Schedule) {
        val list = getAll(context).toMutableList()
        list.add(schedule)
        saveAll(context, list)
    }

    fun update(context: Context, schedule: Schedule) {
        val list = getAll(context).toMutableList()
        val idx = list.indexOfFirst { it.id == schedule.id }
        if (idx >= 0) {
            list[idx] = schedule
        } else {
            list.add(schedule)
        }
        saveAll(context, list)
    }

    fun delete(context: Context, scheduleId: String) {
        val list = getAll(context).filter { it.id != scheduleId }
        saveAll(context, list)
    }

    fun newId(): String = UUID.randomUUID().toString()
}
