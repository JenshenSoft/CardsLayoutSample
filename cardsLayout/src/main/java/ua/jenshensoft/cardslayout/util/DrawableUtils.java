package ua.jenshensoft.cardslayout.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DrawableUtils {

    public static void tintWidget(View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable, color);
        setBackground(wrappedDrawable, view);
    }

    public static Drawable getTintedDrawable(Context context, @DrawableRes int drawableResId, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void setBackground(Drawable drawable, View view) {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(drawable);
        } else {
            setBackgroundV16(drawable, view);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setBackgroundV16(Drawable drawable, View view) {
        view.setBackground(drawable);
    }

    public static void setColorFilter(View view, @Nullable ColorFilter colorFilter) {
        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(colorFilter);
            if (colorFilter == null) {
                ((ImageView) view).clearColorFilter();
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            final Drawable background = viewGroup.getBackground();
            if (background != null) {
                background.setColorFilter(colorFilter);
                if (colorFilter == null) {
                    background.clearColorFilter();
                }
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setColorFilter(viewGroup.getChildAt(i), colorFilter);
            }
        }
    }
}
