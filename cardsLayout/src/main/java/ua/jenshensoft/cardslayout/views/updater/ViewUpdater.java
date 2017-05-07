package ua.jenshensoft.cardslayout.views.updater;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.views.updater.callback.OnViewParamsUpdate;
import ua.jenshensoft.cardslayout.views.updater.callback.ViewUpdaterAction;
import ua.jenshensoft.cardslayout.views.updater.model.ViewUpdaterParams;

public class ViewUpdater<P extends ViewUpdaterParams> {

    @Nullable
    private final MeasurePredicate predicate;
    @Nullable
    private final OnViewParamsUpdate<P> viewParamsUpdate;
    @Nullable
    private P params;
    private boolean measured;
    private List<ViewUpdaterAction> actions;

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
    }

    public void onViewMeasured() {
        measured = true;
        onUpdateViewParams();
        onUpdateViewActions(true);
    }

    public void ping() {
        onUpdateViewParams();
        onUpdateViewActions(false);
    }

    @Nullable
    public P getParams() {
        return params;
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
            onUpdateViewParams();
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

    public boolean isMeasured() {
        return measured;
    }

    private void onUpdateViewParams() {
        boolean predicate = this.predicate == null || this.predicate.test();
        if (measured && params != null && viewParamsUpdate != null && predicate) {
            viewParamsUpdate.onUpdateViewParams(this.params);
        }
    }

    private void onUpdateViewActions(boolean calledInOnMeasure) {
        if (measured && (predicate == null || predicate.test())) {
            for (ViewUpdaterAction action : actions) {
                action.onAction(calledInOnMeasure);
            }
            actions.clear();
        }
    }

    public interface MeasurePredicate {
        boolean test();
    }
}
