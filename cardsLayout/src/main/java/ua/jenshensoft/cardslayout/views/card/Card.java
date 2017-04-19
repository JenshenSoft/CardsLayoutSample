package ua.jenshensoft.cardslayout.views.card;


import android.support.annotation.Px;

import ua.jenshensoft.cardslayout.CardInfo;

public interface Card<Entity> {

    int START_TO_CURRENT = 1;
    int LAST_TO_CURRENT = 2;

    CardInfo<Entity> getCardInfo();

    void setVisibility(int visible);

    void setX(@Px float x);

    void setY(@Px float y);
}
