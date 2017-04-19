package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.internal.util.Predicate;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.listeners.OnUpdateDeskOfCardsUpdater;
import ua.jenshensoft.cardslayout.util.DistributionState;
import ua.jenshensoft.cardslayout.views.card.CardBoxView;
import ua.jenshensoft.cardslayout.views.updater.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.GameTableLayout;
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

    private void init() {
        imageView = (ImageView) findViewById(R.id.imageView);
        for (final CardsLayout<CardsLayoutDefault.CardInfo> cardsLayout : cardsLayouts) {
            cardsLayout.setOnCardSwipedListener(new OnCardSwipedListener<CardsLayoutDefault.CardInfo>() {

                @Override
                public void onCardSwiped(CardInfo<CardsLayoutDefault.CardInfo> cardInfo) {
                    cardsLayout.removeCardView(cardInfo.getCardPositionInLayout());
                }
            });

        }

       updateDistributionState(new DistributionState<CardsLayoutDefault.CardInfo>(false) {
            @Override
            public Predicate<CardBoxView<CardsLayoutDefault.CardInfo>> getPredicateForCardsForDistribution() {
                return new Predicate<CardBoxView<CardsLayoutDefault.CardInfo>>() {
                    @Override
                    public boolean apply(CardBoxView<CardsLayoutDefault.CardInfo> cardInfoCardView) {
                        return true;
                    }
                };
            }

            @Override
            public Predicate<CardBoxView<CardsLayoutDefault.CardInfo>> getPredicateForCardsBeforeDistribution() {
                return new Predicate<CardBoxView<CardsLayoutDefault.CardInfo>>() {
                    @Override
                    public boolean apply(CardBoxView<CardsLayoutDefault.CardInfo> cardInfoCardView) {
                        return false;
                    }
                };
            }

            @Override
            protected OnUpdateDeskOfCardsUpdater<CardsLayoutDefault.CardInfo> provideDeskOfCardsUpdater() {
                return new OnUpdateDeskOfCardsUpdater<CardsLayoutDefault.CardInfo>() {
                    @Override
                    public float[] getPosition() {
                        int x = getMeasuredWidth() / 2 - imageView.getMeasuredWidth() / 2;
                        int y = getMeasuredHeight() / 2 - imageView.getMeasuredHeight() / 2;
                        return new float[]{x, y};
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
