/*
 * Copyright (C) 2014 Alex Korovyansky.
 */
package com.alexkorovyansky.wearpomodoro.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.alexkorovyansky.wearpomodoro.PomodoroApplication;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroMaster;
import com.alexkorovyansky.wearpomodoro.model.ActivityType;

import javax.inject.Inject;

public class PomodoroEntryActivity extends Activity {

    @Inject PomodoroMaster pomodoroMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PomodoroApplication.get(this).component().inject(this);
        pomodoroMaster.stop();
        pomodoroMaster.check();
        Intent intent = new Intent(this, PomodoroTransitionActivity.class);
        intent.putExtra(PomodoroTransitionActivity.EXTRA_NEXT_ACTIVITY_TYPE, ActivityType.POMODORO.value());
        startActivity(intent);
        finish();
    }

}
