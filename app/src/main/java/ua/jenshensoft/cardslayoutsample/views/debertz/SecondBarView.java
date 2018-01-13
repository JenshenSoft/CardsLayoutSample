package ua.jenshensoft.cardslayoutsample.views.debertz;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import ua.jenshensoft.cardslayout.views.FirstPosition;
import ua.jenshensoft.cardslayout.views.FirstPositionProvider;
import ua.jenshensoft.cardslayout.views.ValidateViewBlocker;

public class SecondBarView extends Button implements ValidateViewBlocker, FirstPositionProvider {

    private boolean inAnimation;
    private FirstPosition firstPosition = new FirstPosition(hashCode());

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
    public boolean isInAnimation() {
        return inAnimation;
    }

    @Override
    public void setInAnimation(boolean inAnimation) {
        this.inAnimation = inAnimation;
    }

    @Override
    public FirstPosition getFirstPosition() {
        return firstPosition;
    }
}
