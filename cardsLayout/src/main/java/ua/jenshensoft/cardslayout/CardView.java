package ua.jenshensoft.cardslayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.listeners.CardTranslationListener;
import ua.jenshensoft.cardslayout.listeners.OnCardPercentageChangeListener;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.zabelnikov.swipelayout.layout.frame.SwipeableLayout;
import ua.zabelnikov.swipelayout.layout.listener.LayoutShiftListener;
import ua.zabelnikov.swipelayout.layout.listener.OnLayoutPercentageChangeListener;
import ua.zabelnikov.swipelayout.layout.listener.OnLayoutSwipedListener;

public class CardView extends SwipeableLayout {

    private CardInfo cardInfo;

    public CardView(Context context) {
        super(context);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public void setCardTranslationListener(final CardTranslationListener cardTranslationListener) {
        this.setLayoutShiftListener(new LayoutShiftListener() {
            @Override
            public void onLayoutShifted(float positionX, float positionY, boolean isTouched) {
                cardTranslationListener.onCardTranslation(positionX, positionY, cardInfo.getCardIndex(), isTouched);
            }
        });
    }

    public void setCardSwipedListener(final OnCardSwipedListener onCardSwipedListener) {
        this.setOnSwipedListener(new OnLayoutSwipedListener() {
            @Override
            public void onLayoutSwiped() {
                onCardSwipedListener.onCardSwiped(cardInfo.getCardIndex());
            }
        });
    }

    public void setCardPercentageChangeListener(final OnCardPercentageChangeListener onCardPercentageChangeListener) {
        this.setOnLayoutPercentageChangeListener(new OnLayoutPercentageChangeListener() {
            @Override
            public void percentageX(float percentage) {
                super.percentageX(percentage);
                onCardPercentageChangeListener.percentageX(percentage, cardInfo.getCardIndex());
            }

            @Override
            public void percentageY(float percentage) {
                super.percentageY(percentage);
                onCardPercentageChangeListener.percentageY(percentage, cardInfo.getCardIndex());
            }
        });
    }
}
