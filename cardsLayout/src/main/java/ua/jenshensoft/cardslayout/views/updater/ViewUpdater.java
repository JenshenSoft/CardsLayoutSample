package ua.jenshensoft.cardslayout.views.updater;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jenshen.awesomeanimation.OnAnimationCallbackDelegator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import ua.jenshensoft.cardslayout.views.updater.callback.OnViewParamsUpdate;
import ua.jenshensoft.cardslayout.views.updater.callback.ViewUpdaterAction;
import ua.jenshensoft.cardslayout.views.updater.callback.ViewUpdaterQueueAction;
import ua.jenshensoft.cardslayout.views.updater.model.ViewUpdaterParams;

public class ViewUpdater<P extends ViewUpdaterParams> {

    @Nullable
    private final MeasurePredicate predicate;
    @Nullable
    private final OnViewParamsUpdate<P> viewParamsUpdate;
    @Nullable
    private P params;
    private boolean updated;
    private List<ViewUpdaterAction> actions;
    private Queue<ViewUpdaterQueueAction> queueActions;
    private boolean isPassingOnQueue;

    public ViewUpdater() {
        this(null, null);
    }

    public ViewUpdater(@NonNull OnViewParamsUpdate<P> viewParamsUpdate) {
        this(null, viewParamsUpdate);
    }

    public ViewUpdater(@Nullable MeasurePredicate predicate, @Nullable OnViewParamsUpdate<P> viewParamsUpdate) {
        this.predicate = predicate;
        this.viewParamsUpdate = viewParamsUpdate;
        this.actions = new ArrayList<>();
        this.queueActions = new ArrayDeque<>();
    }

    public void onViewUpdated() {
        updated = true;
        onUpdateViewParams(true);
        onUpdateViewActions(true);
    }

    public void setIsUpdated(boolean updated) {
        this.updated = updated;
    }

    public void ping() {
        onUpdateViewParams(false);
        onUpdateViewActions(false);
    }

    @Nullable
    public P getParams() {
        return params;
    }

    public void addActionToQueue(@NonNull ViewUpdaterQueueAction params) {
        addActionToQueue(params, true);
    }

    public void addActionToQueue(@NonNull ViewUpdaterQueueAction params, boolean update) {
        this.queueActions.add(params);
        if (update) {
            onUpdateViewActions(update);
        }
    }

    public void addAction(@NonNull ViewUpdaterAction action) {
        addAction(action, true);
    }

    public void addAction(@NonNull ViewUpdaterAction params, boolean update) {
        this.actions.add(params);
        if (update) {
            onUpdateViewActions(false);
        }
    }

    public void setParams(@NonNull P params) {
        setParams(params, true);
    }

    public void setParams(@NonNull P params, boolean update) {
        this.params = params;
        if (update) {
            onUpdateViewParams(false);
        }
    }

    @Nullable
    public <T> T getAction(Class<T> clazz) {
        for (ViewUpdaterAction action : actions) {
            if (clazz.isInstance(action)) {
                return (T) action;
            }
        }
        return null;
    }

    public boolean isUpdated() {
        return updated;
    }

    private void onUpdateViewParams(boolean calledInOnMeasure) {
        boolean predicate = this.predicate == null || this.predicate.test();
        if (updated && params != null && viewParamsUpdate != null && predicate) {
            viewParamsUpdate.onUpdateViewParams(this.params, calledInOnMeasure);
        }
    }

    private void onUpdateViewActions(boolean calledInOnMeasure) {
        if (updated && (predicate == null || predicate.test())) {
            invokeActions(calledInOnMeasure, actions);
            if (!isPassingOnQueue) {
                isPassingOnQueue = true;
                invokeQueueActions(calledInOnMeasure, queueActions);
            }
        }
    }

    private void invokeActions(boolean calledInOnMeasure, List<ViewUpdaterAction> actions) {
        for (ViewUpdaterAction action : actions) {
            action.onAction(calledInOnMeasure);
        }
        actions.clear();
    }

    private void invokeQueueActions(boolean calledInOnMeasure, Queue<ViewUpdaterQueueAction> queueActions) {
        if (!queueActions.isEmpty()) {
            ViewUpdaterQueueAction queueAction = queueActions.element();
            OnAnimationCallbackDelegator onAnimationCallbackDelegator = queueAction.onAction(calledInOnMeasure);
            onAnimationCallbackDelegator.addAdapter(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    queueActions.remove(queueAction);
                    invokeQueueActions(calledInOnMeasure, queueActions);
                }
            });
        } else {
            isPassingOnQueue = false;
        }
    }

    public interface MeasurePredicate {
        boolean test();
    }
}
