package com.leon.lamti.leonmusicplayer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class AwesomeProgressBar extends View {

    private Paint mPaint, mPaint2, mPaint3, mBgPaint2, mBgPaint3, mTransparentPaint;
    private RectF oval = new RectF();
    private Canvas mCanvas;

    private int mWidth, mHeight, mWidth2, mHeight2, mProgressAngle, mProgressAngle2;
    private Context mContext;

    private Handler mHandler;

    public AwesomeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mHandler = new Handler();
        init();
    }

    private void init() {

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setStrokeWidth(10);
        mPaint.setAntiAlias(true);
        //bodyPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));

        mPaint2 = new Paint();
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(15);
        mPaint2.setAntiAlias(true);
        //bodyPaint.setDither(true);
        mPaint2.setColor(getResources().getColor(R.color.colorTeal));

        mPaint3 = new Paint();
        mPaint3.setStyle(Paint.Style.FILL);
        mPaint3.setStrokeWidth(15);
        mPaint3.setAntiAlias(true);
        //bodyPaint3.setDither(true);
        mPaint3.setColor(getResources().getColor(R.color.colorTeal));

        mBgPaint2 = new Paint();
        mBgPaint2.setStyle(Paint.Style.STROKE);
        mBgPaint2.setStrokeWidth(15);
        mBgPaint2.setAntiAlias(true);
        //bodyPaint.setDither(true);
        mBgPaint2.setColor(getResources().getColor(R.color.colorTealLight));

        mBgPaint3 = new Paint();
        mBgPaint3.setStyle(Paint.Style.FILL);
        mBgPaint3.setStrokeWidth(15);
        mBgPaint3.setAntiAlias(true);
        //bodyPaint3.setDither(true);
        mBgPaint3.setColor(getResources().getColor(R.color.colorTealLight));

        mTransparentPaint = new Paint();
        mTransparentPaint.setStyle(Paint.Style.FILL);
        //mTransparentPaint.setStrokeWidth(10);
        mTransparentPaint.setAntiAlias(true);
        //bodyPaint.setDither(true);
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));



        float scale = mContext.getResources().getDisplayMetrics().density;
        int pixels = (int) (60 * scale + 0.5f);

        mHeight = pixels;
        mHeight2 = pixels;
        mWidth2 = 0;
        mProgressAngle = 0;
        mProgressAngle2 = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();

        //canvas.drawRect(0, (mHeight/2) - 10, mWidth, (mHeight/2) + 10, mBgPaint3);
        //canvas.drawRect(0, (mHeight/2) - 10, mWidth, (mHeight/2) + 10, mPaint3);
        canvas.drawLine(0, (mHeight/2), mWidth, (mHeight/2), mBgPaint3);
        canvas.drawLine(0, (mHeight / 2), mWidth2, (mHeight / 2), mPaint3);

        //oval.set( (mWidth / 2) - (mHeight2 / 2), mHeight - (mHeight2 / 2), (mWidth / 2) + (mHeight2 / 2), mHeight + (mHeight2 / 2));
        oval.set( (mWidth / 2) - (mHeight / 2) + 25, 25, (mWidth / 2) + ( mHeight / 2 ) - 25, mHeight - 25 );
        canvas.drawArc(oval, 0, 360, false, mBgPaint2);
        canvas.drawArc(oval, 180, mProgressAngle, false, mPaint2);
        canvas.drawArc(oval, 180, mProgressAngle2, false, mPaint2);
    }

    public void setProgress( float progress ) {

        // Create a new value animator that will use the range 0 to 1
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        // It will take 5000ms for the animator to go from 0 to 1
        animator.setDuration(5000);
        animator.setInterpolator(new LinearInterpolator());

        // Callback that executes on animation steps.
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float value = ((Float) (animation.getAnimatedValue())).floatValue();

                mWidth2 = (int) (mWidth *  value);
                invalidate();
            }
        });
        animator.start();

        mProgressAngle = 0;
        mProgressAngle2 = 0;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                setCircleProgress();
            }
        }, 2000);
    }

    public void setCircleProgress () {

        // Create a new value animator that will use the range 0 to 1
        ValueAnimator animator2 = ValueAnimator.ofFloat(0, 1);

        // It will take 5000ms for the animator to go from 0 to 1
        animator2.setDuration(1800);
        animator2.setInterpolator(new LinearInterpolator());

        // Callback that executes on animation steps.
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float value2 = ((Float) (animation.getAnimatedValue())).floatValue();

                mProgressAngle = (int) (180 * value2 * 2);
                mProgressAngle2 = (int) (-180 * value2 * 2);
                invalidate();
            }
        });
        animator2.start();
    }
}
