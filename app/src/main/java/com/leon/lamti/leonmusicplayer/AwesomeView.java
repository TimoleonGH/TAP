package com.leon.lamti.leonmusicplayer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class AwesomeView extends View {

    private Paint mPaint, mPaint2, mTransparentPaint;
    private RectF oval = new RectF();
    private Canvas mCanvas;
    private int mWidth, mHeight, mHeight2;
    private boolean mCloseViews, mHeader;
    private Context mContext;
    float scale;
    int pixels;

    public AwesomeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        //In versions > 3.0 need to define layer Type
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

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
        mPaint2.setStyle(Paint.Style.FILL);
        //mPaint.setStrokeWidth(10);
        mPaint2.setAntiAlias(true);
        //bodyPaint.setDither(true);
        mPaint2.setColor(Color.GRAY);

        mTransparentPaint = new Paint();
        mTransparentPaint.setStyle(Paint.Style.FILL);
        //mTransparentPaint.setStrokeWidth(10);
        mTransparentPaint.setAntiAlias(true);
        //bodyPaint.setDither(true);
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mCloseViews = false;
        mHeader = false;

        float scale = mContext.getResources().getDisplayMetrics().density;
        int pixels = (int) (60 * scale + 0.5f);
        mHeight = pixels;
        mHeight2 = pixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCanvas = canvas;
        mWidth = getMeasuredWidth();

        /*if ( !mCloseViews ) {

            mHeight = 200;

        } else {

            mHeight = getMeasuredHeight();
        }*/

        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        oval.set( (mWidth / 2) - (mHeight2 / 2), mHeight - (mHeight2 / 2), (mWidth / 2) + (mHeight2 / 2), mHeight + (mHeight2 / 2));
        canvas.drawArc(oval, 180, 180, false, mTransparentPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void rotateView() {

        this.setRotation(180);
    }

    public void closeViews(String name) {

        if ( mCloseViews ) {

            mCloseViews = false;
        } else {
            mCloseViews = true;
        }

        animateView(name);
    }

    ObjectAnimator colorFade;

    private void animateView( final String name ) {

        // Create a new value animator that will use the range 0 to 1
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        // It will take 5000ms for the animator to go from 0 to 1
        animator.setDuration(300);

        if ( !mCloseViews ) {
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            colorFade = ObjectAnimator.ofObject(mPaint, "color", new ArgbEvaluator(), getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary));
        } else {
            animator.setInterpolator(new DecelerateInterpolator());
            colorFade = ObjectAnimator.ofObject(mPaint, "color", new ArgbEvaluator(), getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent));
        }

        /*colorFade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                invalidate();
            }
        });*/
        colorFade.setDuration(300);

        // Callback that executes on animation steps.
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float value = ((Float) (animation.getAnimatedValue())).floatValue();

                float scale = mContext.getResources().getDisplayMetrics().density;
                int pixels = (int) (60 * scale + 0.5f);

                if (mCloseViews) {

                    if ( name.equals("top") ) {

                        mHeight = (int) (pixels + ((getHeight() - pixels) * value));

                    } else {

                        mHeight = (int) (getHeight() * value);
                    }



                } else {

                    if ( name.equals("top") ) {

                        mHeight = (int) (getHeight() - ((getHeight() - pixels) * value));

                    } else {

                        mHeight = (int) (getHeight() - ((getHeight()) * value));
                    }
                }

                invalidate();
            }
        });
        animator.start();
        colorFade.start();
    }

    public void header() {

        mHeader = true;
        //closeViews();
    }

    public void footer() {

        mHeader = false;
        //closeViews();
    }
}
