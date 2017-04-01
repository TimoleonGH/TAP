package com.leon.lamti.leonmusicplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class AwesomeTab extends View {

    private Paint bgPaint, fillPaint;
    private int mWidth, mHeight, startX, startY, barWidth;
    private Context mContext;

    public AwesomeTab(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
    }

    private void init() {

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        //mPaint.setStrokeWidth(10);
        bgPaint.setAntiAlias(true);
        //bodyPaint.setDither(true);
        bgPaint.setColor(getResources().getColor(R.color.colorPrimary));

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(getResources().getColor(R.color.colorAmber));

        float scale = mContext.getResources().getDisplayMetrics().density;
        int pixels = (int) (3 * scale + 0.5f);
        mHeight = pixels;
        startX = 0;
        startY = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (true) {

            mWidth = getWidth();
            barWidth = mWidth / 3;
        }

        canvas.drawRect(0, 0, mWidth, mHeight, bgPaint);
        canvas.drawRect(startX, startY, barWidth, mHeight, fillPaint);
    }
}
