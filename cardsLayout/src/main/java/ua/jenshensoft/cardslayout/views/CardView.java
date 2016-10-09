package ua.jenshensoft.cardslayout.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.util.SwipeGestureManager;

public class CardView<Entity> extends FrameLayout {

    public static final int START_TO_CURRENT = 1;
    public static final int LAST_TO_CURRENT = 2;

    private SwipeGestureManager<Entity> swipeManager;
    private CardInfo<Entity> cardInfo;
    private boolean scrollAndClickable;

    public CardView(Context context) {
        super(context);
        if (!isInEditMode()) {
            inflateAttributes(context, null);
        }
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setScrollAndClickableState(enabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardView cardView = (CardView) o;
        return cardInfo.equals(cardView.cardInfo);
    }

    @Override
    public int hashCode() {
        return cardInfo.hashCode();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (scrollAndClickable) {
            super.dispatchTouchEvent(ev);
            return swipeManager.onTouch(this, ev);
        }
        return false;
    }

    public CardInfo<Entity> getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo<Entity> cardInfo) {
        this.cardInfo = cardInfo;
        if (swipeManager != null)
            swipeManager.setCardInfoProvider(() -> cardInfo);
    }

    public void addBlock(int orientationMode) {
        swipeManager.addBlock(orientationMode);
    }

    public void removeBlock(int orientationMode) {
        swipeManager.removeBlock(orientationMode);
    }

    public void setSwipeSpeed(int swipeSpeed) {
        swipeManager.setSwipeSpeed(swipeSpeed);
    }

    public void setSwipeOffset(float swipeOffset) {
        swipeManager.setSwipeOffset(swipeOffset);
    }

    public void setScrollAndClickableState(boolean scrollAndClickable) {
        this.scrollAndClickable = scrollAndClickable;
    }

    /**
     * @param orientationMode 0 - LEFT_RIGHT , 1 - UP_BOTTOM, 2 - BOTH, 3 - NONE
     */
    public void setSwipeOrientationMode(int orientationMode) {
        swipeManager.setOrientationMode(orientationMode);
    }

    public void setCardTranslationListener(final OnCardTranslationListener<Entity> cardTranslationListener) {
        swipeManager.setCardTranslationListener(cardTranslationListener);
    }

    public void setCardSwipedListener(final OnCardSwipedListener<Entity> cardSwipedListener) {
        swipeManager.setCardSwipedListener(cardSwipedListener);
    }

    /**
     * @param mode 0 - START_TO_CURRENT , 1 - LAST_TO_CURRENT
     */
    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener<Entity> cardPercentageChangeListener, int mode) {
        swipeManager.setCardPercentageChangeListener(cardPercentageChangeListener, mode);
    }


    /* private methods */

    private void inflateAttributes(Context context, @Nullable AttributeSet attributeSet) {
        float swipeSpeed = 1.0f;
        int swipeOrientationMode = SwipeGestureManager.OrientationMode.UP_BOTTOM;
        float swipeOffset = -1;

        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.SwipeableLayout);
            try {
                swipeSpeed = attributes.getFloat(R.styleable.SwipeableLayout_card_speed, swipeSpeed);
                swipeOrientationMode = attributes.getInt(R.styleable.SwipeableLayout_card_swipeOrientation, swipeOrientationMode);
                swipeOffset = attributes.getFloat(R.styleable.SwipeableLayout_card_swipeOffset, swipeOffset);
                scrollAndClickable = attributes.getBoolean(R.styleable.SwipeableLayout_card_scrollAndClickable, scrollAndClickable);
            } finally {
                attributes.recycle();
            }
        }

        SwipeGestureManager.Builder<Entity> builder = new SwipeGestureManager.Builder<>(context);
        builder.setSwipeSpeed(swipeSpeed);
        builder.setSwipeOffset(swipeOffset);
        builder.setOrientationMode(swipeOrientationMode);
        swipeManager = builder.create();
        this.setOnTouchListener(swipeManager);
    }
}
