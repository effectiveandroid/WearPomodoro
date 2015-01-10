package com.alexkorovyansky.wearpomodoro;

import android.app.Application;
import android.content.Context;

public class PomodoroApplication extends Application {
    private PomodoroComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

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
