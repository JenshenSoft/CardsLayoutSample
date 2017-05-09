package ua.jenshensoft.cardslayout.views.table;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.util.Predicate;
import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.table.OnCardClickListener;
import ua.jenshensoft.cardslayout.listeners.table.OnDistributedCardsListener;
import ua.jenshensoft.cardslayout.pattern.CardDeckCoordinatesPattern;
import ua.jenshensoft.cardslayout.pattern.models.ThreeDCardCoordinates;
import ua.jenshensoft.cardslayout.util.CardsUtil;
import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.views.ViewUpdateConfig;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardDeckView;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.updater.ViewUpdater;
import ua.jenshensoft.cardslayout.views.updater.callback.OnViewParamsUpdate;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;

public abstract class GameTableLayout<
        Entity,
        Layout extends CardsLayout<Entity>>
        extends ViewGroup
        implements OnViewParamsUpdate<GameTableParams<Entity>> {

    public static final float EPSILON = 0.00000001f;

    //Views
    protected List<Layout> cardsLayouts;
    protected List<Card<Entity>> cardDeckCards = new ArrayList<>();
    @Nullable
    protected CardDeckView<Entity> cardDeckView;
    //updaters
    protected ViewUpdater<GameTableParams<Entity>> viewUpdater;
    protected ViewUpdateConfig viewUpdateConfig;
    protected List<Animator> startedAnimators;
    //attr
    private int durationOfDistributeAnimation = 1000;
    private boolean isEnableSwipe;
    private boolean isEnableTransition;
    private int currentPlayerLayoutId = -1;
    //card deck attr
    private boolean canAutoDistribute = true;
    private boolean deskOfCardsEnable = true;
    private FlagManager cardDeckGravity;
    private float cardDeckCardOffsetX = -1;
    private float cardDeckCardOffsetY = -1;
    private float cardDeckElevationMin = -1;
    private float cardDeckElevationMax = -1;
    //listeners
    @Nullable
    private OnCardClickListener<Entity> onCardClickListener;
    @Nullable
    private OnDistributedCardsListener<Entity> onDistributedCardsListener;

    public GameTableLayout(Context context) {
        super(context);
        if (!isInEditMode()) {
            initLayout(null);
        }
        inflateLayout();
    }

    public GameTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initLayout(attrs);
        }
        inflateLayout();
    }

    public GameTableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            initLayout(attrs);
        }
        inflateLayout();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GameTableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            initLayout(attrs);
        }
        inflateLayout();
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof CardsLayout) {
            Layout layout = (Layout) child;
            cardsLayouts.add(layout);
            if (currentPlayerLayoutId != -1 && currentPlayerLayoutId == child.getId()) {
                if (isEnableSwipe) {
                    setSwipeValidatorEnabled(layout);
                }
                if (isEnableTransition) {
                    setPercentageValidatorEnabled(layout);
                }
            }
        } else if (child instanceof CardDeckView) {
            cardDeckView = (CardDeckView<Entity>) child;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (viewUpdateConfig.needUpdateViewOnMeasure()) {
            // Find out how big everyone wants to be
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (viewUpdateConfig.needUpdateViewOnLayout(changed)) {
            float cardDeckX = -1;
            float cardDeckY = -1;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof CardsLayout) {
                    child.layout(l, t, r, b);
                } else if (child instanceof CardDeckView) {
                    CardDeckView cardDeckView = (CardDeckView) child;
                    cardDeckX = getXPositionForCardDeck(cardDeckView.getMeasuredWidth(), getMeasuredWidth());
                    cardDeckY = getYPositionForCardDeck(cardDeckView.getMeasuredHeight(), getMeasuredHeight());
                    child.layout(
                            Math.round(cardDeckX),
                            Math.round(cardDeckY),
                            Math.round(cardDeckX) + cardDeckView.getMeasuredWidth(),
                            Math.round(cardDeckY) + cardDeckView.getMeasuredHeight());
                } else {
                    throw new RuntimeException("Can't support this view " + child.getClass().getSimpleName());
                }
            }
            if (!cardDeckCards.isEmpty()) {
                if (cardDeckView == null || cardDeckView.getCardsCoordinates().isEmpty()) {
                    int widthOfCardDeck = 0;
                    int heightOfCardDeck = 0;
                    for (Card<Entity> deckCard : cardDeckCards) {
                        widthOfCardDeck += deckCard.getMeasuredWidth();
                        heightOfCardDeck += deckCard.getMeasuredHeight();
                    }
                    widthOfCardDeck /= cardDeckCards.size();
                    heightOfCardDeck /= cardDeckCards.size();
                    cardDeckX = getXPositionForCardDeck(widthOfCardDeck, getMeasuredWidth());
                    cardDeckY = getYPositionForCardDeck(heightOfCardDeck, getMeasuredHeight());
                } else {
                    List<ThreeDCardCoordinates> cardsCoordinates = cardDeckView.getCardsCoordinates();
                    if (cardsCoordinates == null) {
                        throw new RuntimeException("Something went wrong, coordinates can't be null");
                    }
                    ThreeDCardCoordinates lastCardCoordinates;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        lastCardCoordinates = cardsCoordinates.get(0);
                    } else {
                        lastCardCoordinates = cardsCoordinates.get(cardsCoordinates.size() - 1);
                    }

                    cardDeckX += lastCardCoordinates.getX() + cardDeckView.getPaddingLeft();
                    cardDeckY += lastCardCoordinates.getY() + cardDeckView.getPaddingTop();
                }
                @SuppressLint("DrawAllocation")
                List<ThreeDCardCoordinates> cardsCoordinates = new CardDeckCoordinatesPattern(
                        cardDeckCards.size(),
                        cardDeckCardOffsetX,
                        cardDeckCardOffsetY,
                        cardDeckX,
                        cardDeckY,
                        cardDeckElevationMin,
                        cardDeckElevationMax)
                        .getCardsCoordinates();
                for (int i = 0; i < cardDeckCards.size(); i++) {
                    onLayoutCardInCardDeck((View & Card<Entity>) cardDeckCards.get(i), cardsCoordinates.get(i));
                }
            }
            viewUpdater.onViewMeasured();
        }
    }


    /* view updater */

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimators();
    }

    @Override
    public void onUpdateViewParams(GameTableParams<Entity> params, boolean calledInOnMeasure) {
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = params.getDistributionState();
            distributionState.getDeskOfCardsUpdater().updatePosition();
            if (!distributionState.isCardsAlreadyDistributed() && canAutoDistribute) {
                startDistributeCards();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Layout cardsLayout : cardsLayouts) {
            cardsLayout.setEnabled(enabled);
        }
    }

    /* public methods */

    public void setEnabled(boolean enabled, @Nullable ColorFilter colorFilter) {
        super.setEnabled(enabled);
        for (Layout cardsLayout : cardsLayouts) {
            cardsLayout.setEnabledCards(enabled, colorFilter, null);
        }
    }

    public void setOnCardClickListener(@Nullable OnCardClickListener<Entity> onCardClickListener) {
        this.onCardClickListener = onCardClickListener;
    }

    public void setOnDistributedCardsListener(@Nullable OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        this.onDistributedCardsListener = onDistributedCardsListener;
    }

    public void updateDistributionState(@Nullable DistributionState<Entity> distributionState) {
        viewUpdater.setParams(new GameTableParams<>(distributionState));
        setCardDeckCards();
    }

    public int getCurrentPlayerLayoutId() {
        return currentPlayerLayoutId;
    }

    public void setCurrentPlayerLayoutId(int currentPlayerLayoutId) {
        this.currentPlayerLayoutId = currentPlayerLayoutId;
    }

    public Layout getCurrentPlayerCardsLayout() {
        if (currentPlayerLayoutId != -1) {
            for (Layout layout : cardsLayouts) {
                if (layout.getId() == currentPlayerLayoutId) {
                    return layout;
                }
            }
        }
        throw new RuntimeException("Can't find the current player layout");
    }

    public void setSwipeValidatorEnabled(final Layout cardsLayout) {
        cardsLayout.addOnCardSwipedListener(cardInfo -> {
            onActionWithCard(cardInfo.getEntity());
            if (!cardsLayout.isEnabled()) {
                cardsLayout.setEnabled(true);
            }
        });
    }

    public void setPercentageValidatorEnabled(final Layout cardsLayout) {
        cardsLayout.addCardPercentageChangeListener((percentageX, percentageY, cardInfo, isTouched) -> {
            if (!isTouched) {
                if (percentageX >= 100 || percentageY >= 100) {
                    onActionWithCard(cardInfo.getEntity());
                    return;
                }
                if (!cardsLayout.isEnabled()) {
                    cardsLayout.setEnabled(true);
                }
            } else {
                if (cardsLayout.isEnabled()) {
                    cardsLayout.setEnabledCards(false, Collections.singletonList(cardInfo.getCardPositionInLayout()));
                }
            }
        });
    }

    public int getDurationOfDistributeAnimation() {
        return durationOfDistributeAnimation;
    }

    public void setDurationOfDistributeAnimation(int durationOfDistributeAnimation) {
        this.durationOfDistributeAnimation = durationOfDistributeAnimation;
    }

    @SuppressWarnings("ConstantConditions")
    public void setCardDeckCards() {
        if (!hasDistributionState()) {
            throw new RuntimeException("You need to set distribution state before");
        }
        DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
        if (distributionState.isCardsAlreadyDistributed()) {
            Predicate<Card<Entity>> predicateForCardsOnTheHands = entityCard ->
                    distributionState.getCardsPredicateBeforeDistribution().apply(entityCard) ||
                            distributionState.getCardsPredicateForDistribution().apply(entityCard);
            setCardDeckCards(predicateForCardsOnTheHands);
        } else {
            setCardDeckCards(distributionState.getCardsPredicateBeforeDistribution());
        }
    }

    public void setCardDeckCards(Predicate<Card<Entity>> predicateForCardsOnTheHands) {
        List<Iterator<Card<Entity>>> cardsInDeskForPlayers = new ArrayList<>();
        for (Layout cardsLayout : cardsLayouts) {
            List<Card<Entity>> cardsInDeskForPlayer = new ArrayList<>();
            for (Card<Entity> card : cardsLayout.getCards()) {
                if (predicateForCardsOnTheHands.apply(card)) {
                    if (deskOfCardsEnable) {
                        card.getCardInfo().setCardDistributed(true);
                    } else {
                        card.setVisibility(VISIBLE);
                    }
                } else {
                    if (deskOfCardsEnable) {
                        CardInfo<Entity> cardInfo = card.getCardInfo();
                        cardInfo.setCardDistributed(false);
                    } else {
                        card.setVisibility(INVISIBLE);
                    }
                    cardsInDeskForPlayer.add(card);
                }
            }
            if (!cardsInDeskForPlayer.isEmpty()) {
                cardsInDeskForPlayers.add(cardsInDeskForPlayer.iterator());
            }
        }
        if (!cardsInDeskForPlayers.isEmpty()) {
            cardDeckCards.addAll(CardsUtil.getCardsForDesk(cardsInDeskForPlayers));
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void startDistributeCards() {
        if (!hasDistributionState()) {
            throw new RuntimeException("You need to set distribution state before");
        }
        clearAnimators();
        viewUpdater.addAction(
                calledInOnMeasure ->
                        postDelayed(() ->
                                startDistributeCards(viewUpdater.getParams().getDistributionState().getCardsPredicateForDistribution()), 500));
    }

    public void startDistributeCards(Predicate<Card<Entity>> predicate) {
        setEnabled(false, null);//disable cards without filters
        OnDistributedCardsListener<Entity> onDistributedCardsListener = new OnDistributedCardsListener<Entity>() {

            private int count;
            private List<Card<Entity>> startDistributedCardViews = new ArrayList<>();
            private List<Card<Entity>> endDistributedCardViews = new ArrayList<>();

            @Override
            public void onDistributedCards() {
                count++;
                if (count == cardsLayouts.size()) {
                    count = 0;
                    GameTableLayout.this.onDistributedCards();
                }
            }

            @Override
            public void onStartDistributedCardWave(List<Card<Entity>> cards) {
                startDistributedCardViews.addAll(cards);
                if (startDistributedCardViews.size() == cardsLayouts.size()) {
                    GameTableLayout.this.onStartDistributedCardWave(startDistributedCardViews);
                    startDistributedCardViews.clear();
                }
            }

            @Override
            public void onEndDistributeCardWave(List<Card<Entity>> cards) {
                endDistributedCardViews.addAll(cards);
                if (endDistributedCardViews.size() == cardsLayouts.size()) {
                    GameTableLayout.this.onEndDistributeCardWave(endDistributedCardViews);
                    endDistributedCardViews.clear();
                }
            }
        };

        for (Layout cardsLayout : cardsLayouts) {
            distributeCardForPlayer(
                    cardsLayout,
                    predicate,
                    onDistributedCardsListener);
        }
    }


    /* protected methods */

    @SuppressWarnings("ConstantConditions")
    protected void onDistributedCards() {
        setEnabled(true);
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            distributionState.setCardsAlreadyDistributed(true);
        }
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onDistributedCards();
        }
    }

    protected void onStartDistributedCardWave(List<Card<Entity>> cards) {
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onStartDistributedCardWave(cards);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void onEndDistributeCardWave(List<Card<Entity>> cards) {
        //set elevation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Card<Entity> card : cards) {
                card.setElevation(card.getNormalElevation());
            }
        }
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            distributionState.getDeskOfCardsUpdater().removeCardsFromDesk(cards);
        }
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onEndDistributeCardWave(cards);
        }
    }

    protected CardsLayout.OnCreateAnimatorAction<Entity> creteAnimationForCardDistribution(Layout cardsLayout,
                                                                                           Card<Entity> card) {
        return new CardsLayout.OnCreateAnimatorAction<Entity>() {
            @Override
            public <C extends View & Card<Entity>> Animator createAnimation(C view) {
                if (view.equals(card)) {
                    AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(view)
                            .setX(AwesomeAnimation.CoordinationMode.COORDINATES, view.getCardInfo().getCurrentPositionX(), view.getCardInfo().getFirstPositionX())
                            .setY(AwesomeAnimation.CoordinationMode.COORDINATES, view.getCardInfo().getCurrentPositionY(), view.getCardInfo().getFirstPositionY())
                            .setRotation(180, card.getCardInfo().getFirstRotation())
                            .setDuration(durationOfDistributeAnimation);
                    if (cardsLayout.getInterpolator() != null) {
                        awesomeAnimation.setInterpolator(cardsLayout.getInterpolator());
                    }
                    return awesomeAnimation.build().getAnimatorSet();
                } else {
                    return cardsLayout.getDefaultCreateAnimatorAction().createAnimation(view);
                }
            }
        };
    }

    /* private methods */

    @SuppressWarnings({"unchecked"})
    private void initLayout(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributesTable = getContext().obtainStyledAttributes(attrs, R.styleable.GameTableLayout_Params);
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CardDeckView_Params);
            try {
                durationOfDistributeAnimation = attributesTable.getInteger(ua.jenshensoft.cardslayout.R.styleable.GameTableLayout_Params_gameTableLayout_duration_distributeAnimation, durationOfDistributeAnimation);
                isEnableSwipe = attributesTable.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardValidatorSwipe, isEnableSwipe);
                isEnableTransition = attributesTable.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardValidatorTransition, isEnableTransition);
                currentPlayerLayoutId = attributesTable.getResourceId(R.styleable.GameTableLayout_Params_gameTableLayout_currentPlayerLayoutId, currentPlayerLayoutId);

                //card deck
                canAutoDistribute = attributesTable.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_canAutoDistribute, canAutoDistribute);
                deskOfCardsEnable = attributesTable.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardDeckEnable, deskOfCardsEnable);
                cardDeckGravity = new FlagManager(attributes.getInt(R.styleable.GameTableLayout_Params_gameTableLayout_cardDeckGravity, FlagManager.Gravity.CENTER));

                cardDeckCardOffsetX = attributes.getDimension(R.styleable.CardDeckView_Params_cardDeck_cardDeck_cardOffset_x, cardDeckCardOffsetX);
                cardDeckCardOffsetY = attributes.getDimension(R.styleable.CardDeckView_Params_cardDeck_cardDeck_cardOffset_y, cardDeckCardOffsetY);
                cardDeckElevationMin = attributes.getDimension(R.styleable.CardDeckView_Params_cardDeck_cardDeck_elevation_min, cardDeckElevationMin);
                cardDeckElevationMax = attributes.getDimension(R.styleable.CardDeckView_Params_cardDeck_cardDeck_elevation_max, cardDeckElevationMax);
            } finally {
                attributesTable.recycle();
                attributes.recycle();
            }
        }
    }

    private void inflateLayout() {
        cardsLayouts = new ArrayList<>();
        viewUpdater = new ViewUpdater<>(this);
        viewUpdateConfig = new ViewUpdateConfig(this, false);
        startedAnimators = new ArrayList<>();
        cardDeckGravity = new FlagManager(FlagManager.Gravity.CENTER);

        if (Math.abs(cardDeckElevationMin - (-1)) < EPSILON) {
            cardDeckElevationMin = getResources().getDimension(R.dimen.cardsLayout_card_elevation_normal);
        }
        if (Math.abs(cardDeckElevationMax - (-1)) < EPSILON) {
            cardDeckElevationMax = getResources().getDimension(R.dimen.cardsLayout_card_elevation_pressed);
        }
        if (Math.abs(cardDeckCardOffsetX - (-1)) < EPSILON) {
            cardDeckCardOffsetX = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
        }
        if (Math.abs(cardDeckCardOffsetY - (-1)) < EPSILON) {
            cardDeckCardOffsetY = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
        }
    }

    private <CV extends View & Card<Entity>> void onLayoutCardInCardDeck(CV cardView, ThreeDCardCoordinates coordinates) {
        int x = Math.round(coordinates.getX());
        int y = Math.round(coordinates.getY());
        float z = coordinates.getZ();
        int angle = Math.round(coordinates.getAngle());
        cardView.setRotation(angle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setElevation(z);
        }
        cardView.layout(x, y, x + cardView.getMeasuredWidth(), y + cardView.getMeasuredHeight());
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstPositionX(x);
        cardInfo.setCurrentPositionX(x);
        cardInfo.setFirstPositionY(y);
        cardInfo.setCurrentPositionY(y);
        cardInfo.setFirstRotation(angle);
        cardInfo.setCurrentRotation(angle);
    }

    private void onActionWithCard(Entity entity) {
        if (onCardClickListener != null) {
            onCardClickListener.onCardAction(entity);
        }
    }

    private void distributeCardForPlayer(final Layout cardsLayout,
                                         final Predicate<Card<Entity>> predicate,
                                         final OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        List<Card<Entity>> filteredCardsViews = new ArrayList<>();
        List<Card<Entity>> cards = cardsLayout.getCards();
        for (Card<Entity> card : cards) {
            if (predicate.apply(card) && (isDeskOfCardsEnable() || card.getVisibility() != VISIBLE)) {
                filteredCardsViews.add(card);
            }
        }
        animateCardViews(cardsLayout, filteredCardsViews.iterator(), onDistributedCardsListener);
    }

    private void animateCardViews(final Layout cardsLayout,
                                  final Iterator<Card<Entity>> cardViewsIterator,
                                  final OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        if (cardViewsIterator.hasNext()) {
            final Card<Entity> card = cardViewsIterator.next();
            if (onDistributedCardsListener != null) {
                onDistributedCardsListener.onStartDistributedCardWave(Collections.singletonList(card));
            }
            animateCardView(cardsLayout, card, new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    startedAnimators.add(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    startedAnimators.remove(animation);
                    if (onDistributedCardsListener != null) {
                        onDistributedCardsListener.onEndDistributeCardWave(Collections.singletonList(card));
                    }
                    animateCardViews(cardsLayout, cardViewsIterator, onDistributedCardsListener);
                }
            });
        } else {
            if (onDistributedCardsListener != null) {
                onDistributedCardsListener.onDistributedCards();
            }
        }
    }

    private void animateCardView(Layout cardsLayout,
                                 Card<Entity> card,
                                 AnimatorListenerAdapter adapter) {
        if (hasDistributionState() && deskOfCardsEnable) {
            card.getCardInfo().setCardDistributed(true);
        } else {
            card.setVisibility(VISIBLE);
        }
        cardsLayout.invalidateCardsPosition(true, creteAnimationForCardDistribution(cardsLayout, card), adapter);
    }

    private void clearAnimators() {
        if (!startedAnimators.isEmpty()) {
            for (Animator animator : startedAnimators) {
                animator.removeAllListeners();
                animator.cancel();
            }
        }
        clearAnimation();
    }

    private float getXPositionForCardDeck(float widthOfCardDeck, float rootWidth) {
        float cardPositionX = 0;
        if (cardDeckGravity.containsFlag(FlagManager.Gravity.LEFT)) {
            cardPositionX = 0;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.RIGHT)) {
            cardPositionX = rootWidth - widthOfCardDeck;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER_HORIZONTAL)
                || cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER)) {
            cardPositionX = rootWidth / 2f - widthOfCardDeck / 2f;
        }
        return cardPositionX;
    }

    private float getYPositionForCardDeck(float heightOfCardDeck, float rootHeight) {
        float cardPositionY = 0;
        if (cardDeckGravity.containsFlag(FlagManager.Gravity.TOP)) {
            cardPositionY = 0;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.BOTTOM)) {
            cardPositionY = rootHeight - heightOfCardDeck;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER_VERTICAL)
                || cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER)) {
            cardPositionY = rootHeight / 2f - heightOfCardDeck / 2f;
        }
        return cardPositionY;
    }

    private boolean hasDistributionState() {
        return viewUpdater.getParams() != null && viewUpdater.getParams().getDistributionState() != null;
    }

    private boolean isDeskOfCardsEnable() {
        return hasDistributionState() && deskOfCardsEnable;
    }
}
