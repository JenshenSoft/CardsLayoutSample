package ua.jenshensoft.cardslayout.views.layout;

public class Config {
    private final float distanceBetweenViews;
    private final float distanceForCards;
    private float startCoordinates;

    Config(float startCoordinates, float distanceBetweenViews, float distanceForCards) {
        this.startCoordinates = startCoordinates;
        this.distanceBetweenViews = distanceBetweenViews;
        this.distanceForCards = distanceForCards;
    }

    public float getDistanceBetweenViews() {
        return distanceBetweenViews;
    }

    public float getDistanceForCards() {
        return distanceForCards;
    }

    public float getStartCoordinates() {
        return startCoordinates;
    }

    public void setStartCoordinates(float startCoordinates) {
        this.startCoordinates = startCoordinates;
    }
}