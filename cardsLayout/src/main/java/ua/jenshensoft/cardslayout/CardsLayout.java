package ua.jenshensoft.cardslayout;


import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.listeners.CardTranslationListener;
import ua.jenshensoft.cardslayout.listeners.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.zabelnikov.swipelayout.layout.SwipeGestureManager;

public class CardsLayout extends FrameLayout implements CardTranslationListener, OnCardSwipedListener, OnCardPercentageChangeListener {

    public static final int RIGHT = 1; // 0000 0001
    public static final int LEFT = 2; // 0000 0010
    public static final int TOP = 4; // 0000 0100
    public static final int BOTTOM = 8; // 0000 1000
    public static final int CENTER = 16; // 0001 0000
    public static final int CENTER_HORIZONTAL = 32; // 0010 0000;
    public static final int CENTER_VERTICAL = 64; // 0100 0000;

    public static final int ORIENTATION_VERTICAL = 0;
    public static final int ORIENTATION_HORIZONTAL = 1;

    private int gravity;

    private int childOrientation;

    private int cardsOnTheHand = 8;

    private List<CardView> cardViewList = new ArrayList<>();
    private OnConfigureList onConfigureList;

    public CardsLayout(Context context) {
        super(context);
    }

    public CardsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateAttributes(context, attrs);
    }

    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAttributes(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateAttributes(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (!(child instanceof CardView)) {
            CardView cardView = new CardView(getContext());
            cardView.setSwipeOrientationMode(SwipeGestureManager.OrientationMode.BOTH);
            FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardView.setLayoutParams(layoutParams);
            ((ViewGroup) child.getParent()).removeView(child);
            if (cardViewList.size() >= cardsOnTheHand) {
                return;
            }
            cardView.addView(child);
            this.addView(cardView);
            cardViewList.add(cardView);
            cardView.setCardTranslationListener(this);
            //cardView.setCardSwipedListener(this);
            cardView.setCardPercentageChangeListener(this);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof CardView) {
            CardView cardView = (CardView) child;
            cardView.setCardTranslationListener(null);
            cardView.setCardSwipedListener(null);
            cardView.setCardPercentageChangeListener(null);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        setCardInfo();
        setPositionsX(width);
        setPositionsY(height);
        if (onConfigureList != null) {
            onConfigureList.onConfiguration(cardViewList);
        }
        moveViewsToStartPosition(cardViewList);
    }

    @Override
    public void onCardTranslation(float positionX, float positionY, int index, boolean isTouched) {
        if (!isTouched && hasViewInList(index)) {
            removeCardView(index);
        }
    }

    @Override
    public void onCardSwiped(int index) {
        removeCardView(index);
    }

    @Override
    public void percentageX(float percentage, int index) {

    }

    @Override
    public void percentageY(float percentage, int index) {

    }


    /* public methods */

    public void setConfiguration(OnConfigureList onConfigureList) {
        this.onConfigureList = onConfigureList;
    }

    public void setChildOrientation(int childOrientation) {
        this.childOrientation = childOrientation;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void setCardsOnTheHand(int cardsOnTheHand) {
        this.cardsOnTheHand = cardsOnTheHand;
    }


    /* private methods */

    private void inflateAttributes(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.CardsLayout);
            try {
                gravity = attributes.getInt(R.styleable.CardsLayout_cardsGravity, CENTER);
                childOrientation = attributes.getInt(R.styleable.CardsLayout_childListOrientation, ORIENTATION_HORIZONTAL);
            } finally {
                attributes.recycle();
            }
        }
    }

    private void setCardInfo() {
        for (int i = 0; i < cardViewList.size(); i++) {
            cardViewList.get(i).setCardInfo(new CardInfo(i));
        }
    }

    private CardView findCardView(int index) {
        for (CardView cardView : cardViewList) {
            if (cardView.getCardInfo().getCardIndex() == index) {
                return cardView;
            }
        }
        throw new RuntimeException("Can't find view");
    }

    private void setPositionsX(int width) {
        int widthOfViews = calcWidthOfViews(cardViewList, 0);
        int offset = widthOfViews - width;
        int lastCardPositionX = 0;
        int widthOfItem = cardViewList.get(0).getWidth();
        if (offset > 0) {
            offset /= cardViewList.size() - 1;
            if ((gravity & RIGHT) == RIGHT) {
                lastCardPositionX = 0;
            } else if ((gravity & LEFT) == LEFT) {
                if (childOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (width - calcWidthOfViews(cardViewList, offset));
                } else {
                    lastCardPositionX = (width - widthOfItem);
                }
            } else if ((gravity & CENTER_HORIZONTAL) == CENTER_HORIZONTAL ||
                    (gravity & CENTER) == CENTER) {
                if (childOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (width - calcWidthOfViews(cardViewList, offset)) / 2;
                } else {
                    lastCardPositionX = width / 2 - widthOfItem / 2;
                }
            }
        } else {
            offset = 0;
            if ((gravity & RIGHT) == RIGHT) {
                lastCardPositionX = 0;
            } else if ((gravity & LEFT) == LEFT) {
                if (childOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = width - widthOfViews;
                } else {
                    lastCardPositionX = (width - widthOfItem);
                }
            } else if ((gravity & CENTER_HORIZONTAL) == CENTER_HORIZONTAL
                    || (gravity & CENTER) == CENTER) {
                if (childOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (width - widthOfViews) / 2;
                } else {
                    lastCardPositionX = width / 2 - widthOfItem / 2;
                }
            }
        }
        for (CardView card : cardViewList) {
            card.getCardInfo().setCurrentPositionX(lastCardPositionX);
            if (childOrientation == ORIENTATION_HORIZONTAL)
                lastCardPositionX += card.getWidth() - offset;
        }
    }

    private void setPositionsY(int height) {
        int heightOfViews = calcHeightOfViews(cardViewList, 0);
        int offset = heightOfViews - height;

        int lastCardPositionY = 0;
        int heightOfItem = cardViewList.get(0).getHeight();
        if (offset > 0) {
            offset /= cardViewList.size() - 1;
            if ((gravity & TOP) == TOP) {
                lastCardPositionY = 0;
            } else if ((gravity & BOTTOM) == BOTTOM) {
                if (childOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (height - calcHeightOfViews(cardViewList, offset));
                } else {
                    lastCardPositionY = (height - heightOfItem);
                }
            } else if ((gravity & CENTER_VERTICAL) == CENTER_VERTICAL ||
                    (gravity & CENTER) == CENTER) {
                if (childOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (height - calcHeightOfViews(cardViewList, offset)) / 2;
                } else {
                    lastCardPositionY = height / 2 - heightOfItem / 2;
                }
            }
        } else {
            offset = 0;
            if ((gravity & TOP) == TOP) {
                lastCardPositionY = 0;
            } else if ((gravity & BOTTOM) == BOTTOM) {
                if (childOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = height - heightOfViews;
                } else {
                    lastCardPositionY = (height - heightOfItem);
                }
            } else if ((gravity & CENTER_VERTICAL) == CENTER_VERTICAL
                    || (gravity & CENTER) == CENTER) {
                if (childOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (height - heightOfViews) / 2;
                } else {
                    lastCardPositionY = height / 2 - heightOfItem / 2;
                }
            }
        }
        for (CardView card : cardViewList) {
            card.getCardInfo().setCurrentPositionY(lastCardPositionY);
            if (childOrientation == ORIENTATION_VERTICAL)
                lastCardPositionY += card.getHeight() - offset;
        }
    }

    private int calcWidthOfViews(List<CardView> cards, int offset) {
        int widthOfViews = 0;
        for (CardView card : cards) {
            widthOfViews += card.getWidth() - offset;
        }
        widthOfViews += offset;
        return widthOfViews;
    }

    private int calcHeightOfViews(List<CardView> cards, int offset) {
        int heightViews = 0;
        for (CardView card : cards) {
            heightViews += card.getHeight() - offset;
        }
        heightViews += offset;
        return heightViews;
    }

    private boolean hasViewInList(int index) {
        for (CardView cardView : cardViewList) {
            if (cardView.getCardInfo().getCardIndex() == index) {
                return true;
            }
        }
        return false;
    }

    private void removeCardView(int index) {
        CardView cardView = findCardView(index);
        ViewParent parent = cardView.getParent();
        ((ViewGroup) parent).removeView(cardView);
        cardViewList.remove(cardView);
        setPositionsX(getWidth());
        setPositionsY(getHeight());
        moveViewsToStartPosition(cardViewList);
    }

    private void moveViewsToStartPosition(List<CardView> cards) {
        for (int i = 0; i < cardViewList.size(); i++) {
            CardView cardView = cards.get(i);
            CardInfo cardInfo = cardView.getCardInfo();
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(cardView, "x", cardInfo.getLastPositionX(), cardInfo.getCurrentPositionX());
            animatorX.setDuration(500);
            animatorX.start();
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(cardView, "y", cardInfo.getLastPositionY(), cardInfo.getCurrentPositionY());
            animatorY.setDuration(500);
            animatorY.start();
        }
    }

    public interface OnConfigureList {
        void onConfiguration(List<CardView> cards);
    }
}
