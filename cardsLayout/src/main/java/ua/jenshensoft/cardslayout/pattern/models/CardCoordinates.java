package ua.jenshensoft.cardslayout.pattern.models;

public class CardCoordinates {
    private final float x;
    private final float y;
    private final float angle;

    public CardCoordinates(float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}