package ua.jenshensoft.cardslayout.views.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.jenshen.awesomeanimation.util.animator.AnimatorHandler;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.util.SwipeGestureManager;
import ua.jenshensoft.cardslayout.views.FirstPosition;
import ua.jenshensoft.cardslayout.views.layout.CardsLayoutParams;

import static ua.jenshensoft.cardslayout.util.SwipeGestureManager.EPSILON;

public class CardView extends AppCompatImageView  implements Card {
    //attr
    private float swipeSpeed = 1.0f;
    private float swipeOffset = -1;
    private int swipeOrientationMode = SwipeGestureManager.OrientationMode.UP_BOTTOM;
    private SwipeGestureManager swipeManager;
    private CardInfo cardInfo;
    private boolean ownSwipeController = true;
    private boolean scrollAndClickable = true;
    private boolean inAnimation;
    private float cardElevation = -1;
    private float cardElevationPressed = -1;
    @Nullable
    private AnimatorHandler animatorHandler;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardView)) return false;
        CardView that = (CardView) o;
        return cardInfo != null ? cardInfo.equals(that.cardInfo) : that.cardInfo == null;
    }

    @Override
    public int hashCode() {
        return cardInfo != null ? cardInfo.hashCode() : 0;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setScrollAndClickableState(enabled);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animatorHandler != null) {
            animatorHandler.onDestroy();
        }
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
    public int getCardWidth() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (getCardInfo().isCardDistributed() && layoutParams != null && layoutParams instanceof CardsLayoutParams) {
            CardsLayoutParams params = (CardsLayoutParams) layoutParams;
            int widthForCalculation = params.getWidthForCalculation();
            if (widthForCalculation != -1) {
                return widthForCalculation;
            }
        }
        return getMeasuredWidth();
    }

    @Override
    public int getCardHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (getCardInfo().isCardDistributed() && layoutParams != null && layoutParams instanceof CardsLayoutParams) {
            CardsLayoutParams params = (CardsLayoutParams) layoutParams;
            int heightForCalculation = params.getHeightForCalculation();
            if (heightForCalculation != -1) {
                return heightForCalculation;
            }
        }
        return getMeasuredHeight();
    }

    @Override
    public float getCardZ() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getElevation();
        }
        return 0;
    }

    @Override
    public void setCardZ(float z) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(z);
        }
    }

    /* attr */

    @Override
    public float getNormalElevation() {
        return cardElevation;
    }

    @Override
    public float getPressedElevation() {
        return cardElevationPressed;
    }

    @Override
    public CardInfo getCardInfo() {
        return cardInfo;
    }

    @Override
    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    @Override
    public boolean isCardDragged() {
        return swipeManager.isCardDragged();
    }

    @Override
    public void setSwipeOrientationMode(int orientationMode) {
        swipeManager.setOrientationMode(orientationMode);
    }

    @Override
    public void setAnimationDuration(int animationDuration) {
        swipeManager.setAnimationDuration(animationDuration);
    }

    @Override
    public void setCardTranslationListener(final OnCardTranslationListener cardTranslationListener) {
        swipeManager.setCardTranslationListener(cardTranslationListener);
    }

    @Override
    public void setCardSwipedListener(final OnCardSwipedListener cardSwipedListener) {
        swipeManager.setCardSwipedListener(cardSwipedListener);
    }

    @Override
    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener cardPercentageChangeListener, int mode) {
        swipeManager.setCardPercentageChangeListener(cardPercentageChangeListener, mode);
    }

    @Override
    public void addBlock(int orientationMode) {
        swipeManager.addBlock(orientationMode);
    }

    @Override
    public void removeBlock(int orientationMode) {
        swipeManager.removeBlock(orientationMode);
    }

    @Override
    public void setSwipeSpeed(int swipeSpeed) {
        swipeManager.setSwipeSpeed(swipeSpeed);
    }

    @Override
    public void setSwipeOffset(float swipeOffset) {
        swipeManager.setSwipeOffset(swipeOffset);
    }

    @Override
    public void setScrollAndClickableState(boolean scrollAndClickable) {
        this.scrollAndClickable = scrollAndClickable;
    }

    @Override
    public void setSwipeController(SwipeGestureManager swipeManager) {
        this.swipeManager = swipeManager;
        this.swipeManager.setSwipeSpeed(swipeSpeed);
        this.swipeManager.setSwipeOffset(swipeOffset);
        this.swipeManager.setOrientationMode(swipeOrientationMode);
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
        return cardInfo;
    }

    /* private methods */

    private void inflateAttributes(Context context, @Nullable AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout);
            try {
                ownSwipeController = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_ownSwipeController, ownSwipeController);
                swipeSpeed = attributes.getFloat(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_speed, swipeSpeed);
                swipeOrientationMode = attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_swipeOrientation, swipeOrientationMode);
                swipeOffset = attributes.getFloat(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_swipeOffset, swipeOffset);
                scrollAndClickable = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_scrollAndClickable, scrollAndClickable);
                cardElevation = attributes.getDimension(ua.jenshensoft.cardslayout.R.styleable.SwipeableLayout_card_elevation, cardElevation);
                cardElevationPressed = attributes.getDimension(R.styleable.SwipeableLayout_card_elevation_pressed, cardElevationPressed);
            } finally {
                attributes.recycle();
            }
        }
    }

    private void init() {
        if (Math.abs(cardElevation - (-1)) < EPSILON) {
            cardElevation = getResources().getDimension(R.dimen.cardsLayout_card_elevation_normal);
        }
        setCardZ(cardElevation);
        if (Math.abs(cardElevationPressed - (-1)) < EPSILON) {
            cardElevationPressed = getResources().getDimension(R.dimen.cardsLayout_card_elevation_pressed);
        }
        if (ownSwipeController) {
            animatorHandler = new AnimatorHandler();
            SwipeGestureManager.Builder builder = new SwipeGestureManager.Builder(getContext(), animatorHandler);
            builder.setSwipeSpeed(swipeSpeed);
            builder.setSwipeOffset(swipeOffset);
            builder.setOrientationMode(swipeOrientationMode);
            swipeManager = builder.create();
        }
    }

    @Override
    public String toString() {
        return "CardBox{" +
                "cardInfo=" + cardInfo +
                '}';
    }
}
