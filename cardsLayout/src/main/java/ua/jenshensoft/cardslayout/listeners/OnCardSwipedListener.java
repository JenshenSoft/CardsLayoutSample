package ua.jenshensoft.cardslayout.listeners;


import ua.jenshensoft.cardslayout.CardInfo;

public interface OnCardSwipedListener<Entity> {

    void onCardSwiped(CardInfo<Entity> cardInfo);

}
