package ua.jenshensoft.cardslayout.views;


import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import ua.jenshensoft.cardslayout.util.AwesomeAnimation;
import ua.jenshensoft.cardslayout.util.FlagManager;

import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorPosition.VIEW_POSITION_CENTER;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorPosition.VIEW_POSITION_END;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorPosition.VIEW_POSITION_START;

public abstract class CardsLayoutWithAdditionalViews<
        Entity,
        UserBarView extends View,
        GameInfoView extends View>
        extends CardsLayout<Entity> {

    //additional views
    @Nullable
    protected UserBarView userBarView;
    @Nullable
    protected GameInfoView gameInfoView;

    //attr
    private FlagManager userBarAnchorGravity;
    private FlagManager gameInfoBarAnchorGravity;
    @AnchorPosition
    private int userBarAnchorPosition;
    @AnchorPosition
    private int gameInfoBarAnchorPosition;
    private int barsMargin;
    private boolean distributeBarsByWidth;
    private boolean distributeBarsByHeight;
    @Nullable
    private Class<UserBarView> userBarClassName;
    @Nullable
    private Class<GameInfoView> gameInfoClassName;

    public CardsLayoutWithAdditionalViews(Context context) {
        super(context);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(null);
        }
    }

    public CardsLayoutWithAdditionalViews(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(attrs);
        }
    }

    public CardsLayoutWithAdditionalViews(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(attrs);
        }
    }

    @SuppressWarnings("unused")
    public CardsLayoutWithAdditionalViews(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            inflateAttributesWithAdditional(attrs);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewAdded(View child) {
        if (userBarClassName != null && userBarClassName.isInstance(child)) {
            userBarView = (UserBarView) child;
        } else if (gameInfoClassName != null && gameInfoClassName.isInstance(child)) {
            gameInfoView = (GameInfoView) child;
        } else {
            super.onViewAdded(child);
        }
    }

    @CallSuper
    @Override
    protected void moveViewsToStartPosition(boolean withAnimation, @Nullable OnCreateAnimatorAction animationCreateAction, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        super.moveViewsToStartPosition(withAnimation, animationCreateAction, animatorListenerAdapter);
        moveBarsToCurrentPosition(withAnimation);
    }

    /* protected methods */

    protected <T extends View> void setPositionsForViews(Config xConfig, Config yConfig, List<T> views, boolean withAnimation) {
        int[] coordinates = new int[2];
        for (T view : views) {
            coordinates[0] = (int) xConfig.startCoordinates;
            coordinates[1] = (int) yConfig.startCoordinates;
            moveViewToPosition(view, coordinates, withAnimation);
            if (getChildListOrientation() == LinearLayout.HORIZONTAL)
                xConfig.startCoordinates += view.getMeasuredWidth() - xConfig.distanceBetweenViews;

            if (getChildListOrientation() == LinearLayout.VERTICAL)
                yConfig.startCoordinates += view.getMeasuredHeight() - yConfig.distanceBetweenViews;
        }
    }

    protected void moveUserBarToPosition(int[] coordinatesForUserInfo, boolean withAnimation) {
        moveViewToPosition(userBarView, coordinatesForUserInfo, withAnimation);
    }

    protected void moveGameInfoBarToPosition(int[] coordinatesForGameInfoBar, boolean withAnimation) {
        moveViewToPosition(gameInfoView, coordinatesForGameInfoBar, withAnimation);
    }

    protected void moveViewToPosition(View view, int[] coordinates, boolean isAnimated) {
        if (isAnimated) {
            AwesomeAnimation.Builder awesomeAnimation = new AwesomeAnimation.Builder(view)
                    .setX(AwesomeAnimation.CoordinationMode.COORDINATES, view.getX(), coordinates[0])
                    .setY(AwesomeAnimation.CoordinationMode.COORDINATES, view.getY(), coordinates[1])
                    .setDuration(getDurationOfAnimation());
            if (interpolator != null)
                awesomeAnimation.setInterpolator(interpolator);
            awesomeAnimation.build().start();
        } else {
            view.setX(coordinates[0]);
            view.setY(coordinates[1]);
        }
    }


    /* private methods */

    @SuppressWarnings({"unchecked", "WrongConstant"})
    private void inflateAttributesWithAdditional(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params);
            try {
                userBarAnchorGravity = new FlagManager(attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_userBar_anchorGravity, FlagManager.Gravity.LEFT | FlagManager.Gravity.CENTER_VERTICAL));
                gameInfoBarAnchorGravity = new FlagManager(attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_gameInfoBar_anchorGravity, FlagManager.Gravity.RIGHT | FlagManager.Gravity.CENTER_VERTICAL));
                userBarAnchorPosition = attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_userBar_anchorPosition, VIEW_POSITION_START);
                gameInfoBarAnchorPosition = attributes.getInt(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_gameInfoBar_anchorPosition, VIEW_POSITION_END);
                distributeBarsByWidth = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_distributeBars_byWidth, false);
                distributeBarsByHeight = attributes.getBoolean(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_distributeBars_byHeight, false);
                barsMargin = attributes.getDimensionPixelOffset(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_barsMargin, 0);
                try {
                    String userBarClassName = attributes.getString(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_userBarViewClass);
                    if (userBarClassName != null) {
                        this.userBarClassName = (Class<UserBarView>) Class.forName(userBarClassName);
                    }
                    String gamInfoBarClassName = attributes.getString(ua.jenshensoft.cardslayout.R.styleable.CardsLayoutAV_Params_cardsLayoutAV_gameInfoBarViewClass);
                    if (gamInfoBarClassName != null) {
                        this.gameInfoClassName = (Class<GameInfoView>) Class.forName(gamInfoBarClassName);
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

    private void moveBarsToCurrentPosition(boolean withAnimation) {
        List<CardView<Entity>> cardViews = getCardViews();

        List<CardView<Entity>> visibleCardViews = new ArrayList<>();
        for (CardView<Entity> cardView : cardViews) {
            if (cardView.getVisibility() == VISIBLE) {
                visibleCardViews.add(cardView);
            }
        }
        if (visibleCardViews.isEmpty()) {
            setBarsStartPosition(withAnimation);
            return;
        }

        FlagManager userBarAnchorGravity = this.userBarAnchorGravity;
        FlagManager gameInfoBarAnchorGravity = this.gameInfoBarAnchorGravity;

        if (distributeBarsByWidth) {
            userBarAnchorGravity = validateAnchorGravityByWidthDistribution(userBarAnchorPosition);
            gameInfoBarAnchorGravity = validateAnchorGravityByWidthDistribution(gameInfoBarAnchorPosition);
        }

        if (distributeBarsByHeight) {
            userBarAnchorGravity = validateAnchorGravityByHeightDistribution(userBarAnchorPosition);
            gameInfoBarAnchorGravity = validateAnchorGravityByHeightDistribution(gameInfoBarAnchorPosition);
        }

        if (userBarView != null) {
            final int[] coordinatesForUserInfo = getBarCoordinates(userBarAnchorGravity, getAnchorViewInfo(userBarAnchorPosition), userBarView);
            moveUserBarToPosition(coordinatesForUserInfo, withAnimation);
        }

        if (gameInfoView != null) {
            int[] coordinatesForGameInfoBar = getBarCoordinates(gameInfoBarAnchorGravity, getAnchorViewInfo(gameInfoBarAnchorPosition), gameInfoView);
            moveGameInfoBarToPosition(coordinatesForGameInfoBar, withAnimation);
        }
    }

    private AnchorViewInfo getAnchorViewInfo(@AnchorPosition int position) {
        List<CardView<Entity>> cardViews = getCardViews();
        List<CardView<Entity>> visibleCardViews = new ArrayList<>();
        for (CardView<Entity> cardView : cardViews) {
            if (cardView.getVisibility() == VISIBLE) {
                visibleCardViews.add(cardView);
            }
        }
        int firstPositionX;
        int firstPositionY;
        int cardsLayoutWidth;
        int cardsLayoutHeight;
        final CardView<Entity> cardView;
        switch (position) {
            case VIEW_POSITION_START:
                cardView = visibleCardViews.iterator().next();
                firstPositionX = cardView.getCardInfo().getFirstPositionX();
                firstPositionY = cardView.getCardInfo().getFirstPositionY();
                cardsLayoutWidth = cardView.getMeasuredHeight();
                cardsLayoutHeight = cardView.getMeasuredHeight();
                break;
            case VIEW_POSITION_END:
                cardView = visibleCardViews.get(visibleCardViews.size() - 1);
                firstPositionX = cardView.getCardInfo().getFirstPositionX();
                firstPositionY = cardView.getCardInfo().getFirstPositionY();
                cardsLayoutWidth = cardView.getMeasuredHeight();
                cardsLayoutHeight = cardView.getMeasuredHeight();
                break;
            case VIEW_POSITION_CENTER:
                if (visibleCardViews.size() == 1) {
                    cardView = visibleCardViews.iterator().next();
                    firstPositionX = cardView.getCardInfo().getFirstPositionX();
                    firstPositionY = cardView.getCardInfo().getFirstPositionY();
                } else if (visibleCardViews.size() % 2 == 0) {
                    cardView = visibleCardViews.get(visibleCardViews.size() / 2 - 1);
                    final CardView<Entity> middleRightCardView = visibleCardViews.get(visibleCardViews.size() / 2);
                    firstPositionX = Math.round((cardView.getCardInfo().getFirstPositionX() + middleRightCardView.getCardInfo().getFirstPositionX()) / 2f);
                    firstPositionY = Math.round((cardView.getCardInfo().getFirstPositionY() + middleRightCardView.getCardInfo().getFirstPositionY()) / 2f);
                } else {
                    cardView = visibleCardViews.get(visibleCardViews.size() / 2);
                    firstPositionX = cardView.getCardInfo().getFirstPositionX();
                    firstPositionY = cardView.getCardInfo().getFirstPositionY();
                }
                cardsLayoutWidth = (int) getChildWidth(visibleCardViews);
                cardsLayoutHeight = (int) getChildHeight(visibleCardViews);
                break;
            default:
                throw new RuntimeException("Unsupported position");
        }
        return new AnchorViewInfo(firstPositionX, firstPositionY, cardsLayoutWidth, cardsLayoutHeight);
    }

    /**
     * call this if cards layout is empty
     */
    private void setBarsStartPosition(boolean withAnimation) {
        List<View> views = new ArrayList<>();
        if (userBarView != null) {
            views.add(userBarView);
        }
        if (gameInfoView != null) {
            views.add(gameInfoView);
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
        setPositionsForViews(xConfig, yConfig, views, withAnimation);

        setChildListPaddingTop(childListPaddingTop);
        setChildListPaddingBottom(childListPaddingBottom);
        setChildListPaddingLeft(childListPaddingLeft);
        setChildListPaddingRight(childListPaddingRight);
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
        float widthOfViews = getWidthOfViews(getCardViews(), 0);
        float difference = rootWidth - widthOfViews;

        int additionalViewsWidth = 0;
        if (userBarView != null) {
            additionalViewsWidth += userBarView.getMeasuredWidth();
        }
        if (gameInfoView != null) {
            additionalViewsWidth += gameInfoView.getMeasuredWidth();
        }
        return difference >= additionalViewsWidth;
    }

    @CheckResult
    private boolean canDistributeByHeight() {
        float rootHeight = getRootHeight();
        float heightOfViews = getHeightOfViews(getCardViews(), 0);
        float difference = rootHeight - heightOfViews;

        int additionalViewsHeight = 0;
        if (userBarView != null) {
            additionalViewsHeight += userBarView.getMeasuredHeight();
        }
        if (gameInfoView != null) {
            additionalViewsHeight += gameInfoView.getMeasuredHeight();
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

    private static class AnchorViewInfo {
        private int firstPositionX;
        private int firstPositionY;
        private int cardsLayoutWidth;
        private int cardsLayoutHeight;

        private AnchorViewInfo(int firstPositionX, int firstPositionY, int cardsLayoutWidth, int cardsLayoutHeight) {
            this.firstPositionX = firstPositionX;
            this.firstPositionY = firstPositionY;
            this.cardsLayoutWidth = cardsLayoutWidth;
            this.cardsLayoutHeight = cardsLayoutHeight;
        }

        int getFirstPositionX() {
            return firstPositionX;
        }

        int getFirstPositionY() {
            return firstPositionY;
        }

        int getCardsLayoutWidth() {
            return cardsLayoutWidth;
        }

        int getCardsLayoutHeight() {
            return cardsLayoutHeight;
        }
    }
}
