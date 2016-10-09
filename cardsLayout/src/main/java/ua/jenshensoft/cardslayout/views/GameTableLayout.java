package ua.jenshensoft.cardslayout.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.util.AwesomeAnimation;

public abstract class GameTableLayout<
        Entity,
        Layout extends CardsLayout<Entity>>
        extends FrameLayout {

    //Views
    protected List<Layout> cardsLayouts;
    @Nullable
    protected ColorFilter colorFilter;
    private int durationOfDistributeAnimation;

    @Nullable
    private OnCardClickListener<Entity> onCardClickListener;
    @Nullable
    private OnDistributedCardsListener onDistributedCardsListener;
    private boolean isCardDistributed;

    private int countOfDistributedLayouts;

    public GameTableLayout(Context context) {
        super(context);
        inflateLayout();
        if (!isInEditMode()) {
            initLayout(null);
        }
    }

    public GameTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateLayout();
        if (!isInEditMode()) {
            initLayout(attrs);
        }
    }

    public GameTableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout();
        if (!isInEditMode()) {
            initLayout(attrs);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GameTableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateLayout();
        if (!isInEditMode()) {
            initLayout(attrs);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isCardDistributed) {
            return;
        }
        DistributionState<Entity> distributionState = provideDistributionState();
        if (distributionState != null) {
            if (!distributionState.isCardsAlreadyDistributed()) {
                startDistributeCards(
                        distributionState.getPredicateForCardsForDistribution(),
                        distributionState.provideCoordinateForDistribution());
            } else {
                if (onDistributedCardsListener != null) {
                    onDistributedCardsListener.onDistributedCards();
                }
            }
        }
        isCardDistributed = true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (colorFilter != null) {
            getCardsLayoutCurrentPlayer().setEnabledExceptViewsWithPositionsWithFilter(enabled, colorFilter);
        } else {
            getCardsLayoutCurrentPlayer().setEnabled(enabled);
        }
    }


    /* public methods */

    public void setOnCardClickListener(@Nullable OnCardClickListener<Entity> onCardClickListener) {
        this.onCardClickListener = onCardClickListener;
    }

    public void setOnDistributedCardsListener(@Nullable OnDistributedCardsListener onDistributedCardsListener) {
        this.onDistributedCardsListener = onDistributedCardsListener;
    }

    public void setColorFilter(@ColorInt int color) {
        this.colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public Layout getCardsLayoutCurrentPlayer() {
        return cardsLayouts.get(0);
    }

    public void setSwipeValidatorEnabled(boolean enabled) {
        final Layout cardsLayout = getCardsLayoutCurrentPlayer();
        if (enabled) {
            cardsLayout.setOnCardSwipedListener(cardInfo -> onActionWithCard(cardInfo.getEntity()));
        } else {
            cardsLayout.setOnCardSwipedListener(null);
        }
    }

    public void setTransitionValidatorEnabled(boolean enabled) {
        final Layout cardsLayout = getCardsLayoutCurrentPlayer();
        if (enabled) {
            cardsLayout.setCardPercentageChangeListener((percentageX, percentageY, cardInfo, isTouched) -> {
                if (!isTouched) {
                    if ((percentageX >= 100 || percentageY >= 100)) {
                        onActionWithCard(cardInfo.getEntity());
                        return;
                    }
                    if (colorFilter != null) {
                        cardsLayout.setEnabledExceptViewsWithPositionsWithFilter(true, null);
                    } else {
                        cardsLayout.setEnabled(true);
                    }
                } else {
                    if (colorFilter != null) {
                        cardsLayout.setEnabledExceptViewsWithPositionsWithFilter(false, colorFilter, cardInfo.getCardPositionInLayout());
                    } else {
                        cardsLayout.setEnabledExceptViewsWithPositions(false, cardInfo.getCardPositionInLayout());
                    }
                }
            });
        } else {
            cardsLayout.setCardPercentageChangeListener(null);
        }
    }

    public void setDurationOfDistributeAnimation(int durationOfDistributeAnimation) {
        this.durationOfDistributeAnimation = durationOfDistributeAnimation;
    }


    /* protected methods */

    protected abstract int[] provideCardsLayoutsIds();

    protected abstract int getLayoutId();

    @Nullable
    protected abstract DistributionState<Entity> provideDistributionState();

    protected void setCardsBeforeDistribution(Predicate<CardView<Entity>> beforeDistributionPredicate) {
        for (Layout cardsLayout : cardsLayouts) {
            for (CardView<Entity> cardView : cardsLayout.getCardViews()) {
                if (beforeDistributionPredicate.apply(cardView)) {
                    cardView.setVisibility(VISIBLE);
                } else {
                    cardView.setVisibility(INVISIBLE);
                }
            }
        }
    }

    protected void startDistributeCards(Predicate<CardView<Entity>> predicate, float[] distributeFromCoordinates) {
        countOfDistributedLayouts = 0;
        for (Layout cardsLayout : cardsLayouts) {
            distributeCardForPlayer(cardsLayout, predicate, distributeFromCoordinates, () -> {
                countOfDistributedLayouts++;
                if (countOfDistributedLayouts == cardsLayouts.size()) {
                    if (this.onDistributedCardsListener != null) {
                        this.onDistributedCardsListener.onDistributedCards();
                    }
                }
            });
        }
    }


    /* private methods */

    @SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
    private void inflateLayout() {
        inflate(getContext(), getLayoutId(), this);
        cardsLayouts = new ArrayList<>();
        int[] layoutsIds = provideCardsLayoutsIds();
        for (int i = 0; i < layoutsIds.length; i++) {
            cardsLayouts.add((Layout) findViewById(layoutsIds[i]));
        }
        DistributionState<Entity> distributionState = provideDistributionState();
        if (distributionState != null) {
            Predicate<CardView<Entity>> beforeDistributionPredicate = distributionState.getPredicateForCardsBeforeDistribution();
            setCardsBeforeDistribution(beforeDistributionPredicate);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void initLayout(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.GameTableLayout_Params);
            try {
                final int color = attributes.getColor(R.styleable.GameTableLayout_Params_gameTableLayout_tintColor, -1);
                durationOfDistributeAnimation = attributes.getInteger(R.styleable.GameTableLayout_Params_gameTableLayout_duration_distributeAnimation, 3000);
                if (color != -1) {
                    colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
                }

                if (attributes.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardValidatorSwipe, false)) {
                    setSwipeValidatorEnabled(true);
                }
                if (attributes.getBoolean(R.styleable.GameTableLayout_Params_gameTableLayout_cardValidatorTransition, false)) {
                    setTransitionValidatorEnabled(true);
                }
            } finally {
                attributes.recycle();
            }
        }
    }

    private void onActionWithCard(Entity entity) {
        if (onCardClickListener != null) {
            onCardClickListener.onCardAction(entity);
        }
    }

    private void distributeCardForPlayer(Layout cardsLayout,
                                         Predicate<CardView<Entity>> predicate,
                                         float[] distributeFromCoordinates,
                                         OnDistributedCardsListener onDistributedCardsListener) {
        List<CardView<Entity>> filteredCardsViews = new ArrayList<>();
        for (CardView<Entity> cardsView : cardsLayout.getCardViews()) {
            if (predicate.apply(cardsView) && cardsView.getVisibility() != VISIBLE) {
                filteredCardsViews.add(cardsView);
            }
        }

        animateCardViews(cardsLayout, filteredCardsViews.iterator(), distributeFromCoordinates, onDistributedCardsListener);
    }

    private void animateCardViews(Layout cardsLayout,
                                  Iterator<CardView<Entity>> cardViewsIterator,
                                  float[] distributeFromCoordinates,
                                  OnDistributedCardsListener onDistributedCardsListener) {
        if (cardViewsIterator.hasNext()) {
            animateCardView(cardsLayout, cardViewsIterator.next(), distributeFromCoordinates, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
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
        cardView.setVisibility(VISIBLE);
        cardsLayout.invalidateCardsPosition(true, view -> {
            if (view.equals(cardView)) {
                AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(view)
                        .setX(AwesomeAnimation.CoordinationMode.COORDINATES, distributeFromCoordinates[0], view.getCardInfo().getFirstPositionX())
                        .setY(AwesomeAnimation.CoordinationMode.COORDINATES, distributeFromCoordinates[1], view.getCardInfo().getFirstPositionY())
                        .setRotation(0, 180)
                        .setDuration(durationOfDistributeAnimation)
                        .setInterpolator(cardsLayout.interpolator);
                return awesomeAnimation.build().getAnimatorSet();
            } else {
                return cardsLayout.getDefaultCreateAnimatorAction().createAnimation(view);
            }
        }, adapter);
    }


    /* inner types */

    @FunctionalInterface
    public interface OnCardClickListener<Entity> {
        void onCardAction(@Nullable Entity entity);
    }

    @FunctionalInterface
    public interface OnDistributedCardsListener {
        void onDistributedCards();
    }

    public abstract static class DistributionState<Entity> {

        private final boolean isCardsAlreadyDistribution;

        public DistributionState(boolean isCardsAlreadyDistribution) {
            this.isCardsAlreadyDistribution = isCardsAlreadyDistribution;
        }

        public boolean isCardsAlreadyDistributed() {
            return isCardsAlreadyDistribution;
        }

        public abstract Predicate<CardView<Entity>> getPredicateForCardsForDistribution();

        public abstract Predicate<CardView<Entity>> getPredicateForCardsBeforeDistribution();

        public abstract float[] provideCoordinateForDistribution();
    }
}
