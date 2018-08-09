package ua.jenshensoft.cardslayout.listeners.card;


import ua.jenshensoft.cardslayout.CardInfo;

public interface OnCardTranslationListener {

    void onCardTranslation(float positionX, float positionY, CardInfo cardInfo, boolean isTouched);
}
