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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

import static ua.jenshensoft.cardslayout.views.CardsLayout.CircleCenterLocation.BOTTOM;
import static ua.jenshensoft.cardslayout.views.CardsLayout.CircleCenterLocation.TOP;
import static ua.jenshensoft.cardslayout.views.CardsLayout.DistributeCradsBy.CIRCLE;
import static ua.jenshensoft.cardslayout.views.CardsLayout.DistributeCradsBy.LINE;


public abstract class CardsLayout<Entity> extends FrameLayout implements OnCardTranslationListener<Entity>, OnCardSwipedListener<Entity>, OnCardPercentageChangeListener<Entity> {

    public static final int EMPTY = -1;

    //animation params
    @Nullable
    protected Interpolator interpolator;
    protected OnCreateAnimatorAction defaultAnimatorAction;

    //property
    @LinearLayoutCompat.OrientationMode
    private int childListOrientation;

    private int childListPaddingLeft;
    private int childListPaddingRight;
    private int childListPaddingTop;
    private int childListPaddingBottom;
    private int childList_height;
    private int childList_width;
    private int childList_circleRadius;
    private int durationOfAnimation;
    //distribution
    @DistributeCradsBy
    private int childList_distributeCardsBy;
    @CircleCenterLocation
    private int childList_circleCenterLocation;
    private List<CardView<Entity>> cardViewList;
    private FlagManager gravityFlag;

