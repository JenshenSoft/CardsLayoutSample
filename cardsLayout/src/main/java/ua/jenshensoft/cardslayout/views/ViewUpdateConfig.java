package ua.jenshensoft.cardslayout.views;

import android.view.View;
import android.view.ViewGroup;

public class ViewUpdateConfig {

    private final View view;
    private Config onMeasure;
    private Config onLayout;

    public ViewUpdateConfig(View view) {
        this.view = view;
        this.onMeasure = new Config();
        this.onLayout = new Config();
    }

    public boolean needUpdateViewOnMeasure() {
        boolean validateCardCount = view instanceof ViewGroup;
        return onMeasure.needUpdate(view.getMeasuredWidth(), view.getMeasuredHeight(), validateCardCount ? ((ViewGroup) view).getChildCount() : -1, view.getVisibility());
    }

    public boolean needUpdateViewOnLayout(boolean changed) {
        boolean validateCardCount = view instanceof ViewGroup;
        boolean needUpdate = onLayout.needUpdate(view.getMeasuredWidth(), view.getMeasuredHeight(), validateCardCount ? ((ViewGroup) view).getChildCount() : -1, view.getVisibility());
        return changed || needUpdate;
    }

    private static class Config {

        private int lastWidth;
        private int lastHeight;
        private int childCount;
        private int lastVisibility;

        Config() {
            this.lastWidth = -1;
            this.lastHeight = -1;
            this.childCount = -1;
            this.lastVisibility = -1;
        }

        boolean needUpdate(int measuredWidth, int measuredHeight, int childCount, int visibility) {
            boolean isEqual = measuredHeight == lastHeight && measuredWidth == lastWidth && visibility == lastVisibility;
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
