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
import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.views.ViewUpdateConfig;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardDeckView;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.updater.ViewUpdater;
import ua.jenshensoft.cardslayout.views.updater.callback.OnViewParamsUpdate;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;

@SuppressWarnings("unused")
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
    private boolean onDistributeAnimation;

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

    @SuppressWarnings("unchecked")
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
        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof CardsLayout) {
                CardsLayout layout = (CardsLayout) child;
                child.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
            } else if (child instanceof CardDeckView) {
                CardDeckView cardDeckView = (CardDeckView) child;
                float cardDeckX = getXPositionForCardDeck(cardDeckView.getMeasuredWidth(), getMeasuredWidth());
                float cardDeckY = getYPositionForCardDeck(cardDeckView.getMeasuredHeight(), getMeasuredHeight());
                child.layout(
                        Math.round(cardDeckX),
                        Math.round(cardDeckY),
                        Math.round(cardDeckX) + cardDeckView.getMeasuredWidth(),
                        Math.round(cardDeckY) + cardDeckView.getMeasuredHeight());
            } else {
                throw new RuntimeException("Can't support this view " + child.getClass().getSimpleName());
            }
        }
        onLayoutCardDeck(changed);
        viewUpdater.onViewUpdated();
    }

    /* view updater */

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onUpdateViewParams(GameTableParams<Entity> params, boolean calledInOnMeasure) {
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = params.getDistributionState();
            if (!onDistributeAnimation && !distributionState.isCardsAlreadyDistributed() && canAutoDistribute) {
                startDistributeCards();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimators();
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
        for (Layout layout : cardsLayouts) {
            for (Card<Entity> card : layout.getCards()) {
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
                    cardDeckCards.add(0, card);
                }
            }
        }
        /*List<Iterator<Card<Entity>>> cardsIterators = new ArrayList<>();
        for (Layout layout : cardsLayouts) {
            cardsIterators.add(layout.getCards().iterator());
        }

        while (hasNextCard(cardsIterators)) {
            List<Card<Entity>> cards = getNextCards(cardsIterators);
            for (Card card : cards) {
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
                    cardDeckCards.add(card);
                }
            }
        }*/
    }

    private boolean hasNextCard(List<Iterator<Card<Entity>>> cardsIterators) {
        for (Iterator<Card<Entity>> cardIterator : cardsIterators) {
            if (cardIterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    private List<Card<Entity>> getNextCards(List<Iterator<Card<Entity>>> cardsIterators) {
        List<Card<Entity>> cards = new ArrayList<>();
        for (Iterator<Card<Entity>> cardIterator : cardsIterators) {
            if (cardIterator.hasNext()) {
                cards.add(cardIterator.next());
            }
        }
        return cards;
    }

    @SuppressWarnings("ConstantConditions")
    public void startDistributeCards() {
        if (!hasDistributionState()) {
            throw new RuntimeException("You need to set distribution state before");
        }
        clearAnimators();
        viewUpdater.addAction(
                calledInOnMeasure -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        postOnAnimation(() -> startDistributeCards(viewUpdater.getParams().getDistributionState().getCardsPredicateForDistribution()));
                    } else {
                        postDelayed(() -> startDistributeCards(viewUpdater.getParams().getDistributionState().getCardsPredicateForDistribution()), 300);
                    }
                });
    }

    public void startDistributeCards(Predicate<Card<Entity>> predicate) {
        onDistributeAnimation = true;
        setEnabled(false, null);//disable cards without filters
        TableDistributedCardsListener<Entity> distributedCardsListener = new TableDistributedCardsListener<>(cardsLayouts.size(), new OnDistributedCardsListener<Entity>() {
            @Override
            public void onDistributedCards() {
                GameTableLayout.this.onDistributedCards();
            }

            @Override
            public void onStartDistributedCardWave(List<Card<Entity>> cards) {
                GameTableLayout.this.onStartDistributedCardWave(cards);
            }

            @Override
            public void onEndDistributeCardWave(List<Card<Entity>> cards) {
                GameTableLayout.this.onEndDistributeCardWave(cards);
            }
        });
        for (Layout cardsLayout : cardsLayouts) {
            distributeCardForPlayer(
                    cardsLayout,
                    predicate,
                    distributedCardsListener);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void onDistributedCards() {
        setEnabled(true);
        onDistributeAnimation = false;
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            distributionState.setCardsAlreadyDistributed(true);
        }
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onDistributedCards();
        }
    }


    /* protected methods */

    protected void onStartDistributedCardWave(List<Card<Entity>> cards) {
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onStartDistributedCardWave(cards);
        }
    }

    protected void onEndDistributeCardWave(List<Card<Entity>> cards) {
        //set elevation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Card<Entity> card : cards) {
                card.setCardZ(card.getNormalElevation());
            }
        }
        cardDeckCards.removeAll(cards);
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
                            .setX(AwesomeAnimation.CoordinationMode.COORDINATES, view.getX(), view.getCardInfo().getFirstPositionX())
                            .setY(AwesomeAnimation.CoordinationMode.COORDINATES, view.getY(), view.getCardInfo().getFirstPositionY())
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

    @SuppressWarnings({"unchecked"})
    private void initLayout(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributesTable = getContext().obtainStyledAttributes(attrs, R.styleable.GameTableLayout);
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CardDeckView);
            try {
                durationOfDistributeAnimation = attributesTable.getInteger(R.styleable.GameTableLayout_gameTableLayout_duration_distributeAnimation, durationOfDistributeAnimation);
                isEnableSwipe = attributesTable.getBoolean(R.styleable.GameTableLayout_gameTableLayout_cardValidatorSwipe, isEnableSwipe);
                isEnableTransition = attributesTable.getBoolean(R.styleable.GameTableLayout_gameTableLayout_cardValidatorTransition, isEnableTransition);
                currentPlayerLayoutId = attributesTable.getResourceId(R.styleable.GameTableLayout_gameTableLayout_currentPlayerLayoutId, currentPlayerLayoutId);

                //card deck
                canAutoDistribute = attributesTable.getBoolean(R.styleable.GameTableLayout_gameTableLayout_canAutoDistribute, canAutoDistribute);
                deskOfCardsEnable = attributesTable.getBoolean(R.styleable.GameTableLayout_gameTableLayout_cardDeckEnable, deskOfCardsEnable);
                cardDeckGravity = new FlagManager(attributesTable.getInt(R.styleable.GameTableLayout_gameTableLayout_cardDeckGravity, FlagManager.Gravity.CENTER));

                cardDeckCardOffsetX = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_x, cardDeckCardOffsetX);
                cardDeckCardOffsetY = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_y, cardDeckCardOffsetY);
                cardDeckElevationMin = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_elevation_min, cardDeckElevationMin);
                cardDeckElevationMax = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_elevation_max, cardDeckElevationMax);
            } finally {
                attributesTable.recycle();
                attributes.recycle();
            }
        }
    }

    /* private methods */

    private void inflateLayout() {
        cardsLayouts = new ArrayList<>();
        viewUpdater = new ViewUpdater<>(this);
        viewUpdateConfig = new ViewUpdateConfig(this);
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

    @SuppressWarnings("unchecked")
    private <CV extends View & Card<Entity>> void onLayoutCardDeck(boolean changed) {
        if (cardDeckCards.isEmpty()) {
            return;
        }
        if (viewUpdateConfig.needUpdateViewOnLayout(changed)) {
            float cardDeckX;
            float cardDeckY;
            if (cardDeckView != null && !cardDeckView.getCardsCoordinates().isEmpty()) {
                List<ThreeDCardCoordinates> cardsCoordinates = cardDeckView.getCardsCoordinates();
                if (cardsCoordinates == null) {
                    throw new RuntimeException("Something went wrong, coordinates can't be null");
                }
                ThreeDCardCoordinates lastCardCoordinates = cardsCoordinates.get(0);
                cardDeckX = cardDeckView.getX() + lastCardCoordinates.getX() + cardDeckView.getPaddingLeft();
                cardDeckY = cardDeckView.getY() + lastCardCoordinates.getY() + cardDeckView.getPaddingTop();
            } else {
                int widthOfCardDeck = 0;
                int heightOfCardDeck = 0;
                for (Card<Entity> deckCard : cardDeckCards) {
                    int measuredWidth = deckCard.getCardWidth();
                    int measuredHeight = deckCard.getCardHeight();
                    if (measuredWidth > widthOfCardDeck) {
                        widthOfCardDeck = measuredWidth;
                    }
                    if (measuredHeight > heightOfCardDeck) {
                        heightOfCardDeck = measuredHeight;
                    }
                }
                cardDeckX = getXPositionForCardDeck(widthOfCardDeck, getMeasuredWidth());
                cardDeckY = getYPositionForCardDeck(heightOfCardDeck, getMeasuredHeight());
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
                ThreeDCardCoordinates coordinates = cardsCoordinates.get(i);
                onLayoutCardInCardDeck((CV) cardDeckCards.get(i), coordinates.getX(), coordinates.getY(), coordinates.getZ(), coordinates.getAngle());
            }
        } else {
            for (int i = 0; i < cardDeckCards.size(); i++) {
                CV card = (CV) cardDeckCards.get(i);
                int x = Math.round(card.getX());
                int y = Math.round(card.getY());
                card.layout(x, y, x + card.getMeasuredWidth(), y + card.getMeasuredHeight());
            }
        }
    }

    private <CV extends View & Card<Entity>> void onLayoutCardInCardDeck(CV card, float cardX, float cardY, float cardZ, float cardAngle) {
        int x;
        int y;
        if (card.getCardInfo().isCardDistributed()) {
            x = Math.round(card.getX());
            y = Math.round(card.getY());
        } else {
            if ((Math.abs(cardX - (card.getX())) < EPSILON) &&
                    (Math.abs(cardY - (card.getY())) < EPSILON)) {
                return;
            }
            x = Math.round(cardX);
            y = Math.round(cardY);
            int angle = Math.round(cardAngle);
            card.setRotation(angle);
            card.setCardZ(cardZ);
            card.setFirstX(x);
            card.setFirstY(y);
            card.setFirstRotation(angle);
        }
        card.layout(x, y, x + card.getMeasuredWidth(), y + card.getMeasuredHeight());
    }

    private void onActionWithCard(Entity entity) {
        if (onCardClickListener != null) {
            onCardClickListener.onCardAction(entity);
        }
    }

    private void distributeCardForPlayer(final Layout cardsLayout,
                                         final Predicate<Card<Entity>> predicate,
                                         final TableDistributedCardsListener<Entity> onDistributedCardsListener) {
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
            onDistributedCardsListener.onStartDistributedCardWave(Collections.singletonList(card));
            animateCardView(cardsLayout, card, new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    startedAnimators.add(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    startedAnimators.remove(animation);
                    onDistributedCardsListener.onEndDistributeCardWave(Collections.singletonList(card));
                    animateCardViews(cardsLayout, cardViewsIterator, onDistributedCardsListener);
                }
            });
        } else {
            onDistributedCardsListener.onDistributedCards();
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
