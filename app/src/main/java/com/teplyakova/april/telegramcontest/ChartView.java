package com.teplyakova.april.telegramcontest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.teplyakova.april.telegramcontest.Drawing.ChartDrawer;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class ChartView extends View implements ValueAnimator.AnimatorUpdateListener{
    private final float DRAWING_AREA_OFFSET_X_PX;
    private final float DRAWING_AREA_OFFSET_Y_PX;
    private final float SCROLL_DRAWING_AREA_HEIGHT_PX;

    private ChartDrawer mDrawer;

    public float normSliderPosLeft   = 0.8f;
    public float normSliderPosRight  = 1;
    public int   chosenPointPosition = -1;

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        DRAWING_AREA_OFFSET_X_PX = MathUtils.dpToPixels(8, context);
        DRAWING_AREA_OFFSET_Y_PX = MathUtils.dpToPixels(16, context);
        SCROLL_DRAWING_AREA_HEIGHT_PX = MathUtils.dpToPixels(50, context);
    }

    public void init(ChartDrawer drawer) {
        mDrawer = drawer;
        mDrawer.setAnimatorUpdateListener(this);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int viewWidth  = getWidth();
        int viewHeight = getHeight();

        mDrawer.setViewDimens(viewWidth, viewHeight, DRAWING_AREA_OFFSET_X_PX, DRAWING_AREA_OFFSET_Y_PX, SCROLL_DRAWING_AREA_HEIGHT_PX);
        mDrawer.setSliderPositions(normSliderPosLeft, normSliderPosRight);
        mDrawer.setChosenPointPosition(chosenPointPosition);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawer.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if (mDrawer.handleTouchEvent(event, x, y)) {
                    invalidate();
                    return true;
                }
        }
        return false;
    }

    public void setLines(LineData[] lines) {
        mDrawer.setLines(lines);
        invalidate();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable outState = super.onSaveInstanceState();
        SavedState ss = new SavedState(outState);
        if (mDrawer != null) {
            float[] positions = mDrawer.getSliderPositions();
            ss.normPos1 = positions[0];
            ss.normPos2 = positions[1];
            ss.chosenPoint = mDrawer.getChosenPointPosition();
        }
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        normSliderPosLeft = ss.normPos1;
        normSliderPosRight = ss.normPos2;
        chosenPointPosition = ss.chosenPoint;
    }

    private static class SavedState extends BaseSavedState {
        float normPos1;
        float normPos2;
        int   chosenPoint;

        private SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            normPos1 = in.readFloat();
            normPos2 = in.readFloat();
            chosenPoint = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(normPos1);
            out.writeFloat(normPos2);
            out.writeInt(chosenPoint);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
