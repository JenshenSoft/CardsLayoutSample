package ua.jenshensoft.cardslayout.util;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.views.card.Card;

public abstract class DistributionState {

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

    /**
     * set cards for players hands before the distribution
     *
     * @return
     */
    public abstract Predicate<Card> getCardsPredicateBeforeDistribution();

    /**
     * set cards for the distribution to players hands
     *
     * @return
     */
    public abstract Predicate<Card> getCardsPredicateForDistribution();
}