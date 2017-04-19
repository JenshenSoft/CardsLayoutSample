package ua.jenshensoft.cardslayout.util;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.Property;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.util.HashSet;
import java.util.Set;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.views.card.Card;

public class SwipeGestureManager<Entity> implements View.OnTouchListener {

    private static final float EPSILON = 0.00000001f;

    private final GestureDetector gestureDetector;
    private final Context context;
    //Listeners
    private OnCardTranslationListener<Entity> cardTranslationListener;
    private OnCardSwipedListener<Entity> cardSwipedListener;
    private OnCardPercentageChangeListener<Entity> cardPercentageChangeListener;
    // Configs
    private float swipeSpeed;
    private float swipeOffset;
    private int orientationMode;
    private Set<Integer> blocks;
    private int shiftX;
    private int shiftY;
    private int lastMotion = -1;
    private int lastXPosition;
    private int lastYPosition;
    private float percentageX;
    private float percentageY;
    private int mode;
    private CardInfoProvider<Entity> cardInfoProvider;

    private SwipeGestureManager(Context context, float swipeSpeed, float swipeOffset, int orientationMode) {
        this.context = context;
        this.swipeSpeed = swipeSpeed;
        this.swipeOffset = swipeOffset;
        this.orientationMode = orientationMode;
        this.blocks = new HashSet<>();
        this.gestureDetector = new GestureDetector(context, new FlingGestureDetector());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!(view instanceof Card)) {
            throw new RuntimeException("View doesn't belongs to Card");
        }

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                    .setSizeX(AwesomeAnimation.SizeMode.SCALE, 1.2f)
                    .setSizeY(AwesomeAnimation.SizeMode.SCALE, 1.2f)
                    .setDuration(200)
                    .build();
            awesomeAnimation.start();
            int cardElevationNormal = context.getResources().getDimensionPixelOffset(R.dimen.cardsLayout_card_elevation_pressed);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setElevation(cardElevationNormal);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                    .setSizeX(AwesomeAnimation.SizeMode.SCALE, 1f)
                    .setSizeY(AwesomeAnimation.SizeMode.SCALE, 1f)
                    .setDuration(200)
                    .build();
            awesomeAnimation.start();
            int cardElevationPressed = context.getResources().getDimensionPixelOffset(R.dimen.cardsLayout_card_elevation_normal);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setElevation(cardElevationPressed);
            }
        }

        boolean status;
        switch (orientationMode) {
            case OrientationMode.LEFT_RIGHT:
                status = swipeByX((View & Card<Entity>) view, event);
                break;
            case OrientationMode.UP_BOTTOM:
                status = swipeByY((View & Card<Entity>) view, event);
                break;
            case OrientationMode.BOTH:
                status = swipeByY((View & Card<Entity>) view, event) && swipeByX((View & Card<Entity>) view, event);
                break;
            default:
                status = false;
        }
        return status;
    }

    public void addBlock(int orientationMode) {
        blocks.add(orientationMode);
    }

    public void setBlockSet(Set<Integer> blockSet) {
        this.blocks.addAll(blockSet);
    }

    public void removeBlock(int orientationMode) {
        blocks.remove(orientationMode);
    }

    public void setOrientationMode(int orientationMode) {
        this.orientationMode = orientationMode;
    }

    public void setCardTranslationListener(final OnCardTranslationListener<Entity> cardTranslationListener) {
        this.cardTranslationListener = cardTranslationListener;
    }

    public void setCardSwipedListener(final OnCardSwipedListener<Entity> cardSwipedListener) {
        this.cardSwipedListener = cardSwipedListener;
    }

    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener<Entity> cardPercentageChangeListener, int mode) {
        this.mode = mode;
        this.cardPercentageChangeListener = cardPercentageChangeListener;
    }

    public void setCardInfoProvider(CardInfoProvider<Entity> cardInfoProvider) {
        this.cardInfoProvider = cardInfoProvider;
    }

    /* private methods */

    private float getPercent(float from, float to) {
        float offset = from - to;
        float onePercent = swipeOffset / 100f;
        float dif = Math.abs(offset) / onePercent;
        return dif > 100f ? 100f : dif;
    }


    @SuppressWarnings("SuspiciousNameCombination")
    private <C extends View & Card<Entity>> boolean swipeByY(C view, MotionEvent event) {
        if (!blocks.contains(OrientationMode.UP_BOTTOM)) {
            CardInfo cardInfo = view.getCardInfo();
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            final int y = (int) event.getRawY();
            if (Math.abs(swipeOffset - (-1)) < EPSILON) {
                swipeOffset = view.getHeight();
            }
            percentageY = getPercent(view.getY(), y);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastYPosition = y;
                lastMotion = MotionEvent.ACTION_DOWN;
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                shiftY = y - lastYPosition;
                float currentY = cardInfo.getFirstPositionY() + (shiftY * swipeSpeed);
                view.setY(currentY);
                cardInfo.setCurrentPositionY((int) currentY);

                triggerPercentageListener(true);
                triggerPositionChangeListener(shiftX, shiftY, true);

                lastMotion = MotionEvent.ACTION_MOVE;
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerPercentageListener(false);
                triggerPositionChangeListener(shiftX, shiftY, false);
                lastMotion = MotionEvent.ACTION_UP;
                if (percentageY >= 100f) {
                    triggerSwipeListener();
                }
                int firstPositionY = cardInfo.getFirstPositionY();
                cardInfo.setCurrentPositionY(firstPositionY);
                rollback(view, View.Y, view.getY(), firstPositionY);
            } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                return false;
            }
        }
        return true;
    }

    private <C extends View & Card<Entity>> boolean swipeByX(C view, MotionEvent event) {
        if (!blocks.contains(OrientationMode.LEFT_RIGHT)) {
            CardInfo cardInfo = view.getCardInfo();
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }

            final int x = (int) event.getRawX();
            if (Math.abs(swipeOffset - (-1)) < EPSILON) {
                swipeOffset = view.getWidth();
            }
            percentageX = getPercent(view.getX(), x);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastXPosition = x;
                lastMotion = MotionEvent.ACTION_DOWN;
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                shiftX = x - lastXPosition;
                float currentX = cardInfo.getFirstPositionX() + (shiftX * swipeSpeed);
                view.setX(currentX);
                cardInfo.setCurrentPositionX((int) currentX);

                triggerPercentageListener(true);
                triggerPositionChangeListener(shiftX, shiftY, true);

                lastMotion = MotionEvent.ACTION_MOVE;
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerPercentageListener(false);
                triggerPositionChangeListener(shiftX, shiftY, false);
                lastMotion = MotionEvent.ACTION_UP;
                if (percentageX >= 100f) {
                    triggerSwipeListener();
                }
                int firstPositionX = cardInfo.getFirstPositionX();
                cardInfo.setCurrentPositionX(firstPositionX);
                rollback(view, View.X, view.getX(), firstPositionX);
            } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                return false;
            }
        }
        Log.d("Motion", String.valueOf(event.getActionMasked()));
        return true;
    }

    private void rollback(View view, Property<View, Float> xProperty, float fromPosition, float startPosition) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, xProperty, fromPosition, startPosition);
        animator.setDuration(300);
        animator.start();
    }

    private CardInfo<Entity> getCardInfo() {
        if (cardInfoProvider != null) {
            return cardInfoProvider.getCardInfo();
        }
        throw new RuntimeException("Cant provide Card info, please attach it to SwipeGestureManager");
    }

    private void triggerPercentageListener(boolean state) {
        if (cardPercentageChangeListener != null && lastMotion != MotionEvent.ACTION_UP) {
            float percentageX, percentageY;
            CardInfo<Entity> cardInfo = getCardInfo();
            if (mode == Card.START_TO_CURRENT) {
                percentageX = getPercent(cardInfo.getFirstPositionX(), cardInfo.getCurrentPositionX());
                percentageY = getPercent(cardInfo.getFirstPositionY(), cardInfo.getCurrentPositionY());
            } else if (mode == Card.LAST_TO_CURRENT) {
                percentageX = this.percentageX;
                percentageY = this.percentageY;
            } else {
                throw new RuntimeException("Can't support this mode");
            }
            cardPercentageChangeListener.onPercentageChanged(percentageX, percentageY, cardInfo, state);
        }
    }

    private void triggerSwipeListener() {
        if (cardSwipedListener != null) {
            cardSwipedListener.onCardSwiped(getCardInfo());
        }
    }

    private void triggerPositionChangeListener(float positionX, float positionY, boolean state) {
        if (cardTranslationListener != null && lastMotion != MotionEvent.ACTION_UP) {
            cardTranslationListener.onCardTranslation(positionX, positionY, getCardInfo(), state);
        }
    }

    public void setSwipeSpeed(int swipeSpeed) {
        this.swipeSpeed = swipeSpeed;
    }

    public void setSwipeOffset(float swipeOffset) {
        this.swipeOffset = swipeOffset;
    }

    @FunctionalInterface
    public interface CardInfoProvider<Entity> {
        CardInfo<Entity> getCardInfo();
    }

    /* inner types */

    public static class Builder<Entity> {
        private final Context context;
        private float swipeOffset;
        private float swipeSpeed;
        private int mOrientationMode;

        public Builder(Context context) {
            this.context = context;
        }

        public void setSwipeSpeed(float mSwipeSpeed) {
            this.swipeSpeed = mSwipeSpeed;
        }

        public void setOrientationMode(int orientationMode) {
            this.mOrientationMode = orientationMode;
        }

        public void setSwipeOffset(float swipeOffset) {
            this.swipeOffset = swipeOffset;
        }

        public SwipeGestureManager<Entity> create() {
            return new SwipeGestureManager<>(context, swipeSpeed, swipeOffset, mOrientationMode);
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

        private OrientationMode() {}
    }
}
