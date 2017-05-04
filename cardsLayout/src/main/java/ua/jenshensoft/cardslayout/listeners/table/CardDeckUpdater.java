package ua.jenshensoft.cardslayout.listeners.table;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.views.card.Card;

public abstract class CardDeckUpdater<Entity> {

    private List<Card<Entity>> cards;

    protected CardDeckUpdater() {
        cards = new ArrayList<>();
    }

    /**
     * called in on the onMeasure method
     *
     * @return
     */
    public abstract CardDeckLocation getLocation();

    public List<Card<Entity>> getCards() {
        return cards;
    }

    public void addCardsToCardDeck(@NonNull List<Card<Entity>> cards) {
        this.cards.addAll(cards);
    }

    public void updatePosition() {
        if (cards.isEmpty()) {
            return;
        }
        CardDeckLocation location = getLocation();
        float shadowXOffset = location.getXCardOffset();
        float shadowYOffset = location.getYCardOffset();
        float shadowZOffset = (location.getElevationMax() - location.getElevationMin()) / cards.size();
        float x = location.getX() - (cards.size() * shadowXOffset);
        float y = location.getY() - (cards.size() * shadowYOffset);
        float z = location.getElevationMax();
        for (Card<Entity> card : cards) {
            CardInfo<Entity> cardInfo = card.getCardInfo();

            cardInfo.setFirstPositionX(Math.round(x));
            card.setX(Math.round(x));
            x += shadowXOffset;
            cardInfo.setFirstPositionY(Math.round(y));
            card.setY(Math.round(y));
            y += shadowYOffset;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                card.setElevation(Math.round(z));
                z -= shadowZOffset;
            }
        }
    }

    public void removeCardsFromDesk(List<Card<Entity>> cards) {
        this.cards.removeAll(cards);
    }

    public static class CardDeckLocation {

        private final float x;
        private final float y;
        private final float elevationMin;
        private final float elevationMax;
        private final float xCardOffset;
        private final float yCardOffset;
        private final float zCardOffset;

        public CardDeckLocation(Context context, float x, float y) {
            this(x,
                    y,
                    context.getResources().getDimension(R.dimen.cardsLayout_card_elevation_normal),
                    context.getResources().getDimension(R.dimen.cardsLayout_card_elevation_pressed),
                    context.getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset),
                    context.getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset),
                    1);
        }

        public CardDeckLocation(float x,
                                float y,
                                float elevationMin,
                                float elevationMax,
                                float xCardOffset,
                                float yCardOffset,
                                float zCardOffset) {
            this.x = x;
            this.y = y;
            this.elevationMin = elevationMin;
            this.elevationMax = elevationMax;
            this.xCardOffset = xCardOffset;
            this.yCardOffset = yCardOffset;
            this.zCardOffset = zCardOffset;
        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }

        float getElevationMin() {
            return elevationMin;
        }

        float getElevationMax() {
            return elevationMax;
        }

        float getXCardOffset() {
            return xCardOffset;
        }

        float getYCardOffset() {
            return yCardOffset;
        }

        float getZCardOffset() {
            return zCardOffset;
        }
    }
}