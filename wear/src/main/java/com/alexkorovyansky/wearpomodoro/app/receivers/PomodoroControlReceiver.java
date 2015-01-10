/*
 * Copyright (C) 2014 Alex Korovyansky.
 */
package com.alexkorovyansky.wearpomodoro.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alexkorovyansky.wearpomodoro.BuildConfig;
import com.alexkorovyansky.wearpomodoro.PomodoroApplication;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroMaster;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PomodoroControlReceiver extends BroadcastReceiver {

    public static final String ACTION = BuildConfig.APPLICATION_ID + ".action.CONTROL";
    public static final String EXTRA_COMMAND = BuildConfig.APPLICATION_ID + ".extra.COMMAND";
    public static final int COMMAND_STOP = 101;
    public static final int COMMAND_COMPLETE = 102;

    @Inject PomodoroMaster pomodoroMaster;

    @DebugLog
    public PomodoroControlReceiver() {
    }

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        PomodoroApplication.get(context).component().inject(this);
        int command = intent.getIntExtra(EXTRA_COMMAND, -1);
        if (command == COMMAND_STOP) {
            pomodoroMaster.stop();
        } else if (command == COMMAND_COMPLETE) {
            pomodoroMaster.complete();
        } else {
            throw new IllegalStateException("Unsupported command " + command);
        }
    }
}
