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

    protected float getShadowOffset() {
        return 0f;
    }

    /**
     * called in on the onMeasure method
     *
     * @return
     */
    public abstract float[] getPosition();

    public void addCards(@NonNull List<Card<Entity>> cards) {
        this.cards.addAll(cards);
    }

    public void updatePosition() {
        float[] position = getPosition();
        float x = position[0];
        float y = position[1];
        float z = -1;
        for (Card<Entity> card : cards) {
            CardInfo<Entity> cardInfo = card.getCardInfo();
            if (z == -1) {
                z = card.getElevation();
            }
            cardInfo.setFirstPositionX(Math.round(x));
            cardInfo.setFirstPositionY(Math.round(y));
            card.setX(Math.round(x));
            card.setY(Math.round(y));
            card.setElevation(Math.round(z));
            x -= getShadowOffset();
            y -= getShadowOffset();
            z -= getShadowOffset();
        }
    }

    public void clear() {
        cards.clear();
    }

    public void removeCardsFromDesk(List<Card<Entity>> cards) {
        this.cards.removeAll(cards);
    }
}