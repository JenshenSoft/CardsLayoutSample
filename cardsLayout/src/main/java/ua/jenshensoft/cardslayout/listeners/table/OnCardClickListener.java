package ua.jenshensoft.cardslayout.listeners.table;

import ua.jenshensoft.cardslayout.CardInfo;

@FunctionalInterface
public interface OnCardClickListener {
    void onCardAction(CardInfo cardInfo);
}