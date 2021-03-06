package ua.jenshensoft.cardslayout.views.layout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.pattern.CardDeckCoordinatesPattern;
import ua.jenshensoft.cardslayout.pattern.models.ThreeDCardCoordinates;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.views.FirstPosition;
import ua.jenshensoft.cardslayout.views.ViewUpdateConfig;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.card.CardBoxView;
import ua.jenshensoft.cardslayout.views.updater.ViewUpdater;

public abstract class CardDeckView extends ViewGroup {

    public static final float EPSILON = 0.00000001f;
    //updaters
    protected ViewUpdater viewUpdater;
    protected ViewUpdateConfig viewUpdateConfig;
    private List<Card> cards;
    //attr
    private float cardDeckCardOffsetX = -1;
    private float cardDeckCardOffsetY = -1;
    private float cardDeckCardOffsetZ = -1;
    private float offsetLeft = -1;
    private float offsetRight = -1;
    private float offsetTop = -1;
    private float offsetBottom = -1;
    private FlagManager cardDeckGravity;

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
            setUpCard((View & Card) child);
        } else {
            ((ViewGroup) child.getParent()).removeView(child);
            addCardBoxView(child);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        List<Card> validatedCardViews = getValidatedCards();
        int maxHeight = 0;
        int maxWidth = 0;
        // Find rightmost and bottom-most child
        for (int i = 0; i < validatedCardViews.size(); i++) {
            Card card = validatedCardViews.get(i);
            float childRight = card.getCardWidth();
            float childBottom = card.getCardHeight();
            maxWidth = Math.max(maxWidth, Math.round(childRight));
            maxHeight = Math.max(maxHeight, Math.round(childBottom));
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight() + offsetLeft + offsetRight;
        maxHeight += getPaddingTop() + getPaddingBottom() + offsetBottom + offsetTop;

        // Check against minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (viewUpdateConfig.needUpdateViewOnLayout(changed)) {
            List<Card> validatedCards = getValidatedCards();
            if (!validatedCards.isEmpty()) {
                ThreeDCardCoordinates cardDeckPosition = getCardDeckPosition(validatedCards);
                cardsCoordinates = new CardDeckCoordinatesPattern(
                        validatedCards.size(),
                        cardDeckCardOffsetX,
                        cardDeckCardOffsetY,
                        cardDeckCardOffsetZ,
                        cardDeckPosition.getX(),
                        cardDeckPosition.getY(),
                        cardDeckPosition.getZ())
                        .getCardsCoordinates();
                onLayoutCardsInCardDeck(cardsCoordinates, validatedCards.iterator());
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewUpdater.clear();
    }

    public void addCardBoxView(View view) {
        CardBoxView cardView = createCardBoxView(view);
        cardView.setCardInfo(new CardInfo(cards.size()));
        this.addView(cardView);
    }

    public List<Card> getCards() {
        return cards;
    }

    @Nullable
    public List<ThreeDCardCoordinates> getCardsCoordinates() {
        return cardsCoordinates;
    }

    protected ThreeDCardCoordinates getCardDeckPosition(List<Card> cards) {
        float maxWidth = 0;
        float maxHeight = 0;
        float maxElevation = 0;
        for (Card card : cards) {
            float cardWidth = card.getCardWidth();
            float cardHeight = card.getCardHeight();
            float cardZ = card.getCardZ();
            if (cardWidth > maxWidth) {
                maxWidth = cardWidth;
            }
            if (cardHeight > maxHeight) {
                maxHeight = cardHeight;
            }
            if (cardZ > maxElevation) {
                maxElevation = cardZ;
            }
        }
        return new ThreeDCardCoordinates(
                getXPositionForCardDeck(maxWidth, getMeasuredWidth()),
                getYPositionForCardDeck(maxHeight, getMeasuredHeight()),
                maxElevation,
                0);
    }

    protected <CV extends View & Card> void onLayoutCardInCardDeck(CV cardView,
                                                                   ThreeDCardCoordinates coordinates) {
        int x = Math.round(coordinates.getX());
        int y = Math.round(coordinates.getY());
        setCardDeckCardToStartPosition(cardView, coordinates);
        cardView.setRotation(cardView.getCardInfo().getFirstRotation());
        cardView.setCardZ(coordinates.getZ());
        cardView.layout(x, y, x + cardView.getMeasuredWidth(), y + cardView.getMeasuredHeight());
    }

    protected void setCardDeckCardToStartPosition(final Card card, final ThreeDCardCoordinates cardCoordinates) {
        card.setCardZ(cardCoordinates.getZ());
        FirstPosition firstPosition = card.getFirstPosition();
        firstPosition.setFirstPositionX(Math.round(cardCoordinates.getX()));
        firstPosition.setFirstPositionY(Math.round(cardCoordinates.getY()));
        firstPosition.setFirstRotation(Math.round(cardCoordinates.getAngle()));
    }

    /* private methods */

    private void inflateAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CardDeckView);
            try {
                cardDeckCardOffsetX = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_x, cardDeckCardOffsetX);
                cardDeckCardOffsetY = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_y, cardDeckCardOffsetY);
                cardDeckCardOffsetZ = attributes.getDimension(R.styleable.CardDeckView_cardDeck_cardDeck_cardOffset_z, cardDeckCardOffsetZ);

                offsetLeft = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetLeft, offsetLeft);
                offsetRight = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetRight, offsetRight);
                offsetTop = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetTop, offsetTop);
                offsetBottom = attributes.getDimension(R.styleable.CardDeckView_cardDeck_offsetBottom, offsetBottom);
                int flagSet = attributes.getInt(R.styleable.CardDeckView_cardDeck_gravity, -1);
                if (flagSet != -1) {
                    cardDeckGravity = new FlagManager(flagSet);
                }
            } finally {
                attributes.recycle();
            }
        }
    }

    private void init() {
        cards = new ArrayList<>();
        viewUpdater = new ViewUpdater<>();
        viewUpdateConfig = new ViewUpdateConfig(this);
        if (cardDeckGravity == null) {
            cardDeckGravity = new FlagManager(FlagManager.Gravity.CENTER);
        }
        if (Math.abs(cardDeckCardOffsetX - (-1)) < EPSILON) {
            cardDeckCardOffsetX = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
        }
        if (Math.abs(cardDeckCardOffsetY - (-1)) < EPSILON) {
            cardDeckCardOffsetY = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
        }
        if (Math.abs(cardDeckCardOffsetZ - (-1)) < EPSILON) {
            cardDeckCardOffsetZ = getResources().getDimension(R.dimen.cardsLayout_shadow_desk_offset);
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

    protected float getXPositionForCardDeck(float widthOfCardDeck, float rootWidth) {
        float cardPositionX = 0;
        if (cardDeckGravity.containsFlag(FlagManager.Gravity.LEFT)) {
            cardPositionX = offsetLeft;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.RIGHT)) {
            cardPositionX = rootWidth - widthOfCardDeck - offsetRight;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER_HORIZONTAL)
                || cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER)) {
            cardPositionX = rootWidth / 2f - widthOfCardDeck / 2f;
        }
        return cardPositionX;
    }

    protected float getYPositionForCardDeck(float heightOfCardDeck, float rootHeight) {
        float cardPositionY = 0;
        if (cardDeckGravity.containsFlag(FlagManager.Gravity.TOP)) {
            cardPositionY = offsetTop;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.BOTTOM)) {
            cardPositionY = rootHeight - heightOfCardDeck - offsetBottom;
        } else if (cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER_VERTICAL)
                || cardDeckGravity.containsFlag(FlagManager.Gravity.CENTER)) {
            cardPositionY = rootHeight / 2f - heightOfCardDeck / 2f;
        }
        return cardPositionY;
    }


    /* card view methods */

    @SuppressWarnings("unchecked")
    private <CV extends View & Card> void onLayoutCardsInCardDeck(List<ThreeDCardCoordinates> cardsCoordinates,
                                                                  Iterator<Card> validatedCardViews) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = cardsCoordinates.size() - 1; i >= 0; i--) {
                CV card = (CV) validatedCardViews.next();
                if (card.getVisibility() != GONE) {
                    ThreeDCardCoordinates coordinates = cardsCoordinates.get(i);
                    onLayoutCardInCardDeck(card, coordinates);
                }
            }
        } else {
            for (int i = 0; i < cardsCoordinates.size(); i++) {
                CV card = (CV) validatedCardViews.next();
                if (card.getVisibility() != GONE) {
                    ThreeDCardCoordinates coordinates = cardsCoordinates.get(i);
                    onLayoutCardInCardDeck(card, coordinates);
                }
            }
        }
    }

    private void setUpCard(Card card) {
        CardInfo cardInfo = card.getCardInfo();
        if (cardInfo == null) {
            cardInfo = new CardInfo(cards.size());
            card.setCardInfo(cardInfo);
        }
        cardInfo.setCardDistributed(false);
        card.setEnabled(false);
        this.cards.add(cardInfo.getCardPositionInLayout(), card);
    }

    private CardBoxView createCardBoxView(View view) {
        CardBoxView cardView = new CardBoxView(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        cardView.addView(view);
        return cardView;
    }

    private List<Card> getValidatedCards() {
        final List<Card> validatedCards = new ArrayList<>();
        for (Card card : cards) {
            if (!shouldPassCard(card)) {
                validatedCards.add(card);
            }
        }
        return validatedCards;
    }

    private boolean shouldPassCard(Card card) {
        return card.getVisibility() != VISIBLE || card.getCardInfo().isCardDistributed();
    }
}
