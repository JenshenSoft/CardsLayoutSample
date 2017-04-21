package ua.jenshensoft.cardslayout.listeners.table;

import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.views.card.Card;

import static ua.jenshensoft.cardslayout.util.SwipeGestureManager.EPSILON;

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

            cardInfo.setFirstPositionX(Math.round(x));
            card.setX(Math.round(x));
            x -= getShadowOffset();
            cardInfo.setFirstPositionY(Math.round(y));
            card.setY(Math.round(y));
            y -= getShadowOffset();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Math.abs(z - (-1)) < EPSILON) {
                    z = card.getElevation();
                }
                card.setElevation(Math.round(z));
                z -= getShadowOffset();
            }
        }
    }

    public void clear() {
        cards.clear();
    }

    public void removeCardsFromDesk(List<Card<Entity>> cards) {
        this.cards.removeAll(cards);
    }
}