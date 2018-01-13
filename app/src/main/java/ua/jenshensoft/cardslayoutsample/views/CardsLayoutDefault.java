package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.pattern.models.CardCoordinates;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.views.FirstPositionProvider;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;

public class CardsLayoutDefault extends CardsLayout {

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
        addOnCardSwipedListener(new OnCardSwipedListener() {
            @Override
            public void onCardSwiped(ua.jenshensoft.cardslayout.CardInfo cardInfo) {
                removeCardView(cardInfo.getCardPositionInLayout());
            }
        });
    }

    @Override
    protected void setViewCoordinatesToStartPosition(FirstPositionProvider card, CardCoordinates cardCoordinates) {
        if (getGravityFlag().containsFlag(FlagManager.Gravity.RIGHT) || getGravityFlag().containsFlag(FlagManager.Gravity.LEFT)) {
            cardCoordinates.setAngle(cardCoordinates.getAngle() + 90);
        }
        super.setViewCoordinatesToStartPosition(card, cardCoordinates);
    }
}
