package ua.jenshensoft.cardslayout.views.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
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

import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.provider.CardCoordinates;
import ua.jenshensoft.cardslayout.provider.CardsCoordinatesProvider;
import ua.jenshensoft.cardslayout.util.DrawableUtils;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.util.SwipeGestureManager;
import ua.jenshensoft.cardslayout.views.ViewMeasureConfig;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.card.CardBoxView;
import ua.jenshensoft.cardslayout.views.card.CardView;

import static ua.jenshensoft.cardslayout.views.layout.CardsLayout.CardsDirection.LEFT_TO_RIGHT;
import static ua.jenshensoft.cardslayout.views.layout.CardsLayout.CardsDirection.RIGHT_TO_LEFT;
import static ua.jenshensoft.cardslayout.views.layout.CardsLayout.CircleCenterLocation.BOTTOM;
import static ua.jenshensoft.cardslayout.views.layout.CardsLayout.CircleCenterLocation.TOP;
import static ua.jenshensoft.cardslayout.views.layout.CardsLayout.DistributeCardsBy.CIRCLE;
import static ua.jenshensoft.cardslayout.views.layout.CardsLayout.DistributeCardsBy.LINE;


public abstract class CardsLayout<Entity> extends FrameLayout
        implements
        OnCardTranslationListener<Entity>,
        OnCardSwipedListener<Entity>,
        OnCardPercentageChangeListener<Entity> {

    public static final int EMPTY = -1;

    //animation params
    @Nullable
    protected Interpolator interpolator;
    protected OnCreateAnimatorAction<Entity> defaultAnimatorAction;

    //property
    @LinearLayoutCompat.OrientationMode
    private int childListOrientation;
    @CardsDirection
    private int cardsLayout_cardsDirection;
    private int childListPaddingLeft;
    private int childListPaddingRight;
    private int childListPaddingTop;
    private int childListPaddingBottom;
    private int childList_height;
    private int childList_width;
    private int childList_circleRadius;
    private int durationOfAnimation;
    //distribution
    @DistributeCardsBy
    private int childList_distributeCardsBy;
    @CircleCenterLocation
    private int childList_circleCenterLocation;
    //card size
    private boolean fixedCardMeasure;
    private int cardWidth;
    private int cardHeight;
    private List<Card<Entity>> cards;
    private FlagManager gravityFlag;
    @Nullable
    private ColorFilter colorFilter;

    //listeners
    private OnCardSwipedListener<Entity> onCardSwipedListener;
    private OnCardPercentageChangeListener<Entity> onCardPercentageChangeListener;
    private OnCardTranslationListener<Entity> onCardTranslationListener;

    private boolean animateOnMeasure;
    private ViewMeasureConfig viewMeasureConfig;

    @Nullable
    private Animator animator;

    public CardsLayout(Context context) {
        super(context);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, null);
        }
    }

    public CardsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }


    /* lifecycle */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (viewMeasureConfig.needUpdateView()) {
            invalidateCardsPosition(animateOnMeasure);
            animateOnMeasure = false;
        }
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof Card) {
            setUpCard((View & Card<Entity>) child);
        } else {
            ((ViewGroup) child.getParent()).removeView(child);
            addCardBoxView(child);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof Card) {
            Card<Entity> cardView = (Card<Entity>) child;
            cardView.setCardTranslationListener(null);
            cardView.setCardSwipedListener(null);
            cardView.setCardPercentageChangeListener(null, CardBoxView.START_TO_CURRENT);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimator();
    }

    /* public methods */

    @SuppressWarnings("unchecked")
    public <CV extends View & Card<Entity>> List<CV> getCardViews() {
        List<CV> cardViews = new ArrayList<>();
        for (Card<Entity> card : cards) {
            cardViews.add((CV) card);
        }
        return cardViews;
    }

    public List<Card<Entity>> getCards() {
        return cards;
    }

    public void addCardView(CardView<Entity> cardView, int position) {
        cardView.setCardInfo(new CardInfo<>(position));
        this.addView(cardView, position);
    }

    public void addCardView(CardView<Entity> cardView) {
        cardView.setCardInfo(new CardInfo<>(cards.size()));
        this.addView(cardView);
    }

    public void addCardBoxView(View view, int position) {
        CardBoxView<Entity> cardView = createCardBoxView(view);
        cardView.setCardInfo(new CardInfo<>(position));
        this.addView(cardView, position);
    }

    public void addCardBoxView(View view) {
        CardBoxView<Entity> cardView = createCardBoxView(view);
        cardView.setCardInfo(new CardInfo<>(cards.size()));
        this.addView(cardView);
    }

    public <CV extends View & Card<Entity>> void removeCardView(int position) {
        CV cardView = findCardView(position);
        ViewParent parent = cardView.getParent();
        ((ViewGroup) parent).removeView(cardView);
        cards.remove(cardView);
        for (Card<Entity> card : cards) {
            CardInfo<Entity> cardInfo = card.getCardInfo();
            int cardPosition = cardInfo.getCardPositionInLayout();
            if (cardPosition > position) {
                cardInfo.setCardPositionInLayout(cardPosition - 1);
            }
        }
        animateOnMeasure = true;
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

    @CallSuper
    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition() {
        invalidateCardsPosition(false, null, null);
    }

    @CallSuper
    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition(boolean withAnimation) {
        invalidateCardsPosition(withAnimation, null, null);
    }

    @CallSuper
    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition(boolean withAnimation,
                                        @NonNull OnCreateAnimatorAction<Entity> onCreateAnimatorAction) {
        invalidateCardsPosition(withAnimation, onCreateAnimatorAction, null);
    }

    @CallSuper
    @SuppressWarnings("ConstantConditions")
    public void invalidateCardsPosition(boolean withAnimation,
                                        @NonNull AnimatorListenerAdapter animatorListenerAdapter) {
        invalidateCardsPosition(withAnimation, null, animatorListenerAdapter);
    }

    @CallSuper
    public void invalidateCardsPosition(boolean withAnimation,
                                        @Nullable OnCreateAnimatorAction<Entity> onCreateAnimatorAction,
                                        @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
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
        setEnabledExceptViewsWithPositions(enabled, colorFilter);
    }

    public void setEnabled(boolean enabled,
                           @Nullable int... ignoredPositions) {
        super.setEnabled(enabled);
        setEnabledExceptViewsWithPositions(enabled, colorFilter, ignoredPositions);
    }

    public void setEnabled(boolean enabled,
                           @Nullable ColorFilter colorFilter,
                           @Nullable int... ignoredPositions) {
        super.setEnabled(enabled);
        setEnabledExceptViewsWithPositions(enabled, colorFilter, ignoredPositions);
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
    }

    /* animation property */

    public int getDurationOfAnimation() {
        return durationOfAnimation;
    }

    public void setDurationOfAnimation(int durationOfAnimation) {
        this.durationOfAnimation = durationOfAnimation;
    }

    @Nullable
    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(@NonNull Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public OnCreateAnimatorAction<Entity> getDefaultCreateAnimatorAction() {
        return defaultAnimatorAction;
    }

    /* children size property */

    public boolean isFixedCardMeasure() {
        return fixedCardMeasure;
    }

    public void setFixedCardMeasure(boolean fixedCardMeasure) {
        this.fixedCardMeasure = fixedCardMeasure;
    }

    public int getCardWidth() {
        return cardWidth;
    }

    public void setCardWidth(int cardWidth) {
        this.cardWidth = cardWidth;
    }

    public int getCardHeight() {
        return cardHeight;
    }

    public void setCardHeight(int cardHeight) {
        this.cardHeight = cardHeight;
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

    protected <CV extends View & Card<Entity>> void setViewsCoordinatesToStartPosition() {
        List<CV> cards = getValidatedCardViews();
        final Config xConfig = getXConfiguration(cards);
        final Config yConfig = getYConfiguration(cards);

        if (childList_distributeCardsBy == LINE) {
            setXForViews(cards, xConfig.getStartCoordinates(), xConfig.getDistanceBetweenViews());
            setYForViews(cards, yConfig.getStartCoordinates(), yConfig.getDistanceBetweenViews());
            setRotationForViews(cards);
        } else {
            if (childList_circleRadius == EMPTY) {
                throw new RuntimeException("You need to set radius");
            }
            final float cardsLayoutLength;
            if (childListOrientation == LinearLayoutCompat.HORIZONTAL) {
                cardsLayoutLength = xConfig.getDistanceForCards();
            } else {
                cardsLayoutLength = yConfig.getDistanceForCards();
            }
            final CardsCoordinatesProvider cardsCoordinatesProvider = new CardsCoordinatesProvider(
                    childListOrientation,
                    childList_circleCenterLocation,
                    getCardViewsCount(cards),
                    childList_circleRadius,
                    getChildrenWidth(cards),
                    getChildrenHeight(cards),
                    cardsLayoutLength,
                    gravityFlag,
                    xConfig,
                    yConfig);
            final List<CardCoordinates> cardsCoordinates = cardsCoordinatesProvider.getCardsCoordinates();
            for (int i = 0; i < cardsCoordinates.size(); i++) {
                final Card<Entity> card = cards.get(i);
                final CardCoordinates cardCoordinates = cardsCoordinates.get(i);
                setXForCard(card, cardCoordinates.getX());
                setYForCard(card, cardCoordinates.getY());
                setRotationForCard(card, cardCoordinates.getAngle());
            }
        }
    }

    protected <CV extends View & Card<Entity>> void moveViewsToStartPosition(boolean withAnimation,
                                                                             @Nullable OnCreateAnimatorAction<Entity> animationCreateAction,
                                                                             @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        List<CV> cards = getValidatedCardViews();
        final List<Animator> animators = new ArrayList<>();
        for (CV card : cards) {
            CardInfo<Entity> cardInfo = card.getCardInfo();
            if (withAnimation) {
                final Animator animator;
                if (animationCreateAction != null) {
                    animator = animationCreateAction.createAnimation(card);
                } else {
                    animator = this.defaultAnimatorAction.createAnimation(card);
                }
                animators.add(animator);
            } else {
                card.setX(cardInfo.getFirstPositionX());
                card.setY(cardInfo.getFirstPositionY());
                card.setRotation(cardInfo.getFirstRotation());
            }
        }

        if (!animators.isEmpty()) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    CardsLayout.this.animator = null;
                }
            });
            if (animatorListenerAdapter != null) {
                animatorSet.addListener(animatorListenerAdapter);
            }

            clearAnimator();
            CardsLayout.this.animator = animatorSet;
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
        float startXPositionFromList = getStartXPositionForList(getChildrenWidth(views), widthOfViews, rootWidth);
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
        float startYPositionFromList = getStartYPositionForList(getChildrenHeight(views), heightOfViews, rootHeight);
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
                cardPositionX = rootWidth - widthOfViews;
            } else {
                cardPositionX = rootWidth - widthOfItem;
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
                cardPositionY = rootHeight - heightOfViews;
            } else {
                cardPositionY = rootHeight - heightOfItem;
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

    protected <T extends View> float getChildrenWidth(@NonNull List<T> views) {
        float width = 0;
        if (views.isEmpty())
            return width;
        for (T view : views) {
            float currentWidth = getChildWidth(view);
            if (currentWidth > width) {
                width = currentWidth;
            }
        }
        return width;
    }

    protected <T extends View> float getChildrenHeight(@NonNull List<T> views) {
        float height = 0;
        if (views.isEmpty())
            return height;
        for (T view : views) {
            float currentHeight = getChildHeight(view);
            if (currentHeight > height) {
                height = currentHeight;
            }
        }
        return height;
    }

    protected int getChildWidth(View view) {
        if (fixedCardMeasure) {
            if (cardWidth == -1) {
                throw new RuntimeException("You should set the \"child width\" attr");
            }
            return cardWidth;
        } else {
            return view.getMeasuredWidth();
        }
    }

    protected int getChildHeight(View view) {
        if (fixedCardMeasure) {
            if (cardHeight == -1) {
                throw new RuntimeException("You should set the \"child height\" attr");
            }
            return cardHeight;
        } else {
            return view.getMeasuredHeight();
        }
    }

    protected <T extends View> float getWidthOfViews(@NonNull List<T> views, float offset) {
        float widthOfViews = 0;
        for (T view : views) {
            if (shouldPassView(view)) {
                continue;
            }
            widthOfViews += getChildWidth(view) - offset;
        }
        widthOfViews += offset;
        return widthOfViews;
    }

    protected <T extends View> float getHeightOfViews(@NonNull List<T> views, float offset) {
        float heightViews = 0;
        for (T view : views) {
            if (shouldPassView(view)) {
                continue;
            }
            heightViews += getChildHeight(view) - offset;
        }
        heightViews += offset;
        return heightViews;
    }

    protected <CV extends View & Card<Entity>> void setXForViews(@NonNull List<CV> views, float cardPositionX, float distanceBetweenViews) {
        float x = cardPositionX;
        for (CV view : views) {
            if (shouldPassCard(view)) {
                continue;
            }
            setXForCard(view, x);
            if (childListOrientation == LinearLayout.HORIZONTAL)
                x += getChildWidth(view) - distanceBetweenViews;
        }
    }

    protected <CV extends View & Card<Entity>> void setYForViews(@NonNull List<CV> views, float cardPositionY, float distanceBetweenViews) {
        float y = cardPositionY;
        for (CV view : views) {
            if (shouldPassCard(view)) {
                continue;
            }
            setYForCard(view, y);
            if (childListOrientation == LinearLayout.VERTICAL)
                y += getChildHeight(view) - distanceBetweenViews;
        }
    }

    protected <CV extends View & Card<Entity>> void setRotationForViews(@NonNull List<CV> views) {
        for (CV view : views) {
            if (shouldPassCard(view)) {
                continue;
            }
            setRotationForCard(view, 0);
        }
    }

    protected void setXForCard(Card<Entity> cardView, float cardPositionX) {
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstPositionX(Math.round(cardPositionX));
    }

    protected void setYForCard(Card<Entity> cardView, float cardPositionY) {
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstPositionY(Math.round(cardPositionY));
    }

    protected void setRotationForCard(Card<Entity> cardView, float rotation) {
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstRotation(Math.round(rotation));
    }


    /* private methods */

    private void init() {
        //attr
        childListOrientation = LinearLayoutCompat.HORIZONTAL;
        cardsLayout_cardsDirection = CardsDirection.LEFT_TO_RIGHT;
        childListPaddingLeft = 0;
        childListPaddingRight = 0;
        childListPaddingTop = 0;
        childListPaddingBottom = 0;
        childList_height = EMPTY;
        childList_width = EMPTY;
        childList_circleRadius = EMPTY;
        durationOfAnimation = 500;
        //distribution
        childList_distributeCardsBy = LINE;
        childList_circleCenterLocation = BOTTOM;
        gravityFlag = new FlagManager(FlagManager.Gravity.BOTTOM);
        cards = new ArrayList<>();
        defaultAnimatorAction = new OnCreateAnimatorAction<Entity>() {
            @Override
            public <C extends View & Card<Entity>> Animator createAnimation(C cardView) {
                AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(cardView)
                        .setX(AwesomeAnimation.CoordinationMode.COORDINATES, cardView.getCardInfo().getCurrentPositionX(), cardView.getCardInfo().getFirstPositionX())
                        .setY(AwesomeAnimation.CoordinationMode.COORDINATES, cardView.getCardInfo().getCurrentPositionY(), cardView.getCardInfo().getFirstPositionY())
                        .setRotation(cardView.getRotation(), cardView.getCardInfo().getFirstRotation())
                        .setDuration(durationOfAnimation);
                if (interpolator != null)
                    awesomeAnimation.setInterpolator(interpolator);
                return awesomeAnimation.build().getAnimatorSet();
            }
        };
        viewMeasureConfig = new ViewMeasureConfig(this);
    }

    @SuppressWarnings("WrongConstant")
    private void inflateAttributes(Context context, @Nullable AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.CardsLayout_Params);
            try {
                cardsLayout_cardsDirection = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_cardsDirection, cardsLayout_cardsDirection);
                gravityFlag = new FlagManager(attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_cardsGravity, FlagManager.Gravity.CENTER));
                childListOrientation = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_childList_orientation, childListOrientation);
                durationOfAnimation = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_animationDuration, durationOfAnimation);
                childListPaddingLeft = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingLeft, childListPaddingLeft);
                childListPaddingRight = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingRight, childListPaddingRight);
                childListPaddingTop = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingTop, childListPaddingTop);
                childListPaddingBottom = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_paddingBottom, childListPaddingBottom);
                childList_height = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_height, childList_height);
                childList_width = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_width, childList_width);
                final int color = attributes.getColor(R.styleable.CardsLayout_Params_cardsLayout_tintColor, -1);
                if (color != -1) {
                    colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
                }

                //distribution
                childList_distributeCardsBy = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_childList_distributeCardsBy, childList_distributeCardsBy);
                childList_circleRadius = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_childList_circleRadius, childList_circleRadius);
                childList_circleCenterLocation = attributes.getInt(R.styleable.CardsLayout_Params_cardsLayout_childList_circleCenterLocation, childList_circleCenterLocation);

                //card measure
                fixedCardMeasure = attributes.getBoolean(R.styleable.CardsLayout_Params_cardsLayout_fixedCardMeasure, false);
                cardWidth = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_cardWidth, -1);
                cardHeight = attributes.getDimensionPixelOffset(R.styleable.CardsLayout_Params_cardsLayout_cardHeight, -1);
            } finally {
                attributes.recycle();
            }
        }
    }


    /* card view methods */

    private CardBoxView<Entity> createCardBoxView(View view) {
        CardBoxView<Entity> cardView = new CardBoxView<>(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        cardView.addView(view);
        return cardView;
    }

    private <CV extends View & Card<Entity>> void setUpCard(CV card) {
        if (card.getCardInfo() == null) {
            int index;
            if (cardsLayout_cardsDirection == LEFT_TO_RIGHT) {
                index = cards.size();
            } else {
                index = 0;
            }
            card.setCardInfo(new CardInfo<>(index));
        }
        this.cards.add(card.getCardInfo().getCardPositionInLayout(), card);
        card.setSwipeOrientationMode(SwipeGestureManager.OrientationMode.BOTH);
        card.setCardTranslationListener(this);
        card.setCardSwipedListener(this);
        card.setCardPercentageChangeListener(this, CardBoxView.START_TO_CURRENT);
    }

    private <CV extends View & Card<Entity>> CV findCardView(int position) {
        List<CV> cards = getCardViews();
        for (CV card : cards) {
            if (card.getCardInfo().getCardPositionInLayout() == position) {
                return card;
            }
        }
        throw new RuntimeException("Can't find view");
    }

    private <T extends View> int getCardViewsCount(@NonNull List<T> views) {
        int count = 0;
        for (T view : views) {
            if (shouldPassView(view)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private <CV extends View & Card<Entity>> void setEnabledExceptViewsWithPositions(boolean state,
                                                                                     @Nullable ColorFilter colorFilter,
                                                                                     @Nullable int... ignoredPositions) {
        List<Integer> positionsList = null;
        if (ignoredPositions != null) {
            positionsList = new ArrayList<>();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < ignoredPositions.length; i++) {
                positionsList.add(ignoredPositions[i]);
            }
        }
        List<CV> cards = getCardViews();
        for (CV card : cards) {
            if (state && card.getCardInfo().isCardDistributed()) {
                if (!card.isEnabled()) {
                    DrawableUtils.setColorFilter(card, null);
                }
                card.setEnabled(true);
            } else {
                boolean ignoredCard =
                        card.getCardInfo() != null &&
                                positionsList != null &&
                                        positionsList.contains(card.getCardInfo().getCardPositionInLayout());
                if (!ignoredCard) {
                    if (card.isEnabled() && card.getCardInfo().isCardDistributed() && colorFilter != null) {
                        DrawableUtils.setColorFilter(card, colorFilter);
                    }
                    card.setEnabled(false);
                }
            }
        }
    }

    private boolean shouldPassView(View view) {
        return view.getVisibility() != VISIBLE || Card.class.isInstance(view) && !((Card) view).getCardInfo().isCardDistributed();
    }

    private boolean shouldPassCard(Card<Entity> card) {
        return card.getVisibility() != VISIBLE || !card.getCardInfo().isCardDistributed();
    }

    private <CV extends View & Card<Entity>> List<CV> getValidatedCardViews() {
        final List<CV> validatedCards = new ArrayList<>();
        List<CV> cards = getCardViews();
        for (CV card : cards) {
            if (!shouldPassCard(card)) {
                validatedCards.add(card);
            }
        }
        return validatedCards;
    }

    private void clearAnimator() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            clearAnimation();
        }
    }

    /* inner types */

    @FunctionalInterface
    public interface OnCreateAnimatorAction<Entity> {
        <CV extends View & Card<Entity>> Animator createAnimation(CV cardView);
    }

    @IntDef({TOP, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CircleCenterLocation {
        int TOP = 0;
        int BOTTOM = 1;
    }

    @IntDef({LINE, CIRCLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DistributeCardsBy {
        int LINE = 0;
        int CIRCLE = 1;
    }

    @IntDef({LEFT_TO_RIGHT, RIGHT_TO_LEFT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CardsDirection {
        int LEFT_TO_RIGHT = 0;
        int RIGHT_TO_LEFT = 1;
    }
}
