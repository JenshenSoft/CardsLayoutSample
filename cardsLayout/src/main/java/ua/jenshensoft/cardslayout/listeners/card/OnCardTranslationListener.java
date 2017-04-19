package ua.jenshensoft.cardslayout.listeners.card;


import ua.jenshensoft.cardslayout.CardInfo;

public interface OnCardTranslationListener<Entity> {

    void onCardTranslation(float positionX, float positionY, CardInfo<Entity> cardInfo, boolean isTouched);
}
