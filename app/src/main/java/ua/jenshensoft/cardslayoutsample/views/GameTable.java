package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.OnUpdateDeskOfCardsUpdater;
import ua.jenshensoft.cardslayout.views.CardView;
import ua.jenshensoft.cardslayout.views.CardsLayout;
import ua.jenshensoft.cardslayout.views.GameTableLayout;
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

    private void init() {
        for (final CardsLayout<CardsLayoutDefault.CardInfo> cardsLayout : cardsLayouts) {
            cardsLayout.setOnCardSwipedListener(new OnCardSwipedListener<CardsLayoutDefault.CardInfo>() {

                @Override
                public void onCardSwiped(CardInfo<CardsLayoutDefault.CardInfo> cardInfo) {
                    cardsLayout.removeCardView(cardInfo.getCardPositionInLayout());
                }
            });

        }

        setDistributionState(new DistributionState<CardsLayoutDefault.CardInfo>(false) {
            @Override
            public Predicate<CardView<CardsLayoutDefault.CardInfo>> getPredicateForCardsForDistribution() {
                return new Predicate<CardView<CardsLayoutDefault.CardInfo>>() {
                    @Override
                    public boolean apply(CardView<CardsLayoutDefault.CardInfo> cardInfoCardView) {
                        return true;
                    }
                };
            }

            @Override
            public Predicate<CardView<CardsLayoutDefault.CardInfo>> getPredicateForCardsBeforeDistribution() {
                return new Predicate<CardView<CardsLayoutDefault.CardInfo>>() {
                    @Override
                    public boolean apply(CardView<CardsLayoutDefault.CardInfo> cardInfoCardView) {
                        return false;
                    }
                };
            }

            @Override
            public float[] provideCoordinateForDistribution() {
                return new float[] {0, 0};
            }

            @Override
            public OnUpdateDeskOfCardsUpdater<CardsLayoutDefault.CardInfo> getDeskOfCardsUpdater() {
                return new OnUpdateDeskOfCardsUpdater<CardsLayoutDefault.CardInfo>() {
                    @Override
                    protected float[] getPosition() {
                        return new float[]{0, 0};
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