    //listeners
    private OnCardSwipedListener<Entity> onCardSwipedListener;
    private OnCardPercentageChangeListener<Entity> onCardPercentageChangeListener;
    private OnCardTranslationListener<Entity> onCardTranslationListener;

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
        invalidateCardsPosition();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof CardView) {
            setUpCardView((CardView<Entity>) child);
        } else {
            ((ViewGroup) child.getParent()).removeView(child);
            addCardView(child);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof CardView) {
            CardView<Entity> cardView = (CardView<Entity>) child;
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

    
    /* listeners */

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
        return childListPaddingTop;
    }

    public void setChildListPaddingTop(int childListPaddingTop) {
        this.childListPaddingTop = childListPaddingTop;
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
        Config xConfig = getXConfiguration(cardViewList);
        Config yConfig = getYConfiguration(cardViewList);

        if (childList_distributeCardsBy == LINE) {
            setXForViews(xConfig.startCoordinates, xConfig.distanceBetweenViews);
            setYForViews(yConfig.startCoordinates, yConfig.distanceBetweenViews);
        } else {
            if (childList_circleRadius == EMPTY) {
                throw new RuntimeException("You need to set radius");
            }
            final float cardsLayoutLength;
            if (childListOrientation == LinearLayoutCompat.HORIZONTAL) {
                cardsLayoutLength = getRootWidth();
            } else {
                cardsLayoutLength = getRootHeight();
            }
            CardsCoordinatesProvider cardsCoordinatesProvider = new CardsCoordinatesProvider(
                    childList_circleRadius,
                    childList_circleCenterLocation,
                    cardViewList.size(),
                    getChildWidth(cardViewList),
                    getChildHeight(cardViewList),
                    childListOrientation,
                    cardsLayoutLength,
                    xConfig,
                    yConfig);
            final List<CardCoordinates> cardsCoordinates = cardsCoordinatesProvider.getCardsCoordinates();
            for (int i = 0; i < cardsCoordinates.size(); i++) {
                final CardView<Entity> cardView = cardViewList.get(i);
                final CardCoordinates cardCoordinates = cardsCoordinates.get(i);
                setXForView(cardView, cardCoordinates.getX());
                setYForView(cardView, cardCoordinates.getY());
                cardView.setRotation(cardCoordinates.getAngle());
            }
        }
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
        return new Config(startXPositionFromList, distanceBetweenViews, widthOfViews);
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
        return new Config(startYPositionFromList, distanceBetweenViews, heightOfViews);
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
            if (childListOrientation == LinearLayoutCompat.HORIZONTAL) {
                cardPositionX = (rootWidth - widthOfViews);
            } else {
                cardPositionX = (rootWidth - widthOfItem);
            }
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.CENTER_HORIZONTAL)
                || gravityFlag.containsFlag(FlagManager.Gravity.CENTER)) {
            if (childListOrientation == LinearLayoutCompat.HORIZONTAL) {
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
            if (childListOrientation == LinearLayoutCompat.VERTICAL) {
                cardPositionY = (rootHeight - heightOfViews);
            } else {
                cardPositionY = (rootHeight - heightOfItem);
            }
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.CENTER_VERTICAL)
                || gravityFlag.containsFlag(FlagManager.Gravity.CENTER)) {
            if (childListOrientation == LinearLayoutCompat.VERTICAL) {
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
        float x = cardPositionX;
        for (CardView<Entity> view : cardViewList) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            setXForView(view, x);
            if (childListOrientation == LinearLayout.HORIZONTAL)
                x += view.getMeasuredWidth() - distanceBetweenViews;
        }
    }

    protected void setYForViews(float cardPositionY, float distanceBetweenViews) {
        float y = cardPositionY;
        for (CardView<Entity> view : cardViewList) {
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            setYForView(view, y);
            if (childListOrientation == LinearLayout.VERTICAL)
                y += view.getMeasuredHeight() - distanceBetweenViews;
        }
    }

    protected void setXForView(CardView<Entity> cardView, float cardPositionX) {
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstPositionX(Math.round(cardPositionX));
    }

    protected void setYForView(CardView<Entity> cardView, float cardPositionY) {
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstPositionY(Math.round(cardPositionY));
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
                childListPaddingTop = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingTop, 0);
                childListPaddingBottom = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingBottom, 0);
                childList_height = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_height, EMPTY);
                childList_width = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_width, EMPTY);
                //distribution
                childList_distributeCardsBy = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_childList_distributeCardsBy, LINE);
                childList_circleRadius = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_circleRadius, EMPTY);
                childList_circleCenterLocation = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_childList_circleCenterLocation, BOTTOM);
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
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

    @IntDef({TOP, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CircleCenterLocation {
        int TOP = 0;
        int BOTTOM = 1;
    }

    @IntDef({LINE, CIRCLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DistributeCradsBy {
        int LINE = 0;
        int CIRCLE = 1;
    }

    protected static class Config {
        public final float distanceBetweenViews;
        public final float distanceForCards;
        public float startCoordinates;

        protected Config(float startCoordinates, float distanceBetweenViews, float distanceForCards) {
            this.startCoordinates = startCoordinates;
            this.distanceBetweenViews = distanceBetweenViews;
            this.distanceForCards = distanceForCards;
        }
    }

    public static class CardCoordinates {
        private final float x;
        private final float y;
        private final float angle;

        public CardCoordinates(float x, float y, float angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public float getAngle() {
            return angle;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public static class CardsCoordinatesProvider {

        private final float radius;
        private final int cardsCount;
        @LinearLayoutCompat.OrientationMode
        private final int orientation;
        private final float[] center;
        //angles
        private final float cardSectorAngle;
        //private final float cardAngle;
        private final float allCardsAngle;
        private final float startAngle;
        private final float endAngle;
        private final float cardWidth;

        public CardsCoordinatesProvider(float radius,
                                        @CircleCenterLocation int circleCenterLocation,
                                        int cardsCount,
                                        float cardWidth,
                                        float cardHeight,
                                        @LinearLayoutCompat.OrientationMode int orientation, float cardsLayoutLength,
                                        Config xConfig,
                                        Config yConfig) {
            this.cardWidth = cardWidth;
            if (cardsLayoutLength > radius * 2f) {
                radius = cardsLayoutLength / 2f;
                Log.e("CardsLayout", "Diameter can't be bigger then CardsLayoutLength");
            }
            this.radius = radius;

            this.cardsCount = cardsCount;
            this.orientation = orientation;
            this.center = getCoordinatesForCenter(orientation, circleCenterLocation, radius, xConfig, yConfig);

            //arcs
            //final float cardArc = calcCardArc(radius, cardWidth, cardHeight);
            final float maxArc = calcArcFromChord(radius, cardsLayoutLength);
            final float generalArc = maxArc; //calcGeneralArc(maxArc, cardArc, cardsCount);


            //angles
            final float generalAngle = calcAngleFromArc(generalArc, radius);
            //this.cardAngle = calcAngleFromArc(cardArc, radius);
            this.allCardsAngle = round(generalAngle, 6); //- cardAngle;
            this.cardSectorAngle = round(allCardsAngle / (cardsCount - 1), 6);
            this.startAngle = round(90f - (generalAngle / 2f), 6);
            this.endAngle = round(90f - ((generalAngle / 2f)), 6);// - cardAngle);
        }

        public List<CardCoordinates> getCardsCoordinates() {
            List<CardCoordinates> cardsCoordinates = new ArrayList<>();
            boolean isLeftArc = true;
            float fault = 0.0001f;
            float angle = 0f;
            for (int i = 0; i < cardsCount; i++) {
                if (i == 0) {// for first card
                    angle = startAngle;
                } else {
                    if (isLeftArc) { //left side of arc
                        if (angle + cardSectorAngle - fault >= startAngle && angle + cardSectorAngle - fault <= 90) {
                            angle += cardSectorAngle;
                        } else if (angle + cardSectorAngle > 90) {
                            isLeftArc = false;
                            angle += cardSectorAngle;
                            angle -= 90f;
                            angle = 90f - angle;
                        } else {
                            throw new RuntimeException("Something went wrong");
                        }
                    } else {
                        if (angle - cardSectorAngle + fault >= endAngle && angle - cardSectorAngle + fault <= 90) {
                            angle -= cardSectorAngle;
                        } else {
                            throw new RuntimeException("Something went wrong");
                        }
                    }
                }
                final float[] coordinatesForCard = getCoordinatesForCard(angle, isLeftArc);
                coordinatesForCard[0] -= cardWidth / 2f;
                cardsCoordinates.add(new CardCoordinates(coordinatesForCard[0], coordinatesForCard[1], validateAngle(angle, isLeftArc)));
            }
            return cardsCoordinates;
        }


        /* private methods */

        private float validateAngle(float angle, boolean isLeftArc) {
            if (isLeftArc) {
                return -90f + angle;
            } else {
                return 90f - angle;
            }
        }

        private float calcAngleFromArc(float arc, float radius) {
            return (float) Math.toDegrees(arc / radius);
        }

        private float[] getCoordinatesForCard(float angle, boolean isLeftArc) {
            float x;
            float y;

            if (orientation == LinearLayoutCompat.HORIZONTAL) {
                if (isLeftArc) {
                    x = (float) (center[0] - radius * Math.cos(Math.toRadians(angle)));
                } else {
                    x = (float) (center[0] + radius * Math.cos(Math.toRadians(angle)));
                }
                y = (float) (center[1] - radius * Math.sin(Math.toRadians(angle)));
                return new float[]{x, y};
            } else {
                if (isLeftArc) {
                    y = (float) (center[1] - radius * Math.sin(Math.toRadians(angle)));
                } else {
                    y = (float) (center[1] + radius * Math.sin(Math.toRadians(angle)));
                }
                x = (float) (center[0] + radius * Math.cos(Math.toRadians(angle)));
                return new float[]{x, y};
            }
        }

        protected float[] getCoordinatesForCenter(@LinearLayoutCompat.OrientationMode int orientation,
                                                  @CircleCenterLocation int circleCenterLocation,
                                                  float radius,
                                                  Config xConfig,
                                                  Config yConfig) {
            float x;
            float y;
            if (orientation == LinearLayoutCompat.HORIZONTAL) {
                x = xConfig.startCoordinates + (xConfig.distanceForCards / 2);
                if (circleCenterLocation == BOTTOM) {
                    y = yConfig.startCoordinates + radius;
                } else {
                    y = yConfig.startCoordinates - radius;
                }
                return new float[]{x, y};
            } else {
                y = yConfig.startCoordinates + (yConfig.distanceForCards / 2);
                if (circleCenterLocation == BOTTOM) {
                    x = xConfig.startCoordinates + radius;
                } else {
                    x = xConfig.startCoordinates - radius;
                }
                return new float[]{x, y};
            }
        }

        /*private float[] considerCenterCoordinates() {
            final Config xConfiguration = getXConfiguration(cardViewList);
            final Config yConfiguration = getYConfiguration(cardViewList);
            float halfOfLength;
            float x;
            float y;
            switch (orientation) {
                case LinearLayoutCompat.HORIZONTAL:
                    halfOfLength = xConfiguration.distanceForCards / 2;
                    x = xConfiguration.startCoordinates + halfOfLength;
                    y = Math.round(yConfiguration.startCoordinates + Math.sqrt(Math.pow(radius, 2f) - Math.pow(halfOfLength, 2f)));
                    return new float[]{x, y};
                case LinearLayoutCompat.VERTICAL:
                    halfOfLength = yConfiguration.distanceForCards / 2;
                    x = Math.round(xConfiguration.startCoordinates + Math.sqrt(Math.pow(radius, 2f) - Math.pow(halfOfLength, 2f)));
                    y = yConfiguration.startCoordinates + halfOfLength;
                    return new float[]{x, y};
                default:
                    throw new RuntimeException("Can't support this orientation " + orientation);
            }
        }*/

        private float calcGeneralArc(float maxArc, float cardArc, int cardsCount) {
            float cardsArc = cardsCount * cardArc;
            return maxArc < cardsArc ? maxArc : cardsArc;
        }

        private float calcCardArc(float radius, float cardWidth, float cardHeight) {
            if (radius >= cardWidth) {
                float RB = (float) Math.sqrt(Math.pow(cardWidth, 2) + Math.pow(radius, 2));
                float BN = RB - radius;
                float angleDegreesARB = (float) Math.toDegrees(Math.atan(cardWidth / radius));
                float angleDegreesANR = (180f - angleDegreesARB) / 2f;
                float angleDegreesBAN = 90f - angleDegreesANR;
                float angleDegreesANB = 180f - angleDegreesANR;
                float angleDegreesABN = 180f - angleDegreesANB - angleDegreesBAN;
                float angleDegreesNBM = 90f - angleDegreesABN;
                float BM = (float) (BN * Math.cos(Math.toRadians(angleDegreesNBM)));
                float NM = (float) (BN * Math.tan(Math.toRadians(angleDegreesNBM)));
                float angleDegreesNRM = (float) Math.toDegrees(Math.asin(NM / (radius * 2f)));
                float angleDegreesARM = angleDegreesNRM + angleDegreesARB;
                return calcArc(radius, angleDegreesARM);
            } else if (2 * radius >= cardHeight) {
                throw new RuntimeException();
            } else {
                return calcArc(radius * 2f, 180f);
            }
        }

        private float calcArc(float radius, float angleDegrees) {
            return (float) ((Math.PI * radius) / 180f * angleDegrees);
        }

        private float calcСhord(float radius, float angleDegrees) {
            return (float) (2f * radius * Math.sin(Math.toRadians(angleDegrees)));
        }

        private float calcArcFromChord(float radius, float сhordLenght) {
            float angleDegrees = (float) Math.toDegrees(Math.asin((сhordLenght / 2f) / radius)) * 2f;
            return calcArc(radius, angleDegrees);
        }

        private float round(float number, int scale) {
            int pow = 10;
            for (int i = 1; i < scale; i++)
                pow *= 10;
            float tmp = number * pow;
            return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
        }
    }
}
