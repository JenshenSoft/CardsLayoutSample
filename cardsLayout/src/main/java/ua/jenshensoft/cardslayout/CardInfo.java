package ua.jenshensoft.cardslayout;

public class CardInfo {

    private int cardIndex;
    private int lastPositionX;
    private int lastPositionY;
    private int currentPositionX;
    private int currentPositionY;

    public CardInfo(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public int getCurrentPositionX() {
        return currentPositionX;
    }

    public void setCurrentPositionX(int currentPositionX) {
        this.lastPositionX = this.currentPositionX;
        this.currentPositionX = currentPositionX;
    }

    public int getCurrentPositionY() {
        return currentPositionY;
    }

    public void setCurrentPositionY(int currentPositionY) {
        this.lastPositionY = this.currentPositionY;
        this.currentPositionY = currentPositionY;
    }

    public int getLastPositionX() {
        return lastPositionX;
    }

    public int getLastPositionY() {
        return lastPositionY;
    }

    public void setLastPositionX(int lastPositionX) {
        this.lastPositionX = lastPositionX;
    }

    public void setLastPositionY(int lastPositionY) {
        this.lastPositionY = lastPositionY;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(int cardIndex) {
        this.cardIndex = cardIndex;
    }
}
