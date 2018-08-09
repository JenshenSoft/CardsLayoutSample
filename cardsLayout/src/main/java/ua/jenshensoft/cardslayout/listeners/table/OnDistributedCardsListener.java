package ua.jenshensoft.cardslayout.listeners.table;

import java.util.List;

import ua.jenshensoft.cardslayout.views.card.Card;

public interface OnDistributedCardsListener {
    void onDistributedCards();

    void onStartDistributedCardWave(List<Card> cards);

    void onEndDistributeCardWave(List<Card> cards);
}