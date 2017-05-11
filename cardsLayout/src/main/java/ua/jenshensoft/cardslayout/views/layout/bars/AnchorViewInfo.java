package ua.jenshensoft.cardslayout.views.layout.bars;

class AnchorViewInfo {
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

    int getFirstPositionX() {
        return firstPositionX;
    }

    int getFirstPositionY() {
        return firstPositionY;
    }

    int getCardsLayoutWidth() {
        return cardsLayoutWidth;
    }

    int getCardsLayoutHeight() {
        return cardsLayoutHeight;
    }
}