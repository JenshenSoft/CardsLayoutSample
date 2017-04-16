package ua.jenshensoft.cardslayout.listeners;

import ua.jenshensoft.cardslayout.views.CardView;

public interface OnDistributedCardsListener<Entity> {
    void onDistributedCards();

    void onStartDistributedCardWave(CardView<Entity>... cardViews);

    void onEndDistributeCardWave(CardView<Entity>... cardViews);
}