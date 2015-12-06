package ua.jenshensoft.cardslayout.listeners;

import ua.jenshensoft.cardslayout.CardInfo;

public interface OnCardPercentageChangeListener {

    void percentageX(float percentage, CardInfo cardInfo);

    void percentageY(float percentage, CardInfo cardInfo);
}
