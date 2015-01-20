/*
 * Copyright (C) 2014 Alex Korovyansky.
 */
package com.alexkorovyansky.wearpomodoro.helpers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.PowerManager;

import com.alexkorovyansky.wearpomodoro.R;
import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroAlarmReceiver;
import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroAlarmTickReceiver;
import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroControlReceiver;
import com.alexkorovyansky.wearpomodoro.app.services.PomodoroNotificationService;
import com.alexkorovyansky.wearpomodoro.model.ActivityType;

import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class PomodoroMaster {

    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;
    private PowerManager powerManager;
    private final PersistentStorage persistentStorage;

    public PomodoroMaster(Context context, PersistentStorage persistentStorage) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.persistentStorage = persistentStorage;
    }

    public void check() {
        long now = System.currentTimeMillis();
        long last = persistentStorage.readLastEatenPomodoroTimestampMs();
        if (!isTheSamePomodoroDay(last, now)) {
            persistentStorage.writeEatenPomodoros(0);
        }
    }

    public void start(ActivityType activityType) {
        long now = System.currentTimeMillis();
        long when = now + activityType.getLengthMs();

        Timber.d("Starting new activityType %s in %s ms", activityType.toString(), when);
        persistentStorage.writeWhenMs(when);
        persistentStorage.writeActivityType(activityType);
        scheduleAlarms(when);
        syncNotification(activityType, when, isScreenOn());
        startDisplayService();
    }

    public int getEatenPomodoros() {
        return persistentStorage.readEatenPomodoros();
    }

    public boolean isActive() {
        return persistentStorage.readActivityType() != ActivityType.NONE;
    }

    public void syncNotification() {
        syncNotification(isScreenOn());
    }

    public void syncNotification(boolean isOn) {

        syncNotification(persistentStorage.readActivityType(), persistentStorage.readWhenMs(), isOn);
    }

    public void cancelNotification() {
        Timber.d("Cancelling notification");
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public ActivityType stop() {
        ActivityType stoppingForType = persistentStorage.readActivityType();
        Timber.d("Stopping activityType %s", stoppingForType.toString());
        if (stoppingForType.isBreak()) {
            final int eatenPomodoros = persistentStorage.readEatenPomodoros() + 1;
            final long lastEatenPomodoroTimestampMs = System.currentTimeMillis();
            Timber.d("Increase eaten pomodoros to %d", eatenPomodoros);
            persistentStorage.writeEatenPomodoros(eatenPomodoros);
            persistentStorage.writeLastEatenPomodoroTimestampMs(lastEatenPomodoroTimestampMs);
        }
        persistentStorage.writeActivityType(ActivityType.NONE);
        unscheduleAlarms();
        cancelNotification();
        stopDisplayService();
        return stoppingForType;
    }

    public void complete() {

    }

    private void scheduleAlarms(long whenMs) {
        PendingIntent pendingAlarmIntent = createPendingIntentAlarmBroadcast(context);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, whenMs, pendingAlarmIntent);
        PendingIntent pendingAlarmTickIntent = createPendingIntentTickAlarmBroadcast(context);
        long now = System.currentTimeMillis();
        int oneMinuteMs = 20 * 1000;
        int fiveSecondsMs = 20 * 1000;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now + fiveSecondsMs, oneMinuteMs, pendingAlarmTickIntent);
    }

    private void unscheduleAlarms() {
        PendingIntent pendingAlarmIntent = createPendingIntentAlarmBroadcast(context);
        alarmManager.cancel(pendingAlarmIntent);
        PendingIntent pendingAlarmTickIntent = createPendingIntentTickAlarmBroadcast(context);
        alarmManager.cancel(pendingAlarmTickIntent);
    }

    private void syncNotification(ActivityType activityType, long whenMs, boolean screenOn) {
        Timber.d("Syncing notification (activityType: %s, whenMs: %d, screenOn: %s)", activityType.toString(), whenMs, screenOn);
        if (activityType != ActivityType.NONE) {
            notificationManager.notify(NOTIFICATION_ID,
                    createNotificationBuilderForActivityType(context, activityType, getEatenPomodoros(), whenMs, screenOn));
        } else {
            Timber.e("Ignore notify for activityType " + ActivityType.NONE);
        }
    }

    private void startDisplayService() {
        Timber.d("Starting display service");
        context.startService(new Intent(context, PomodoroNotificationService.class));
    }

    private void stopDisplayService() {
        Timber.d("Stopping display service");
        context.stopService(new Intent(context, PomodoroNotificationService.class));
    }

    public static String convertDiffToPrettyMinutesLeft(long diffMs) {
        diffMs = Math.max(0, diffMs);
        int secondsTotal = (int) diffMs / 1000;
        int seconds = secondsTotal % 60;
        int minutes = (secondsTotal - seconds) / 60;
        if (minutes == 0) {
            return "< 1 minute";
        } else {
            return String.format("%d minute%s", minutes, minutes > 1 ? "s" : "");
        }
    }

    private static Notification createNotificationBuilderForActivityType(Context context, ActivityType activityType,
                                                                         int eatenPomodors, long whenMs, boolean isScreenOn) {
        Notification.Action stopAction = createStopAction(context);
        Notification.Action completeAction = createCompleteAction(context);

        Notification.WearableExtender wearableExtender = new Notification.WearableExtender()
                .addAction(stopAction)
                .addAction(completeAction)
                .setBackground(BitmapFactory.decodeResource(context.getResources(), backgroundResourceForActivityType(activityType)));

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setLocalOnly(true)
                .setStyle(new Notification.BigPictureStyle())
                .setContentTitle(titleForActivityType(activityType, eatenPomodors))
                .extend(wearableExtender);

        if (isScreenOn) {
            builder.setUsesChronometer(true);
            builder.setWhen(whenMs);
        } else {
            builder.setUsesChronometer(false);
            builder.setContentText(convertDiffToPrettyMinutesLeft(whenMs - System.currentTimeMillis()));
        }

        return builder.build();
    }

    private static Notification.Action createStopAction(Context context) {
        PendingIntent stopActionPendingIntent =
                createPendingIntentControlBroadcast(context,PomodoroControlReceiver.COMMAND_STOP);

        return new Notification.Action.Builder(
                R.drawable.ic_stop,
                context.getString(R.string.action_stop),
                stopActionPendingIntent)
                .build();
    }

    private static Notification.Action createCompleteAction(Context context) {
        PendingIntent completeActionPendingIntent =
                createPendingIntentControlBroadcast(context, PomodoroControlReceiver.COMMAND_COMPLETE);

        return new Notification.Action.Builder(
                R.drawable.ic_complete,
                context.getString(R.string.action_complete),
                completeActionPendingIntent)
                .build();
    }

    private static PendingIntent createPendingIntentControlBroadcast(Context context, int command) {
        Intent stopActionIntent = new Intent(PomodoroControlReceiver.ACTION);
        stopActionIntent.putExtra(PomodoroControlReceiver.EXTRA_COMMAND, command);
        return PendingIntent.getBroadcast(context, command, stopActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent createPendingIntentAlarmBroadcast(Context context) {
        Intent intent = new Intent(PomodoroAlarmReceiver.ACTION);
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent createPendingIntentTickAlarmBroadcast(Context context) {
        Intent intent = new Intent(PomodoroAlarmTickReceiver.ACTION);
        return PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static int backgroundResourceForActivityType(ActivityType activityType) {
        switch (activityType) {
            case LONG_BREAK:
                return R.drawable.bg_long_break;
            case POMODORO:
                return R.drawable.bg_pomodoro;
            case SHORT_BREAK:
                return R.drawable.bg_short_break;
        }
        throw new IllegalStateException("unsupported activityType " + activityType);
    }

    private static String titleForActivityType(ActivityType activityType, int eatenPomodoros) {
        switch (activityType) {
            case LONG_BREAK:
                return "Long Break";
            case POMODORO:
                return "Pomodoro #" + (eatenPomodoros + 1);
            case SHORT_BREAK:
                return "Short Break";
        }
        throw new IllegalStateException("unsupported activityType " + activityType);
    }

    private boolean isScreenOn() {
        return powerManager.isInteractive();
    }

    private static boolean isTheSamePomodoroDay(long first, long second) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(new Date(first));
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date(second));
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        boolean isBothAfter6am = cal1.get(Calendar.HOUR_OF_DAY) > 6 &&
                cal2.get(Calendar.HOUR_OF_DAY) > 6;
        return sameDay && isBothAfter6am;
    }
}
