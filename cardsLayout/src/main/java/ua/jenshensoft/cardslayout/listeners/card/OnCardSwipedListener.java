package ua.jenshensoft.cardslayout.listeners.card;


import ua.jenshensoft.cardslayout.CardInfo;

public interface OnCardSwipedListener<Entity> {

    void onCardSwiped(CardInfo<Entity> cardInfo);

}
