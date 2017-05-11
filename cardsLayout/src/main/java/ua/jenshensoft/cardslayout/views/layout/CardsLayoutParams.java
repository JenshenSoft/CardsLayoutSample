package ua.jenshensoft.cardslayout.views.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import ua.jenshensoft.cardslayout.R;

public class CardsLayoutParams extends ViewGroup.LayoutParams {

    private int widthForCalculation = -1;
    private int heightForCalculation = -1;

    public CardsLayoutParams(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CardsLayout_Params);
        try {
            widthForCalculation = arr.getDimensionPixelSize(R.styleable.CardsLayout_Params_cardsLayout_widthForCalculation, -1);
            heightForCalculation = arr.getDimensionPixelSize(R.styleable.CardsLayout_Params_cardsLayout_heightForCalculation, -1);
        } finally {
            arr.recycle();
        }
    }

    public CardsLayoutParams(int width, int height) {
        super(width, height);
    }

    public CardsLayoutParams(ViewGroup.LayoutParams source) {
        super(source);
    }

    public int getWidthForCalculation() {
        return widthForCalculation;
    }

    public int getHeightForCalculation() {
        return heightForCalculation;
    }
}