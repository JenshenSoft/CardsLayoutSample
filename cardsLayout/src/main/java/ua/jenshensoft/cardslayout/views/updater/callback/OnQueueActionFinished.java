package ua.jenshensoft.cardslayout.views.updater.callback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ua.jenshensoft.cardslayout.util.HandlerTimer;

public class OnQueueActionFinished {

    private boolean finished;
    private List<Action> actions;

    public void addAction(Action action) {
        if (finished) {
            action.finish();
        } else {
            if (actions == null) {
                actions = new CopyOnWriteArrayList<>();
            }
            actions.add(action);
        }
    }

    public void finishWithDelay(final long delay) {
        if (delay > 0) {
            HandlerTimer handlerTimer = new HandlerTimer();
            handlerTimer.schedule(() -> {
                handlerTimer.cancel();
                if (!finished) {
                    finish();
                }
            }, (int) delay);
        } else {
            finish();
        }
    }

    public void finish() {
        finished = true;
        if (actions != null && !actions.isEmpty()) {
            for (Action action : actions) {
                action.finish();
            }
            actions.clear();
            actions = null;
        }
    }

    /* private methods */

    public interface Action {
        void finish();
    }
}