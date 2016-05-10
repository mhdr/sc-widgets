package com.sccomponents.widgets;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

/**
 * Create a Gauge
 */
public class ScGauge
        extends ScArc
        implements ValueAnimator.AnimatorUpdateListener {

    /**
     * Private attributes
     */

    private boolean mStrokeShow = true;
    private boolean mProgressShow = true;

    private int mProgressMargin = 0;
    private int mProgressColor = Color.BLACK;

    private int mValue = 0;
    private int mAnimationDuration = 0;


    /**
     * Private variables
     */

    private Paint mProgressPaint = null;

    private int mAnimatedValue = 0;
    private ValueAnimator mAnimator = null;


    /**
     * Constructors
     */

    public ScGauge(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    /**
     * Privates methods
     */

    // Get the progress arc stroke size
    private float getProgressSize() {
        return this.getPainter().getStrokeWidth() - this.mProgressMargin * 2;
    }

    // Limit the value within the angles range
    private int limitAngle(int angle) {
        // Find the limits
        int startAngle = this.getStartAngle();
        int endAngle = startAngle + this.getSweepAngle();

        // Sort the limits
        int min = startAngle < endAngle ? startAngle : endAngle;
        int max = startAngle > endAngle ? startAngle : endAngle;

        // Check
        if (angle > max) angle = max;
        if (angle < min) angle = min;

        // Return
        return angle;
    }

    // Init the component
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mStrokeShow = attrArray.getBoolean(R.styleable.ScComponents_scc_stroke_show, true);

        this.mProgressShow = attrArray.getBoolean(R.styleable.ScComponents_scc_progress_show, true);
        this.mProgressMargin = attrArray.getDimensionPixelSize(R.styleable.ScComponents_scc_progress_margin, 0);
        this.mProgressColor = attrArray.getColor(R.styleable.ScComponents_scc_progress_color, Color.BLACK);

        this.mValue = attrArray.getInt(R.styleable.ScComponents_scc_value, this.getStartAngle());
        this.mAnimationDuration = attrArray.getInt(R.styleable.ScComponents_scc_animation_duration, 0);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.mValue = this.limitAngle(this.mValue);
        this.mAnimatedValue = this.mValue;

        //--------------------------------------------------
        // PAINTS

        this.mProgressPaint = new Paint();
        this.mProgressPaint.setColor(this.mProgressColor);
        this.mProgressPaint.setAntiAlias(true);
        this.mProgressPaint.setStrokeWidth(this.getProgressSize());
        this.mProgressPaint.setStyle(Paint.Style.STROKE);
        this.mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        //--------------------------------------------------
        // ANIMATOR

        this.mAnimator = new ValueAnimator();
        this.mAnimator.setDuration(this.mAnimationDuration);
        this.mAnimator.setInterpolator(new DecelerateInterpolator());
        this.mAnimator.addUpdateListener(this);
    }

    // Draw arc
    private void drawArc(Canvas canvas, Paint paint, int margin, int sweepAngle) {
//        // Check for  null value
//        if (this.mDrawArea != null) {
//            // Apply the stroke increment to the area
//            float strokeSize = paint.getStrokeWidth() + margin * 2;
//            float increment = 0;
//
//            RectF fixedArea = new RectF(
//                    this.mDrawArea.left + increment,
//                    this.mDrawArea.top + increment,
//                    this.mDrawArea.right - increment,
//                    this.mDrawArea.bottom - increment
//            );
//
//            // Draw the arc
//            canvas.drawArc(
//                    fixedArea,
//                    this.getStartAngle(),
//                    sweepAngle,
//                    false,
//                    paint
//            );
//        }
    }


    /**
     * Overrides
     */

    // Check all input values
    @Override
    protected void checkValues() {
        // Call the super
        super.checkValues();

        // Fix the values
        this.mValue = this.limitAngle(this.mValue);
        this.mAnimatedValue = this.limitAngle(this.mAnimatedValue);
    }

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the base arc if needed
        if (this.mStrokeShow) {
            this.drawArc(
                    canvas,
                    this.getPainter(),
                    0,
                    this.getSweepAngle()
            );
        }

        // Draw the progress arc if needed
        if (this.mProgressShow) {
            this.drawArc(
                    canvas,
                    this.mProgressPaint,
                    this.mProgressMargin,
                    this.mAnimatedValue
            );
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // Get the current animation value
        int value = (int) animation.getAnimatedValue();

        // Save the current animation value value
        this.mAnimatedValue = this.limitAngle(value);
        this.invalidate();
    }


    /**
     * Instance state
     */

    // Save
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable("PARENT", superState);
        state.putBoolean("mStrokeShow", this.mStrokeShow);
        state.putBoolean("mProgressShow", this.mProgressShow);
        state.putInt("mProgressMargin", this.mProgressMargin);
        state.putInt("mProgressColor", this.mProgressColor);
        state.putInt("mValue", this.mValue);
        state.putInt("mAnimationDuration", this.mAnimationDuration);
        state.putInt("mAnimatedValue", this.mAnimatedValue);

        return state;
    }

    // Restore
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        this.mStrokeShow = savedState.getBoolean("mStrokeShow");
        this.mProgressShow = savedState.getBoolean("mProgressShow");
        this.mProgressMargin = savedState.getInt("mProgressMargin");
        this.mProgressColor = savedState.getInt("mProgressColor");
        this.mValue = savedState.getInt("mValue");
        this.mAnimationDuration = savedState.getInt("mAnimationDuration");
        this.mAnimatedValue = savedState.getInt("mAnimatedValue");
    }


    /**
     * Public methods
     */

    // Set the animation interpolation
    @SuppressWarnings("unused")
    public void setAnimationInterpolator(TimeInterpolator interpolator) {
        // If null reset the interpolator
        if (interpolator == null) {
            this.mAnimator.setInterpolator(new DecelerateInterpolator());
        }
        // Apply the new interpolator
        else {
            this.mAnimator.setInterpolator(interpolator);
        }
    }


    /**
     * Public properties
     */

    // Show or hide the base arc
    @SuppressWarnings("unused")
    public boolean getStrokeShow() {
        return this.mStrokeShow;
    }

    @SuppressWarnings("unused")
    public void setStrokeShow(boolean value) {
        this.mStrokeShow = value;
        this.invalidate();
    }

    // Show or hide the progress arc
    @SuppressWarnings("unused")
    public boolean getProgressShow() {
        return this.mProgressShow;
    }

    @SuppressWarnings("unused")
    public void setProgressShow(boolean value) {
        this.mProgressShow = value;
        this.invalidate();
    }

    // Stroke size
    @SuppressWarnings("unused")
    public int getProgressMargin() {
        return this.mProgressMargin;
    }

    @SuppressWarnings("unused")
    public void setProgressMargin(int value) {
        this.mProgressMargin = value;
        this.invalidate();
    }

    // Progress stroke color
    @SuppressWarnings("unused")
    public int getProgressColor() {
        return this.mProgressColor;
    }

    @SuppressWarnings("unused")
    public void setProgressColor(int color) {
        this.mProgressColor = color;
        this.mProgressPaint.setColor(this.mProgressColor);
        this.invalidate();
    }

    // Value
    @SuppressWarnings("unused")
    public int getValue() {
        return this.mValue;
    }

    @SuppressWarnings("unused")
    public void setValue(int value) {
        // Hold and check
        this.mValue = value;
        this.checkValues();

        // Animate
        this.mAnimator.setIntValues(this.mAnimatedValue, value);
        this.mAnimator.start();
    }

    // The duration in milliseconds of the animation
    @SuppressWarnings("unused")
    public int getAnimationDuration() {
        return this.mAnimationDuration;
    }

    @SuppressWarnings("unused")
    public void setAnimationDuration(int value) {
        if (value < 0) value = 0;
        this.mAnimationDuration = value;
    }

}
