package ua.jenshensoft.cardslayout.util;

import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.listeners.OnUpdateDeskOfCardsUpdater;
import ua.jenshensoft.cardslayout.views.CardView;

public abstract class DistributionState<Entity> {

    @Nullable
    private OnUpdateDeskOfCardsUpdater<Entity> deskOfCardsUpdater;
    private boolean isCardsAlreadyDistributed;

    protected DistributionState(boolean isCardsAlreadyDistributed) {
        this.isCardsAlreadyDistributed = isCardsAlreadyDistributed;
    }

    protected abstract OnUpdateDeskOfCardsUpdater<Entity> provideDeskOfCardsUpdater() ;

    public OnUpdateDeskOfCardsUpdater<Entity> getDeskOfCardsUpdater() {
        if (deskOfCardsUpdater == null) {
            deskOfCardsUpdater = provideDeskOfCardsUpdater();
        }
        return deskOfCardsUpdater;
    }

    public boolean isCardsAlreadyDistributed() {
        return isCardsAlreadyDistributed;
    }

    public void setCardsAlreadyDistributed(boolean cardsAlreadyDistributed) {
        isCardsAlreadyDistributed = cardsAlreadyDistributed;
    }

    public abstract Predicate<CardView<Entity>> getPredicateForCardsForDistribution();

    public abstract Predicate<CardView<Entity>> getPredicateForCardsBeforeDistribution();

}