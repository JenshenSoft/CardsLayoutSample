package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;

public class CardsLayoutDefault extends CardsLayout<CardInfoModel> {

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
        addOnCardSwipedListener(new OnCardSwipedListener<CardInfoModel>() {
            @Override
            public void onCardSwiped(ua.jenshensoft.cardslayout.CardInfo<CardInfoModel> cardInfo) {
                removeCardView(cardInfo.getCardPositionInLayout());
            }
        });
    }
}
