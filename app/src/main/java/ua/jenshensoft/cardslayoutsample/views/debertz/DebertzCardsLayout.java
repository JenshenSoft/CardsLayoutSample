package ua.jenshensoft.cardslayoutsample.views.debertz;

import android.content.Context;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.views.layout.bars.CardsLayoutWithBars;

import static android.support.v7.widget.LinearLayoutCompat.VERTICAL;

public class DebertzCardsLayout extends CardsLayoutWithBars<FirstBarView, SecondBarView> {

    public DebertzCardsLayout(Context context) {
        super(context);
        initLayout();
    }

    public DebertzCardsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public DebertzCardsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    public DebertzCardsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initLayout();
    }

    public int getCardRotation() {
        if (getChildListOrientation() == VERTICAL) {
            return 90;
        } else {
            return 0;
        }
    }

    private void initLayout() {
    }


    public static class CardInfo {

        private final int number;

        public CardInfo(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }
}
