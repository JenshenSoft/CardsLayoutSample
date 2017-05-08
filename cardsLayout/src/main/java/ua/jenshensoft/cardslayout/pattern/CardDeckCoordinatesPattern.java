package ua.jenshensoft.cardslayout.pattern;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.pattern.models.ThreeDCardCoordinates;


public class CardDeckCoordinatesPattern implements CardCoordinatesPattern<ThreeDCardCoordinates> {

    private final int cardsCount;
    private final float cardOffsetX;
    private final float cardOffsetY;
    private final float cardDeckX;
    private final float cardDeckY;
    private final float elevationMin;
    private final float elevationMax;

    public CardDeckCoordinatesPattern(int cardsCount,
                                      float cardOffsetX,
                                      float cardOffsetY,
                                      float cardDeckX,
                                      float cardDeckY,
                                      float elevationMin,
                                      float elevationMax) {
        this.cardsCount = cardsCount;

        this.cardOffsetX = cardOffsetX;
        this.cardOffsetY = cardOffsetY;
        this.cardDeckX = cardDeckX;
        this.cardDeckY = cardDeckY;
        this.elevationMin = elevationMin;
        this.elevationMax = elevationMax;
    }

    @Override
    public List<ThreeDCardCoordinates> getCardsCoordinates() {
        List<ThreeDCardCoordinates> cardCoordinates = new ArrayList<>();
        float shadowXOffset = cardOffsetX;
        float shadowYOffset = cardOffsetY;
        float shadowZOffset = (elevationMax - elevationMin) / cardsCount;
        float x = cardDeckX - (cardsCount * shadowXOffset);
        float y = cardDeckY - (cardsCount * shadowYOffset);
        float z = elevationMax;

        for (int i = 0; i < cardsCount; i++) {
            cardCoordinates.add(new ThreeDCardCoordinates(Math.round(x), Math.round(y), Math.round(z), 0));
            x += shadowXOffset;
            y += shadowYOffset;
            z -= shadowZOffset;
        }
        return cardCoordinates;
    }
}
