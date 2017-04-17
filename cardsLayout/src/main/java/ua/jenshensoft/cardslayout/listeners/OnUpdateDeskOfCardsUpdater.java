package ua.jenshensoft.cardslayout.listeners;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;

public abstract class OnUpdateDeskOfCardsUpdater<Entity> {

    private List<CardInfo<Entity>> cards;

    protected OnUpdateDeskOfCardsUpdater() {
        cards = new ArrayList<>();
    }

    protected abstract float[] getPosition();

    public void addCards(@NonNull List<CardInfo<Entity>> cards) {
        this.cards.addAll(cards);
    }

    public void updatePosition() {
        float[] position = getPosition();
        for (CardInfo<Entity> cardInfo : cards) {
            cardInfo.setFirstPositionX((int) position[0]);
            cardInfo.setFirstPositionX((int) position[1]);
        }
    }

    public void clear() {
        cards.clear();
    }
}