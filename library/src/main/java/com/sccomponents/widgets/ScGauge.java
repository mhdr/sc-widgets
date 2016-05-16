package com.sccomponents.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

/**
 * Create a Gauge
 */
public class ScGauge
        extends View
        implements ValueAnimator.AnimatorUpdateListener, ScNotchs.OnDrawListener {

    /**
     * Private variables
     */

    private ScArc mArcBase = null;
    private ScArc mArcProgress = null;
    private ScNotchs mNotchs = null;

    private ValueAnimator mAnimator = null;

    private OnDrawListener mOnDrawListener = null;
    private OnCustomPaddingListener mOnCustomPadding = null;


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
     * Privates utils
     */

    // Get the display metric.
    // This method is used for screen measure conversion.
    private DisplayMetrics getDisplayMetrics(Context context) {
        // Get the window manager from the window service
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // Create the variable holder and inject the values
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        // Return
        return displayMetrics;
    }

    // Convert Dip to Pixel
    private float dpToPixel(Context context, float dp) {
        // Get the display metrics
        DisplayMetrics metrics = this.getDisplayMetrics(context);
        // Calc the conversion by the screen density
        return dp * metrics.density;
    }

    // Limit number within a range
    private float valueRangeLimit(float value, float startValue, float endValue) {
        // If is over the limit return the normalized value
        if (value < Math.min(startValue, endValue)) return Math.min(startValue, endValue);
        if (value > Math.max(startValue, endValue)) return Math.max(startValue, endValue);
        // Else return the original value
        return value;
    }


    /**
     * Privates methods
     */

    // Init the component.
    // Retrieve all attributes with the default values if needed and create the internal using
    // objects.
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        float angleStart = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_start, 0.0f);
        float angleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, 360.0f);

        float strokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dpToPixel(context, 3.0f));
        int strokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, Color.BLACK);

        float progressSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_progress_size, this.dpToPixel(context, 1.0f));
        int progressColor = attrArray.getColor(
                R.styleable.ScComponents_scc_progress_color, Color.GRAY);

        float value = attrArray.getFloat(
                R.styleable.ScComponents_scc_value, angleStart);

        int notchsCount = attrArray.getInt(
                R.styleable.ScComponents_scc_notchs, 0);
        float notchsLength = attrArray.getDimension(
                R.styleable.ScComponents_scc_notchs_length, this.dpToPixel(context, 5.0f));

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // ARCS

        // Base arc
        this.mArcBase = new ScArc(context);
        this.mArcBase.setAngleStart(angleStart);
        this.mArcBase.setAngleSweep(angleSweep);
        this.mArcBase.setStrokeSize(strokeSize);
        this.mArcBase.setStrokeColor(strokeColor);

        // Notchs
        this.mNotchs = new ScNotchs(context);
        this.mNotchs.setAngleStart(angleStart);
        this.mNotchs.setAngleSweep(angleSweep);
        this.mNotchs.setStrokeSize(strokeSize);
        this.mNotchs.setStrokeColor(strokeColor);
        this.mNotchs.setNotchs(notchsCount);
        this.mNotchs.setNotchsLength(notchsLength);

        // Progress arc
        this.mArcProgress = new ScArc(context);
        this.mArcProgress.setAngleStart(angleStart);
        this.mArcProgress.setAngleSweep(angleSweep);
        this.mArcProgress.setAngleDraw(value);
        this.mArcProgress.setStrokeSize(progressSize);
        this.mArcProgress.setStrokeColor(progressColor);

        //--------------------------------------------------
        // ANIMATOR

        this.mAnimator = new ValueAnimator();
        this.mAnimator.setDuration(500);
        this.mAnimator.setInterpolator(new DecelerateInterpolator());
        this.mAnimator.addUpdateListener(this);
    }

    // Find the max given a series of values
    private float findMax(float... values) {
        // Check for null values
        if (values == null || values.length == 0) return 0;
        // Save the first value inside the max holder
        float max = values[0];

        // Cycle all other values
        for (int index = 1; index < values.length; index++) {
            // Find the max
            if (max < values[index]) max = values[index];
        }
        // Return
        return max;
    }

    // Fix the arcs padding
    private void fixArcsPadding() {
        // Define the padding holder
        Rect baseArc = new Rect();
        Rect progressArc = new Rect();
        Rect notchs = new Rect();

        // If have instantiate the customer padding listener the padding will be decided by
        // the final user inside the calling function
        if (this.mOnCustomPadding != null) {
            // Call the method
            this.mOnCustomPadding.onCustomPadding(baseArc, progressArc, notchs);

        } else {
            // Find the middle of max stroke sizes
            float maxSize = this.findMax(
                    this.mArcBase.getStrokeSize(),
                    this.mArcBase.getStrokeSize(),
                    this.mNotchs.getNotchsLength()
            );

            // Calc the padding by the case for both arcs
            int basePadding = Math.round((maxSize - this.mArcBase.getStrokeSize()) / 2);
            int progressPadding = Math.round((maxSize - this.mArcProgress.getStrokeSize()) / 2);
            int notchsPadding = Math.round((maxSize - this.mNotchs.getNotchsLength()) / 2);

            // Fill the padding holder variables
            baseArc = new Rect(
                    this.getPaddingLeft() + basePadding, this.getPaddingTop() + basePadding,
                    this.getPaddingRight() + basePadding, this.getPaddingBottom() + basePadding
            );
            progressArc = new Rect(
                    this.getPaddingLeft() + progressPadding, this.getPaddingTop() + progressPadding,
                    this.getPaddingRight() + progressPadding, this.getPaddingBottom() + progressPadding
            );
            notchs = new Rect(
                    this.getPaddingLeft() + notchsPadding, this.getPaddingTop() + notchsPadding,
                    this.getPaddingRight() + notchsPadding, this.getPaddingBottom() + notchsPadding
            );
        }

        // Apply the padding to the related arc
        this.mArcBase.setPadding(
                baseArc.left, baseArc.top, baseArc.right, baseArc.bottom
        );
        this.mArcProgress.setPadding(
                progressArc.left, progressArc.top, progressArc.right, progressArc.bottom
        );
        this.mNotchs.setPadding(
                notchs.left, notchs.top, notchs.right, notchs.bottom
        );
    }

    // Call the on before draw if needed
    private void callOnBeforeDraw() {
        // Check the listener
        if (this.mOnDrawListener != null) {
            // Call the method
            this.mOnDrawListener.onBeforeDraw(
                    this.mArcBase.getPainter(),
                    this.mArcProgress.getPainter(),
                    this.mNotchs.getPainter()
            );
        }
    }


    /**
     * Overrides
     */

    // On measure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Fix arcs the padding
        this.fixArcsPadding();

        // Cycle all arcs and do the common operations like apply the parent layout and measure
        // the arc.
        // It is important to call measure for all arcs before draw in the onDraw method.
        for (ScArc arc : this.getArcs()) {
            // Apply the parent layout and measure the arc
            arc.setLayoutParams(this.getLayoutParams());
            arc.measure(widthMeasureSpec, heightMeasureSpec);
        }

        // Layout wrapping
        boolean hWrap = this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean vWrap = this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        // If no have wrapping just call the super class method and finish the procedure
        if (!hWrap && !vWrap) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        // Else determine the max dimensions of the gauge component measuring all arcs
        else {
            // Create the max dimensions holder
            int maxWidth =
                    hWrap ? 0 : View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
            int maxHeight =
                    vWrap ? 0 : View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

            // Cycle all arcs and check for update the component dimensions
            for (ScArc arc : this.getArcs()) {
                // Horizontal wrap
                if (hWrap && maxWidth < arc.getMeasuredWidth())
                    maxWidth = arc.getMeasuredWidth();
                // Vertical wrap
                if (vWrap && maxHeight < arc.getMeasuredHeight())
                    maxHeight = arc.getMeasuredHeight();
            }

            // Set the dimension
            this.setMeasuredDimension(maxWidth, maxHeight);
        }
    }

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Call the events if needed
        this.callOnBeforeDraw();

        // Draw
        this.mArcBase.draw(canvas);
        this.mNotchs.draw(canvas);
        this.mArcProgress.draw(canvas);
    }

    // On animation update
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // Get the current value
        float value = (float) animation.getAnimatedValue();
        // Set and refresh
        this.mArcProgress.setAngleDraw(value);
        this.invalidate();
    }

    // On before to draw the single notch for each notchs
    @Override
    public float onBeforeNotch(Paint painter, float angle, int count) {
        // If have a listener linked
        if (this.mOnDrawListener != null) {
            // Forward the event
            return this.mOnDrawListener.onBeforeNotch(painter, angle, count);
        } else {
            // Else return the standard notchs length
            return this.mNotchs.getNotchsLength();
        }
    }


    /**
     * Instance state
     */

    // Save
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putParcelable("mArcBase", this.mArcBase.onSaveInstanceState());
        state.putParcelable("mArcProgress", this.mArcProgress.onSaveInstanceState());
        state.putParcelable("mNotchs", this.mNotchs.onSaveInstanceState());

        // Return the new state
        return state;
    }

    // Restore
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mArcBase.onRestoreInstanceState(savedState.getParcelable("mArcBase"));
        this.mArcProgress.onRestoreInstanceState(savedState.getParcelable("mArcProgress"));
        this.mNotchs.onRestoreInstanceState(savedState.getParcelable("mNotchs"));
    }


    /**
     * Public methods
     */

    // Get the arcs.
    // This method is implemented only for have an advanced management of this component.
    // A wrong use of arcs can generate a malfunction of this component.
    @SuppressWarnings("unused")
    public ScArc[] getArcs() {
        return new ScArc[]{this.mArcBase, this.mArcProgress, this.mNotchs};
    }

    // Set stroke cap of painter for all arcs and notchs.
    // Default value is BUTT from the ScArc settings.
    @SuppressWarnings("unused")
    public void setStrokesCap(Paint.Cap cap) {
        // Cycle all arcs and set the stroke definition by painter
        for (ScArc arc : this.getArcs()) {
            arc.getPainter().setStrokeCap(cap);
        }
        // Refresh
        this.invalidate();
    }

    // Set the start angle on every arcs
    @SuppressWarnings("unused")
    public void setAngleStart(int value) {
        // Cycle all arcs and set the start angle
        for (ScArc arc : this.getArcs()) {
            arc.setAngleStart(value);
        }
        // Refresh
        this.requestLayout();
    }

    // Set the sweep angle on every arcs
    @SuppressWarnings("unused")
    public void setAngleSweep(int value) {
        // Cycle all arcs and set the start angle
        for (ScArc arc : this.getArcs()) {
            arc.setAngleSweep(value);
        }
        // Refresh
        this.requestLayout();
    }

    // Set the arcs visibility.
    // For a correct measure of the component it is better not use GONE.
    @SuppressWarnings("unused")
    public void show(boolean baseArc, boolean progressArc) {
        // Apply the visibility status
        this.mArcBase.setVisibility(baseArc ? View.VISIBLE : View.INVISIBLE);
        this.mArcProgress.setVisibility(progressArc ? View.VISIBLE : View.INVISIBLE);
        // Refresh
        this.requestLayout();
    }

    // Translate the angle in a value within the passed range of values.
    @SuppressWarnings("unused")
    public float translateAngleToValue(float angle, float startRange, float endRange) {
        // Limit the value within the range
        angle = this.valueRangeLimit(angle, 0.0f, this.mArcProgress.getAngleSweep());
        // Check for the division domain
        if (this.mArcProgress.getAngleSweep() != 0.0f) {
            return (angle / this.mArcProgress.getAngleSweep()) * (endRange - startRange);
        } else {
            return 0.0f;
        }
    }


    /**
     * Public properties
     */

    // Stroke size
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mArcBase.getStrokeSize();
    }

    @SuppressWarnings("unused")
    public void setStrokeSize(float value) {
        // Check if value is changed
        if (this.getStrokeSize() != value) {
            // Store the new value
            this.mArcBase.setStrokeSize(value);
            // Check if the notchs painter have the same value than the system maintain
            // the values linked.
            if (this.mNotchs.getStrokeSize() == value) {
                this.mNotchs.setStrokeSize(value);
            }
            // Refresh the component
            this.requestLayout();
        }
    }

    // Stroke color
    @SuppressWarnings("unused")
    public int getStrokeColor() {
        return this.mArcBase.getStrokeColor();
    }

    @SuppressWarnings("unused")
    public void setStrokeColor(int value) {
        // Check if value is changed
        if (this.getStrokeColor() != value) {
            // Store the new value
            this.mArcBase.setStrokeColor(value);
            // Check if the notchs painter have the same value than the system maintain
            // the values linked.
            if (this.mNotchs.getStrokeColor() == value) {
                this.mNotchs.setStrokeColor(value);
            }
            // Refresh the component
            this.invalidate();
        }
    }

    // Progress size
    @SuppressWarnings("unused")
    public float getProgressSize() {
        return this.mArcProgress.getStrokeSize();
    }

    @SuppressWarnings("unused")
    public void setProgressSize(float value) {
        // Check if value is changed
        if (this.getProgressSize() != value) {
            // Store the new value and refresh the component
            this.mArcProgress.setStrokeSize(value);
            this.requestLayout();
        }
    }

    // Progress color
    @SuppressWarnings("unused")
    public int getProgressColor() {
        return this.mArcProgress.getStrokeColor();
    }

    @SuppressWarnings("unused")
    public void setProgressColor(int value) {
        // Check if value is changed
        if (this.getProgressColor() != value) {
            // Store the new value and refresh the component
            this.mArcProgress.setStrokeColor(value);
            this.invalidate();
        }
    }

    // Progress value in degrees
    @SuppressWarnings("unused")
    public float getValue() {
        return this.mArcProgress.getAngleDraw();
    }

    @SuppressWarnings("unused")
    public void setValue(float degrees) {
        // Hold the sweep angle
        float sweep = this.mArcProgress.getAngleSweep();

        // Set and start animation
        this.mAnimator.setFloatValues(
                this.mArcProgress.getAngleDraw(),
                this.valueRangeLimit(degrees, 0, sweep)
        );
        this.mAnimator.start();
    }

    // Progress value but based on a values range.
    // Translate the reference value to the angle in degrees and call the base methods.
    @SuppressWarnings("unused")
    public float getValue(float startRange, float endRange) {
        // Return the translated value
        return this.translateAngleToValue(
                this.mArcProgress.getAngleDraw(),
                startRange,
                endRange
        );
    }

    @SuppressWarnings("unused")
    public void setValue(float value, float startRange, float endRange) {
        // Limit the value within the range
        value = this.valueRangeLimit(value, startRange, endRange);
        // Check for the division domain
        if (endRange == startRange) {
            value = 0;

        } else {
            // Convert the value in the relative angle respect the arc length
            value = ((value - startRange) / (endRange - startRange)) *
                    this.mArcProgress.getAngleSweep();
        }
        // Call the base method
        this.setValue(value);
    }

    // Notchs count
    @SuppressWarnings("unused")
    public int getNotchs() {
        return this.mNotchs.getNotchs();
    }

    @SuppressWarnings("unused")
    public void setNotchs(int value) {
        // Check if value is changed
        if (this.mNotchs.getNotchs() != value) {
            // Store the new value and refresh the component
            this.mNotchs.setNotchsLength( value);
            this.requestLayout();
        }
    }

    // Progress size
    @SuppressWarnings("unused")
    public float getNotchsLength() {
        return this.mNotchs.getNotchsLength();
    }

    @SuppressWarnings("unused")
    public void setNotchsLength(float value) {
        // Check if value is changed
        if (this.mNotchs.getNotchsLength() != value) {
            // Store the new value and refresh the component
            this.mNotchs.setNotchsLength(value);
            this.requestLayout();
        }
    }


    /**
     * Public listener and interface
     */

    // Before draw
    @SuppressWarnings("unused")
    public interface OnDrawListener {
        void onBeforeDraw(Paint baseArc, Paint progressArc, Paint notchs);
        float onBeforeNotch(Paint painter, float angle, int count);
    }

    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

    // Custom padding
    @SuppressWarnings("unused")
    public interface OnCustomPaddingListener {
        void onCustomPadding(Rect baseArc, Rect progressArc, Rect notchs);
    }

    @SuppressWarnings("unused")
    public void setCustomPaddingListener(OnCustomPaddingListener listener) {
        this.mOnCustomPadding = listener;
    }

}
