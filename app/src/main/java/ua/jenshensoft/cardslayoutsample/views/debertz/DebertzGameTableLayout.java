package ua.jenshensoft.cardslayoutsample.views.debertz;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.internal.util.Predicate;

import java.util.List;

import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.card.CardBoxView;
import ua.jenshensoft.cardslayout.views.card.CardView;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.table.GameTableLayout;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;
import ua.jenshensoft.cardslayoutsample.BitmapUtils;
import ua.jenshensoft.cardslayoutsample.R;
import ua.jenshensoft.cardslayoutsample.views.CardInfoModel;

public class DebertzGameTableLayout extends GameTableLayout<CardInfoModel, DebertzCardsLayout> {

    public DebertzGameTableLayout(Context context) {
        super(context);
        init();
    }

    public DebertzGameTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DebertzGameTableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DebertzGameTableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onUpdateViewParams(GameTableParams params, boolean calledInOnMeasure) {
        super.onUpdateViewParams(params, calledInOnMeasure);
    }

    @Override
    protected void onStartDistributedCardWave(List<Card<CardInfoModel>> cards) {
        for (Card<CardInfoModel> card : cards) {
            int cardResId = getCardResId(card.getCardInfo().getEntity().getPosition());
            int cardRotation = findLayout(card).getCardRotation();
            setIcon(card, BitmapUtils.rotateBitmap(getContext(), cardResId, cardRotation));
        }
        super.onStartDistributedCardWave(cards);
    }

    private DebertzCardsLayout findLayout(Card<CardInfoModel> card) {
        for (DebertzCardsLayout cardsLayout : cardsLayouts) {
            if (cardsLayout.getCards().contains(card)) {
                return cardsLayout;
            }
        }
        return null;
    }

    private void init() {
        inflate(getContext(), R.layout.viewgroup_table_debertz, this);
        setDurationOfDistributeAnimation(10000);

        int number = 0;
        for (CardsLayout<CardInfoModel> cardsLayout : cardsLayouts) {
            int position = 0;
            for (Card<CardInfoModel> card : cardsLayout.getCards()) {
                card.getCardInfo().setEntity(new CardInfoModel(position, number));
                position++;
                number ++;
            }
        }

        updateDistributionState(new DistributionState<CardInfoModel>(false) {
            @Override
            public Predicate<Card<CardInfoModel>> getCardsPredicateBeforeDistribution() {
                return new Predicate<Card<CardInfoModel>>() {
                    @Override
                    public boolean apply(Card<CardInfoModel> cardInfoCard) {
                        return cardInfoCard.getCardInfo().getEntity().getPosition() < 2;
                    }
                };
            }

            @Override
            public Predicate<Card<CardInfoModel>> getCardsPredicateForDistribution() {
                return new Predicate<Card<CardInfoModel>>() {
                    @Override
                    public boolean apply(Card<CardInfoModel> cardInfoCard) {
                        return cardInfoCard.getCardInfo().getEntity().getPosition() >= 2 && cardInfoCard.getCardInfo().getEntity().getPosition() < 9;
                    }
                };
            }
        });

        for (DebertzCardsLayout cardsLayout : cardsLayouts) {
            for (Card<CardInfoModel> card : cardsLayout.getCards()) {
                int cardRotation = cardsLayout.getCardRotation();
                Bitmap bitmap = BitmapUtils.rotateBitmap(getContext(), getCardResId(card.getCardInfo().getEntity().getPosition()), 0);
                setIcon(card, bitmap);
            }
        }
    }

    private void setIcon(Card<CardInfoModel> card, Bitmap bitmap) {
        if (card instanceof CardView) {
            CardView cardView = (CardView) card;
            cardView.setImageBitmap(bitmap);
        } else if (card instanceof CardBoxView) {
            CardBoxView cardView = (CardBoxView) card;
            ((ImageView) cardView.getChildAt(0)).setImageBitmap(bitmap);
        }
    }

    private int getCardResId(int number) {
        switch (number) {
            case 0:
                return R.drawable.ic_card0;
            case 1:
                return R.drawable.ic_card1;
            case 2:
                return R.drawable.ic_card2;
            case 3:
                return R.drawable.ic_card3;
            case 4:
                return R.drawable.ic_card4;
            case 5:
                return R.drawable.ic_card5;
            case 6:
                return R.drawable.ic_card6;
            case 7:
                return R.drawable.ic_card7;
        }
        throw new RuntimeException("Can't support card number");
    }
}
