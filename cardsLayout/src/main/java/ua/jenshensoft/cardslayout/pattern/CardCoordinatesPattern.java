package ua.jenshensoft.cardslayout.pattern;


import java.util.List;

import ua.jenshensoft.cardslayout.pattern.models.CardCoordinates;

public interface CardCoordinatesPattern<Coordinates extends CardCoordinates> {
    List<Coordinates> getCardsCoordinates();
}
