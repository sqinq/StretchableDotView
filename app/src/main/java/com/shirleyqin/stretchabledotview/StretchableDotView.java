package com.shirleyqin.stretchabledotview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by shirleyqin on 2017-11-09.
 */

public class StretchableDotView extends View {
    private static final String TAG = StretchableDotView.class.getSimpleName();

    private int backgroundColor = Color.RED;

    public StretchableDotView(Context context) {
        super(context);
        init();
    }

    public StretchableDotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StretchableDotView);
        int indexCount = array.getIndexCount();

        for(int i=0; i<indexCount; i++) {
            int index = array.getIndex(i);
            if (index == R.styleable.StretchableDotView_color) {
                backgroundColor = array.getColor(index, Color.RED);
            }
        }

        array.recycle();
        init();
    }

    public StretchableDotView(Context context, AttributeSet attrs, int stypeAttr) {
        super(context, attrs, stypeAttr);
        init();
    }


    private Context context;

    private Paint paint;
    private Point startPoint = new Point();
    private Point endPoint = new Point();

    private int originRadius;
    private int originWidth;
    private int originHeight;
    private int touchedPointRadius;

    private int maxStretchLen;
    private int minRadius = 15;

    private int currentRadius;

    private Triangle triangle;

    private boolean isTouched;

    ConstraintLayout.LayoutParams originalLp;
    ConstraintLayout.LayoutParams newLp;


    private void init() {
        paint = new Paint();
        paint.setColor(backgroundColor);
        paint.setAntiAlias(true);

        triangle = new Triangle();
    }

    Path path = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);

        int startDotX = originRadius;
        int startDotY = originRadius;

        if(isTouched) {
            startDotX = startPoint.x;
            startDotY = startPoint.y;
            canvas.drawCircle(startDotX, startDotY, originRadius, paint);

            int endDotX = endPoint.x;
            int endDotY = endPoint.y;
            canvas.drawCircle(endDotX, endDotY, touchedPointRadius, paint);

            path.reset();
            Path checkPath = new Path();
            Paint blue = new Paint(Color.BLUE);
            if (dragDis > 0) {
                double cos = (double)triangle.b/(double)triangle.getC();
                double sin = (double)triangle.a/(double) triangle.getC();


                path.moveTo(startDotX - (int)(originRadius * sin),
                            startDotY - (int) (originRadius * cos));
                checkPath.moveTo(startDotX - (int)(originRadius * sin),
                        startDotY - (int) (originRadius * cos));
                checkPath.lineTo(startDotX + (int)(originRadius * sin),
                        startDotY + (int) (originRadius * cos));

                path.lineTo(startDotX + (int)(originRadius * sin),
                            startDotY + (int) (originRadius * cos));

                path.quadTo((startDotX+endDotX)/2, (startDotY+endDotY)/2,
                        endDotX + (int) (touchedPointRadius * sin),
                        endDotY + (int) (touchedPointRadius * cos));

                path.lineTo(endDotX - (int) (touchedPointRadius * sin),
                        endDotY - (int) (touchedPointRadius * cos));

                path.quadTo((startDotX+endDotX)/2, (startDotY+endDotY)/2,
                        startDotX - (int)(originRadius * sin),
                        startDotY - (int) (originRadius * cos));

                canvas.drawPath(path, paint);
               // canvas.drawPath(checkPath, blue);
            }

        } else {
            canvas.drawCircle(startDotX, startDotY, originRadius, paint);
        }

    }

    float downX = Float.MAX_VALUE;
    float downY = Float.MAX_VALUE;

    private int [] locationInWindow;

    boolean firstTime = true;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (firstTime) {
            firstTime = false;
            originHeight = h;
            originWidth = w;
            originRadius = Math.min(w, h) / 2;
            currentRadius = originRadius;
            touchedPointRadius = originRadius;
            maxStretchLen = originHeight*3;

            ViewGroup.LayoutParams lp = this.getLayoutParams();
            Log.d(TAG, Arrays.toString(new int[]{lp.width, lp.height}));
            originalLp = (ConstraintLayout.LayoutParams) lp;

            newLp = new ConstraintLayout.LayoutParams(originalLp);
        }
    }


    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        refreshStartPoint();
        super.setLayoutParams(params);
    }

    private int dragDis;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouched = true;
                dragDis = 0;

                this.setLayoutParams(newLp);
                endPoint.x = startPoint.x;
                endPoint.y = startPoint.y;

                originRadius = currentRadius*3 / 2;
                refreshViewHeight(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                postInvalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                triangle.a = startPoint.y-(int) event.getY();
                triangle.b = (int) event.getX() - startPoint.x;

                int measuredDis = triangle.getC();
                if (measuredDis <= maxStretchLen) {
                    dragDis = measuredDis;
                    endPoint.x = (int) event.getX();
                    endPoint.y = (int) event.getY();
                } else {
                    dragDis = maxStretchLen;
                    endPoint.x = (triangle.b*dragDis/measuredDis)+startPoint.x;
                    endPoint.y = - (triangle.a*dragDis/measuredDis)+startPoint.y;
                }

                refreshOrginRadius();

                postInvalidate();
                break;

            case MotionEvent.ACTION_UP:
                isTouched = false;
                this.setLayoutParams(originalLp);
                originRadius = currentRadius;
                triangle.reset();
                dragDis = 0;
                refreshOrginRadius();
                postInvalidate();
                break;
        }
        return true;
    }

    private void refreshStartPoint() {
        locationInWindow = new int[2];
        this.getLocationInWindow(locationInWindow);
        locationInWindow[1] = locationInWindow[1] - getMeasuredHeightAndState();
        Log.d(TAG+" Start Point", Arrays.toString(new int[]{locationInWindow[0], locationInWindow[1]}));
        startPoint.set(locationInWindow[0]+originRadius, locationInWindow[1]+originRadius);
    }


    private void refreshViewHeight(int w, int h) {
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width = w;
        lp.height = h;
        this.setLayoutParams(lp);
        Log.d("new loc", Arrays.toString(new float[]{getX(), getY()}));
    }

    private void refreshOrginRadius() {
        int radius = (int) (currentRadius*(1 - (double) dragDis/maxStretchLen));
        if (radius <= minRadius)
            originRadius = minRadius;
        else
            originRadius = radius;
    }
}
