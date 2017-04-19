package ua.jenshensoft.cardslayout.views.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.util.SwipeGestureManager;

public class CardView<Entity> extends AppCompatImageView implements Card<Entity> {
    //attr
    private float swipeSpeed = 1.0f;
    private float swipeOffset = -1;
    private int swipeOrientationMode = SwipeGestureManager.OrientationMode.UP_BOTTOM;
    private SwipeGestureManager<Entity> swipeManager;
    private CardInfo<Entity> cardInfo;
    private boolean scrollAndClickable = true;

    public CardView(Context context) {
        super(context);
        if (!isInEditMode()) {
            inflateAttributes(context, null);
        }
        init();
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
        init();
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
        init();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setScrollAndClickableState(enabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (Card.class.isInstance(o)) {
            Card card = (Card<Entity>) o;
            return cardInfo.equals(card.getCardInfo());
        }
        return false;
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

    @Override
    public void setX(@Px float x) {
        super.setX(x);
        getCardInfo().setCurrentPositionX((int) x);
    }

    @Override
    public void setY(@Px float y) {
        super.setY(y);
        getCardInfo().setCurrentPositionY((int) y);
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        getCardInfo().setCurrentRotation((int) rotation);
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

    @Override
    public void setSwipeOrientationMode(int orientationMode) {
        swipeManager.setOrientationMode(orientationMode);
    }

    @Override
    public void setCardTranslationListener(final OnCardTranslationListener<Entity> cardTranslationListener) {
        swipeManager.setCardTranslationListener(cardTranslationListener);
    }

    @Override
    public void setCardSwipedListener(final OnCardSwipedListener<Entity> cardSwipedListener) {
        swipeManager.setCardSwipedListener(cardSwipedListener);
    }

    @Override
    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener<Entity> cardPercentageChangeListener, int mode) {
        swipeManager.setCardPercentageChangeListener(cardPercentageChangeListener, mode);
    }


    /* private methods */

    private void inflateAttributes(Context context, @Nullable AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout);
            try {
                swipeSpeed = attributes.getFloat(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_speed, swipeSpeed);
                swipeOrientationMode = attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_swipeOrientation, swipeOrientationMode);
                swipeOffset = attributes.getFloat(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_swipeOffset, swipeOffset);
                scrollAndClickable = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_scrollAndClickable, scrollAndClickable);
            } finally {
                attributes.recycle();
            }
        }
    }

    private void init() {
        SwipeGestureManager.Builder<Entity> builder = new SwipeGestureManager.Builder<>(getContext());
        builder.setSwipeSpeed(swipeSpeed);
        builder.setSwipeOffset(swipeOffset);
        builder.setOrientationMode(swipeOrientationMode);
        swipeManager = builder.create();
        setOnTouchListener(swipeManager);
    }
}
