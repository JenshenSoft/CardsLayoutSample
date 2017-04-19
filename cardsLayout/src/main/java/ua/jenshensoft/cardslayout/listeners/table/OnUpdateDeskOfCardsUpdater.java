package ua.jenshensoft.cardslayout.listeners.table;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.views.card.Card;

public abstract class OnUpdateDeskOfCardsUpdater<Entity> {

    private List<Card<Entity>> cards;

    protected OnUpdateDeskOfCardsUpdater() {
        cards = new ArrayList<>();
    }

    /**
     called in on the onMeasure method
     * @return
     */
    public abstract float[] getPosition();

    public void addCards(@NonNull List<Card<Entity>> cards) {
        this.cards.addAll(cards);
    }

    public void updatePosition() {
        float[] position = getPosition();
        for (Card<Entity> card : cards) {
            CardInfo<Entity> cardInfo = card.getCardInfo();
            cardInfo.setFirstPositionX((int) position[0]);
            cardInfo.setFirstPositionY((int) position[1]);
            card.setX((int) position[0]);
            card.setY((int) position[1]);
        }
    }

    public void clear() {
        cards.clear();
    }

    public void removeCardsFromDesk(List<Card<Entity>> cards) {
        this.cards.removeAll(cards);
    }
}