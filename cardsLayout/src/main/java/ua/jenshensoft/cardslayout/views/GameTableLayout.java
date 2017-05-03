package ua.jenshensoft.cardslayout.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.internal.util.Predicate;
import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.table.CardDeckUpdater;
import ua.jenshensoft.cardslayout.listeners.table.OnCardClickListener;
import ua.jenshensoft.cardslayout.listeners.table.OnDistributedCardsListener;
import ua.jenshensoft.cardslayout.util.CardsUtil;
import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.updater.ViewUpdater;
import ua.jenshensoft.cardslayout.views.updater.callback.OnViewParamsUpdate;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;

public abstract class GameTableLayout<
        Entity,
        Layout extends CardsLayout<Entity>>
        extends FrameLayout
        implements OnViewParamsUpdate<GameTableParams<Entity>> {

    //Views
    protected List<Layout> cardsLayouts;
    //updaters
    protected ViewUpdater<GameTableParams<Entity>> viewUpdater;
    protected ViewMeasureConfig viewMeasureConfig;
    //attr
    private int durationOfDistributeAnimation = 1000;
    private boolean isEnableSwipe;
    private boolean isEnableTransition;
    private int currentPlayerLayoutId = -1;
    private boolean canAutoDistribute = true;
    private boolean deskOfCardsEnable = true;
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
                    setTransitionValidatorEnabled(layout);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (viewMeasureConfig.needUpdateView()) {
            viewUpdater.onViewMeasured();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Layout cardsLayout : cardsLayouts) {
            cardsLayout.setEnabled(enabled);
        }
    }

    public void setEnabled(boolean enabled, @Nullable ColorFilter colorFilter) {
        super.setEnabled(enabled);
        for (Layout cardsLayout : cardsLayouts) {
            cardsLayout.setEnabledCards(enabled, colorFilter, null);
        }
    }

    /* view updater */

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onUpdateViewParams(GameTableParams params) {
        if (hasDistributionState()) {
            DistributionState distributionState = params.getDistributionState();
            distributionState.getDeskOfCardsUpdater().updatePosition();
            if (!distributionState.isCardsAlreadyDistributed() && canAutoDistribute) {
                startDistributeCards();
            }
        }
    }

    /* public methods */

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

    public void setTransitionValidatorEnabled(final Layout cardsLayout) {
        cardsLayout.addCardTranslationListener((percentageX, percentageY, cardInfo, isTouched) -> {
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
            setCardDeckCards(predicateForCardsOnTheHands, distributionState.getDeskOfCardsUpdater());
        } else {
            setCardDeckCards(distributionState.getCardsPredicateBeforeDistribution(), distributionState.getDeskOfCardsUpdater());
        }
    }

    public void setCardDeckCards(Predicate<Card<Entity>> predicateForCardsOnTheHands, CardDeckUpdater<Entity> cardDeckUpdater) {
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
                        cardsInDeskForPlayer.add(card);
                    } else {
                        card.setVisibility(INVISIBLE);
                    }
                }
            }
            if (!cardsInDeskForPlayer.isEmpty()) {
                cardsInDeskForPlayers.add(cardsInDeskForPlayer.iterator());
            }
        }
        if (!cardsInDeskForPlayers.isEmpty()) {
            cardDeckUpdater.addCardsToCardDeck(CardsUtil.getCardsForDesk(cardsInDeskForPlayers));
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void startDistributeCards() {
        if (!hasDistributionState()) {
            throw new RuntimeException("You need to set distribution state before");
        }
        viewUpdater.addAction(() -> {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            startDistributeCards(distributionState.getCardsPredicateForDistribution());
        });
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
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, ua.jenshensoft.cardslayout.R.styleable.GameTableLayout_Params);
            try {
                durationOfDistributeAnimation = attributes.getInteger(ua.jenshensoft.cardslayout.R.styleable.GameTableLayout_Params_gameTableLayout_duration_distributeAnimation, durationOfDistributeAnimation);
                isEnableSwipe = attributes.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardValidatorSwipe, isEnableSwipe);
                isEnableTransition = attributes.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardValidatorTransition, isEnableTransition);
                currentPlayerLayoutId = attributes.getResourceId(R.styleable.GameTableLayout_Params_gameTableLayout_currentPlayerLayoutId, currentPlayerLayoutId);
                canAutoDistribute = attributes.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_canAutoDistribute, canAutoDistribute);
                deskOfCardsEnable = attributes.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_deskOfCardsEnable, deskOfCardsEnable);
            } finally {
                attributes.recycle();
            }
        }
    }

    private void inflateLayout() {
        cardsLayouts = new ArrayList<>();
        viewUpdater = new ViewUpdater<>(this);
        viewMeasureConfig = new ViewMeasureConfig(this);
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
                public void onAnimationEnd(Animator animation) {
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

    private boolean hasDistributionState() {
        return viewUpdater.getParams() != null && viewUpdater.getParams().getDistributionState() != null;
    }

    private boolean isDeskOfCardsEnable() {
        return hasDistributionState() && deskOfCardsEnable;
    }
}
