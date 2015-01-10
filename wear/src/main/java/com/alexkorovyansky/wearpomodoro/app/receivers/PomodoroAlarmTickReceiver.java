/*
 * Copyright (C) 2014 Alex Korovyansky.
 */
package com.alexkorovyansky.wearpomodoro.app.receivers;

import android.content.Context;
import android.content.Intent;

import com.alexkorovyansky.wearpomodoro.BuildConfig;
import com.alexkorovyansky.wearpomodoro.PomodoroApplication;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroMaster;
import com.alexkorovyansky.wearpomodoro.helpers.WakefulBroadcastReceiver;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PomodoroAlarmTickReceiver extends WakefulBroadcastReceiver {

    public static final String ACTION = BuildConfig.APPLICATION_ID + ".action.ALARM_TICK";

    @Inject
    PomodoroMaster pomodoroMaster;

    @DebugLog
    public PomodoroAlarmTickReceiver() {
    }

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        PomodoroApplication.get(context).component().inject(this);
        pomodoroMaster.syncNotification();
    }

}
