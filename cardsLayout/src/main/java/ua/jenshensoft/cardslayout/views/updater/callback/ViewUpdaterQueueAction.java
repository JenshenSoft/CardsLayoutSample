package ua.jenshensoft.cardslayout.views.updater.callback;

import android.support.annotation.NonNull;

@FunctionalInterface
public interface ViewUpdaterQueueAction {
    @NonNull
    OnQueueActionFinished onAction(boolean calledInOnMeasure);
}