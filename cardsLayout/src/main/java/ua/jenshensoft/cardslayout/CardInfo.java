package ua.jenshensoft.cardslayout;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CardInfo {
    private int cardPositionInLayout;
    private int firstPositionX;
    private int firstPositionY;
    private int firstRotation;
    private boolean hasFilter;
    private boolean cardDistributed = true;
    @Nullable
    private Object entity;

    public CardInfo(int cardPositionInLayout) {
        this.cardPositionInLayout = cardPositionInLayout;
    }

    @Nullable
    public Object getEntity() {
        return entity;
    }

    public boolean isCardDistributed() {
        return cardDistributed;
    }

    public void setCardDistributed(boolean cardDistributed) {
        this.cardDistributed = cardDistributed;
    }

    public void setEntity(@NonNull Object entity) {
        this.entity = entity;
    }

    public int getFirstPositionX() {
        return firstPositionX;
    }

    public void setFirstPositionX(int firstPositionX) {
        this.firstPositionX = firstPositionX;
    }

    public int getFirstPositionY() {
        return firstPositionY;
    }

    public void setFirstPositionY(int firstPositionY) {
        this.firstPositionY = firstPositionY;
    }

    public void setFirstRotation(int firstRotation) {
        this.firstRotation = firstRotation;
    }

    public int getFirstRotation() {
        return firstRotation;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardInfo  cardInfo = (CardInfo) o;

        if (cardPositionInLayout != cardInfo.cardPositionInLayout) return false;
        return entity != null ? entity.equals(cardInfo.entity) : cardInfo.entity == null;
    }

    @Override
    public int hashCode() {
        int result = cardPositionInLayout;
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}


