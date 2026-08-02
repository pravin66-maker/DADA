package com.family.bhajanalarm

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * One schedule = a song + which weekdays it should play on + a start time (hour:minute).
 * days uses java.util.Calendar day constants: Calendar.MONDAY .. Calendar.SUNDAY
 */
data class Schedule(
    val id: String,
    val songUri: String,
    val songName: String,
    val days: Set<Int>,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("songUri", songUri)
        obj.put("songName", songName)
        obj.put("days", JSONArray(days.toList()))
        obj.put("hour", hour)
        obj.put("minute", minute)
        obj.put("enabled", enabled)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Schedule {
            val daysArray = obj.getJSONArray("days")
            val daysSet = mutableSetOf<Int>()
            for (i in 0 until daysArray.length()) {
                daysSet.add(daysArray.getInt(i))
            }
            return Schedule(
                id = obj.getString("id"),
                songUri = obj.getString("songUri"),
                songName = obj.getString("songName"),
                days = daysSet,
                hour = obj.getInt("hour"),
                minute = obj.getInt("minute"),
                enabled = obj.optBoolean("enabled", true)
            )
        }

        fun dayLabel(context: android.content.Context, day: Int): String {
            val res = context.resources
            return when (day) {
                Calendar.MONDAY -> res.getString(R.string.day_mon)
                Calendar.TUESDAY -> res.getString(R.string.day_tue)
                Calendar.WEDNESDAY -> res.getString(R.string.day_wed)
                Calendar.THURSDAY -> res.getString(R.string.day_thu)
                Calendar.FRIDAY -> res.getString(R.string.day_fri)
                Calendar.SATURDAY -> res.getString(R.string.day_sat)
                else -> res.getString(R.string.day_sun)
            }
        }
    }
}
