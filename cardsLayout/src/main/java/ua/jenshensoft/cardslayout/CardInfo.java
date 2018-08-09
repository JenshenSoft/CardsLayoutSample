package ua.jenshensoft.cardslayout;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ua.jenshensoft.cardslayout.views.FirstPosition;

public class CardInfo extends FirstPosition {
    private int cardPositionInLayout;
    private boolean hasFilter;
    private boolean cardDistributed = true;
    @Nullable
    private Object entity;

    public CardInfo(int cardPositionInLayout) {
        super(cardPositionInLayout);
        this.cardPositionInLayout = cardPositionInLayout;
    }

    public CardInfo(CardInfo cardInfo) {
        super(cardInfo.getCardPositionInLayout());
        this.cardPositionInLayout = cardInfo.getCardPositionInLayout();
        this.hasFilter = cardInfo.hasFilter();
        this.cardDistributed = cardInfo.isCardDistributed();
        this.entity = cardInfo.getEntity();
        setFirstPositionX(cardInfo.getFirstPositionX());
        setFirstPositionY(cardInfo.getFirstPositionY());
        setFirstRotation(cardInfo.getFirstRotation());
    }

    @Nullable
    public Object getEntity() {
        return entity;
    }

    public void setEntity(@NonNull Object entity) {
        this.entity = entity;
    }

    public boolean isCardDistributed() {
        return cardDistributed;
    }

    public void setCardDistributed(boolean cardDistributed) {
        this.cardDistributed = cardDistributed;
    }

    public int getCardPositionInLayout() {
        return cardPositionInLayout;
    }

    public void setCardPositionInLayout(int cardPositionInLayout) {
        this.cardPositionInLayout = cardPositionInLayout;
    }

    public void setHasFilter(boolean hasFilter) {
        this.hasFilter = hasFilter;
    }

    public boolean hasFilter() {
        return hasFilter;
    }

    @Override
    public String toString() {
        return "CardInfo{" +
                "cardPositionInLayout=" + cardPositionInLayout +
                ", hasFilter=" + hasFilter +
                ", cardDistributed=" + cardDistributed +
                ", entity=" + entity +
                '}';
    }
}


