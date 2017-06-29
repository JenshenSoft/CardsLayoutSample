package ua.jenshensoft.cardslayout.views.updater.callback;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ua.jenshensoft.cardslayout.util.HandlerTimer;

public class OnQueueActionFinished {

    private boolean finished;
    private List<Action> actions;
    @Nullable
    private HandlerTimer handlerTimer;

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
            cancelTimer();
            handlerTimer = new HandlerTimer();
            handlerTimer.schedule(this::finish, (int) delay);
        } else {
            finish();
        }
    }

    public void finish() {
        cancelTimer();
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

    private void cancelTimer() {
        if (handlerTimer != null) {
            handlerTimer.cancel();
            handlerTimer = null;
        }
    }

    public interface Action {
        void finish();
    }
}