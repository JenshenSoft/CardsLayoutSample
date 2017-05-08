package ua.jenshensoft.cardslayoutsample;

import android.content.Context;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;

public class CardsLayoutDefault extends CardsLayout<CardsLayoutDefault.CardInfo> {

    public CardsLayoutDefault(Context context) {
        super(context);
        initLayout();
    }

    public CardsLayoutDefault(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public CardsLayoutDefault(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    public CardsLayoutDefault(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initLayout();
    }


    private void initLayout() {
        addOnCardSwipedListener(new OnCardSwipedListener<CardInfo>() {
            @Override
            public void onCardSwiped(ua.jenshensoft.cardslayout.CardInfo<CardInfo> cardInfo) {
                removeCardView(cardInfo.getCardPositionInLayout());
            }
        });
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
