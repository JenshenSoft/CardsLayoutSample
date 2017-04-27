package ua.jenshensoft.cardslayout.util;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CardsUtil {

    public static <Entity> List<Entity> getCardsForDesk(List<Iterator<Entity>> cardsInDeskForPlayers) {
        List<Entity> entities = new ArrayList<>();
        while (hasNextCardForWave(cardsInDeskForPlayers)) {
            for (Iterator<Entity> entityIterator : cardsInDeskForPlayers) {
                if (entityIterator.hasNext()) {
                    entities.add(entityIterator.next());
                }
            }
        }
        return entities;
    }

    private static <Entity> boolean hasNextCardForWave(List<Iterator<Entity>> cards) {
        for (Iterator<Entity> cardIterator : cards) {
            if (cardIterator.hasNext()) {
                return true;
            }
        }
        return false;
    }
}
