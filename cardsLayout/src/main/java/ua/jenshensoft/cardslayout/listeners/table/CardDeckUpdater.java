package ua.jenshensoft.cardslayout.listeners.table;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.views.card.Card;

public abstract class CardDeckUpdater<Entity> {

    @Nullable
    private List<Card<Entity>> cards;

    /**
     * called in on the onMeasure method
     *
     * @return
     */
    public abstract CardDeckLocation getLocation();

    public void setCardsForCardDeck(@NonNull List<Card<Entity>> cards) {
        this.cards = cards;
    }

    public void updatePosition() {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        CardDeckLocation location = getLocation();
        float shadowXOffset = location.getXCardOffset();
        float shadowYOffset = location.getYCardOffset();
        float shadowZOffset = location.getZCardOffset();
        float x = location.getX();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            x -= (cards.size() * shadowXOffset);
        } else {
            x += (cards.size() * shadowXOffset);
        }
        float y = location.getY() - (cards.size() * shadowYOffset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            y -= (cards.size() * shadowYOffset);
        } else {
            y += (cards.size() * shadowYOffset);
        }
        float z = location.getElevation() + (cards.size() * shadowZOffset);
        for (Card<Entity> card : cards) {
            CardInfo<Entity> cardInfo = card.getCardInfo();

            //x
            cardInfo.setFirstPositionX(Math.round(x));
            card.setX(Math.round(x));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                x += shadowXOffset;
            } else {
                x -= shadowXOffset;
            }

            //y
            cardInfo.setFirstPositionY(Math.round(y));
            card.setY(Math.round(y));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                y += shadowYOffset;
            } else {
                y -= shadowYOffset;
            }
            //z
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
        private final float elevation;
        private final float xCardOffset;
        private final float yCardOffset;
        private final float zCardOffset;

        public CardDeckLocation(Context context, float x, float y) {
            this(x,
                    y,
                    context.getResources().getDimension(R.dimen.cardsLayout_card_elevation_normal),
                    context.getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset),
                    context.getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset),
                    1);
        }

        public CardDeckLocation(float x,
                                float y,
                                float elevation,
                                float xCardOffset,
                                float yCardOffset,
                                float zCardOffset) {
            this.x = x;
            this.y = y;
            this.elevation = elevation;
            this.xCardOffset = xCardOffset;
            this.yCardOffset = yCardOffset;
            this.zCardOffset = zCardOffset;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getElevation() {
            return elevation;
        }

        public float getXCardOffset() {
            return xCardOffset;
        }

        public float getYCardOffset() {
            return yCardOffset;
        }

        public float getZCardOffset() {
            return zCardOffset;
        }
    }
}