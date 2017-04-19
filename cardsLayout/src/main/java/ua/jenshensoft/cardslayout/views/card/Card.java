package ua.jenshensoft.cardslayout.views.card;


import ua.jenshensoft.cardslayout.CardInfo;

public interface Card<Entity> {

    int START_TO_CURRENT = 1;
    int LAST_TO_CURRENT = 2;

    CardInfo<Entity> getCardInfo();
}
