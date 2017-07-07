package ua.jenshensoft.cardslayout.views.card;


import android.support.annotation.Px;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.views.ValidateViewBlocker;

public interface Card extends ValidateViewBlocker {

    int START_TO_CURRENT = 1;
    int LAST_TO_CURRENT = 2;

    CardInfo getCardInfo();

    void setCardInfo(CardInfo objectCardInfo);

    boolean isCardDragged();

    /* params */

    int getVisibility();

    void setVisibility(int visible);

    float getX();

    void setX(@Px float x);

    float getY();

    void setY(@Px float y);

    float getRotation();

    void setRotation(float rotation);

    float getCardZ();

    void setCardZ(@Px float z);

    int getCardWidth();

    int getCardHeight();

    void setEnabled(boolean enable);

    /* attr */

    void setFirstX(float cardPositionX);

    void setFirstY(float cardPositionY);

    void setFirstRotation(float rotation);

    float getNormalElevation();

    float getPressedElevation();

    /**
     * @param mode 0 - LEFT_RIGHT , 1 - UP_BOTTOM, 2 - BOTH, 3 - NONE
     */
    void setSwipeOrientationMode(int mode);

    void setCardTranslationListener(OnCardTranslationListener cardTranslationListener);

    void setCardSwipedListener(OnCardSwipedListener cardSwipedListener);

    /**
     * @param mode 0 - START_TO_CURRENT , 1 - LAST_TO_CURRENT
     */
    void setCardPercentageChangeListener(OnCardPercentageChangeListener cardPercentageChangeListener, int mode);

    void addBlock(int orientationMode);

    void removeBlock(int orientationMode);

    void setSwipeSpeed(int swipeSpeed);

    void setSwipeOffset(float swipeOffset);

    void setScrollAndClickableState(boolean scrollAndClickable);
}
