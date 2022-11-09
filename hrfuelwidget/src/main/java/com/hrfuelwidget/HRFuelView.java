package com.hrfuelwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


public class HRFuelView extends View {

    private final static int TICK_STROKE_WIDTH_PX = 8;

    private final float mDensity;
    private float mLevel;

    private final float mNumDegreesToShow;
    private final float mDegreesStart;
    private final float mDegreesEnd;
    private Point mNeedlePivot;

    private final Paint mPaint;
    private final Path mNeedlePath;
    private final int mNeedleColor;
    private int mLineColor;
    private final int mTextColor;
    private final int mLabelColor;
    private final int mEmptyColor;
    private final String mLabel;

    private final float mShortTickLength;
    private final float mLongTickLength;
    private float mTickStart;

    public HRFuelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HRFuelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.HRFuelView, defStyleAttr, 0);

        mLevel = attributes.getFloat(R.styleable.HRFuelView_level, 1);
        mNeedleColor = attributes.getColor(R.styleable.HRFuelView_needleColor, Color.BLUE);
        mTextColor = attributes.getColor(R.styleable.HRFuelView_textColor, Color.TRANSPARENT);
        mLineColor = attributes.getColor(R.styleable.HRFuelView_lineColor, Color.TRANSPARENT);
        mLabelColor = attributes.getColor(R.styleable.HRFuelView_labelColor, Color.TRANSPARENT);
        mEmptyColor = attributes.getColor(R.styleable.HRFuelView_emptyColor, Color.TRANSPARENT);
        mLabel = attributes.getString(R.styleable.HRFuelView_label);

        attributes.recycle();

        mNeedlePath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDensity = getResources().getDisplayMetrics().density;

        mNumDegreesToShow = 127;
        mDegreesStart = -(mNumDegreesToShow / 2);
        mDegreesEnd = mDegreesStart + mNumDegreesToShow;

        mShortTickLength = 10 * mDensity;
        mLongTickLength = 25 * mDensity;

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Force to be a square.
        int widthPixels = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightPixels = View.MeasureSpec.getSize(heightMeasureSpec);
        int minLength = Math.min(widthPixels, heightPixels);
        setMeasuredDimension(minLength, minLength);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mNeedlePivot == null) {
            int height = getHeight();
            mNeedlePivot = new Point((height / 2), (height / 4) * 3);
            mTickStart = (float) (height / 2);

            mNeedlePath.reset();
            float needleBottom = mNeedlePivot.y + (10 * mDensity);
            float needleTop = mNeedlePivot.y - (115 * mDensity);
            float topWidth = 4 * mDensity;
            float bottomWidth = 9 * mDensity;
            mNeedlePath.moveTo(mNeedlePivot.x - (bottomWidth / 2), needleBottom);
            mNeedlePath.lineTo(mNeedlePivot.x - (topWidth / 2), needleTop);
            mNeedlePath.lineTo(mNeedlePivot.x + (topWidth / 2), needleTop);
            mNeedlePath.lineTo(mNeedlePivot.x + (bottomWidth / 2), needleBottom);
            mNeedlePath.close();
            mNeedlePath.lineTo(mNeedlePivot.x - (bottomWidth / 2), needleBottom);
            mNeedlePath.addCircle(mNeedlePivot.x, mNeedlePivot.y, (10 * mDensity), Path.Direction.CW);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGrid(canvas, mNeedlePivot);
        drawGridLabels(canvas, mNeedlePivot);
        drawNeedle(canvas, mNeedlePivot);

        if (!TextUtils.isEmpty(mLabel)) {
            drawLabel(canvas);
        }
    }

    public void setFuelLevel(float level) {
        level = constrain(level, 0, 1f);

        if (level != this.mLevel) {
            this.mLevel = level;
            invalidate();
        }
    }

    public float getFuelLevel() {
        return mLevel;
    }

    protected void drawGrid(Canvas canvas, Point pivot) {
        float longTickIncrement = mNumDegreesToShow / 4f;
        float tickIncrement = longTickIncrement / 2f;

        float tickStartX = pivot.x;
        float tickStartY = pivot.y - mTickStart;
        float tickStopX = pivot.x;
        float tickStopY = tickStartY - mLongTickLength;
        float shortTickStopY = tickStartY - mShortTickLength;

        mPaint.setColor(mEmptyColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(TICK_STROKE_WIDTH_PX * mDensity);

        canvas.save();
        canvas.rotate(mDegreesStart, pivot.x, pivot.y);
        boolean drawLongTick = true;
        for (int i = 0; i < 9; i++) {

            canvas.drawLine(tickStartX, tickStartY, tickStopX,
                    drawLongTick ? tickStopY : shortTickStopY, mPaint);

            canvas.rotate(tickIncrement, pivot.x, pivot.y);
            drawLongTick = !drawLongTick;

        }
        canvas.restore();
    }

    protected void drawGridLabels(Canvas canvas, Point pivot) {
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(10 * mDensity);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        String oneHalf = getResources().getString(R.string.half);
        float textWidth = mPaint.measureText(oneHalf);
        float textX = pivot.x - (textWidth / 2);
        float textY = pivot.y - mTickStart - mLongTickLength - mShortTickLength;
        canvas.drawText(oneHalf, textX, textY, mPaint);

        canvas.save();
        canvas.translate(pivot.x, pivot.y);
        float radius = pivot.y - (pivot.y - mTickStart - mLongTickLength - mShortTickLength);

        String empty = getResources().getString(R.string.empty);
        textWidth = mPaint.measureText(empty);
        double radians = Math.toRadians(mDegreesStart - 90);
        textX = (float) (radius * Math.cos(radians)) - (textWidth/2);
        textY = (float) (radius * Math.sin(radians));
        canvas.drawText(empty, textX, textY, mPaint);

        String full = getResources().getString(R.string.full);
        textWidth = mPaint.measureText(full);
        radians = Math.toRadians(mDegreesEnd - 90);
        textX = (float) (radius * Math.cos(radians)) - (textWidth/2);
        textY = (float) (radius * Math.sin(radians));
        canvas.drawText(full, textX, textY, mPaint);

        canvas.restore();
    }

    protected void drawNeedle(Canvas canvas, Point pivot) {
        mPaint.setColor(mNeedleColor);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.save();
        canvas.rotate(mDegreesStart, pivot.x, pivot.y);
        float degreesToAdd = constrain(Math.round(mNumDegreesToShow * mLevel), 0, mNumDegreesToShow);
        canvas.rotate(degreesToAdd, pivot.x, pivot.y);
        canvas.drawPath(mNeedlePath, mPaint);
        canvas.restore();
    }

    protected void drawLabel(Canvas canvas) {
        mPaint.setColor(mLabelColor);
        mPaint.setTextSize(mDensity * 12);
        float textWidth = mPaint.measureText(mLabel);
        canvas.drawText(mLabel, (float) (getWidth()/2) - (textWidth/2), (float) getHeight()/2, mPaint);
    }

    protected float constrain(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
