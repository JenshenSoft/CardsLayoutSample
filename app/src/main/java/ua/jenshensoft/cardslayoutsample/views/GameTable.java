package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.table.GameTableLayout;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;
import ua.jenshensoft.cardslayoutsample.CardsLayoutDefault;
import ua.jenshensoft.cardslayoutsample.R;

public class GameTable extends GameTableLayout<CardsLayoutDefault.CardInfo, CardsLayout<CardsLayoutDefault.CardInfo>> {

    public GameTable(Context context) {
        super(context);
        init();
    }

    public GameTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GameTable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onUpdateViewParams(GameTableParams params, boolean calledInOnMeasure) {
        super.onUpdateViewParams(params, calledInOnMeasure);
    }

    private void init() {
        inflate(getContext(), R.layout.viewgroup_table, this);
        setDurationOfDistributeAnimation(1000);

        getCurrentPlayerCardsLayout().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY));

        for (CardsLayout<CardsLayoutDefault.CardInfo> cardsLayout : cardsLayouts) {
            int i = 0;
            for (Card<CardsLayoutDefault.CardInfo> card : cardsLayout.getCards()) {
                card.getCardInfo().setEntity(new CardsLayoutDefault.CardInfo(i));
                i++;
            }
        }

        updateDistributionState(new DistributionState<CardsLayoutDefault.CardInfo>(false) {
            @Override
            public Predicate<Card<CardsLayoutDefault.CardInfo>> getCardsPredicateBeforeDistribution() {
                return new Predicate<Card<CardsLayoutDefault.CardInfo>>() {
                    @Override
                    public boolean apply(Card<CardsLayoutDefault.CardInfo> cardInfoCard) {
                        return cardInfoCard.getCardInfo().getEntity().getNumber() < 2;
                    }
                };
            }

            @Override
            public Predicate<Card<CardsLayoutDefault.CardInfo>> getCardsPredicateForDistribution() {
                return new Predicate<Card<CardsLayoutDefault.CardInfo>>() {
                    @Override
                    public boolean apply(Card<CardsLayoutDefault.CardInfo> cardInfoCard) {
                        return cardInfoCard.getCardInfo().getEntity().getNumber() >= 2 && cardInfoCard.getCardInfo().getEntity().getNumber() < 9;
                    }
                };
            }
        });
    }
}
