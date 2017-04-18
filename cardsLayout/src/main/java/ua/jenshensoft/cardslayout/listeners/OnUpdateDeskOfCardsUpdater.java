package ua.jenshensoft.cardslayout.listeners;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.views.CardView;

public abstract class OnUpdateDeskOfCardsUpdater<Entity> {

    private List<CardView<Entity>> cards;

    protected OnUpdateDeskOfCardsUpdater() {
        cards = new ArrayList<>();
    }

    /**
     called in on the onMeasure method
     * @return
     */
    public abstract float[] getPosition();

    public void addCards(@NonNull List<CardView<Entity>> cards) {
        this.cards.addAll(cards);
    }

    public void updatePosition() {
        float[] position = getPosition();
        for (CardView<Entity> cardView : cards) {
            CardInfo<Entity> cardInfo = cardView.getCardInfo();
            cardInfo.setFirstPositionX((int) position[0]);
            cardInfo.setFirstPositionY((int) position[1]);
            cardView.setX((int) position[0]);
            cardView.setY((int) position[1]);
        }
    }

    public void clear() {
        cards.clear();
    }

    public void removeCardsFromDesk(List<CardView<Entity>> cardViews) {
        cards.removeAll(cardViews);
    }
}