package ua.jenshensoft.cardslayout.pattern.models;

public class ThreeDCardCoordinates extends CardCoordinates {

    private float z;

    public ThreeDCardCoordinates(float x, float y, float z, float angle) {
        super(x, y, angle);
    }

    public float getZ() {
        return z;
    }
}