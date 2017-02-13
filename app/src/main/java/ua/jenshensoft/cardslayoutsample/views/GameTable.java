package ua.jenshensoft.cardslayoutsample.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.listeners.OnCardSwipedListener;
import ua.jenshensoft.cardslayout.views.CardsLayout;
import ua.jenshensoft.cardslayout.views.GameTableLayout;
import ua.jenshensoft.cardslayoutsample.R;

public class GameTable extends GameTableLayout<Object, CardsLayout<Object>> {

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
        for (final CardsLayout<Object> cardsLayout : cardsLayouts) {
            cardsLayout.setOnCardSwipedListener(new OnCardSwipedListener<Object>() {

                @Override
                public void onCardSwiped(CardInfo<Object> cardInfo) {
                    cardsLayout.removeCardView(cardInfo.getCardPositionInLayout());
                }
            });

        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.viewgroup_table;
    }

    @Override
    protected int[] provideCardsLayoutsIds() {
        return new int[]{R.id.cardsLayout1, R.id.cardsLayout0, R.id.cardsLayout1, R.id.cardsLayout2};
    }


    @Nullable
    @Override
    protected DistributionState<Object> provideDistributionState() {
        return null;
    }
}
