/*
 * Copyright (C) 2014 Alex Korovyansky.
 */
package com.alexkorovyansky.wearpomodoro.app.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ViewAnimator;

import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

public class BasePomodoroActivity extends Activity implements WatchViewStub.OnLayoutInflatedListener {

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewsLayoutInflatedListener = new WatchViewStub.OnLayoutInflatedListener() {
            @DebugLog
            @Override
            public void onLayoutInflated(WatchViewStub watchViewStub) {
                container.showNext();
            }
        };
    }

    @DebugLog
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @DebugLog
    public void setContentViews(int rectRoundLayoutResId) {
        setContentViews(rectRoundLayoutResId, rectRoundLayoutResId);
    }

    private ViewAnimator container;

    private WatchViewStub.OnLayoutInflatedListener viewsLayoutInflatedListener;

    @DebugLog
    public void setContentViews(int rectLayoutResId, int roundLayoutResId) {
        container = new ViewAnimator(this);
        WatchViewStub watchViewStub = createWatchViewStub(rectLayoutResId, roundLayoutResId, this);
        container.addView(watchViewStub);
        setContentView(container);
    }

    private WatchViewStub createWatchViewStub(int rectLayoutResId, int roundLayoutResId, WatchViewStub.OnLayoutInflatedListener listener) {
        WatchViewStub watchViewStub = new WatchViewStub(this);
        watchViewStub.setLayoutParams(new WatchViewStub.LayoutParams(WatchViewStub.LayoutParams.MATCH_PARENT, WatchViewStub.LayoutParams.MATCH_PARENT));
        watchViewStub.setRectLayout(rectLayoutResId);
        watchViewStub.setRoundLayout(roundLayoutResId);
        watchViewStub.setOnLayoutInflatedListener(listener);
        return watchViewStub;
    }

    @DebugLog
    @Override
    public void onLayoutInflated(WatchViewStub stub) {
        ButterKnife.inject(this, stub.getRootView());
    }

    @DebugLog
    @Override
    protected void onStart() {
        super.onStart();
    }

    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();
    }

    @DebugLog
    @Override
    protected void onPause() {
        super.onPause();
    }

    @DebugLog
    @Override
    protected void onStop() {
        super.onStop();
    }

    @DebugLog
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        viewsLayoutInflatedListener = null;
    }

    protected void showView(int layoutId) {
        showView(layoutId, layoutId);
    }

    @DebugLog
    protected void showView(int rectLayoutId, int roundLayoutId) {
        WatchViewStub watchViewStub = createWatchViewStub(rectLayoutId, roundLayoutId, viewsLayoutInflatedListener);
        container.addView(watchViewStub);
    }
}
