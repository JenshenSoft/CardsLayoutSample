package ua.jenshensoft.cardslayout.views;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.util.AwesomeAnimation;
import ua.jenshensoft.cardslayout.util.DrawableUtils;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.util.SwipeGestureManager;

public abstract class CardsLayout<Entity> extends FrameLayout implements OnCardTranslationListener<Entity>, OnCardSwipedListener<Entity>, OnCardPercentageChangeListener<Entity> {

    public static final int EMPTY = -1;

    //animation params
    @Nullable
    protected Interpolator interpolator;
    protected OnCreateAnimatorAction defaultAnimatorAction;

    //property
    private int childListOrientation;
    private int childListPaddingLeft;
    private int childListPaddingRight;
    private int childLisPaddingTop;
    private int childListPaddingBottom;
    private int childList_height;
    private int childList_width;
    private int durationOfAnimation;
    private List<CardView<Entity>> cardViewList;
    private FlagManager gravityFlag;

    //ua.jenshensoft.cardslayout.listeners
    private OnCardSwipedListener<Entity> onCardSwipedListener;
    private OnCardPercentageChangeListener<Entity> onCardPercentageChangeListener;
    private OnCardTranslationListener<Entity> onCardTranslationListener;

    private boolean isCardPositionIsValided;

    public CardsLayout(Context context) {
        super(context);
        if (!isInEditMode()) {
            inflateAttributes(context, null);
        }
        init();
    }

