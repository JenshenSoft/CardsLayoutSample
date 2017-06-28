package ua.jenshensoft.cardslayout.views.updater.callback;

import android.support.annotation.NonNull;

import com.jenshen.awesomeanimation.OnAnimationCallbackDelegator;

@FunctionalInterface
public interface ViewUpdaterQueueAction {
    @NonNull
    OnAnimationCallbackDelegator onAction(boolean calledInOnMeasure);
}