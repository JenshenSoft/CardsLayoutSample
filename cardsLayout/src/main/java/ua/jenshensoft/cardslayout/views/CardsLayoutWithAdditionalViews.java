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

import ua.jenshensoft.cardslayout.CardInfo;
import ua.jenshensoft.cardslayout.R;
import ua.jenshensoft.cardslayout.util.AwesomeAnimation;
import ua.jenshensoft.cardslayout.util.FlagManager;

import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorGravity.VIEW_LOCATION_BOTTOM;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorGravity.VIEW_LOCATION_LEFT;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorGravity.VIEW_LOCATION_RIGHT;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorGravity.VIEW_LOCATION_TOP;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorPosition.VIEW_POSITION_END;
import static ua.jenshensoft.cardslayout.views.CardsLayoutWithAdditionalViews.AnchorPosition.VIEW_POSITION_START;

public abstract class CardsLayoutWithAdditionalViews<
        Entity,
        UserBarView extends View,
        GameInfoView extends View>
        extends CardsLayout<Entity> {

    //additional ua.jenshensoft.ua.jenshensoft.cardslayout.views
    @Nullable
    protected UserBarView userBarView;
    @Nullable
    protected GameInfoView gameInfoView;

    //attr
    @AnchorGravity
    private int userBarAnchorGravity;
    @AnchorGravity
    private int gameInfoBarAnchorGravity;
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


    /* private methods */

    @SuppressWarnings({"unchecked", "WrongConstant"})
    private void inflateAttributesWithAdditional(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CardsLayoutAV_Params);
            try {
                userBarAnchorGravity = attributes.getInt(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_userBar_anchorGravity, VIEW_LOCATION_LEFT);
                gameInfoBarAnchorGravity = attributes.getInt(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_gameInfoBar_anchorGravity, VIEW_LOCATION_RIGHT);
                userBarAnchorPosition = attributes.getInt(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_userBar_anchorPosition, VIEW_POSITION_START);
                gameInfoBarAnchorPosition = attributes.getInt(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_gameInfoBar_anchorPosition, VIEW_POSITION_END);
                distributeBarsByWidth = attributes.getBoolean(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_distributeBars_byWidth, false);
                distributeBarsByHeight = attributes.getBoolean(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_distributeBars_byHeight, false);
                barsMargin = attributes.getDimensionPixelOffset(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_barsMargin, 0);
                try {
                    String userBarClassName = attributes.getString(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_userBarViewClass);
                    if (userBarClassName != null) {
                        this.userBarClassName = (Class<UserBarView>) Class.forName(userBarClassName);
                    }
                    String gamInfoBarClassName = attributes.getString(R.styleable.CardsLayoutAV_Params_cardsLayoutAV_gameInfoBarViewClass);
                    if (gamInfoBarClassName != null) {
                        this.gameInfoClassName = (Class<GameInfoView>) Class.forName(gamInfoBarClassName);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(getContext().getString(R.string.cardsLayout_app_name), "You need to set your class name in layout attr!");
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

        int userBarAnchorGravity = this.userBarAnchorGravity;
        int gameInfoBarAnchorGravity = this.gameInfoBarAnchorGravity;

        if (distributeBarsByWidth) {
            userBarAnchorGravity = validateAnchorGravityByWidthDistribution(userBarAnchorGravity, userBarAnchorPosition);
            gameInfoBarAnchorGravity = validateAnchorGravityByWidthDistribution(gameInfoBarAnchorGravity, gameInfoBarAnchorPosition);
        }

        if (distributeBarsByHeight) {
            userBarAnchorGravity = validateAnchorGravityByHeightDistribution(userBarAnchorGravity, userBarAnchorPosition);
            gameInfoBarAnchorGravity = validateAnchorGravityByHeightDistribution(gameInfoBarAnchorGravity, gameInfoBarAnchorPosition);
        }

        if (userBarView != null) {
            final int[] coordinatesForUserInfo = getBarCoordinates(userBarView, getAttachedView(userBarAnchorPosition), userBarAnchorGravity);
            moveUserBarToPosition(coordinatesForUserInfo, withAnimation);
        }

        if (gameInfoView != null) {
            int[] coordinatesForGameInfoBar = getBarCoordinates(gameInfoView, getAttachedView(gameInfoBarAnchorPosition), gameInfoBarAnchorGravity);
            moveGameInfoBarToPosition(coordinatesForGameInfoBar, withAnimation);
        }
    }

    private CardView<Entity> getAttachedView(int position) {
        List<CardView<Entity>> cardViews = getCardViews();
        List<CardView<Entity>> visibleCardViews = new ArrayList<>();
        for (CardView<Entity> cardView : cardViews) {
            if (cardView.getVisibility() == VISIBLE) {
                visibleCardViews.add(cardView);
            }
        }
        switch (position) {
            case VIEW_POSITION_START:
                return visibleCardViews.iterator().next();
            case VIEW_POSITION_END:
                return visibleCardViews.get(visibleCardViews.size() - 1);
            default:
                throw new RuntimeException("Unsupported position");
        }
    }

    private void moveViewToPosition(View view, int[] coordinates, boolean isAnimated) {
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

    private int[] getBarCoordinates(View view, CardView cardView, int viewLocation) {
        CardInfo cardInfo = cardView.getCardInfo();
        int viewHeight = view.getMeasuredHeight();
        int viewWidth = view.getMeasuredWidth();
        int x;
        int y;
        switch (viewLocation) {
            case VIEW_LOCATION_LEFT:
                x = cardInfo.getFirstPositionX() - viewWidth - barsMargin;
                y = cardInfo.getFirstPositionY();
                break;
            case VIEW_LOCATION_RIGHT:
                x = cardInfo.getFirstPositionX() + cardView.getMeasuredWidth() + barsMargin;
                y = cardInfo.getFirstPositionY();
                break;
            case VIEW_LOCATION_TOP:
                x = cardInfo.getFirstPositionX();
                y = cardInfo.getFirstPositionY() - viewHeight - barsMargin;
                break;
            case VIEW_LOCATION_BOTTOM:
                x = cardInfo.getFirstPositionX();
                y = cardInfo.getFirstPositionY() + cardView.getMeasuredHeight() + barsMargin;
                break;
            default:
                throw new RuntimeException("Unsupported location");
        }
        return new int[]{x, y};
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

    @SuppressWarnings("SuspiciousNameCombination")
    @AnchorGravity
    private int validateAnchorGravityByWidthDistribution(@AnchorGravity int gravity, @AnchorPosition int position) {
        if (canDistributeByWidth()) {
            if (gravity == VIEW_LOCATION_TOP || gravity == VIEW_LOCATION_BOTTOM) {
                if (position == VIEW_POSITION_START) {
                    return VIEW_LOCATION_LEFT;
                } else if (position == VIEW_POSITION_END) {
                    return VIEW_LOCATION_RIGHT;
                } else {
                    throw new RuntimeException("Can't support this anchor position");
                }
            } else {
                return gravity;
            }
        } else {
            if (gravity == VIEW_LOCATION_LEFT || gravity == VIEW_LOCATION_RIGHT) {
                if (getGravityFlag().containsFlag(FlagManager.Gravity.BOTTOM)) {
                    return VIEW_LOCATION_TOP;
                } else if (getGravityFlag().containsFlag(FlagManager.Gravity.TOP)) {
                    return VIEW_LOCATION_BOTTOM;
                } else {
                    throw new RuntimeException("DistributeByWidth attr support only TOP or BOTTOM cardsLayout gravity attr");
                }
            } else {
                return gravity;
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @AnchorGravity
    private int validateAnchorGravityByHeightDistribution(@AnchorGravity int gravity, @AnchorPosition int position) {
        if (canDistributeByHeight()) {
            if (gravity == VIEW_LOCATION_LEFT || gravity == VIEW_LOCATION_RIGHT) {
                if (position == VIEW_POSITION_START) {
                    return VIEW_LOCATION_TOP;
                } else if (position == VIEW_POSITION_END) {
                    return VIEW_LOCATION_BOTTOM;
                } else {
                    throw new RuntimeException("Can't support this anchor position");
                }
            } else {
                return gravity;
            }
        } else {
            if (gravity == VIEW_LOCATION_TOP || gravity == VIEW_LOCATION_BOTTOM) {
                if (getGravityFlag().containsFlag(FlagManager.Gravity.LEFT)) {
                    return VIEW_LOCATION_RIGHT;
                } else if (getGravityFlag().containsFlag(FlagManager.Gravity.RIGHT)) {
                    return VIEW_LOCATION_LEFT;
                } else {
                    throw new RuntimeException("DistributeByHeight attr support only LEFT or RIGHT cardsLayout gravity attr");
                }
            } else {
                return gravity;
            }
        }
    }


    /* inner types */

    @IntDef({VIEW_LOCATION_LEFT, VIEW_LOCATION_RIGHT, VIEW_LOCATION_TOP, VIEW_LOCATION_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnchorGravity {
        int VIEW_LOCATION_LEFT = 0;
        int VIEW_LOCATION_RIGHT = 1;
        int VIEW_LOCATION_TOP = 2;
        int VIEW_LOCATION_BOTTOM = 3;
    }

    @IntDef({VIEW_POSITION_START, VIEW_POSITION_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnchorPosition {
        int VIEW_POSITION_START = 0;
        int VIEW_POSITION_END = 1;
    }
}
