package com.blogfa.j2me.cardreader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by DB Solanki on 22/3/18.
 */

public class VTextView extends androidx.appcompat.widget.AppCompatTextView {

    public VTextView(Context context) {
        super(context);
        this.setTextColor(Color.rgb(255, 255, 255));
    }

    public VTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTextColor(Color.rgb(255, 255, 255));
    }

    public VTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTextColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        this.setTextColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        this.setTextColor(Color.WHITE);
        canvas.translate(getWidth(), 0);
        canvas.rotate(90);
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        getLayout().getPaint().setColor(Color.WHITE);
        getLayout().draw(canvas);
    }
}