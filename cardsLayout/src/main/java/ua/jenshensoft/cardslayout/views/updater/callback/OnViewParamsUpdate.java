package ua.jenshensoft.cardslayout.views.updater.callback;

import ua.jenshensoft.cardslayout.views.updater.model.ViewUpdaterParams;

@FunctionalInterface
public interface OnViewParamsUpdate<P extends ViewUpdaterParams> {
    void onUpdateViewParams(P params);
}