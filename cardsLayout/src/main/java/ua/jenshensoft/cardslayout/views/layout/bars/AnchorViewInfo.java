package ua.jenshensoft.cardslayout.views.layout.bars;

public class AnchorViewInfo {
    private int firstPositionX;
    private int firstPositionY;
    private int cardsLayoutWidth;
    private int cardsLayoutHeight;

    AnchorViewInfo(int firstPositionX, int firstPositionY, int cardsLayoutWidth, int cardsLayoutHeight) {
        this.firstPositionX = firstPositionX;
        this.firstPositionY = firstPositionY;
        this.cardsLayoutWidth = cardsLayoutWidth;
        this.cardsLayoutHeight = cardsLayoutHeight;
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

    public int getCardsLayoutWidth() {
        return cardsLayoutWidth;
    }

    public void setCardsLayoutWidth(int cardsLayoutWidth) {
        this.cardsLayoutWidth = cardsLayoutWidth;
    }

    public int getCardsLayoutHeight() {
        return cardsLayoutHeight;
    }

    public void setCardsLayoutHeight(int cardsLayoutHeight) {
        this.cardsLayoutHeight = cardsLayoutHeight;
    }
}