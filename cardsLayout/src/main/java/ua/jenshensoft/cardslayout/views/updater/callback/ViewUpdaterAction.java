package ua.jenshensoft.cardslayout.views.updater.callback;

@FunctionalInterface
public interface ViewUpdaterAction {
    void onAction(boolean calledInOnMeasure);
}