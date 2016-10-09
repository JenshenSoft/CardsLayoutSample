package ua.jenshensoft.cardslayout;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CardInfo<Entity> {
    private int cardPositionInLayout;
    private int firstPositionX;
    private int firstPositionY;
    private int lastPositionX;
    private int lastPositionY;
    private int currentPositionX;
    private int currentPositionY;
    @Nullable
    private Entity entity;

    public CardInfo(int cardPositionInLayout) {
        this.cardPositionInLayout = cardPositionInLayout;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
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

    public int getLastPositionX() {
        return lastPositionX;
    }

    public void setLastPositionX(int lastPositionX) {
        this.lastPositionX = lastPositionX;
    }

    public int getLastPositionY() {
        return lastPositionY;
    }

    public void setLastPositionY(int lastPositionY) {
        this.lastPositionY = lastPositionY;
    }

    public int getCardPositionInLayout() {
        return cardPositionInLayout;
    }

    public void setCardPositionInLayout(int cardPositionInLayout) {
        this.cardPositionInLayout = cardPositionInLayout;
    }

}
