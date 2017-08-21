package ua.jenshensoft.cardslayout.pattern.models;

public class BarCoordinates extends CardCoordinates {

    private boolean spread;

    public BarCoordinates(float x, float y, boolean spread) {
        super(x, y, 0);
        this.spread = spread;
    }

    public boolean isSpread() {
        return spread;
    }
}