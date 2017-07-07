package ua.jenshensoft.cardslayout.views.table;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.listeners.table.OnDistributedCardsListener;
import ua.jenshensoft.cardslayout.views.card.Card;

class TableDistributedCardsListener implements OnDistributedCardsListener {

    private final int countOfLayouts;
    private final OnDistributedCardsListener distributedCardsListener;
    private final List<Card> startDistributedCardViews = new ArrayList<>();
    private final List<Card> endDistributedCardViews = new ArrayList<>();
    private int nonDistributedLayoutsCount;
    private int distributedLayoutsCount;

    TableDistributedCardsListener(int countOfLayouts, OnDistributedCardsListener onDistributedCardsListener) {
        this.countOfLayouts = countOfLayouts;
        this.nonDistributedLayoutsCount = countOfLayouts;
        this.distributedCardsListener = onDistributedCardsListener;
    }

    @Override
    public void onDistributedCards() {
        nonDistributedLayoutsCount--;
        distributedLayoutsCount++;
        onCheckIsStartCardWave();
        onCheckIsEndCardWave();
        if (distributedLayoutsCount == countOfLayouts) {
            distributedCardsListener.onDistributedCards();
        }
    }

    @Override
    public void onStartDistributedCardWave(List<Card> cards) {
        startDistributedCardViews.addAll(cards);
        onCheckIsStartCardWave();
    }

    @Override
    public void onEndDistributeCardWave(List<Card> cards) {
        endDistributedCardViews.addAll(cards);
        onCheckIsEndCardWave();
    }

    private void onCheckIsStartCardWave() {
        if (startDistributedCardViews.size() == nonDistributedLayoutsCount) {
            distributedCardsListener.onStartDistributedCardWave(startDistributedCardViews);
            startDistributedCardViews.clear();
        }
    }

    private void onCheckIsEndCardWave() {
        if (endDistributedCardViews.size() == nonDistributedLayoutsCount) {
            distributedCardsListener.onEndDistributeCardWave(endDistributedCardViews);
            endDistributedCardViews.clear();
        }
    }
}