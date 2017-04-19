package ua.jenshensoft.cardslayout.listeners.card;

import ua.jenshensoft.cardslayout.CardInfo;

public interface OnCardPercentageChangeListener<Entity> {

    /**
     * difference between last position and current position (for swipe mode)
     *
     * @param percentageX min 0, max 100
     * @param percentageY min 0, max 100
     * @param cardInfo
     * @param isTouched
     */
    void onPercentageChanged(float percentageX, float percentageY, CardInfo<Entity> cardInfo, boolean isTouched);
}
