package ua.jenshensoft.cardslayoutsample.views.debertz;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import ua.jenshensoft.cardslayout.views.ValidateViewBlocker;

public class SecondBarView extends Button implements ValidateViewBlocker {

    private boolean canInvalidateView = true;

    public SecondBarView(Context context) {
        super(context);
    }

    public SecondBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SecondBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SecondBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isCanInvalidateView() {
        return canInvalidateView;
    }

    @Override
    public void setCanInvalidateView(boolean canInvalidateView) {
        this.canInvalidateView = canInvalidateView;
    }
}
