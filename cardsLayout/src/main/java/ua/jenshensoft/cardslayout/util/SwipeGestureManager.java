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
import com.jenshen.awesomeanimation.util.animator.AnimatorHandler;

import java.util.HashSet;
import java.util.Set;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardClickedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.views.card.Card;

import static ua.jenshensoft.cardslayout.util.CardsUtil.SIZE_MULTIPLIER;

public class SwipeGestureManager implements View.OnTouchListener {

    public static final float EPSILON = 0.00000001f;

    private final GestureDetector gestureDetector;
    private final FlingGestureDetector flingGestureDetector;
    private final AnimatorHandler animatorHandler;
    // Configs
    private float swipeSpeed;
    private int orientationMode;
    private int animationDuration;
    private float swipeOffset;
    //Listeners
    private OnCardTranslationListener cardTranslationListener;
    private OnCardSwipedListener cardSwipedListener;
    private OnCardPercentageChangeListener cardPercentageChangeListener;
    private OnCardClickedListener cardClickListener;
    private Set<Integer> blocks;
    private int shiftX;
    private int shiftY;
    private int lastXPosition;
    private int lastYPosition;
    private float percentageX;
    private float percentageY;
    private int mode;
    private boolean cardDragged;
    private boolean cardInRollback;

    private SwipeGestureManager(Context context,
                                AnimatorHandler animatorHandler,
                                float swipeSpeed,
                                float swipeOffset,
                                int orientationMode,
                                int animationDuration) {
        this.animatorHandler = animatorHandler;
        this.swipeSpeed = swipeSpeed;
        this.swipeOffset = swipeOffset;
        this.orientationMode = orientationMode;
        this.animationDuration = animationDuration;
        this.blocks = new HashSet<>();
        this.flingGestureDetector = new FlingGestureDetector();
        this.gestureDetector = new GestureDetector(context, flingGestureDetector);
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
                shouldRollback = swipeByXandY((View & Card) view, event);
                break;
            default:
                shouldRollback = false;
        }
        if (shouldRollback) {
            cardRollback((View & Card) view);
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

    public void setCardClickListener(OnCardClickedListener cardClickListener) {
        this.cardClickListener = cardClickListener;
    }

    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener cardPercentageChangeListener, int mode) {
        this.mode = mode;
        this.cardPercentageChangeListener = cardPercentageChangeListener;
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
            flingGestureDetector.setCardInfo(cardInfo);
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
                shiftX = 0;
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                shiftX = x - lastXPosition;
                float currentX = cardInfo.getFirstPositionX() + (shiftX * swipeSpeed);
                view.setX(currentX);
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerClickListener(cardInfo);
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
                return true;
            }
        }
        return false;
    }

    private <C extends View & Card> boolean swipeByY(C view, MotionEvent event) {
        if (!blocks.contains(OrientationMode.UP_BOTTOM)) {
            CardInfo cardInfo = view.getCardInfo();
            flingGestureDetector.setCardInfo(cardInfo);
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
                shiftY = 0;
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                shiftY = y - lastYPosition;
                float currentY = cardInfo.getFirstPositionY() + (shiftY * swipeSpeed);
                view.setY(currentY);
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerClickListener(cardInfo);
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
                return true;
            }
        }
        return false;
    }


    private <C extends View & Card> boolean swipeByXandY(C view, MotionEvent event) {
        if (!blocks.contains(OrientationMode.BOTH)) {
            CardInfo cardInfo = view.getCardInfo();
            flingGestureDetector.setCardInfo(cardInfo);
            if (gestureDetector.onTouchEvent(event)) {
                return false;
            }
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();
            if (Math.abs(swipeOffset - (-1)) < EPSILON) {
                swipeOffset = view.getHeight();
            }
            percentageX = getPercent(view.getX(), x);
            percentageY = getPercent(view.getY(), y);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastXPosition = x;
                lastYPosition = y;
                shiftX = 0;
                shiftY = 0;
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                shiftX = x - lastXPosition;
                shiftY = y - lastYPosition;
                float currentX = cardInfo.getFirstPositionX() + (shiftX * swipeSpeed);
                float currentY = cardInfo.getFirstPositionY() + (shiftY * swipeSpeed);
                view.setX(currentX);
                view.setY(currentY);
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                triggerClickListener(cardInfo);
                triggerPositionChangeListener(cardInfo, shiftX, shiftY);
                triggerPercentageListener(view);
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
            cardRollback(view);
            triggerPercentageListener(view);
            triggerPositionChangeListener(view.getCardInfo(), shiftX, shiftY);
            return true;
        }

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (cardDragged) {
                cardRollback(view);
                return true;
            }
            zoomIn(view);
            cardDragged = true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (!cardDragged) {
                cardRollback(view);
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            zoomOut(view);
            cardDragged = false;
        }
        return false;
    }

    private <C extends View & Card> void cardRollback(C view) {
        cardDragged = false;
        rollback(view);
        zoomOut(view);
    }

    private <C extends View & Card> void rollback(C view) {
        animatorHandler.cancel();
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
        AnimatorSet animator = awesomeAnimation.getAnimatorSet();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardInRollback = false;
            }
        });
        animatorHandler.addAnimator(animator);
        animator.start();
    }

    private void zoomIn(View view) {
        AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                .setSizeX(AwesomeAnimation.SizeMode.SCALE, SIZE_MULTIPLIER)
                .setSizeY(AwesomeAnimation.SizeMode.SCALE, SIZE_MULTIPLIER)
                .setDuration(animationDuration)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(((Card) view).getPressedElevation());
        }
        AnimatorSet animator = awesomeAnimation.getAnimatorSet();
        animatorHandler.addAnimator(animator);
        animator.start();
    }

    private void zoomOut(View view) {
        AwesomeAnimation awesomeAnimation = new AwesomeAnimation.Builder(view)
                .setSizeX(AwesomeAnimation.SizeMode.SCALE, 1f)
                .setSizeY(AwesomeAnimation.SizeMode.SCALE, 1f)
                .setDuration(200)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(((Card) view).getNormalElevation());
        }
        AnimatorSet animator = awesomeAnimation.getAnimatorSet();
        animatorHandler.addAnimator(animator);
        animator.start();
    }

    /**
     * Should be invoked in the end
     * @param view
     * @param <C>
     */
    private <C extends View & Card> void triggerPercentageListener(C view) {
        if (cardPercentageChangeListener != null) {
            float percentageX, percentageY;
            CardInfo cardInfo = view.getCardInfo();
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

    private void triggerSwipeListener(CardInfo cardInfo) {
        if (cardSwipedListener != null) {
            cardSwipedListener.onCardSwiped(cardInfo);
        }
    }

    private void triggerClickListener(CardInfo cardInfo) {
        if (cardClickListener != null) {
            cardClickListener.onCardClicked(cardInfo);
        }
    }

    private void triggerPositionChangeListener(CardInfo cardInfo, float positionX, float positionY) {
        if (cardTranslationListener != null) {
            cardTranslationListener.onCardTranslation(positionX, positionY, cardInfo, cardDragged);
        }
    }

    public void setSwipeSpeed(float swipeSpeed) {
        this.swipeSpeed = swipeSpeed;
    }

    public void setSwipeOffset(float swipeOffset) {
        this.swipeOffset = swipeOffset;
    }

    /* inner types */

    public static class Builder {
        private final Context context;
        private final AnimatorHandler animatorHandler;
        private float swipeOffset;
        private float swipeSpeed;
        private int mOrientationMode;
        private int animationDuration = 300;

        public Builder(Context context, AnimatorHandler animatorHandler) {
            this.context = context;
            this.animatorHandler = animatorHandler;
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
            return new SwipeGestureManager(context, animatorHandler, swipeSpeed, swipeOffset, mOrientationMode, animationDuration);
        }
    }

    private class FlingGestureDetector extends GestureDetector.SimpleOnGestureListener {

        float sensitivity = 500;
        private CardInfo cardInfo;

        public void setCardInfo(CardInfo cardInfo) {
            this.cardInfo = cardInfo;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityY) > sensitivity && !blocks.contains(OrientationMode.UP_BOTTOM)) {
                triggerSwipeListener(cardInfo);
            } else if (Math.abs(velocityX) > sensitivity && !blocks.contains(OrientationMode.LEFT_RIGHT)) {
                triggerSwipeListener(cardInfo);
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
