package com.alexkorovyansky.wearpomodoro;

import com.alexkorovyansky.wearpomodoro.helpers.PersistentStorage;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroMaster;
import com.alexkorovyansky.wearpomodoro.helpers.UITimer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PomodoroModule {

    private final PomodoroApplication application;

    public PomodoroModule(PomodoroApplication app) {
        application = app;
    }

    @Provides
    PersistentStorage providePersistentStorage() {
        return new PersistentStorage(application);
    }

    @Provides
    PomodoroMaster providePomodoroMaster(PersistentStorage persistentStorage) {
        return new PomodoroMaster(application, persistentStorage);
    }

    @Provides
    UITimer provideUiTimer() {
        return new UITimer();
    }
}
