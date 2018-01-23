package ua.jenshensoft.cardslayout.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.util.HashSet;
import java.util.Set;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.views.card.Card;

import static ua.jenshensoft.cardslayout.util.CardsUtil.SIZE_MULTIPLIER;

public class SwipeGestureManager implements View.OnTouchListener {

    public static final float EPSILON = 0.00000001f;

    private final GestureDetector gestureDetector;
    // Configs
    private float swipeSpeed;
    private int orientationMode;
    private int animationDuration;
    private float swipeOffset;
    //Listeners
    private OnCardTranslationListener cardTranslationListener;
    private OnCardSwipedListener cardSwipedListener;
    private OnCardPercentageChangeListener cardPercentageChangeListener;
    private Set<Integer> blocks;
    private int shiftX;
    private int shiftY;
    private int lastMotion = -1;
    private int lastXPosition;
    private int lastYPosition;
    private float percentageX;
    private float percentageY;
    private int mode;
    private CardInfoProvider cardInfoProvider;
    private boolean cardDragged;
    private boolean cardInRollback;

    private SwipeGestureManager(Context context,
                                float swipeSpeed,
                                float swipeOffset,
                                int orientationMode,
                                int animationDuration) {
        this.swipeSpeed = swipeSpeed;
        this.swipeOffset = swipeOffset;
        this.orientationMode = orientationMode;
        this.animationDuration = animationDuration;
        this.blocks = new HashSet<>();
        this.gestureDetector = new GestureDetector(context, new FlingGestureDetector());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!(view instanceof Card)) {
            throw new RuntimeException("View doesn't belongs to Card");
        }

        if (switchCardIfDragged((View & Card) view, event)) {
            return true;
        }

