package ua.jenshensoft.cardslayout.views;

import android.view.View;

public class ViewMeasureConfig {

    private final View view;
    private int lastWidth;
    private int lastHeight;
    private int childCount;

    public ViewMeasureConfig(View view) {
        this.view = view;
        this.lastWidth = -1;
        this.lastHeight = -1;
        this.childCount = -1;
    }

    public boolean needUpdateView() {
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        boolean isEqual = measuredHeight == lastHeight && measuredWidth == lastWidth;
        lastWidth = measuredWidth;
        lastHeight = measuredHeight;
        return !isEqual;
    }
}
