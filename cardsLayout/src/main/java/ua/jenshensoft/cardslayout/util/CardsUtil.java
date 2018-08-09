package ua.jenshensoft.cardslayout.util;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CardsUtil {

    public static final float SIZE_MULTIPLIER = 1.2f;

    public static <Entity> List<List<Entity>> getEntitiesByWaves(List<Iterator<Entity>> cardsOnTheTable) {
        List<List<Entity>> entities = new ArrayList<>();
        while (hasNextCardForWave(cardsOnTheTable)) {
            List<Entity> cardsForWave = new ArrayList<>();
            for (Iterator<Entity> entityIterator : cardsOnTheTable) {
                if (entityIterator.hasNext()) {
                    cardsForWave.add(entityIterator.next());
                }
            }
            entities.add(cardsForWave);
        }
        return entities;
    }

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
