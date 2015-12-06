package ua.jenshensoft.cardslayout.views;


import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;


import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.OnCardTranslationListener;
import ua.zabelnikov.swipelayout.layout.SwipeGestureManager;

public class CardsLayout extends FrameLayout implements OnCardTranslationListener, OnCardSwipedListener, OnCardPercentageChangeListener {

    public static final int RIGHT = 1; // 0000 0001
    public static final int LEFT = 2; // 0000 0010
    public static final int TOP = 4; // 0000 0100
    public static final int BOTTOM = 8; // 0000 1000
    public static final int CENTER = 16; // 0001 0000
    public static final int CENTER_HORIZONTAL = 32; // 0010 0000;
    public static final int CENTER_VERTICAL = 64; // 0100 0000;

    public static final int ORIENTATION_VERTICAL = 0;
    public static final int ORIENTATION_HORIZONTAL = 1;

    private final List<CardView> cardViewList = new ArrayList<>();
    private int gravity;
    private int childListOrientation;
    private int childList_paddingLeft;
    private int childList_paddingRight;
    private int childList_paddingTop;
    private int childList_paddingBottom;
    private int childRotation;
    private boolean isOnLayoutCalled;
    private OnCardSwipedListener onCardSwipedListener;
    private OnCardPercentageChangeListener onCardPercentageChangeListener;
    private OnCardTranslationListener onCardTranslationListener;
    private OnConfigureList onConfigureList;
    private final Interpolator interpolator = new BounceInterpolator();

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
        if (isOnLayoutCalled) {
            return;
        }
        if (onConfigureList != null) {
            onConfigureList.onConfiguration(cardViewList);
        }
        setCardInfo();
        invalidateCardsPosition();
        moveViewsToCurrentPosition(false);
        isOnLayoutCalled = true;
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (!(child instanceof CardView)) {
            ((ViewGroup) child.getParent()).removeView(child);
            addCardViewToRootView(child);
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
    public void onCardTranslation(float positionX, float positionY, CardInfo cardInfo, boolean isTouched) {
        if (onCardTranslationListener != null)
            onCardTranslationListener.onCardTranslation(positionX, positionY, cardInfo, isTouched);
    }

    @Override
    public void onCardSwiped(CardInfo cardInfo) {
        if (onCardSwipedListener != null)
            onCardSwipedListener.onCardSwiped(cardInfo);
    }

    @Override
    public void percentageX(float percentage, CardInfo cardInfo) {
        if (onCardPercentageChangeListener != null)
            onCardPercentageChangeListener.percentageX(percentage, cardInfo);
    }

    @Override
    public void percentageY(float percentage, CardInfo cardInfo) {
        if (onCardPercentageChangeListener != null)
            onCardPercentageChangeListener.percentageY(percentage, cardInfo);
    }


    /* public methods */

    public List<CardView> getCardViews() {
        return cardViewList;
    }

    public void setConfigurationForList(OnConfigureList onConfigureList) {
        this.onConfigureList = onConfigureList;
    }

    public void setChildListOrientation(int childListOrientation) {
        this.childListOrientation = childListOrientation;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void addCardView(View view, int position) {
        addCardViewToRootView(view, position);
    }

    public void addCardView(View view) {
        addCardViewToRootView(view);
    }

    public void removeCardView(int index) {
        CardView cardView = findCardView(index);
        ViewParent parent = cardView.getParent();
        ((ViewGroup) parent).removeView(cardView);
        cardViewList.remove(cardView);
        for (CardView view : cardViewList) {
            CardInfo cardInfo = view.getCardInfo();
            int cardIndex = cardInfo.getCardIndex();
            if (cardIndex > index) {
                cardInfo.setCardIndex(cardIndex - 1);
            }
        }
        invalidateCardsPosition();
        moveViewsToCurrentPosition(true);
    }

    public void invalidateCardsPosition() {
        setPositionsX();
        setPositionsY();
    }

    public void setCardViewsState(boolean state) {
        setCardViewsState(state, -1);
    }

    public void setCardViewsState(boolean state, int index) {
        for (CardView cardView : cardViewList) {
            if (!state) {
                if (cardView.getCardInfo() == null || cardView.getCardInfo().getCardIndex() != index) {
                    cardView.addBlock(SwipeGestureManager.OrientationMode.LEFT_RIGHT);
                    cardView.addBlock(SwipeGestureManager.OrientationMode.UP_BOTTOM);
                }
            } else {
                cardView.removeBlock(SwipeGestureManager.OrientationMode.LEFT_RIGHT);
                cardView.removeBlock(SwipeGestureManager.OrientationMode.UP_BOTTOM);
            }
        }
    }

    public void setCardTranslationListener(OnCardTranslationListener cardTranslationListener) {
        this.onCardTranslationListener = cardTranslationListener;
    }

    public void setOnCardPercentageChangeListener(OnCardPercentageChangeListener onCardPercentageChangeListener) {
        this.onCardPercentageChangeListener = onCardPercentageChangeListener;
    }

    public void setOnCardSwipedListener(OnCardSwipedListener onCardSwipedListener) {
        this.onCardSwipedListener = onCardSwipedListener;
    }

    public void moveViewsToCurrentPosition(boolean isAnimated) {
        for (int i = 0; i < cardViewList.size(); i++) {
            CardView cardView = cardViewList.get(i);
            CardInfo cardInfo = cardView.getCardInfo();
            if (isAnimated) {
                ObjectAnimator animatorX = ObjectAnimator.ofFloat(cardView, "x", cardInfo.getLastPositionX(), cardInfo.getCurrentPositionX());
                animatorX.setInterpolator(interpolator);
                animatorX.setDuration(500);
                animatorX.start();
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(cardView, "y", cardInfo.getLastPositionY(), cardInfo.getCurrentPositionY());
                animatorY.setInterpolator(interpolator);
                animatorY.setDuration(500);
                animatorY.start();
            } else {
                cardView.setX(cardInfo.getCurrentPositionX());
                cardView.setY(cardInfo.getCurrentPositionY());
            }
        }
    }

    /* private methods */

    private void addCardViewToRootView(View view) {
        CardView cardView = createCardView(view);
        this.addView(cardView);
        cardViewList.add(cardView);
    }

    private void addCardViewToRootView(View view, int position) {
        CardView cardView = createCardView(view);
        this.addView(cardView);
        cardViewList.add(position, cardView);
    }

    private CardView createCardView(View view) {
        CardView cardView = new CardView(getContext());
        cardView.setSwipeOrientationMode(SwipeGestureManager.OrientationMode.BOTH);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        cardView.setRotation(childRotation);
        cardView.addView(view);
        cardView.setCardTranslationListener(this);
        cardView.setCardSwipedListener(this);
        cardView.setCardPercentageChangeListener(this);
        return cardView;
    }

    private void inflateAttributes(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.CardsLayout);
            try {
                gravity = attributes.getInt(R.styleable.CardsLayout_cardsGravity, CENTER);
                childListOrientation = attributes.getInt(R.styleable.CardsLayout_childListOrientation, ORIENTATION_HORIZONTAL);
                childRotation = attributes.getInt(R.styleable.CardsLayout_childRotation, 0);
                childList_paddingLeft = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_childList_paddingLeft, 0);
                childList_paddingRight = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_childList_paddingRight, 0);
                childList_paddingTop = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_childList_paddingTop, 0);
                childList_paddingBottom = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_childList_paddingBottom, 0);
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

    private void setPositionsX() {
        int rootWidth = getWidth() - childList_paddingRight - childList_paddingLeft;
        int widthOfViews = calcWidthOfViews(cardViewList, 0);
        int offset = widthOfViews - rootWidth;
        int lastCardPositionX = 0;
        int widthOfItem = cardViewList.get(0).getWidth();
        int heightOfItem = cardViewList.get(0).getHeight();
        if (offset > 0) {
            offset /= getCardViewsCount() - 1;
            if ((gravity & LEFT) == LEFT) {
                lastCardPositionX = getRotationOffset(widthOfItem, heightOfItem);
            } else if ((gravity & RIGHT) == RIGHT) {
                if (childListOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (rootWidth - calcWidthOfViews(cardViewList, offset)) - getRotationOffset(widthOfItem, heightOfItem);
                } else {
                    lastCardPositionX = (rootWidth - widthOfItem) - getRotationOffset(widthOfItem, heightOfItem);
                }
            } else if ((gravity & CENTER_HORIZONTAL) == CENTER_HORIZONTAL ||
                    (gravity & CENTER) == CENTER) {
                if (childListOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (rootWidth - calcWidthOfViews(cardViewList, offset)) / 2;
                } else {
                    lastCardPositionX = rootWidth / 2 - widthOfItem / 2;
                }
            }
        } else {
            offset = 0;
            if ((gravity & LEFT) == LEFT) {
                lastCardPositionX = getRotationOffset(widthOfItem, heightOfItem);
            } else if ((gravity & RIGHT) == RIGHT) {
                if (childListOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (rootWidth - widthOfItem) - getRotationOffset(widthOfItem, heightOfItem);
                } else {
                    lastCardPositionX = (rootWidth - widthOfItem) - getRotationOffset(widthOfItem, heightOfItem);
                }
            } else if ((gravity & CENTER_HORIZONTAL) == CENTER_HORIZONTAL
                    || (gravity & CENTER) == CENTER) {
                if (childListOrientation == ORIENTATION_HORIZONTAL) {
                    lastCardPositionX = (rootWidth - widthOfViews) / 2;
                } else {
                    lastCardPositionX = rootWidth / 2 - widthOfItem / 2;
                }
            }
        }
        lastCardPositionX += childList_paddingLeft;
        for (CardView card : cardViewList) {
            if (card.getVisibility() == GONE) {
                continue;
            }
            card.getCardInfo().setCurrentPositionX(lastCardPositionX);
            if (childListOrientation == ORIENTATION_HORIZONTAL)
                lastCardPositionX += card.getWidth() - offset;
        }
    }

    private void setPositionsY() {
        int rootHeight = getHeight() - childList_paddingBottom - childList_paddingTop;
        int heightOfViews = calcHeightOfViews(cardViewList, 0);
        int offset = heightOfViews - rootHeight;
        int lastCardPositionY = 0;
        int widthOfItem = cardViewList.get(0).getWidth();
        int heightOfItem = cardViewList.get(0).getHeight();
        if (offset > 0) {
            offset /= getCardViewsCount() - 1;
            if ((gravity & TOP) == TOP) {
                lastCardPositionY = getRotationOffset(widthOfItem, heightOfItem);
            } else if ((gravity & BOTTOM) == BOTTOM) {
                if (childListOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (rootHeight - calcHeightOfViews(cardViewList, offset)) - getRotationOffset(widthOfItem, heightOfItem);
                } else {
                    lastCardPositionY = (rootHeight - heightOfItem) - getRotationOffset(widthOfItem, heightOfItem);
                }
            } else if ((gravity & CENTER_VERTICAL) == CENTER_VERTICAL ||
                    (gravity & CENTER) == CENTER) {
                if (childListOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (rootHeight - calcHeightOfViews(cardViewList, offset)) / 2;
                } else {
                    lastCardPositionY = rootHeight / 2 - heightOfItem / 2;
                }
            }
        } else {
            offset = 0;
            if ((gravity & TOP) == TOP) {
                lastCardPositionY = getRotationOffset(widthOfItem, heightOfItem);
            } else if ((gravity & BOTTOM) == BOTTOM) {
                if (childListOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (rootHeight - heightOfItem) - getRotationOffset(widthOfItem, heightOfItem);
                } else {
                    lastCardPositionY = (rootHeight - heightOfItem) - getRotationOffset(widthOfItem, heightOfItem);
                }
            } else if ((gravity & CENTER_VERTICAL) == CENTER_VERTICAL
                    || (gravity & CENTER) == CENTER) {
                if (childListOrientation == ORIENTATION_VERTICAL) {
                    lastCardPositionY = (rootHeight - heightOfViews) / 2;
                } else {
                    lastCardPositionY = rootHeight / 2 - heightOfItem / 2;
                }
            }
        }
        lastCardPositionY += childList_paddingTop;
        for (CardView card : cardViewList) {
            if (card.getVisibility() == GONE) {
                continue;
            }
            card.getCardInfo().setCurrentPositionY(lastCardPositionY);
            if (childListOrientation == ORIENTATION_VERTICAL)
                lastCardPositionY += card.getHeight() - offset;
        }
    }

    private int getCardViewsCount() {
        int count = 0;
        for (CardView card : cardViewList) {
            if (card.getVisibility() == GONE) {
                continue;
            }
            count ++;
        }
        return count;
    }

    private int getRotationOffset(int width, int height) {
        //todo fix
        float currentWidth = (width < height ? width : height) / 2f;
        float currentHeight = (width < height ? height : width) / 2f;
        double radiusMax = Math.sqrt(currentWidth * currentWidth + currentHeight * currentHeight);
        float corner1 = (float) Math.toDegrees(Math.sin(currentWidth / radiusMax));
        float corner2 = 90f - validateRotation() - corner1;
        long round = Math.round(radiusMax * Math.cos(Math.toRadians(corner2)) - width / 2f);
        return (int) round;
    }

    private float validateRotation() {
        int rotation = Math.abs(childRotation);
        while (rotation > 90) {
            rotation -= 90;
        }
        return rotation;
    }

    private int calcWidthOfViews(List<CardView> cards, int offset) {
        int widthOfViews = 0;
        for (CardView card : cards) {
            if (card.getVisibility() == GONE) {
                continue;
            }
            widthOfViews += card.getWidth() - offset;
        }
        widthOfViews += offset;
        return widthOfViews;
    }

    private int calcHeightOfViews(List<CardView> cards, int offset) {
        int heightViews = 0;
        for (CardView card : cards) {
            if (card.getVisibility() == GONE) {
                continue;
            }
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



    public interface OnConfigureList {
        void onConfiguration(List<CardView> cards);
    }
}
