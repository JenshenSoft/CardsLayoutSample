package ua.jenshensoft.cardslayout.listeners.table;

import java.util.List;

import ua.jenshensoft.cardslayout.views.card.Card;

public interface OnDistributedCardsListener<Entity> {
    void onDistributedCards();

    void onStartDistributedCardWave(List<Card<Entity>> cards);

    void onEndDistributeCardWave(List<Card<Entity>> cards);
}