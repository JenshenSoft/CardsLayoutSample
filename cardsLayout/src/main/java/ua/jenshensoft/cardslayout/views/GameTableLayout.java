package ua.jenshensoft.cardslayout.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.internal.util.Predicate;
import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.listeners.OnCardClickListener;
import ua.jenshensoft.cardslayout.listeners.OnDistributedCardsListener;
import ua.jenshensoft.cardslayout.listeners.OnUpdateDeskOfCardsUpdater;
import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.updater.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;
import ua.jenshensoft.cardslayout.views.updater.ViewUpdater;
import ua.jenshensoft.cardslayout.views.updater.callback.OnViewParamsUpdate;

public abstract class GameTableLayout<
        Entity,
        Layout extends CardsLayout<Entity>>
        extends FrameLayout
        implements OnViewParamsUpdate<GameTableParams<Entity>> {

    //Views
    protected List<Layout> cardsLayouts;
    //attr
    private int durationOfDistributeAnimation = 1000;
    private boolean isEnableSwipe;
    private boolean isEnableTransition;
    private int currentPlayerLayoutId = -1;
    private boolean canAutoDistribute = true;
    private boolean deskOfCardsEnable = true;
    @Nullable
    private OnCardClickListener<Entity> onCardClickListener;
    @Nullable
    private OnDistributedCardsListener<Entity> onDistributedCardsListener;
    private ViewUpdater<GameTableParams<Entity>> viewUpdater;

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
                setSwipeValidatorEnabled(layout, isEnableSwipe);
                setTransitionValidatorEnabled(layout, isEnableTransition);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewUpdater.onViewMeasured();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getCurrentPlayerCardsLayout().setEnabled(enabled);
    }

    /* view updater */

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onUpdateViewParams(GameTableParams params) {
        if (hasDistributionState()) {
            DistributionState distributionState = params.getDistributionState();
            distributionState.getDeskOfCardsUpdater().updatePosition();
            if (canAutoDistribute) {
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
        if (distributionState != null && !distributionState.isCardsAlreadyDistributed()) {
            setCardsBeforeDistribution(distributionState.getPredicateForCardsBeforeDistribution(), distributionState.getDeskOfCardsUpdater());
        }
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

    public void setSwipeValidatorEnabled(final Layout cardsLayout, boolean enabled) {
        if (enabled) {
            cardsLayout.setOnCardSwipedListener(cardInfo -> onActionWithCard(cardInfo.getEntity()));
        } else {
            cardsLayout.setOnCardSwipedListener(null);
        }
    }

    public void setTransitionValidatorEnabled(final Layout cardsLayout, boolean enabled) {
        if (enabled) {
            cardsLayout.setCardPercentageChangeListener((percentageX, percentageY, cardInfo, isTouched) -> {
                if (!isTouched) {
                    if (percentageX >= 100 || percentageY >= 100) {
                        onActionWithCard(cardInfo.getEntity());
                        return;
                    }
                    cardsLayout.setEnabled(true);
                } else {
                    cardsLayout.setEnabledExceptPositions(false, cardInfo.getCardPositionInLayout());
                }
            });
        } else {
            cardsLayout.setCardPercentageChangeListener(null);
        }
    }

    public void setDurationOfDistributeAnimation(int durationOfDistributeAnimation) {
        this.durationOfDistributeAnimation = durationOfDistributeAnimation;
    }

    @SuppressWarnings("ConstantConditions")
    public void setCardsBeforeDistribution() {
        if (!hasDistributionState()) {
            throw new RuntimeException("You need to set distribution state before");
        }
        DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
        setCardsBeforeDistribution(distributionState.getPredicateForCardsBeforeDistribution(), distributionState.getDeskOfCardsUpdater());
    }

    public void setCardsBeforeDistribution(Predicate<CardView<Entity>> predicate, OnUpdateDeskOfCardsUpdater<Entity> onUpdateDeskOfCardsUpdater) {
        List<CardView<Entity>> cardInDesk = new ArrayList<>();
        for (Layout cardsLayout : cardsLayouts) {
            for (CardView<Entity> cardView : cardsLayout.getCardViews()) {
                if (predicate.apply(cardView)) {
                    if (deskOfCardsEnable) {
                        cardView.getCardInfo().setCardDistributed(true);
                    } else {
                        cardView.setVisibility(VISIBLE);
                    }
                } else {
                    if (deskOfCardsEnable) {
                        CardInfo<Entity> cardInfo = cardView.getCardInfo();
                        cardInfo.setCardDistributed(false);
                        cardInDesk.add(cardView);
                    } else {
                        cardView.setVisibility(INVISIBLE);
                    }
                }
            }
        }
        if (!cardInDesk.isEmpty()) {
            onUpdateDeskOfCardsUpdater.addCards(cardInDesk);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void startDistributeCards() {
        if (!hasDistributionState()) {
            throw new RuntimeException("You need to set distribution state before");
        }
        viewUpdater.addAction(() -> {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            startDistributeCards(distributionState.getPredicateForCardsForDistribution(), distributionState.getDeskOfCardsUpdater().getPosition());
        });
    }

    public void startDistributeCards(Predicate<CardView<Entity>> predicate, float[] coordinateForDistribution) {
        OnDistributedCardsListener<Entity> onDistributedCardsListener = new OnDistributedCardsListener<Entity>() {

            private int count;
            private List<CardView<Entity>> startDistributedCardViews = new ArrayList<>();
            private List<CardView<Entity>> endDistributedCardViews = new ArrayList<>();

            @Override
            public void onDistributedCards() {
                count++;
                if (count == cardsLayouts.size()) {
                    count = 0;
                    GameTableLayout.this.onDistributedCards();
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onStartDistributedCardWave(CardView<Entity>[] cardViews) {
                startDistributedCardViews.addAll(Arrays.asList(cardViews));
                if (startDistributedCardViews.size() == cardsLayouts.size()) {
                    CardView[] array = startDistributedCardViews.toArray(new CardView[startDistributedCardViews.size()]);
                    GameTableLayout.this.onStartDistributedCardWave(array);
                    startDistributedCardViews.clear();
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onEndDistributeCardWave(CardView<Entity>[] cardViews) {
                endDistributedCardViews.addAll(Arrays.asList(cardViews));
                if (endDistributedCardViews.size() == cardsLayouts.size()) {
                    CardView[] array = startDistributedCardViews.toArray(new CardView[startDistributedCardViews.size()]);
                    GameTableLayout.this.onEndDistributeCardWave(array);
                    endDistributedCardViews.clear();
                }
            }
        };

        for (Layout cardsLayout : cardsLayouts) {
            distributeCardForPlayer(
                    cardsLayout,
                    predicate,
                    coordinateForDistribution,
                    onDistributedCardsListener);
        }
    }


    /* protected methods */

    protected abstract int getLayoutId();

    @SuppressWarnings("ConstantConditions")
    protected void onDistributedCards() {
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            distributionState.setCardsAlreadyDistributed(true);
            distributionState.getDeskOfCardsUpdater().clear();
        }
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onDistributedCards();
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void onStartDistributedCardWave(CardView<Entity>[] cardViews) {
        if (hasDistributionState()) {
            DistributionState<Entity> distributionState = viewUpdater.getParams().getDistributionState();
            distributionState.getDeskOfCardsUpdater().removeCardsFromDesk(Arrays.asList(cardViews));
        }
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onStartDistributedCardWave(cardViews);
        }
    }

    protected void onEndDistributeCardWave(CardView<Entity>[] cardViews) {
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onEndDistributeCardWave(cardViews);
        }
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
        inflate(getContext(), getLayoutId(), this);
    }

    private void onActionWithCard(Entity entity) {
        if (onCardClickListener != null) {
            onCardClickListener.onCardAction(entity);
        }
    }

    private void distributeCardForPlayer(final Layout cardsLayout,
                                         final Predicate<CardView<Entity>> predicate,
                                         final float[] distributeFromCoordinates,
                                         final OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        List<CardView<Entity>> filteredCardsViews = new ArrayList<>();
        for (CardView<Entity> cardsView : cardsLayout.getCardViews()) {
            if (predicate.apply(cardsView) &&
                    (isDeskOfCardsEnable() || cardsView.getVisibility() != VISIBLE)) {
                filteredCardsViews.add(cardsView);
            }
        }

        animateCardViews(cardsLayout, filteredCardsViews.iterator(), distributeFromCoordinates, onDistributedCardsListener);
    }

    private void animateCardViews(final Layout cardsLayout,
                                  final Iterator<CardView<Entity>> cardViewsIterator,
                                  final float[] distributeFromCoordinates,
                                  final OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        if (cardViewsIterator.hasNext()) {
            final CardView<Entity> cardView = cardViewsIterator.next();
            if (onDistributedCardsListener != null) {
                onDistributedCardsListener.onStartDistributedCardWave(cardView);
            }
            animateCardView(cardsLayout, cardView, distributeFromCoordinates, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onDistributedCardsListener != null) {
                        onDistributedCardsListener.onEndDistributeCardWave(cardView);
                    }
                    animateCardViews(cardsLayout, cardViewsIterator, distributeFromCoordinates, onDistributedCardsListener);
                }
            });
        } else {
            if (onDistributedCardsListener != null) {
                onDistributedCardsListener.onDistributedCards();
            }
        }
    }

    private void animateCardView(Layout cardsLayout,
                                 CardView<Entity> cardView,
                                 float[] distributeFromCoordinates,
                                 AnimatorListenerAdapter adapter) {
        if (hasDistributionState() && deskOfCardsEnable) {
            cardView.getCardInfo().setCardDistributed(true);
        } else {
            cardView.setVisibility(VISIBLE);
        }

        CardsLayout.OnCreateAnimatorAction<Entity> onCreateAnimatorAction = new CardsLayout.OnCreateAnimatorAction<Entity>() {
            @Override
            public <C extends View & Card<Entity>> Animator createAnimation(C view) {
                if (view.equals(cardView)) {
                    AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(view)
                            .setX(AwesomeAnimation.CoordinationMode.COORDINATES, distributeFromCoordinates[0], view.getCardInfo().getFirstPositionX())
                            .setY(AwesomeAnimation.CoordinationMode.COORDINATES, distributeFromCoordinates[1], view.getCardInfo().getFirstPositionY())
                            .setRotation(180, cardView.getCardInfo().getFirstRotation())
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
        cardsLayout.invalidateCardsPosition(true, onCreateAnimatorAction, adapter);
    }

    private boolean hasDistributionState() {
        return viewUpdater.getParams() != null && viewUpdater.getParams().getDistributionState() != null;
    }

    private boolean isDeskOfCardsEnable() {
        return hasDistributionState() && deskOfCardsEnable;
    }
}
