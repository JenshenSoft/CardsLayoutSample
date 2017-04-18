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

public abstract class GameTableLayout<
        Entity,
        Layout extends CardsLayout<Entity>>
        extends FrameLayout {

    //Views
    protected List<Layout> cardsLayouts;
    @Nullable
    protected DistributionState<Entity> distributionState;
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
        if (this.distributionState != null && !this.distributionState.isCardsAlreadyDistributed()) {
            distributionState.getDeskOfCardsUpdater().updatePosition();
            if (canAutoDistribute) {
                startDistributeCards();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getCurrentPlayerCardsLayout().setEnabled(enabled);
    }


    /* public methods */

    public void setOnCardClickListener(@Nullable OnCardClickListener<Entity> onCardClickListener) {
        this.onCardClickListener = onCardClickListener;
    }

    public void setOnDistributedCardsListener(@Nullable OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        this.onDistributedCardsListener = onDistributedCardsListener;
    }

    public void setDistributionState(@Nullable DistributionState<Entity> distributionState) {
        this.distributionState = distributionState;
        if (distributionState == null) {
            return;
        }
        setCardsBeforeDistribution(distributionState.getPredicateForCardsBeforeDistribution(), distributionState.getDeskOfCardsUpdater());
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

    public void setCardsBeforeDistribution() {
        if (distributionState == null) {
            throw new RuntimeException("You need to set distribution state before");
        }
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

    public void startDistributeCards() {
        if (distributionState == null) {
            throw new RuntimeException("You need to set distribution state before");
        }
        startDistributeCards(distributionState.getPredicateForCardsForDistribution(), distributionState.getDeskOfCardsUpdater().getPosition());
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

    protected void onDistributedCards() {
        if (distributionState != null) {
            distributionState.setCardsAlreadyDistributed(true);
            distributionState.getDeskOfCardsUpdater().clear();
        }
        if (GameTableLayout.this.onDistributedCardsListener != null) {
            GameTableLayout.this.onDistributedCardsListener.onDistributedCards();
        }
    }

    protected void onStartDistributedCardWave(CardView<Entity>[] cardViews) {
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
                    (distributionState != null && deskOfCardsEnable || cardsView.getVisibility() != VISIBLE)) {
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
        if (distributionState != null && deskOfCardsEnable) {
            cardView.getCardInfo().setCardDistributed(true);
        } else {
            cardView.setVisibility(VISIBLE);
        }
        cardsLayout.invalidateCardsPosition(true, view -> {
            if (view.equals(cardView)) {
                AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(view)
                        .setX(AwesomeAnimation.CoordinationMode.COORDINATES, distributeFromCoordinates[0], view.getCardInfo().getFirstPositionX())
                        .setY(AwesomeAnimation.CoordinationMode.COORDINATES, distributeFromCoordinates[1], view.getCardInfo().getFirstPositionY())
                        .setRotation(180, cardView.getCardInfo().getFirstRotation())
                        .setDuration(durationOfDistributeAnimation);
                if (cardsLayout.interpolator != null) {
                    awesomeAnimation.setInterpolator(cardsLayout.interpolator);
                }
                return awesomeAnimation.build().getAnimatorSet();
            } else {
                return cardsLayout.getDefaultCreateAnimatorAction().createAnimation(view);
            }
        }, adapter);
    }
}
