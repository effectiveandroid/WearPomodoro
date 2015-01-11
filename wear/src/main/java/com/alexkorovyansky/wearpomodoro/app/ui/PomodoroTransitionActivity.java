/*
 * Copyright (C) 2014 Alex Korovyansky.
 */
package com.alexkorovyansky.wearpomodoro.app.ui;

import android.animation.ObjectAnimator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexkorovyansky.wearpomodoro.BuildConfig;
import com.alexkorovyansky.wearpomodoro.PomodoroApplication;
import com.alexkorovyansky.wearpomodoro.R;
import com.alexkorovyansky.wearpomodoro.app.base.BasePomodoroActivity;
import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroAlarmReceiver;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroMaster;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroUtils;
import com.alexkorovyansky.wearpomodoro.helpers.UITimer;
import com.alexkorovyansky.wearpomodoro.model.ActivityType;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PomodoroTransitionActivity extends BasePomodoroActivity implements SensorEventListener {

    public static final String EXTRA_NEXT_ACTIVITY_TYPE = BuildConfig.APPLICATION_ID + ".extra.NEXT_ACTIVITY_TYPE";

    @Inject PomodoroMaster pomodoroMaster;
    @Inject UITimer uiTimer;
    private SensorManager sensorManager;
    private Vibrator vibrator;

    private ImageView pomodoroStateImage;

    private ActivityType nextActivityType;

    private int stepSensorTicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViews(R.layout.activity_transite_rect, R.layout.activity_transite_round);
        PomodoroApplication.get(this).component().inject(this);
        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        this.nextActivityType = ActivityType.fromValue(getIntent().getIntExtra(EXTRA_NEXT_ACTIVITY_TYPE, -1));
    }

    @Override
    public void onLayoutInflated(WatchViewStub stub) {
        super.onLayoutInflated(stub);

        PomodoroAlarmReceiver.completeWakefulIntent(getIntent());
        pomodoroMaster.cancelNotification();
        vibrator.vibrate(1000);

        pomodoroStateImage = (ImageView) stub.findViewById(R.id.pomodoro_state_image);

        final TextView messageText = (TextView) stub.findViewById(R.id.transition_text);
        final int eatenPomodoros = pomodoroMaster.getEatenPomodoros();

        if (nextActivityType.isBreak()) {
            float dp = PomodoroUtils.dipToPixels(this, 1);
            ObjectAnimator anim = ObjectAnimator.ofFloat(pomodoroStateImage, View.TRANSLATION_X, -8*dp, 8*dp);
            anim.setDuration(1200);
            anim.setRepeatMode(ObjectAnimator.REVERSE);
            anim.setRepeatCount(ObjectAnimator.INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.start();

            pomodoroStateImage.setImageResource(R.drawable.pomodoro_break);
            if (BuildConfig.DEBUG) {
                pomodoroStateImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleOnPomodoroClick();
                    }
                });
            }

            int templateId = nextActivityType == ActivityType.LONG_BREAK ?
                    R.string.transition_text_before_long_break_message_template :
                    R.string.transition_text_before_short_break_message_template;
            messageText.setText(String.format(
                    getString(templateId),
                    eatenPomodoros + 1));

            activateStepsCounter();
        } else if (nextActivityType.isPomodoro()) {
            pomodoroStateImage.setImageResource(R.drawable.pomodoro_start);
            messageText.setText(String.format(
                    getString(R.string.transition_text_before_pomodoro_message_template),
                    eatenPomodoros + 1));
            uiTimer.schedule(new UITimer.Task() {
                @Override
                public void run() {
                    cancelTask();
                    finish();
                    pomodoroMaster.start(ActivityType.POMODORO);
                }
            }, 3000, "PomodoroTransitionActivity.DelayTimer");
        }
    }

    @DebugLog
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            ++stepSensorTicks;
            if (stepSensorTicks > 5) {
                handleStepsDone();
            }
        }
    }

    private void handleStepsDone() {
        sensorManager.unregisterListener(this);
        showView(R.layout.reward_view);
        uiTimer.schedule(new UITimer.Task() {
            @Override
            public void run() {
                cancelTask();
                startNextActivityType();
            }
        }, 5000, "PomodoroTransitionActivity.nextActivityTypeTimer");
    }

    private int clickedOnPomodoro = 0;

    @SuppressWarnings("UnusedDeclaration") // TODO: use with UITimer
    private void handleOnPomodoroClick() {
        clickedOnPomodoro++;
        switch (clickedOnPomodoro) {
            case 1:
                pomodoroStateImage.setImageResource(R.drawable.pomodoro_break_angry);
                break;
            case 2:
                pomodoroStateImage.setImageResource(R.drawable.pomodoro_break_blink);
                break;
            case 3:
                pomodoroStateImage.setImageResource(R.drawable.pomodoro_break_collapsed);
                uiTimer.schedule(new UITimer.Task() {
                    @Override
                    public void run() {
                        cancelTask();
                        pomodoroMaster.stop();
                        PomodoroTransitionActivity.this.finish();
                    }
                }, 1000, "PomodoroTransitionActivity.stopByCollapsingPomodoroTask");
                break;
            default:
                break;
        }
    }

    private void startNextActivityType() {
        pomodoroMaster.start(nextActivityType);
        finish();
    }

    @DebugLog
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        uiTimer.cancel("PomodoroTransitionActivity.DelayTimer");
        uiTimer.cancel("PomodoroTransitionActivity.nextActivityTypeTimer");
        uiTimer.cancel("PomodoroTransitionActivity.stopByCollapsingPomodoroTask");
    }

    private void activateStepsCounter() {
        stepSensorTicks = 0;
        Sensor stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

}
