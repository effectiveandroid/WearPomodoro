package com.alexkorovyansky.wearpomodoro;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

public class PomodoroApplication extends Application {
    private PomodoroComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

        buildComponentAndInject();
    }

    public void buildComponentAndInject() {
        component = PomodoroComponent.Initializer.init(this);
        component.inject(this);
    }

    public PomodoroComponent component() {
        return component;
    }

    public static PomodoroApplication get(Context context) {
        return (PomodoroApplication) context.getApplicationContext();
    }
}
