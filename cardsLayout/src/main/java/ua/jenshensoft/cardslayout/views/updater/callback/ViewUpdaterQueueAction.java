package ua.jenshensoft.cardslayout.views.updater.callback;

@FunctionalInterface
public interface ViewUpdaterQueueAction {
    void onAction(OnQueueActionFinished actionFinished, boolean calledInOnMeasure);
}