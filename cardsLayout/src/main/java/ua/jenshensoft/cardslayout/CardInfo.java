package ua.jenshensoft.cardslayout;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CardInfo<Entity> {
    private int cardPositionInLayout;
    private int firstPositionX;
    private int firstPositionY;
    private int currentPositionX;
    private int currentPositionY;
    private int firstRotation;
    private int currentRotation;
    private boolean cardDistributed = true;
    @Nullable
    private Entity entity;

    public CardInfo(int cardPositionInLayout) {
        this.cardPositionInLayout = cardPositionInLayout;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    public boolean isCardDistributed() {
        return cardDistributed;
    }

    public void setCardDistributed(boolean cardDistributed) {
        this.cardDistributed = cardDistributed;
    }

    public void setEntity(@NonNull Entity entity) {
        this.entity = entity;
    }

    public int getFirstPositionX() {
        return firstPositionX;
    }

    public void setFirstPositionX(int firstPositionX) {
        this.firstPositionX = firstPositionX;
        setCurrentPositionX(firstPositionX);
    }

    public int getFirstPositionY() {
        return firstPositionY;
    }

    public void setFirstPositionY(int firstPositionY) {
        this.firstPositionY = firstPositionY;
        setCurrentPositionY(firstPositionY);
    }

    public int getCurrentPositionX() {
        return currentPositionX;
    }

    public void setCurrentPositionX(int currentPositionX) {
        this.currentPositionX = currentPositionX;
    }

    public int getCurrentPositionY() {
        return currentPositionY;
    }

    public void setCurrentPositionY(int currentPositionY) {
        this.currentPositionY = currentPositionY;
    }

    public void setFirstRotation(int firstRotation) {
        this.firstRotation = firstRotation;
        setCurrentRotation(firstRotation);
    }

    public int getFirstRotation() {
        return firstRotation;
    }

    public void setCurrentRotation(int currentRotation) {
        this.currentRotation = currentRotation;
    }

    public int getCurrentRotation() {
        return currentRotation;
    }

    public int getCardPositionInLayout() {
        return cardPositionInLayout;
    }

    public void setCardPositionInLayout(int cardPositionInLayout) {
        this.cardPositionInLayout = cardPositionInLayout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardInfo<?> cardInfo = (CardInfo<?>) o;
        return cardPositionInLayout == cardInfo.cardPositionInLayout;
    }

    @Override
    public int hashCode() {
        return cardPositionInLayout;
    }
}
