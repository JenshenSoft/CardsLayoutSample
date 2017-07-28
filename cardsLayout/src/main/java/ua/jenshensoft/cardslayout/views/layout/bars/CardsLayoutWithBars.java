package ua.jenshensoft.cardslayout.views.layout.bars;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jenshen.awesomeanimation.AwesomeAnimation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.pattern.models.CardCoordinates;
import ua.jenshensoft.cardslayout.util.FlagManager;
import ua.jenshensoft.cardslayout.views.ValidateViewBlocker;
import ua.jenshensoft.cardslayout.views.card.Card;
import ua.jenshensoft.cardslayout.views.layout.CardsLayout;
import ua.jenshensoft.cardslayout.views.layout.Config;

import static ua.jenshensoft.cardslayout.views.layout.bars.CardsLayoutWithBars.AnchorPosition.VIEW_POSITION_CENTER;
import static ua.jenshensoft.cardslayout.views.layout.bars.CardsLayoutWithBars.AnchorPosition.VIEW_POSITION_END;
import static ua.jenshensoft.cardslayout.views.layout.bars.CardsLayoutWithBars.AnchorPosition.VIEW_POSITION_START;

public abstract class CardsLayoutWithBars<
        FirstBarView extends View & ValidateViewBlocker,
        SecondBarView extends View & ValidateViewBlocker>
        extends CardsLayout {

    //additional views
    @Nullable
    protected FirstBarView firstBarView;
    @Nullable
    protected SecondBarView secondBarView;

    //attr
    private FlagManager firstBarAnchorGravity;
    private FlagManager secondBarAnchorGravity;
    @AnchorPosition
    private int firstBarAnchorPosition;
    @AnchorPosition
    private int secondBarAnchorPosition;
    private int barsMargin;
    private boolean distributeBarsByWidth;
    private boolean distributeBarsByHeight;
    @Nullable
    private Class<FirstBarView> firstBarClassName;
    @Nullable
    private Class<SecondBarView> secondBarClassName;

    public CardsLayoutWithBars(Context context) {
        super(context);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(null);
        }
    }

    public CardsLayoutWithBars(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(attrs);
        }
    }

    public CardsLayoutWithBars(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(attrs);
        }
    }

    @SuppressWarnings("unused")
    public CardsLayoutWithBars(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(attrs);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewAdded(View child) {
        if (firstBarClassName != null && firstBarClassName.isInstance(child)) {
            firstBarView = (FirstBarView) child;
        } else if (secondBarClassName != null && secondBarClassName.isInstance(child)) {
            secondBarView = (SecondBarView) child;
        } else {
            super.onViewAdded(child);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Iterator<CardCoordinates> cardCoordinates = getCoordinatesForBars().iterator();
        if (cardCoordinates.hasNext()) {
            onLayoutAdditionView(firstBarView, cardCoordinates::next);
        }
        if (cardCoordinates.hasNext()) {
            onLayoutAdditionView(secondBarView, cardCoordinates::next);
        }
    }

    protected <V extends View & ValidateViewBlocker> void onLayoutAdditionView(V view, CoordinatesProvider coordinatesProvider) {
        if (view != null
                && !view.isInAnimation()
                && view.getVisibility() != GONE) {
            CardCoordinates coordinates = coordinatesProvider.get();
            int x = Math.round(coordinates.getX());
            int y = Math.round(coordinates.getY());
            view.layout(x, y, x + view.getMeasuredWidth(), y + view.getMeasuredHeight());
            if (Math.abs(view.getX() - x) > EPSILON) {
                view.setX(x);
            }
            if (Math.abs(view.getY() - y) > EPSILON) {
                view.setY(y);
            }
        }
    }

    /* protected methods */

    @Nullable
    @Override
    public <CV extends View & Card> AnimatorSet createAnimationOrMoveToPosition(boolean withAnimation,
                                                                                @Nullable OnCreateAnimatorAction animationCreateAction) {
        AnimatorSet animatorSet = super.createAnimationOrMoveToPosition(withAnimation, animationCreateAction);
        Iterator<CardCoordinates> cardCoordinates = getCoordinatesForBars().iterator();
        if (firstBarView != null && firstBarView.getVisibility() != GONE) {
            CardCoordinates coordinates = cardCoordinates.next();
            int x = Math.round(coordinates.getX());
            int y = Math.round(coordinates.getY());
            AnimatorSet firstBarAnimation = moveFirstBarToPosition(new int[]{x, y}, withAnimation, null);
            if (animatorSet == null) {
                animatorSet = firstBarAnimation;
            } else {
                animatorSet.playTogether(firstBarAnimation);
            }
        }
        if (secondBarView != null && secondBarView.getVisibility() != GONE) {
            CardCoordinates coordinates = cardCoordinates.next();
            int x = Math.round(coordinates.getX());
            int y = Math.round(coordinates.getY());
            AnimatorSet secondBarAnimation = moveSecondBarToPosition(new int[]{x, y}, withAnimation, null);
            if (animatorSet == null) {
                animatorSet = secondBarAnimation;
            } else {
                animatorSet.playTogether(secondBarAnimation);
            }
        }
        return animatorSet;
    }

    @Nullable
    protected AnimatorSet moveFirstBarToPosition(int[] coordinatesForUserInfo,
                                                 boolean withAnimation,
                                                 @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        return moveViewToStartPosition(firstBarView, coordinatesForUserInfo, withAnimation, animatorListenerAdapter);
    }

    @Nullable
    protected AnimatorSet moveSecondBarToPosition(int[] coordinatesForGameInfoBar,
                                                  boolean withAnimation,
                                                  @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        return moveViewToStartPosition(secondBarView, coordinatesForGameInfoBar, withAnimation, animatorListenerAdapter);
    }

    @Nullable
    protected <V extends View & ValidateViewBlocker> AnimatorSet moveViewToStartPosition(V view,
                                                                                         int[] coordinates,
                                                                                         boolean withAnimation,
                                                                                         @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if (Math.abs(coordinates[0] - (view.getX())) < EPSILON && Math.abs(coordinates[1] - (view.getY())) < EPSILON) {
            return null;
        }
        if (withAnimation) {
            return createAnimationForView(view, coordinates, animatorListenerAdapter);
        } else {
            view.setX(coordinates[0]);
            view.setY(coordinates[1]);
        }
        return null;
    }

    @Nullable
    protected <V extends View & ValidateViewBlocker> AnimatorSet createAnimationForView(V view,
                                                                                        int[] coordinates,
                                                                                        @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if (Math.abs(coordinates[0] - (view.getX())) < EPSILON && Math.abs(coordinates[1] - (view.getY())) < EPSILON) {
            return null;
        }
        AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(view)
                .setX(AwesomeAnimation.CoordinationMode.COORDINATES, view.getX(), coordinates[0])
                .setY(AwesomeAnimation.CoordinationMode.COORDINATES, view.getY(), coordinates[1])
                .setDuration(getDurationOfAnimation());
        if (interpolator != null)
            awesomeAnimation.setInterpolator(interpolator);
        AwesomeAnimation build = awesomeAnimation.build();
        AnimatorSet animatorSet = build.getAnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setInAnimation(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setInAnimation(false);
            }
        });
        if (animatorListenerAdapter != null) {
            animatorSet.addListener(animatorListenerAdapter);
        }
        return animatorSet;
    }

    protected <T extends View> List<CardCoordinates> getCoordinatesForViews(Config xConfig, Config yConfig, List<T> views) {
        List<CardCoordinates> cardCoordinates = new ArrayList<>();
        for (T view : views) {
            int x = (int) xConfig.getStartCoordinates();
            int y = (int) yConfig.getStartCoordinates();
            cardCoordinates.add(new CardCoordinates(x, y, 0));
            if (getChildListOrientation() == LinearLayoutCompat.HORIZONTAL)
                xConfig.setStartCoordinates(xConfig.getStartCoordinates() + view.getMeasuredWidth() - xConfig.getDistanceBetweenViews());

            if (getChildListOrientation() == LinearLayoutCompat.VERTICAL)
                yConfig.setStartCoordinates(yConfig.getStartCoordinates() + view.getMeasuredWidth() - yConfig.getDistanceBetweenViews());
        }
        return cardCoordinates;
    }


    /* private methods */

    @SuppressWarnings({"unchecked", "WrongConstant"})
    private void inflateAttributesWithAdditional(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CardsLayoutAV);
            try {
                firstBarAnchorGravity = new FlagManager(attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_firstBar_anchorGravity, FlagManager.Gravity.LEFT | FlagManager.Gravity.CENTER_VERTICAL));
                secondBarAnchorGravity = new FlagManager(attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_secondBar_anchorGravity, FlagManager.Gravity.RIGHT | FlagManager.Gravity.CENTER_VERTICAL));
                firstBarAnchorPosition = attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_firstBar_anchorPosition, VIEW_POSITION_START);
                secondBarAnchorPosition = attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_secondBar_anchorPosition, VIEW_POSITION_END);
                distributeBarsByWidth = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_distributeBars_byWidth, false);
                distributeBarsByHeight = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_distributeBars_byHeight, false);
                barsMargin = attributes.getDimensionPixelOffset(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_barsMargin, 0);
                try {
                    String userBarClassName = attributes.getString(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_firstBarViewClass);
                    if (userBarClassName != null) {
                        this.firstBarClassName = (Class<FirstBarView>) Class.forName(userBarClassName);
                    }
                    String gamInfoBarClassName = attributes.getString(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_cardsLayoutAV_secondBarViewClass);
                    if (gamInfoBarClassName != null) {
                        this.secondBarClassName = (Class<SecondBarView>) Class.forName(gamInfoBarClassName);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(getContext().getString(ua.jenshensoft.cardslayout.R.string.cardsLayout_app_name), "You need to set your class name in layout attr!");
                }

                if (distributeBarsByHeight && distributeBarsByWidth) {
                    throw new RuntimeException("You can't use both of distribute attr, use only distributeBarsByHeight or distributeBarsByWidth");
                }
            } finally {
                attributes.recycle();
            }
        }
    }

    private List<CardCoordinates> getCoordinatesForBars() {
        List<Card> cardViews = getCards();

        List<Card> visibleCardViews = new ArrayList<>();
        for (Card cardView : cardViews) {
            if (cardView.getCardInfo().isCardDistributed() && cardView.getVisibility() == VISIBLE) {
                visibleCardViews.add(cardView);
            }
        }
        if (visibleCardViews.isEmpty()) {
            return getBarsStartPosition();
        }

        FlagManager firstBarAnchorGravity = this.firstBarAnchorGravity;
        FlagManager secondBarAnchorGravity = this.secondBarAnchorGravity;

        if (distributeBarsByWidth) {
            firstBarAnchorGravity = validateAnchorGravityByWidthDistribution(firstBarAnchorPosition);
            secondBarAnchorGravity = validateAnchorGravityByWidthDistribution(secondBarAnchorPosition);
        }

        if (distributeBarsByHeight) {
            firstBarAnchorGravity = validateAnchorGravityByHeightDistribution(firstBarAnchorPosition);
            secondBarAnchorGravity = validateAnchorGravityByHeightDistribution(secondBarAnchorPosition);
        }
        List<CardCoordinates> cardCoordinates = new ArrayList<>();
        if (firstBarView != null) {
            final int[] coordinatesForUserInfo = getBarCoordinates(firstBarAnchorGravity, getAnchorViewInfo(firstBarAnchorPosition), firstBarView);
            cardCoordinates.add(new CardCoordinates(coordinatesForUserInfo[0], coordinatesForUserInfo[1], 0));
        }

        if (secondBarView != null) {
            int[] coordinatesForGameInfoBar = getBarCoordinates(secondBarAnchorGravity, getAnchorViewInfo(secondBarAnchorPosition), secondBarView);
            cardCoordinates.add(new CardCoordinates(coordinatesForGameInfoBar[0], coordinatesForGameInfoBar[1], 0));
        }
        return cardCoordinates;
    }

    private <CV extends View & Card> AnchorViewInfo getAnchorViewInfo(@AnchorPosition int position) {
        List<CV> cardViews = getCardViews();
        List<CV> visibleCardViews = new ArrayList<>();
        for (CV card : cardViews) {
            if (card.getCardInfo().isCardDistributed() && card.getVisibility() == VISIBLE) {
                visibleCardViews.add(card);
            }
        }
        int firstPositionX;
        int firstPositionY;
        int cardsLayoutWidth;
        int cardsLayoutHeight;
        final CV cardView;
        switch (position) {
            case VIEW_POSITION_START:
                cardView = visibleCardViews.iterator().next();
                firstPositionX = cardView.getCardInfo().getFirstPositionX();
                firstPositionY = cardView.getCardInfo().getFirstPositionY();
                cardsLayoutWidth = getChildWidth(cardView);
                cardsLayoutHeight = getChildHeight(cardView);
                break;
            case VIEW_POSITION_END:
                cardView = visibleCardViews.get(visibleCardViews.size() - 1);
                firstPositionX = cardView.getCardInfo().getFirstPositionX();
                firstPositionY = cardView.getCardInfo().getFirstPositionY();
                cardsLayoutWidth = getChildWidth(cardView);
                cardsLayoutHeight = getChildHeight(cardView);
                break;
            case VIEW_POSITION_CENTER:
                if (visibleCardViews.size() == 1) {
                    cardView = visibleCardViews.iterator().next();
                    firstPositionX = cardView.getCardInfo().getFirstPositionX();
                    firstPositionY = cardView.getCardInfo().getFirstPositionY();
                } else if (visibleCardViews.size() % 2 == 0) {
                    cardView = visibleCardViews.get(visibleCardViews.size() / 2 - 1);
                    final CV middleRightCardView = visibleCardViews.get(visibleCardViews.size() / 2);
                    firstPositionX = Math.round((cardView.getCardInfo().getFirstPositionX() + middleRightCardView.getCardInfo().getFirstPositionX()) / 2f);
                    firstPositionY = Math.round((cardView.getCardInfo().getFirstPositionY() + middleRightCardView.getCardInfo().getFirstPositionY()) / 2f);
                } else {
                    cardView = visibleCardViews.get(visibleCardViews.size() / 2);
                    firstPositionX = cardView.getCardInfo().getFirstPositionX();
                    firstPositionY = cardView.getCardInfo().getFirstPositionY();
                }
                cardsLayoutWidth = getMaxChildWidth(visibleCardViews);
                cardsLayoutHeight = getMaxChildHeight(visibleCardViews);
                break;
            default:
                throw new RuntimeException("Unsupported position");
        }
        return new AnchorViewInfo(firstPositionX, firstPositionY, cardsLayoutWidth, cardsLayoutHeight);
    }

    /**
     * call this  method if cards layout is empty
     */
    private List<CardCoordinates> getBarsStartPosition() {
        List<View> views = new ArrayList<>();
        if (firstBarView != null) {
            views.add(firstBarView);
        }
        if (secondBarView != null) {
            views.add(secondBarView);
        }

        final int childListPaddingBottom = getChildListPaddingBottom();
        final int childListPaddingRight = getChildListPaddingRight();
        final int childListPaddingLeft = getChildListPaddingLeft();
        final int childListPaddingTop = getChildListPaddingTop();

        setChildListPaddingTop(0);
        setChildListPaddingBottom(0);
        setChildListPaddingLeft(0);
        setChildListPaddingRight(0);

        Config xConfig = getXConfiguration(views);
        Config yConfig = getYConfiguration(views);

        List<CardCoordinates> positionsForBars = getCoordinatesForViews(xConfig, yConfig, views);
        setChildListPaddingTop(childListPaddingTop);
        setChildListPaddingBottom(childListPaddingBottom);
        setChildListPaddingLeft(childListPaddingLeft);
        setChildListPaddingRight(childListPaddingRight);
        return positionsForBars;
    }

    private int[] getBarCoordinates(FlagManager flagManager, AnchorViewInfo anchorViewInfo, View view) {
        int x = getXPositionForBar(flagManager, anchorViewInfo, view);
        int y = getYPositionForBar(flagManager, anchorViewInfo, view);
        return new int[]{x, y};
    }

    protected int getXPositionForBar(FlagManager gravityFlag, AnchorViewInfo anchorViewInfo, View barView) {
        int firstPositionX = anchorViewInfo.getFirstPositionX();
        int cardsLayoutWidth = anchorViewInfo.getCardsLayoutWidth();
        if (gravityFlag.containsFlag(FlagManager.Gravity.LEFT)) {
            return firstPositionX - barView.getMeasuredWidth() - barsMargin;
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.RIGHT)) {
            return firstPositionX + cardsLayoutWidth + barsMargin;
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.CENTER_HORIZONTAL)
                || gravityFlag.containsFlag(FlagManager.Gravity.CENTER)) {
            return firstPositionX + cardsLayoutWidth / 2 - barView.getMeasuredWidth() / 2;
        } else {
            return firstPositionX;
        }
    }

    protected int getYPositionForBar(FlagManager gravityFlag, AnchorViewInfo anchorViewInfo, View barView) {
        int firstPositionY = anchorViewInfo.getFirstPositionY();
        int cardsLayoutHeight = anchorViewInfo.getCardsLayoutHeight();
        if (gravityFlag.containsFlag(FlagManager.Gravity.TOP)) {
            return firstPositionY - barView.getMeasuredHeight() - barsMargin;
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.BOTTOM)) {
            return firstPositionY + cardsLayoutHeight + barsMargin;
        } else if (gravityFlag.containsFlag(FlagManager.Gravity.CENTER_VERTICAL)
                || gravityFlag.containsFlag(FlagManager.Gravity.CENTER)) {
            return firstPositionY + cardsLayoutHeight / 2 - barView.getMeasuredHeight() / 2;
        } else {
            return firstPositionY;
        }
    }

    @CheckResult
    private boolean canDistributeByWidth() {
        float rootWidth = getRootWidth();
        float widthOfViews = getWidthOfChildren(getCardViews(), 0);
        float difference = rootWidth - widthOfViews;

        int additionalViewsWidth = 0;
        if (firstBarView != null) {
            additionalViewsWidth += firstBarView.getMeasuredWidth();
        }
        if (secondBarView != null) {
            additionalViewsWidth += secondBarView.getMeasuredWidth();
        }
        return difference >= additionalViewsWidth;
    }

    @CheckResult
    private boolean canDistributeByHeight() {
        float rootHeight = getRootHeight();
        float heightOfViews = getHeightOfChildren(getCardViews(), 0);
        float difference = rootHeight - heightOfViews;

        int additionalViewsHeight = 0;
        if (firstBarView != null) {
            additionalViewsHeight += firstBarView.getMeasuredHeight();
        }
        if (secondBarView != null) {
            additionalViewsHeight += secondBarView.getMeasuredHeight();
        }
        return difference >= additionalViewsHeight;
    }

    private FlagManager validateAnchorGravityByWidthDistribution(@AnchorPosition int position) {
        FlagManager flagManager = new FlagManager();
        if (canDistributeByWidth()) {
            if (position == VIEW_POSITION_START) {
                flagManager.addFlag(FlagManager.Gravity.LEFT);
                flagManager.addFlag(FlagManager.Gravity.CENTER_VERTICAL);
            } else if (position == VIEW_POSITION_END) {
                flagManager.addFlag(FlagManager.Gravity.RIGHT);
                flagManager.addFlag(FlagManager.Gravity.CENTER_VERTICAL);
            } else {
                throw new RuntimeException("Can't support this anchor position " + position);
            }
        } else {
            if (getGravityFlag().containsFlag(FlagManager.Gravity.BOTTOM)) {
                flagManager.addFlag(FlagManager.Gravity.TOP);
                flagManager.addFlag(FlagManager.Gravity.CENTER_HORIZONTAL);
            } else if (getGravityFlag().containsFlag(FlagManager.Gravity.TOP)) {
                flagManager.addFlag(FlagManager.Gravity.BOTTOM);
                flagManager.addFlag(FlagManager.Gravity.CENTER_HORIZONTAL);
            } else {
                throw new RuntimeException("DistributeByWidth attr support only TOP or BOTTOM cardsLayout gravity attr");
            }
        }
        return flagManager;
    }

    private FlagManager validateAnchorGravityByHeightDistribution(@AnchorPosition int position) {
        FlagManager flagManager = new FlagManager();
        if (canDistributeByHeight()) {
            if (position == VIEW_POSITION_START) {
                flagManager.addFlag(FlagManager.Gravity.TOP);
                flagManager.addFlag(FlagManager.Gravity.CENTER_HORIZONTAL);
            } else if (position == VIEW_POSITION_END) {
                flagManager.addFlag(FlagManager.Gravity.BOTTOM);
                flagManager.addFlag(FlagManager.Gravity.CENTER_HORIZONTAL);
            } else {
                throw new RuntimeException("Can't support this anchor position " + position);
            }
        } else {
            if (getGravityFlag().containsFlag(FlagManager.Gravity.LEFT)) {
                flagManager.addFlag(FlagManager.Gravity.RIGHT);
                flagManager.addFlag(FlagManager.Gravity.CENTER_VERTICAL);
            } else if (getGravityFlag().containsFlag(FlagManager.Gravity.RIGHT)) {
                flagManager.addFlag(FlagManager.Gravity.LEFT);
                flagManager.addFlag(FlagManager.Gravity.CENTER_VERTICAL);
            } else {
                throw new RuntimeException("DistributeByHeight attr support only LEFT or RIGHT cardsLayout gravity attr");
            }
        }
        return flagManager;
    }


    /* inner types */

    @IntDef({VIEW_POSITION_START, VIEW_POSITION_END, VIEW_POSITION_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnchorPosition {
        int VIEW_POSITION_START = 0;
        int VIEW_POSITION_END = 1;
        int VIEW_POSITION_CENTER = 2;
    }

    public interface CoordinatesProvider {
        CardCoordinates get();
    }
}
