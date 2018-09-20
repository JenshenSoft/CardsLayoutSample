package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.util.function.Predicate;

import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.table.GameTableLayout;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;
import ua.jenshensoft.cardslayoutsample.R;

public class GameTable extends GameTableLayout<CardsLayout> {

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

    /*

    @Override
    protected void onStartDistributedCardWave(List<Card> cards) {
        *//*Bitmap bitmap = BitmapUtils.rotateBitmap(getContext(), R.drawable.ic_card1, 90);
        for (Card<CardsLayoutDefault.CardInfoModel> card : cards) {
            if (card instanceof CardView) {
                CardView cardView = (CardView) card;
                cardView.setImageBitmap(bitmap);
            } else if (card instanceof CardBoxView) {
                CardBoxView cardView = (CardBoxView) card;
                ((ImageView)cardView.getChildAt(0)).setImageBitmap(bitmap);
            }
        }*//*
        super.onStartDistributedCardWave(cards);
    }*/

    private void init() {
        inflate(getContext(), R.layout.viewgroup_table, this);
        setDurationOfDistributeAnimation(100);

        getCurrentPlayerCardsLayout().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY));

        int j = 0;
        for (CardsLayout cardsLayout : cardsLayouts) {
            int i = 0;
            for (Card card : cardsLayout.getCards()) {
                card.getCardInfo().setEntity(new CardInfoModel(i, j));
                i++;
                j++;
            }
        }

        updateDistributionState(new DistributionState(false) {
            @Override
            public Predicate<Card> getCardsPredicateBeforeDistribution() {
                return cardInfoCard -> {
                    CardInfoModel entity = (CardInfoModel) cardInfoCard.getCardInfo().getEntity();
                    return entity.getPosition() < 2;
                };
            }

            @Override
            public Predicate<Card> getCardsPredicateForDistribution() {
                return cardInfoCard -> {
                    CardInfoModel entity = (CardInfoModel) cardInfoCard.getCardInfo().getEntity();
                    return entity.getPosition() >= 2 && entity.getPosition() < 8;
                };
            }
        });
    }
}
