package ua.jenshensoft.cardslayout.views.card;


import android.os.Build;
import android.support.annotation.Px;
import android.support.annotation.RequiresApi;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.card.OnCardTranslationListener;
import ua.jenshensoft.cardslayout.views.ValidateViewBlocker;

public interface Card<Entity> extends ValidateViewBlocker {

    int START_TO_CURRENT = 1;
    int LAST_TO_CURRENT = 2;

    CardInfo<Entity> getCardInfo();

    void setCardInfo(CardInfo<Entity> objectCardInfo);

    /* params */

    void setVisibility(int visible);

    int getVisibility();

    void setX(@Px float x);

    float getX();

    void setY(@Px float y);

    float getY();

    void setRotation(float rotation);

    float getRotation();

    void setCardZ(@Px float z);

    float getCardZ();

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

    void setCardTranslationListener(OnCardTranslationListener<Entity> cardTranslationListener);

    void setCardSwipedListener(OnCardSwipedListener<Entity> cardSwipedListener);

    /**
     * @param mode 0 - START_TO_CURRENT , 1 - LAST_TO_CURRENT
     */
    void setCardPercentageChangeListener(OnCardPercentageChangeListener<Entity> cardPercentageChangeListener, int mode);

    void addBlock(int orientationMode);

    void removeBlock(int orientationMode);

    void setSwipeSpeed(int swipeSpeed);

    void setSwipeOffset(float swipeOffset);

    void setScrollAndClickableState(boolean scrollAndClickable);
}
