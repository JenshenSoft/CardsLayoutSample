package ua.jenshensoft.cardslayout.pattern;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.pattern.models.ThreeDCardCoordinates;


public class CardDeckCoordinatesPattern implements CardCoordinatesPattern<ThreeDCardCoordinates> {

    private final int cardsCount;
    private final float cardOffsetX;
    private final float cardOffsetY;
    private final float cardOffsetZ;
    private final float cardDeckX;
    private final float cardDeckY;
    private final float cardDeckZ;

    public CardDeckCoordinatesPattern(int cardsCount,
                                      float cardOffsetX,
                                      float cardOffsetY,
                                      float cardOffsetZ,
                                      float cardDeckX,
                                      float cardDeckY,
                                      float cardDeckZ) {
        this.cardsCount = cardsCount;
        this.cardOffsetX = cardOffsetX;
        this.cardOffsetY = cardOffsetY;
        this.cardOffsetZ = cardOffsetZ;
        this.cardDeckX = cardDeckX;
        this.cardDeckY = cardDeckY;
        this.cardDeckZ = cardDeckZ;
    }

    @Override
    public List<ThreeDCardCoordinates> getCardsCoordinates() {
        List<ThreeDCardCoordinates> cardCoordinates = new ArrayList<>();
        float x = cardDeckX;
        float y = cardDeckY;
        float z = cardDeckZ;
        for (int i = 0; i < cardsCount; i++) {
            x -= cardOffsetX;
            y -= cardOffsetY;
            //because of elevation (android 21 > has support of Z
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                z += cardOffsetZ;
            }
            cardCoordinates.add(new ThreeDCardCoordinates(x, y, z, 0));
        }
        return cardCoordinates;
    }
}
