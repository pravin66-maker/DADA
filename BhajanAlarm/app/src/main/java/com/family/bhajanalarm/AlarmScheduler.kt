package com.family.bhajanalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    const val EXTRA_DAY = "extra_day"

    private fun requestCode(scheduleId: String, day: Int): Int {
        return (scheduleId + "_" + day).hashCode()
    }

    private fun pendingIntent(context: Context, scheduleId: String, day: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(EXTRA_DAY, day)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(scheduleId, day),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Cancels any existing alarms for this schedule, then re-creates one alarm per selected day (if enabled). */
    fun reschedule(context: Context, schedule: Schedule) {
        // Cancel all 7 possible day-alarms first (cheap and avoids stale entries)
        for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
            cancelDay(context, schedule.id, day)
        }
        if (!schedule.enabled) return
        schedule.days.forEach { day ->
            scheduleDay(context, schedule, day)
        }
    }

    fun cancelAll(context: Context, scheduleId: String) {
        for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
            cancelDay(context, scheduleId, day)
        }
    }

    private fun cancelDay(context: Context, scheduleId: String, day: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, scheduleId, day))
    }

    fun scheduleDay(context: Context, schedule: Schedule, day: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerMillis(day, schedule.hour, schedule.minute)
        val pi = pendingIntent(context, schedule.id, day)
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (e: SecurityException) {
            // Exact alarm permission not granted; fall back to inexact so it still fires eventually.
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    /** Computes the next occurrence (in millis) of the given weekday + hour:minute, at least 30s in the future. */
    private fun nextTriggerMillis(day: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        target.set(Calendar.DAY_OF_WEEK, day)
        target.set(Calendar.HOUR_OF_DAY, hour)
        target.set(Calendar.MINUTE, minute)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        if (target.timeInMillis <= now.timeInMillis + 30_000) {
            target.add(Calendar.DAY_OF_YEAR, 7)
        }
        return target.timeInMillis
    }

    /** Re-creates alarms for every enabled schedule. Called at boot and after any edit. */
    fun rescheduleAll(context: Context) {
        ScheduleStore.getAll(context).forEach { schedule ->
            reschedule(context, schedule)
        }
    }
}
