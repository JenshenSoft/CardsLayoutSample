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

    public int getFirstPositionY() {
        return firstPositionY;
    }

    public int getCardsLayoutWidth() {
        return cardsLayoutWidth;
    }

    public int getCardsLayoutHeight() {
        return cardsLayoutHeight;
    }
}