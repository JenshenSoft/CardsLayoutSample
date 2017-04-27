package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.card.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.table.CardDeckUpdater;
import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.GameTableLayout;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.updater.model.GameTableParams;
import ua.jenshensoft.cardslayoutsample.CardsLayoutDefault;
import ua.jenshensoft.cardslayoutsample.R;

public class GameTable extends GameTableLayout<CardsLayoutDefault.CardInfo, CardsLayout<CardsLayoutDefault.CardInfo>> {

    private ImageView imageView;

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
    public void onUpdateViewParams(GameTableParams params) {
        int x = getMeasuredWidth() / 2 - imageView.getMeasuredWidth() / 2;
        int y = getMeasuredHeight() / 2 - imageView.getMeasuredHeight() / 2;
        imageView.setX(x);
        imageView.setY(y);
        super.onUpdateViewParams(params);
    }

    private void init() {
        setDurationOfDistributeAnimation(100000);
        imageView = (ImageView) findViewById(R.id.imageView);
        for (final CardsLayout<CardsLayoutDefault.CardInfo> cardsLayout : cardsLayouts) {
            cardsLayout.setOnCardSwipedListener(new OnCardSwipedListener<CardsLayoutDefault.CardInfo>() {

                @Override
                public void onCardSwiped(CardInfo<CardsLayoutDefault.CardInfo> cardInfo) {
                    cardsLayout.removeCardView(cardInfo.getCardPositionInLayout());
                }
            });
        }

        for (CardsLayout<CardsLayoutDefault.CardInfo> cardsLayout : cardsLayouts) {
            int i = 0;
            for (Card<CardsLayoutDefault.CardInfo> card : cardsLayout.getCards()) {
                card.getCardInfo().setEntity(new CardsLayoutDefault.CardInfo(i));
                i++;
            }
        }

        updateDistributionState(new DistributionState<CardsLayoutDefault.CardInfo>(false) {
            @Override
            protected CardDeckUpdater<CardsLayoutDefault.CardInfo> provideDeskOfCardsUpdater() {
                return new CardDeckUpdater<CardsLayoutDefault.CardInfo>() {
                    @Override
                    public CardDeckLocation getLocation() {
                        return new CardDeckLocation(getContext(), imageView.getX(), imageView.getY());
                    }
                };
            }

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
                        return cardInfoCard.getCardInfo().getEntity().getNumber() >= 2 && cardInfoCard.getCardInfo().getEntity().getNumber() < 6;
                    }
                };
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.viewgroup_table;
    }
}
