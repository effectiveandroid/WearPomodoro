package com.alexkorovyansky.wearpomodoro.app;

import android.app.Application;

import com.alexkorovyansky.wearpomodoro.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.wearable.DataMap;

import io.fabric.sdk.android.Fabric;
import pl.tajchert.exceptionwear.ExceptionDataListenerService;
import pl.tajchert.exceptionwear.ExceptionWearHandler;
import timber.log.Timber;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree());
            Fabric.with(this, new Crashlytics());
            ExceptionDataListenerService.setHandler(new ExceptionWearHandler() {
                @Override
                public void handleException(Throwable throwable, DataMap map) {
                    Crashlytics.setBool("wear_exception", true);
                    Crashlytics.setString("board", map.getString("board"));
                    Crashlytics.setString("fingerprint", map.getString("fingerprint"));
                    Crashlytics.setString("model", map.getString("model"));
                    Crashlytics.setString("manufacturer", map.getString("manufacturer"));
                    Crashlytics.setString("product", map.getString("product"));
                    Crashlytics.logException(throwable);
                }
            });
        }
    }

    private static class CrashlyticsTree implements Timber.Tree {

        @Override
        public void v(String message, Object... args) {
            Crashlytics.log("V:" + format(message, args));
        }

        @Override
        public void v(Throwable t, String message, Object... args) {
            Crashlytics.log("V:" + format(message, args));
            Crashlytics.logException(t);
        }

        @Override
        public void d(String message, Object... args) {
            Crashlytics.log("D:" + format(message, args));
        }

        @Override
        public void d(Throwable t, String message, Object... args) {
            Crashlytics.log("D:" + format(message, args));
            Crashlytics.logException(t);
        }

        @Override
        public void i(String message, Object... args) {
            Crashlytics.log("I:" + format(message, args));
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            Crashlytics.log("I:" + format(message, args));
            Crashlytics.logException(t);
        }

        @Override
        public void w(String message, Object... args) {
            Crashlytics.log("W:" + format(message, args));
        }

        @Override
        public void w(Throwable t, String message, Object... args) {
            Crashlytics.log("W:" + format(message, args));
            Crashlytics.logException(t);
        }

        @Override
        public void e(String message, Object... args) {
            Crashlytics.log("E:" + format(message, args));
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            Crashlytics.log("E:" + format(message, args));
            Crashlytics.logException(t);
        }

        private static String format(String message, Object... args) {
            return String.format(message, args);
        }
    }
}
