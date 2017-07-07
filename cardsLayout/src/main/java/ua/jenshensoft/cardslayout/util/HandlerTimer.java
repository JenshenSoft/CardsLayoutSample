package ua.jenshensoft.cardslayout.util;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class HandlerTimer {
    private final Timer timer = new Timer();
    private final Handler handler = new Handler();
    private boolean isCancelled;

    public HandlerTimer() {
    }

    public void scheduleAtFixedRate(final Runnable task, int delay, int period) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                HandlerTimer.this.tickAsync(task);
            }
        }, (long)delay, (long)period);
    }

    public void schedule(final Runnable task, int delay) {
        this.timer.schedule(new TimerTask() {
            public void run() {
                HandlerTimer.this.tickAsync(task);
            }
        }, (long)delay);
    }

    private void tickAsync(final Runnable task) {
        this.handler.post(new Runnable() {
            public void run() {
                HandlerTimer.this.tickSync(task);
            }
        });
    }

    private void tickSync(Runnable task) {
        if(!this.isCancelled) {
            task.run();
        }
    }

    public void cancel() {
        this.timer.cancel();
        this.isCancelled = true;
    }
}
