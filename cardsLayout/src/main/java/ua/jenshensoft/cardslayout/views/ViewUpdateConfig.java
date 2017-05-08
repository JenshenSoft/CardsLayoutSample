package ua.jenshensoft.cardslayout.views;

import android.view.View;
import android.view.ViewGroup;

public class ViewUpdateConfig {

    private final View view;
    private final boolean validateCardCount;
    private Config onMeasure;
    private Config onLayout;

    public ViewUpdateConfig(View view, boolean validateCardCount) {
        this.view = view;
        this.validateCardCount = validateCardCount;
        this.onMeasure = new Config();
        this.onLayout = new Config();
    }

    public boolean needUpdateViewOnMeasure() {
        boolean validateCardCount = this.validateCardCount && view instanceof ViewGroup;
        return onMeasure.needUpdate(view.getMeasuredWidth(), view.getMeasuredHeight(), validateCardCount ? ((ViewGroup) view).getChildCount() : -1);
    }

    public boolean needUpdateViewOnLayout(boolean changed) {
        boolean validateCardCount = this.validateCardCount && view instanceof ViewGroup;
        boolean needUpdate = onLayout.needUpdate(view.getMeasuredWidth(), view.getMeasuredHeight(), validateCardCount ? ((ViewGroup) view).getChildCount() : -1);
        return changed || needUpdate;
    }

    private static class Config {

        private int lastWidth;
        private int lastHeight;
        private int childCount;

        Config() {
            this.lastWidth = -1;
            this.lastHeight = -1;
            this.childCount = -1;
        }

        boolean needUpdate(int measuredWidth, int measuredHeight, int childCount) {
            boolean isEqual = measuredHeight == lastHeight && measuredWidth == lastWidth;
            if (childCount != -1) {
                isEqual = isEqual && this.childCount == childCount;
                this.childCount = childCount;
            }
            lastWidth = measuredWidth;
            lastHeight = measuredHeight;
            return !isEqual;
        }
    }
}
