package ua.jenshensoft.cardslayout.pattern;

import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.pattern.models.CardCoordinates;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.Config;

public class LineCardsCoordinatesPattern<CV extends View & Card> implements CardCoordinatesPattern {

    @LinearLayoutCompat.OrientationMode
    private final int orientation;
    private final List<CV> cards;
    private final Config xConfig;
    private final Config yConfig;

    public LineCardsCoordinatesPattern(@LinearLayoutCompat.OrientationMode int orientation,
                                       List<CV> cards,
                                       Config xConfig,
                                       Config yConfig) {
        this.orientation = orientation;
        this.cards = cards;
        this.xConfig = xConfig;
        this.yConfig = yConfig;
    }

    @Override
    public List<CardCoordinates> getCardsCoordinates() {
        List<CardCoordinates> cardCoordinates = new ArrayList<>();
        float distanceBetweenViewsX = xConfig.getDistanceBetweenViews();
        float distanceBetweenViewsY = yConfig.getDistanceBetweenViews();
        float x = xConfig.getStartCoordinates();
        float y = yConfig.getStartCoordinates();
        for (CV card : cards) {
            cardCoordinates.add(new CardCoordinates(x, y, 0));
            if (orientation == LinearLayout.HORIZONTAL) {
                x += card.getCardWidth() - distanceBetweenViewsX;
            }
            if (orientation == LinearLayout.VERTICAL) {
                y += card.getCardHeight() - distanceBetweenViewsY;
            }
        }
        return cardCoordinates;
    }
}