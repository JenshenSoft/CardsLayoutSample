package ua.jenshensoft.cardslayout.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.BOTTOM;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.CENTER;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.CENTER_HORIZONTAL;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.CENTER_VERTICAL;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.EMPTY;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.LEFT;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.RIGHT;
import static ua.jenshensoft.cardslayout.util.FlagManager.Gravity.TOP;

public class FlagManager implements Cloneable {

    @Gravity
    private int flagSet;

    public FlagManager() {
        this.flagSet = EMPTY;
    }

    public FlagManager(@Gravity int flagSet) {
        this.flagSet = flagSet;
    }

    @Gravity
    public int getFlagSet() {
        return flagSet;
    }

    public void reset() {
        this.flagSet = EMPTY;
    }

    @SuppressWarnings("WrongConstant")
    public boolean containsFlag(@Gravity int flag) {
        return (flagSet | flag) == flagSet; // or flagSet&flag) == flag
    }

    /**
     * @param flag current flag
     */
    @SuppressWarnings("WrongConstant")
    public void addFlag(@Gravity int flag) {
        flagSet = flagSet | flag;
    }

    /**
     * Example 1: 110^010 = 100 (Binary)
     * 6 ^ 2  =  4  (Decimal)
     * Example 2: 100^010 = 110 (Binary)
     * 4 ^ 2  =  6  (Decimal)
     *
     * @param flag current flag
     */
    @SuppressWarnings("WrongConstant")
    public void toggleFlag(@Gravity int flag) {
        flagSet = flagSet ^ flag;
    }

    /**
     * Example
     * 110&amp;(~010) = 110&amp;101 = 100 (Binary)
     * 6 &amp;(~ 2 ) =  6 &amp; 5  =  4  (Decimal)
     *
     * @param flag current flag
     */
    @SuppressWarnings("WrongConstant")
    public void removeFlag(@Gravity int flag) {
        flagSet = flagSet & (~flag);
    }

    @Override
    public FlagManager clone() {
        try {
            return (FlagManager) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone this object");
        }
    }

    @IntDef({RIGHT, LEFT, TOP, BOTTOM, CENTER, CENTER_HORIZONTAL, CENTER_VERTICAL, EMPTY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {
        int RIGHT = 1; // 0000 0001
        int LEFT = 2; // 0000 0010
        int TOP = 4; // 0000 0100
        int BOTTOM = 8; // 0000 1000
        int CENTER = 16; // 0001 0000
        int CENTER_HORIZONTAL = 32; // 0010 0000;
        int CENTER_VERTICAL = 64; // 0100 0000;
        int EMPTY = 0;
    }
}
