package ua.jenshensoft.cardslayout.views.table;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.listeners.table.OnDistributedCardsListener;
import ua.jenshensoft.cardslayout.views.card.Card;

class TableDistributedCardsListener<Entity> implements OnDistributedCardsListener<Entity> {

    private final int countOfLayouts;
    private final OnDistributedCardsListener<Entity> distributedCardsListener;
    private final List<Card<Entity>> startDistributedCardViews = new ArrayList<>();
    private final List<Card<Entity>> endDistributedCardViews = new ArrayList<>();
    private int nonDistributedLayoutsCount;
    private int distributedLayoutsCount;

    TableDistributedCardsListener(int countOfLayouts, OnDistributedCardsListener<Entity> onDistributedCardsListener) {
        this.countOfLayouts = countOfLayouts;
        this.nonDistributedLayoutsCount = countOfLayouts;
        this.distributedCardsListener = onDistributedCardsListener;
    }

    @Override
    public void onDistributedCards() {
        nonDistributedLayoutsCount--;
        distributedLayoutsCount++;
        if (distributedLayoutsCount == countOfLayouts) {
            distributedCardsListener.onDistributedCards();
        }
    }

    @Override
    public void onStartDistributedCardWave(List<Card<Entity>> cards) {
        startDistributedCardViews.addAll(cards);
        if (startDistributedCardViews.size() == nonDistributedLayoutsCount) {
            distributedCardsListener.onStartDistributedCardWave(startDistributedCardViews);
            startDistributedCardViews.clear();
        }
    }

    @Override
    public void onEndDistributeCardWave(List<Card<Entity>> cards) {
        endDistributedCardViews.addAll(cards);
        if (endDistributedCardViews.size() == nonDistributedLayoutsCount) {
            distributedCardsListener.onEndDistributeCardWave(endDistributedCardViews);
            endDistributedCardViews.clear();
        }
    }
}