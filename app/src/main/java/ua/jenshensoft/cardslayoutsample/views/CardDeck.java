package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.views.layout.CardDeckView;
import ua.jenshensoft.cardslayoutsample.CardsLayoutDefault;


public class CardDeck extends CardDeckView<CardsLayoutDefault.CardInfo> {

    public CardDeck(Context context) {
        super(context);
    }

    public CardDeck(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardDeck(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CardDeck(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
