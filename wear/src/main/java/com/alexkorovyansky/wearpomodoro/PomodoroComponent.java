package com.alexkorovyansky.wearpomodoro;

import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroAlarmReceiver;
import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroAlarmTickReceiver;
import com.alexkorovyansky.wearpomodoro.app.receivers.PomodoroControlReceiver;
import com.alexkorovyansky.wearpomodoro.app.services.PomodoroNotificationService;
import com.alexkorovyansky.wearpomodoro.app.ui.PomodoroEntryActivity;
import com.alexkorovyansky.wearpomodoro.app.ui.PomodoroTransitionActivity;
import com.alexkorovyansky.wearpomodoro.helpers.PersistentStorage;
import com.alexkorovyansky.wearpomodoro.helpers.PomodoroMaster;
import com.alexkorovyansky.wearpomodoro.helpers.UITimer;

import javax.inject.Singleton;

import dagger.Component;

@ApplicationScope
@Component(modules = {PomodoroModule.class})
public interface PomodoroComponent {
    PersistentStorage persistentStorage();
    PomodoroMaster pomodoroMaster();
    UITimer uiTimer();
    
    void inject(PomodoroApplication application);
    void inject(PomodoroEntryActivity activity);
    void inject(PomodoroTransitionActivity activity);
    void inject(PomodoroAlarmReceiver receiver);
    void inject(PomodoroAlarmTickReceiver receiver);
    void inject(PomodoroControlReceiver receiver);
    void inject(PomodoroNotificationService service);

    final static class Initializer {
        static PomodoroComponent init(PomodoroApplication app) {
            return Dagger_PomodoroComponent.builder()
                    .pomodoroModule(new PomodoroModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
