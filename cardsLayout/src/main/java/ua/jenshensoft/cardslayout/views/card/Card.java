package ua.jenshensoft.cardslayout.views.card;


import android.support.annotation.Px;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;

public interface Card<Entity> {

    int START_TO_CURRENT = 1;
    int LAST_TO_CURRENT = 2;

    CardInfo<Entity> getCardInfo();

    /* params */

    void setVisibility(int visible);

    int getVisibility();

    void setX(@Px float x);

    void setY(@Px float y);

    void setRotation(float rotation);

    /**
     * @param mode 0 - LEFT_RIGHT , 1 - UP_BOTTOM, 2 - BOTH, 3 - NONE
     */
    void setSwipeOrientationMode(int mode);

    void setCardTranslationListener(OnCardTranslationListener<Entity> cardTranslationListener);

    void setCardSwipedListener(OnCardSwipedListener<Entity> cardSwipedListener);

    /**
     * @param mode 0 - START_TO_CURRENT , 1 - LAST_TO_CURRENT
     */
    void setCardPercentageChangeListener(OnCardPercentageChangeListener<Entity> cardPercentageChangeListener, int mode);
}
