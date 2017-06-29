package ua.jenshensoft.cardslayout.views.updater.callback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    void finish() {
        finished = true;
        if (actions != null && !actions.isEmpty()) {
            for (Action action : actions) {
                action.finish();
            }
            actions.clear();
            actions = null;
        }
    }

    public interface Action {
        void finish();
    }
}