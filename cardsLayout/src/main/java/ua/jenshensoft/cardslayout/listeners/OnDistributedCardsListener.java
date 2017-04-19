package ua.jenshensoft.cardslayout.listeners;

import android.view.View;

import java.util.List;

import ua.jenshensoft.cardslayout.views.card.Card;

public interface OnDistributedCardsListener<Entity> {
    void onDistributedCards();

    <CV extends View & Card<Entity>> void onStartDistributedCardWave(List<CV> cards);

    <CV extends View & Card<Entity>> void onEndDistributeCardWave(List<CV> cards);
}