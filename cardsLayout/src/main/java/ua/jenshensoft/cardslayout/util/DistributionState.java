package ua.jenshensoft.cardslayout.util;

import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.listeners.table.CardDeckUpdater;
import ua.jenshensoft.cardslayout.views.card.Card;

public abstract class DistributionState<Entity> {

    @Nullable
    private CardDeckUpdater<Entity> deskOfCardsUpdater;
    private boolean isCardsAlreadyDistributed;

    protected DistributionState(boolean isCardsAlreadyDistributed) {
        this.isCardsAlreadyDistributed = isCardsAlreadyDistributed;
    }

    protected abstract CardDeckUpdater<Entity> provideDeskOfCardsUpdater() ;

    public CardDeckUpdater<Entity> getDeskOfCardsUpdater() {
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

    /** set cards for players hands before the distribution
     * @return
     */
    public abstract Predicate<Card<Entity>> getCardsPredicateBeforeDistribution();

    /**
     * set cards for the distribution to players hands
     * @return
     */
    public abstract Predicate<Card<Entity>> getCardsPredicateForDistribution();
}