package ua.jenshensoft.cardslayout.pattern;

import android.os.Build;

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
        float x;
        float y;
        //because of elevation (android 21 > has support of Z 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            x = cardDeckX - (cardsCount * shadowXOffset);
            y = cardDeckY - (cardsCount * shadowYOffset);
        } else {
            x = cardDeckX + (cardsCount * shadowXOffset);
            y = cardDeckY + (cardsCount * shadowYOffset);
        }

        float z = elevationMax;

        for (int i = 0; i < cardsCount; i++) {
            cardCoordinates.add(new ThreeDCardCoordinates(x, y, z, 0));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                x += shadowXOffset;
                y += shadowYOffset;
                z -= shadowZOffset;
            } else {
                x -= shadowXOffset;
                y -= shadowYOffset;
            }
        }
        return cardCoordinates;
    }
}
