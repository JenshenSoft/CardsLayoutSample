package ua.jenshensoft.cardslayout.util;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.listeners.OnUpdateDeskOfCardsUpdater;
import ua.jenshensoft.cardslayout.views.CardView;

public abstract class DistributionState<Entity> {

    private boolean isCardsAlreadyDistributed;

    protected DistributionState(boolean isCardsAlreadyDistributed) {
        this.isCardsAlreadyDistributed = isCardsAlreadyDistributed;
    }

    public boolean isCardsAlreadyDistributed() {
        return isCardsAlreadyDistributed;
    }

    public void setCardsAlreadyDistributed(boolean cardsAlreadyDistributed) {
        isCardsAlreadyDistributed = cardsAlreadyDistributed;
    }

    public abstract Predicate<CardView<Entity>> getPredicateForCardsForDistribution();

    public abstract Predicate<CardView<Entity>> getPredicateForCardsBeforeDistribution();

    public abstract OnUpdateDeskOfCardsUpdater<Entity> getDeskOfCardsUpdater();
}