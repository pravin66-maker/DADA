package com.family.bhajanalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getStringExtra(AlarmScheduler.EXTRA_SCHEDULE_ID) ?: return
        val day = intent.getIntExtra(AlarmScheduler.EXTRA_DAY, -1)

        val schedule = ScheduleStore.getAll(context).firstOrNull { it.id == scheduleId }
        if (schedule != null && schedule.enabled) {
            // Start playback in a foreground service so it keeps running reliably.
            val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_PLAY
                putExtra(PlaybackService.EXTRA_URI, schedule.songUri)
                putExtra(PlaybackService.EXTRA_NAME, schedule.songName)
            }
            context.startForegroundService(serviceIntent)

            // Also open the Now Playing screen so it's obvious what's happening.
            val activityIntent = Intent(context, NowPlayingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(PlaybackService.EXTRA_NAME, schedule.songName)
            }
            context.startActivity(activityIntent)

            // Re-arm this same day+time for next week so it repeats weekly.
            if (day != -1) {
                AlarmScheduler.scheduleDay(context, schedule, day)
            }
        }
    }
}