        boolean shouldRollback;
        switch (orientationMode) {
            case OrientationMode.LEFT_RIGHT:
                shouldRollback = swipeByX((View & Card) view, event);
                break;
            case OrientationMode.UP_BOTTOM:
                shouldRollback = swipeByY((View & Card) view, event);
                break;
            case OrientationMode.BOTH:
                shouldRollback = swipeByY((View & Card) view, event) || swipeByX((View & Card) view, event);
                break;
            default:
                shouldRollback = false;
        }
        lastMotion = event.getActionMasked();
        if (shouldRollback) {
            rollback((View & Card) view);
        }
        return true;
    }

    public boolean isCardDragged() {
        return cardDragged;
    }

    public void addBlock(int orientationMode) {
        blocks.add(orientationMode);
    }

    public void setBlockSet(Set<Integer> blockSet) {
        this.blocks.addAll(blockSet);
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void removeBlock(int orientationMode) {
        blocks.remove(orientationMode);
    }

    public void setOrientationMode(int orientationMode) {
        this.orientationMode = orientationMode;
    }

    public void setCardTranslationListener(final OnCardTranslationListener cardTranslationListener) {
        this.cardTranslationListener = cardTranslationListener;
    }

    public void setCardSwipedListener(final OnCardSwipedListener cardSwipedListener) {
        this.cardSwipedListener = cardSwipedListener;
    }

    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener cardPercentageChangeListener, int mode) {
        this.mode = mode;
        this.cardPercentageChangeListener = cardPercentageChangeListener;
    }

    public void setCardInfoProvider(CardInfoProvider cardInfoProvider) {
        this.cardInfoProvider = cardInfoProvider;
    }

    /* private methods */

    private float getPercent(float from, float to) {
        float offset = from - to;
        float onePercent = swipeOffset / 100f;
        float dif = Math.abs(offset) / onePercent;
        return dif > 100f ? 100f : dif;
    }

    private <C extends View & Card> boolean swipeByX(C view, MotionEvent event) {
        if (!blocks.contains(OrientationMode.LEFT_RIGHT)) {
            CardInfo cardInfo = view.getCardInfo();
            if (gestureDetector.onTouchEvent(event)) {
                return false;
            }
            final int x = (int) event.getRawX();
            if (Math.abs(swipeOffset - (-1)) < EPSILON) {
                swipeOffset = view.getWidth();
            }
            percentageX = getPercent(view.getX(), x);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastXPosition = x;
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                shiftX = x - lastXPosition;
                float currentX = cardInfo.getFirstPositionX() + (shiftX * swipeSpeed);
                view.setX(currentX);
                triggerPercentageListener(view);
                triggerPositionChangeListener(shiftX, shiftY);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerPercentageListener(view);
                triggerPositionChangeListener(shiftX, shiftY);
                return true;
            }
        }
        return false;
    }

    private <C extends View & Card> boolean swipeByY(C view, MotionEvent event) {
        if (!blocks.contains(OrientationMode.UP_BOTTOM)) {
            CardInfo cardInfo = view.getCardInfo();
            if (gestureDetector.onTouchEvent(event)) {
                return false;
            }
            final int y = (int) event.getRawY();
            if (Math.abs(swipeOffset - (-1)) < EPSILON) {
                swipeOffset = view.getHeight();
            }
            percentageY = getPercent(view.getY(), y);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastYPosition = y;
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                shiftY = y - lastYPosition;
                float currentY = cardInfo.getFirstPositionY() + (shiftY * swipeSpeed);
                view.setY(currentY);
                triggerPercentageListener(view);
                triggerPositionChangeListener(shiftX, shiftY);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerPercentageListener(view);
                triggerPositionChangeListener(shiftX, shiftY);
                return true;
            }
        }
        return false;
    }

    private <C extends View & Card> boolean switchCardIfDragged(C view, MotionEvent event) {
        if (cardInRollback) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                cardInRollback = false;
            } else {
                return true;
            }
        }

        if (event.getPointerCount() > 1) {
            rollback(view);
            zoomOut(view);
            cardDragged = false;
            triggerPercentageListener(view);
            triggerPositionChangeListener(shiftX, shiftY);
            return true;
        }

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (cardDragged && !cardInRollback) {
                cardRollback(view);
                return true;
            }
            zoomIn(view);
            cardDragged = true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (!cardDragged && !cardInRollback) {
                cardRollback(view);
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (!cardInRollback) {
                zoomOut(view);
                cardDragged = false;
            }
        }
        return false;
    }

    private <C extends View & Card> void cardRollback(C view) {
        rollback(view);
        zoomOut(view);
        cardDragged = false;
    }

    private <C extends View & Card> void rollback(C view) {
        float x = view.getX();
        float y = view.getY();
        CardInfo cardInfo = view.getCardInfo();
        int firstPositionX = cardInfo.getFirstPositionX();
        int firstPositionY = cardInfo.getFirstPositionY();
        if (x == firstPositionX && y == firstPositionY) {
            return;
        }
        cardInRollback = true;
        AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                .setX(AwesomeAnimation.CoordinationMode.COORDINATES, x, firstPositionX)
                .setY(AwesomeAnimation.CoordinationMode.COORDINATES, y, firstPositionY)
                .setDuration(animationDuration)
                .build();
        AnimatorSet animatorSet = awesomeAnimation.getAnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardInRollback = false;
            }
        });
        animatorSet.start();
    }

    private void zoomIn(View view) {
        AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                .setSizeX(AwesomeAnimation.SizeMode.SCALE, SIZE_MULTIPLIER)
                .setSizeY(AwesomeAnimation.SizeMode.SCALE, SIZE_MULTIPLIER)
                .setDuration(animationDuration)
                .build();
        awesomeAnimation.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(((Card) view).getPressedElevation());
        }
    }

    private void zoomOut(View view) {
        AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                .setSizeX(AwesomeAnimation.SizeMode.SCALE, 1f)
                .setSizeY(AwesomeAnimation.SizeMode.SCALE, 1f)
                .setDuration(200)
                .build();
        awesomeAnimation.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(((Card) view).getNormalElevation());
        }
    }

    private CardInfo getCardInfo() {
        if (cardInfoProvider != null) {
            return cardInfoProvider.getCardInfo();
        }
        throw new RuntimeException("Cant provide Card info, please attach it to SwipeGestureManager");
    }

    private void triggerPercentageListener(View view) {
        if (cardPercentageChangeListener != null && lastMotion != MotionEvent.ACTION_UP) {
            float percentageX, percentageY;
            CardInfo cardInfo = getCardInfo();
            if (mode == Card.START_TO_CURRENT) {
                percentageX = getPercent(cardInfo.getFirstPositionX(), view.getX());
                percentageY = getPercent(cardInfo.getFirstPositionY(), view.getY());
            } else if (mode == Card.LAST_TO_CURRENT) {
                percentageX = this.percentageX;
                percentageY = this.percentageY;
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            cardPercentageChangeListener.onPercentageChanged(percentageX, percentageY, cardInfo, cardDragged);
        }
    }

    private void triggerSwipeListener() {
        if (cardSwipedListener != null && lastMotion != MotionEvent.ACTION_UP) {
            cardSwipedListener.onCardSwiped(getCardInfo());
        }
    }

    private void triggerPositionChangeListener(float positionX, float positionY) {
        if (cardTranslationListener != null && lastMotion != MotionEvent.ACTION_UP) {
            cardTranslationListener.onCardTranslation(positionX, positionY, getCardInfo(), cardDragged);
        }
    }

    public void setSwipeSpeed(int swipeSpeed) {
        this.swipeSpeed = swipeSpeed;
    }

    public void setSwipeOffset(float swipeOffset) {
        this.swipeOffset = swipeOffset;
    }

    @FunctionalInterface
    public interface CardInfoProvider {
        CardInfo getCardInfo();
    }

    /* inner types */

    public static class Builder {
        private final Context context;
        private float swipeOffset;
        private float swipeSpeed;
        private int mOrientationMode;
        private int animationDuration = 300;

        public Builder(Context context) {
            this.context = context;
        }

        public void setSwipeSpeed(float mSwipeSpeed) {
            this.swipeSpeed = mSwipeSpeed;
        }

        public void setOrientationMode(int orientationMode) {
            this.mOrientationMode = orientationMode;
        }

        public void setAnimationDuration(int animationDuration) {
            this.animationDuration = animationDuration;
        }

        public void setSwipeOffset(float swipeOffset) {
            this.swipeOffset = swipeOffset;
        }

        public SwipeGestureManager create() {
            return new SwipeGestureManager(context, swipeSpeed, swipeOffset, mOrientationMode, animationDuration);
        }
    }

    private class FlingGestureDetector extends GestureDetector.SimpleOnGestureListener {

        float sensitivity = 500;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityY) > sensitivity && !blocks.contains(OrientationMode.UP_BOTTOM)) {
                triggerSwipeListener();
            } else if (Math.abs(velocityX) > sensitivity && !blocks.contains(OrientationMode.LEFT_RIGHT)) {
                triggerSwipeListener();
            }
            return false;
        }
    }

    public abstract class OrientationMode {
        public static final int LEFT_RIGHT = 0;
        public static final int UP_BOTTOM = 1;
        public static final int BOTH = 2;
        public static final int NONE = 3;

        private OrientationMode() {
        }
    }
}
