package ua.jenshensoft.cardslayout.pattern.models;

public class CardCoordinates {
    private float x;
    private float y;
    private float angle;

    public CardCoordinates(float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "CardCoordinates{" +
                "x=" + x +
                ", y=" + y +
                ", angle=" + angle +
                '}';
    }
}