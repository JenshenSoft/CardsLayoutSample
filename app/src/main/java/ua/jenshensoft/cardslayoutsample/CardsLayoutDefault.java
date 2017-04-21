package ua.jenshensoft.cardslayoutsample;

import android.content.Context;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.views.layout.CardsLayout;

public class CardsLayoutDefault extends CardsLayout<CardsLayoutDefault.CardInfo> {

    public CardsLayoutDefault(Context context) {
        super(context);
    }

    public CardsLayoutDefault(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardsLayoutDefault(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CardsLayoutDefault(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static class CardInfo {

        private final int number;

        public CardInfo(int number) {
            this.number = number;
        }
    }
}
