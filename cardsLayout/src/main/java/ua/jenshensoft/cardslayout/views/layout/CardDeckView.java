package ua.jenshensoft.cardslayout.views.layout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.pattern.CardDeckCoordinatesPattern;
import ua.jenshensoft.cardslayout.pattern.models.ThreeDCardCoordinates;
import ua.jenshensoft.cardslayout.views.ViewUpdateConfig;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.card.CardBoxView;
import ua.jenshensoft.cardslayout.views.updater.ViewUpdater;

public abstract class CardDeckView<Entity> extends ViewGroup {

    public static final float EPSILON = 0.00000001f;
    //updaters
    protected ViewUpdater viewUpdater;
    protected ViewUpdateConfig viewUpdateConfig;
    private List<Card<Entity>> cards;
    //attr
    private float cardDeckCardOffsetX = -1;
    private float cardDeckCardOffsetY = -1;
    private float cardDeckElevationMin = -1;
    private float cardDeckElevationMax = -1;
    private float offsetLeft = -1;
    private float offsetRight = -1;
    private float offsetTop = -1;
    private float offsetBottom = -1;

    private List<ThreeDCardCoordinates> cardsCoordinates;

    public CardDeckView(Context context) {
        super(context);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, null);
        }
    }

    public CardDeckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    public CardDeckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardDeckView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        if (!isInEditMode()) {
            inflateAttributes(context, attrs);
        }
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof Card) {
            setUpCard((View & Card<Entity>) child);
        } else {
            ((ViewGroup) child.getParent()).removeView(child);
            addCardBoxView(child);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (viewUpdateConfig.needUpdateViewOnMeasure()) {

            // Find out how big everyone wants to be
            measureChildren(widthMeasureSpec, heightMeasureSpec);

            List<Card<Entity>> validatedCardViews = getValidatedCards();
            cardsCoordinates = new CardDeckCoordinatesPattern(
                    validatedCardViews.size(),
                    cardDeckCardOffsetX,
                    cardDeckCardOffsetY,
                    offsetLeft,
                    offsetTop,
                    cardDeckElevationMin,
                    cardDeckElevationMax)
                    .getCardsCoordinates();

            int maxHeight = 0;
            int maxWidth = 0;
            // Find rightmost and bottom-most child
            for (int i = 0; i < validatedCardViews.size(); i++) {
                Card<Entity> card = validatedCardViews.get(i);
                ThreeDCardCoordinates cardCoordinates = cardsCoordinates.get(i);
                float childRight = cardCoordinates.getX() + card.getCardWidth();
                float childBottom = cardCoordinates.getY() + card.getCardHeight();
                maxWidth = Math.max(maxWidth, Math.round(childRight));
                maxHeight = Math.max(maxHeight, Math.round(childBottom));
            }

            // Account for padding too
            maxWidth += getPaddingLeft()+ getPaddingRight() + offsetRight;
            maxHeight += getPaddingTop()+ getPaddingBottom() + offsetBottom;

            // Check against minimum height and width
            maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
            maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

            setMeasuredDimension(
                    resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                    resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (viewUpdateConfig.needUpdateViewOnLayout(changed)) {
            List<Card<Entity>> validatedCardViews = getValidatedCards();
            for (int i = 0; i < validatedCardViews.size(); i++) {
                onLayoutCardInCardDeck((View & Card<Entity>) validatedCardViews.get(i), cardsCoordinates.get(i));
            }
        }
    }

    public void addCardBoxView(View view) {
        CardBoxView<Entity> cardView = createCardBoxView(view);
        cardView.setCardInfo(new CardInfo<>(cards.size()));
        this.addView(cardView);
    }

    public List<ThreeDCardCoordinates> getCardsCoordinates() {
        return cardsCoordinates;
    }

    /* private methods */

    private void inflateAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CardDeckView);
            try {
                cardDeckCardOffsetX = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_x, cardDeckCardOffsetX);
                cardDeckCardOffsetY = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_y, cardDeckCardOffsetY);
                cardDeckElevationMin = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_elevation_min, cardDeckElevationMin);
                cardDeckElevationMax = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_elevation_max, cardDeckElevationMax);

                offsetLeft = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetLeft, offsetLeft);
                offsetRight = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetRight, offsetRight);
                offsetTop = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetTop, offsetTop);
                offsetBottom = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetBottom, offsetBottom);
            } finally {
                attributes.recycle();
            }
        }
    }

    private void init() {
        cards = new ArrayList<>();
        viewUpdater = new ViewUpdater<>();
        viewUpdateConfig = new ViewUpdateConfig(this);

        if (Math.abs(cardDeckElevationMin - (-1)) < EPSILON) {
            cardDeckElevationMin = getResources().getDimension(R.dimen.cardsLayout_card_elevation_normal);
        }
        if (Math.abs(cardDeckElevationMax - (-1)) < EPSILON) {
            cardDeckElevationMax = getResources().getDimension(R.dimen.cardsLayout_card_elevation_pressed);
        }
        if (Math.abs(cardDeckCardOffsetX - (-1)) < EPSILON) {
            cardDeckCardOffsetX = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
        }
        if (Math.abs(cardDeckCardOffsetY - (-1)) < EPSILON) {
            cardDeckCardOffsetY = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
        }

        if (Math.abs(offsetLeft - (-1)) < EPSILON) {
            offsetLeft = getResources().getDimension(R.dimen.cardsLayout_cardDeck_offset);
        }
        if (Math.abs(offsetRight - (-1)) < EPSILON) {
            offsetRight = getResources().getDimension(R.dimen.cardsLayout_cardDeck_offset);
        }
        if (Math.abs(offsetTop - (-1)) < EPSILON) {
            offsetTop = getResources().getDimension(R.dimen.cardsLayout_cardDeck_offset);
        }
        if (Math.abs(offsetBottom - (-1)) < EPSILON) {
            offsetBottom = getResources().getDimension(R.dimen.cardsLayout_cardDeck_offset);
        }
    }

    /* card view methods */

    private void setUpCard(Card<Entity> card) {
        CardInfo<Entity> cardInfo = card.getCardInfo();
        if (cardInfo == null) {
            cardInfo = new CardInfo<>(cards.size());
            card.setCardInfo(cardInfo);
        }
        cardInfo.setCardDistributed(false);
        card.setEnabled(false);
        this.cards.add(cardInfo.getCardPositionInLayout(), card);
    }

    private CardBoxView<Entity> createCardBoxView(View view) {
        CardBoxView<Entity> cardView = new CardBoxView<>(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        cardView.addView(view);
        return cardView;
    }

    private List<Card<Entity>> getValidatedCards() {
        final List<Card<Entity>> validatedCards = new ArrayList<>();
        for (Card<Entity> card : cards) {
            if (!shouldPassCard(card)) {
                validatedCards.add(card);
            }
        }
        return validatedCards;
    }

    private <CV extends View & Card<Entity>> void onLayoutCardInCardDeck(CV cardView,
                                                                         ThreeDCardCoordinates coordinates) {
        int x = Math.round(coordinates.getX());
        int y = Math.round(coordinates.getY());
        float z = coordinates.getZ();
        int angle = Math.round(coordinates.getAngle());
        cardView.setRotation(angle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setElevation(z);
        }
        cardView.layout(x, y, x + cardView.getMeasuredWidth(), y + cardView.getMeasuredHeight());
        CardInfo<Entity> cardInfo = cardView.getCardInfo();
        cardInfo.setFirstPositionX(x);
        cardInfo.setFirstPositionY(y);
        cardInfo.setFirstRotation(angle);
    }

    private boolean shouldPassCard(Card<Entity> card) {
        return card.getVisibility() != VISIBLE || card.getCardInfo().isCardDistributed();
    }
}