    public CardsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
        init();
    }

    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
        init();
    }


    /* lifecycle */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isCardPositionIsValided) {
            return;
        }
        invalidateCardsPosition();
        isCardPositionIsValided = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof CardView) {
            setUpCardView((CardView) child);
        } else {
            ((ViewGroup) child.getParent()).removeView(child);
            addCardView(child);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof CardView) {
            CardView cardView = (CardView) child;
            cardView.setCardTranslationListener(null);
            cardView.setCardSwipedListener(null);
            cardView.setCardPercentageChangeListener(null, CardView.START_TO_CURRENT);
        }
    }


    /* public methods */

    public List<CardView<Entity>> getCardViews() {
        return cardViewList;
    }

    public void addCardView(View view, int position) {
        addCardViewToRootView(view, position);
    }

    public void addCardView(View view) {
        addCardViewToRootView(view);
    }

    public void removeCardView(int position) {
        CardView<Entity> cardView = findCardView(position);
        ViewParent parent = cardView.getParent();
        ((ViewGroup) parent).removeView(cardView);
        cardViewList.remove(cardView);
        for (CardView<Entity> view : cardViewList) {
            CardInfo<Entity> cardInfo = view.getCardInfo();
            int cardPosition = cardInfo.getCardPositionInLayout();
            if (cardPosition > position) {
                cardInfo.setCardPositionInLayout(cardPosition - 1);
            }
        }
        invalidateCardsPosition(true);
    }

    public void setIsTestMode() {
        if (childList_height != EMPTY) {
            childList_height += getContext().getResources().getDimensionPixelOffset(R.dimen.cardsLayout_test_card_offset);
        }

        if (childList_width != EMPTY) {
            childList_width += getContext().getResources().getDimensionPixelOffset(R.dimen.cardsLayout_test_card_offset);
        }
    }


    /* invalidate positions */

    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition() {
        invalidateCardsPosition(false, null, null);
    }

    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition(boolean withAnimation) {
        invalidateCardsPosition(withAnimation, null, null);
    }

    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition(boolean withAnimation, @NonNull OnCreateAnimatorAction onCreateAnimatorAction) {
        invalidateCardsPosition(withAnimation, onCreateAnimatorAction, null);
    }

    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition(boolean withAnimation, @NonNull AnimatorListenerAdapter animatorListenerAdapter) {
        invalidateCardsPosition(withAnimation, null, animatorListenerAdapter);
    }

    @CallSuper
    public void invalidateCardsPosition(boolean withAnimation, @NonNull OnCreateAnimatorAction onCreateAnimatorAction, @NonNull AnimatorListenerAdapter animatorListenerAdapter) {
        setViewsCoordinatesToStartPosition();
        moveViewsToStartPosition(withAnimation, onCreateAnimatorAction, animatorListenerAdapter);
    }

    
    /* ua.jenshensoft.cardslayout.listeners */

    public void setCardTranslationListener(OnCardTranslationListener<Entity> cardTranslationListener) {
        this.onCardTranslationListener = cardTranslationListener;
    }

    public void setCardPercentageChangeListener(OnCardPercentageChangeListener<Entity> onCardPercentageChangeListener) {
        this.onCardPercentageChangeListener = onCardPercentageChangeListener;
    }

    public void setOnCardSwipedListener(OnCardSwipedListener<Entity> onCardSwipedListener) {
        this.onCardSwipedListener = onCardSwipedListener;
    }
    
    
    /* property */

    @LinearLayoutCompat.OrientationMode
    public int getChildListOrientation() {
        return childListOrientation;
    }

    public void setChildListOrientation(@LinearLayoutCompat.OrientationMode int childListOrientation) {
        this.childListOrientation = childListOrientation;
    }

    public FlagManager getGravityFlag() {
        return gravityFlag;
    }

    public int getChildListPaddingBottom() {
        return childListPaddingBottom;
    }

    public void setChildListPaddingBottom(int childListPaddingBottom) {
        this.childListPaddingBottom = childListPaddingBottom;
    }

    public int getChildListPaddingLeft() {
        return childListPaddingLeft;
    }

    public void setChildListPaddingLeft(int childListPaddingLeft) {
        this.childListPaddingLeft = childListPaddingLeft;
    }

    public int getChildListPaddingRight() {
        return childListPaddingRight;
    }

    public void setChildListPaddingRight(int childListPaddingRight) {
        this.childListPaddingRight = childListPaddingRight;
    }

    public int getChildListPaddingTop() {
        return childLisPaddingTop;
    }

    public void setChildListPaddingTop(int childListPaddingTop) {
        this.childLisPaddingTop = childListPaddingTop;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setEnabledExceptViewsWithPositions(enabled, EMPTY);
    }

    public void setEnabledExceptViewsWithPositions(boolean state, @Nullable int... position) {
        setEnabledExceptViewsWithPositions(state, null, position);
    }

    public void setEnabledExceptViewsWithPositionsWithFilter(boolean enabled, @Nullable ColorFilter colorFilter, @Nullable int... position) {
        super.setEnabled(enabled);
        setEnabledExceptViewsWithPositions(enabled, colorFilter, position);
    }

    /* animation property */

    public int getDurationOfAnimation() {
        return durationOfAnimation;
    }

    public void setDurationOfAnimation(int durationOfAnimation) {
        this.durationOfAnimation = durationOfAnimation;
    }

    public void setInterpolator(@NonNull Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public OnCreateAnimatorAction getDefaultCreateAnimatorAction() {
        return defaultAnimatorAction;
    }


    /* callbacks */

    @Override
    public void onCardTranslation(float positionX, float positionY, CardInfo<Entity> cardInfo, boolean isTouched) {
        if (onCardTranslationListener != null)
            onCardTranslationListener.onCardTranslation(positionX, positionY, cardInfo, isTouched);
    }

    @Override
    public void onCardSwiped(CardInfo<Entity> cardInfo) {
        if (onCardSwipedListener != null)
            onCardSwipedListener.onCardSwiped(cardInfo);
    }

    @Override
    public void onPercentageChanged(float percentageX, float percentageY, CardInfo<Entity> cardInfo, boolean isTouched) {
        if (onCardPercentageChangeListener != null) {
            onCardPercentageChangeListener.onPercentageChanged(percentageX, percentageY, cardInfo, isTouched);
        }
    }


    /* protected methods */

    protected void setViewsCoordinatesToStartPosition() {
        Config config = getXConfiguration(cardViewList);
        setXForViews(config.startCoordinates, config.distanceBetweenViews);
        config = getYConfiguration(cardViewList);
        setYForViews(config.startCoordinates, config.distanceBetweenViews);
    }

    protected void moveViewsToStartPosition(boolean withAnimation, @Nullable OnCreateAnimatorAction animationCreateAction, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        final List<Animator> animators = new ArrayList<>();
        for (int i = 0; i < cardViewList.size(); i++) {
            CardView<Entity> cardView = cardViewList.get(i);
            CardInfo<Entity> cardInfo = cardView.getCardInfo();
            if (withAnimation) {
                final Animator animator;
                if (animationCreateAction != null) {
                    animator = animationCreateAction.createAnimation(cardView);
                    animator.start();
                } else {
                    animator = this.defaultAnimatorAction.createAnimation(cardView);
                }
                animators.add(animator);
            } else {
                cardView.setX(cardInfo.getFirstPositionX());
                cardView.setY(cardInfo.getFirstPositionY());
            }
        }
        if (!animators.isEmpty()) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            if (animatorListenerAdapter != null) {
                animatorSet.addListener(animatorListenerAdapter);
            }
            animatorSet.start();
        }
    }

    protected <T extends View> Config getXConfiguration(List<T> views) {
        float rootWidth = getRootWidth();
        float widthOfViews = getWidthOfViews(views, 0);
        float difference = widthOfViews - rootWidth;
        float distanceBetweenViews = getDistanceBetweenViews(difference, views);

        if (difference > 0) {
            widthOfViews = getWidthOfViews(views, distanceBetweenViews);
        }
        float startXPositionFromList = getStartXPositionForList(getChildWidth(views), widthOfViews, rootWidth);
        return new Config(startXPositionFromList, distanceBetweenViews);
    }

    protected <T extends View> Config getYConfiguration(List<T> views) {
        float rootHeight = getRootHeight();
        float heightOfViews = getHeightOfViews(views, 0);
        float difference = heightOfViews - rootHeight;
        float distanceBetweenViews = getDistanceBetweenViews(difference, views);

        if (difference > 0) {
            heightOfViews = getHeightOfViews(views, distanceBetweenViews);
        }
        float startYPositionFromList = getStartYPositionForList(getChildHeight(views), heightOfViews, rootHeight);
        return new Config(startYPositionFromList, distanceBetweenViews);
    }

    protected <T extends View> float getDistanceBetweenViews(float difference, List<T> views) {
        if (difference > 0) {
            return difference / (getCardViewsCount(views) - 1f);
        } else {
            return 0;
        }
    }

    protected float getRootWidth() {
        int widthLayout = childList_width == EMPTY ? getMeasuredWidth() : childList_width;
        return widthLayout - getChildListPaddingRight() - getChildListPaddingLeft();
    }

    protected float getRootHeight() {
        int heightLayout = childList_height == EMPTY ? getMeasuredHeight() : childList_height;
        return heightLayout - getChildListPaddingBottom() - getChildListPaddingTop();
    }

    protected float getStartXPositionForList(float widthOfItem, float widthOfViews, float rootWidth) {
        float cardPositionX = 0;
        if (gravityFlag.containsFlag(FlagManager.Gravity.LEFT)) {
            cardPositionX = 0;
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.RIGHT)) {
            if (childListOrientation == LinearLayout.HORIZONTAL) {
                cardPositionX = (rootWidth - widthOfViews);
            } else {
                cardPositionX = (rootWidth - widthOfItem);
            }
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.CENTER_HORIZONTAL)
                || gravityFlag.containsFlag(FlagManager.Gravity.CENTER)) {
            if (childListOrientation == LinearLayout.HORIZONTAL) {
                cardPositionX = (rootWidth - widthOfViews) / 2f;
            } else {
                cardPositionX = rootWidth / 2f - widthOfItem / 2f;
            }
        }

        cardPositionX += getChildListPaddingLeft();
        if (childList_width != EMPTY) {
            cardPositionX += (getMeasuredWidth() - childList_width) / 2f;
        }
        return cardPositionX;
    }

    protected float getStartYPositionForList(float heightOfItem, float heightOfViews, float rootHeight) {
        float cardPositionY = 0;
        if (gravityFlag.containsFlag(FlagManager.Gravity.TOP)) {
            cardPositionY = 0;
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.BOTTOM)) {
            if (childListOrientation == LinearLayout.VERTICAL) {
                cardPositionY = (rootHeight - heightOfViews);
            } else {
                cardPositionY = (rootHeight - heightOfItem);
            }
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.CENTER_VERTICAL)
                || gravityFlag.containsFlag(FlagManager.Gravity.CENTER)) {
            if (childListOrientation == LinearLayout.VERTICAL) {
                cardPositionY = (rootHeight - heightOfViews) / 2f;
            } else {
                cardPositionY = rootHeight / 2f - heightOfItem / 2f;
            }
        }

        cardPositionY += getChildListPaddingTop();
        if (childList_height != EMPTY) {
            cardPositionY += (getMeasuredHeight() - childList_height) / 2f;
        }
        return cardPositionY;
    }

    protected <T extends View> float getChildHeight(@NonNull List<T> views) {
        float height = 0;
        if (views.isEmpty())
            return height;
        for (T view : views) {
            float currentHeight = view.getMeasuredHeight();
            if (currentHeight > height) {
                height = currentHeight;
            }
        }
        return height;
    }

    protected <T extends View> float getChildWidth(@NonNull List<T> views) {
        float width = 0;
        if (views.isEmpty())
            return width;
        for (T view : views) {
            float currentWidth = view.getMeasuredWidth();
            if (currentWidth > width) {
                width = currentWidth;
            }
        }
        return width;
    }

    protected <T extends View> float getWidthOfViews(@NonNull List<T> views, float offset) {
        float widthOfViews = 0;
        for (T view : views) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            widthOfViews += view.getMeasuredWidth() - offset;
        }
        widthOfViews += offset;
        return widthOfViews;
    }

    protected <T extends View> float getHeightOfViews(@NonNull List<T> views, float offset) {
        float heightViews = 0;
        for (T view : views) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            heightViews += view.getMeasuredHeight() - offset;
        }
        heightViews += offset;
        return heightViews;
    }

    protected void setXForViews(float cardPositionX, float distanceBetweenViews) {
        for (CardView<Entity> view : cardViewList) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            CardInfo<Entity> cardInfo = view.getCardInfo();
            cardInfo.setFirstPositionX(Math.round(cardPositionX));
            if (childListOrientation == LinearLayout.HORIZONTAL)
                cardPositionX += view.getMeasuredWidth() - distanceBetweenViews;
        }
    }

    protected void setYForViews(float cardPositionY, float distanceBetweenViews) {
        for (CardView<Entity> view : cardViewList) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            CardInfo<Entity> cardInfo = view.getCardInfo();
            cardInfo.setFirstPositionY(Math.round(cardPositionY));
            if (childListOrientation == LinearLayout.VERTICAL)
                cardPositionY += view.getMeasuredHeight() - distanceBetweenViews;
        }
    }


    /* private methods */

    @SuppressWarnings("WrongConstant")
    private void inflateAttributes(Context context, @Nullable AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.CardsLayout_Params);
            try {
                gravityFlag = new FlagManager(attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_cardsGravity, FlagManager.Gravity.CENTER));
                childListOrientation = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_childList_orientation, LinearLayout.HORIZONTAL);
                durationOfAnimation = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_animationDuration, 500);
                childListPaddingLeft = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingLeft, 0);
                childListPaddingRight = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingRight, 0);
                childLisPaddingTop = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingTop, 0);
                childListPaddingBottom = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingBottom, 0);
                childList_height = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_height, EMPTY);
                childList_width = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_width, EMPTY);
            } finally {
                attributes.recycle();
            }
        }
    }

    private void init() {
        cardViewList = new ArrayList<>();
        defaultAnimatorAction = cardView -> {
            AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(cardView)
                    .setX(AwesomeAnimation.CoordinationMode.COORDINATES, cardView.getX(), cardView.getCardInfo().getFirstPositionX())
                    .setY(AwesomeAnimation.CoordinationMode.COORDINATES, cardView.getY(), cardView.getCardInfo().getFirstPositionY())
                    .setDuration(durationOfAnimation);
            if (interpolator != null)
                awesomeAnimation.setInterpolator(interpolator);
            return awesomeAnimation.build().getAnimatorSet();
        };
    }


    /* card view methods */

    private CardView<Entity> createCardView(View view) {
        CardView<Entity> cardView = new CardView<>(getContext());
        FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        cardView.addView(view);
        return cardView;
    }

    private void setUpCardView(CardView<Entity> cardView) {
        cardView.setSwipeOrientationMode(SwipeGestureManager.OrientationMode.BOTH);
        cardView.setCardTranslationListener(this);
        cardView.setCardSwipedListener(this);
        cardView.setCardPercentageChangeListener(this, CardView.START_TO_CURRENT);
    }

    private void addCardViewToRootView(View view) {
        CardView<Entity> cardView = createCardView(view);
        cardView.setCardInfo(new CardInfo<>(cardViewList.size()));
        this.addView(cardView);
        cardViewList.add(cardView);
    }

    private void addCardViewToRootView(View view, int position) {
        CardView<Entity> cardView = createCardView(view);
        cardView.setCardInfo(new CardInfo<>(cardViewList.size()));
        this.addView(cardView);
        cardViewList.add(position, cardView);
    }

    private CardView<Entity> findCardView(int position) {
        for (CardView<Entity> cardView : cardViewList) {
            if (cardView.getCardInfo().getCardPositionInLayout() == position) {
                return cardView;
            }
        }
        throw new RuntimeException("Can't find view");
    }

    private <T extends View> int getCardViewsCount(@NonNull List<T> views) {
        int count = 0;
        for (T view : views) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            count++;
        }
        return count;
    }

    private void setEnabledExceptViewsWithPositions(boolean state, @Nullable ColorFilter colorFilter, @Nullable int... positions) {
        List<Integer> positionsList = null;
        if (positions != null) {
            positionsList = new ArrayList<>();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < positions.length; i++) {
                positionsList.add(positions[i]);
            }
        }
        for (CardView cardView : cardViewList) {
            if (state) {
                if (!cardView.isEnabled()) {
                    DrawableUtils.setColorFilter(cardView, null);
                }
                cardView.setEnabled(true);
            } else {
                if (cardView.getCardInfo() == null || (positionsList != null && !positionsList.contains(cardView.getCardInfo().getCardPositionInLayout()))) {
                    if (cardView.isEnabled()) {
                        DrawableUtils.setColorFilter(cardView, colorFilter);
                    }
                    cardView.setEnabled(false);
                }
            }
        }
    }


    /* inner types */

    @FunctionalInterface
    public interface OnCreateAnimatorAction {
        Animator createAnimation(CardView cardView);
    }

    protected static class Config {
        public float startCoordinates;
        public float distanceBetweenViews;

        public Config(float startCoordinates, float distanceBetweenViews) {
            this.startCoordinates = startCoordinates;
            this.distanceBetweenViews = distanceBetweenViews;
        }
    }
}
