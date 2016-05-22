package com.sccomponents.widgets;

import android.animation.Animator;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * Create a Gauge
 */
public class ScGauge
        extends ScWidget
        implements ValueAnimator.AnimatorUpdateListener, ScNotchs.OnDrawListener {

    /**
     * Constants
     */

    public static final float DEFAULT_ANGLE_START = 0.0f;
    public static final float DEFAULT_ANGLE_SWEEP = 360.0f;

    public static final float DEFAULT_STROKE_SIZE = 3.0f;
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;

    public static final float DEFAULT_PROGRESS_SIZE = 1.0f;
    public static final int DEFAULT_PROGRESS_COLOR = Color.GRAY;


    /**
     * Private attributes
     */

    private float mAngleStart;
    private float mAngleSweep;
    private float mAngleDraw;

    private float mStrokeSize;
    private int mStrokeColor;

    private float mProgressSize;
    private int mProgressColor;

    private float mNotchsSize;
    private int mNotchsColor;
    private int mNotchsCount;
    private float mNotchsLength;
    private boolean mSnapToNotchs;


    /**
     * Private variables
     */

    private ScArc mArcBase;
    private ScArc mArcProgress;
    private ScArc mArcNotchs;

    private ValueAnimator mAnimator;

    private OnDrawListener mOnDrawListener;
    private OnCustomPaddingListener mOnCustomPaddingListener;
    private OnEventListener mOnEventListener;


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

    // Initialize a ScArc object with the defined settings of components
    private void arcObjectSetter(ScArc arc, boolean isProgress) {
        // Fill the settings
        arc.setAngleStart(this.mAngleStart);
        arc.setAngleSweep(this.mAngleSweep);

        // If progress set also the draw angle
        if (isProgress) {
            arc.setAngleDraw(this.mAngleDraw);
        }

        // Check if notchs instance
        if (arc instanceof ScNotchs) {
            // Cast to notchs
            ScNotchs notchs = (ScNotchs) arc;

            // Set the particular notchs properties
            notchs.setStrokeSize(this.mNotchsSize);
            notchs.setStrokeColor(this.mNotchsColor);
            notchs.setNotchs(this.mNotchsCount);
            notchs.setNotchsLength(this.mNotchsLength);
            notchs.setOnDrawListener(this);

        } else {
            // The arc size and color
            arc.setStrokeSize(isProgress ? this.mProgressSize : this.mStrokeSize);
            arc.setStrokeColor(isProgress ? this.mProgressColor : this.mStrokeColor);
        }
    }

    // Round the degree angle to the near notch value
    private float snapToNotchs(float degrees) {
        // Calc the delta angle and the middle stroke
        float deltaAngle = this.mAngleSweep / this.mNotchsCount;
        // Round at notchs value
        return Math.round(degrees / deltaAngle) * deltaAngle;
    }

    // Init the component.
    // Retrieve all attributes with the default values if needed and create the internal using
    // objects.
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mAngleStart = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_start, ScGauge.DEFAULT_ANGLE_START);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, ScGauge.DEFAULT_ANGLE_SWEEP);

        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dipToPixel(ScGauge.DEFAULT_STROKE_SIZE));
        this.mStrokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, ScGauge.DEFAULT_STROKE_COLOR);

        this.mProgressSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_progress_size, this.dipToPixel(ScGauge.DEFAULT_PROGRESS_SIZE));
        this.mProgressColor = attrArray.getColor(
                R.styleable.ScComponents_scc_progress_color, ScGauge.DEFAULT_PROGRESS_COLOR);

        this.mNotchsSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_notchs_size, this.dipToPixel(ScGauge.DEFAULT_STROKE_SIZE));
        this.mNotchsColor = attrArray.getColor(
                R.styleable.ScComponents_scc_notchs_color, ScGauge.DEFAULT_STROKE_COLOR);
        this.mNotchsCount = attrArray.getInt(
                R.styleable.ScComponents_scc_notchs, 0);
        this.mNotchsLength = attrArray.getDimension(
                R.styleable.ScComponents_scc_notchs_length, this.mStrokeSize * 2);
        this.mSnapToNotchs = attrArray.getBoolean(
                R.styleable.ScComponents_scc_snap_to_notchs, false);

        this.mAngleDraw = attrArray.getFloat(
                R.styleable.ScComponents_scc_value, 0);


        // Recycle
        attrArray.recycle();

        // INTERNAL
        //--------------------------------------------------

        // Check for snap to notchs the new degrees value
        if (this.mSnapToNotchs) {
            // Get the current value and round at the closed notchs value
            this.mAngleDraw = this.snapToNotchs(this.mAngleDraw);
        }

        //--------------------------------------------------
        // ARCS

        // Base arc
        this.mArcBase = new ScArc(context);
        this.arcObjectSetter(this.mArcBase, false);

        // Notchs
        this.mArcNotchs = new ScNotchs(context);
        this.arcObjectSetter(this.mArcNotchs, false);

        // Progress arc.
        // The last one is ALWAYS the progress one.
        this.mArcProgress = new ScArc(context);
        this.arcObjectSetter(this.mArcProgress, true);

        //--------------------------------------------------
        // ANIMATOR

        this.mAnimator = new ValueAnimator();
        this.mAnimator.setDuration(0);
        this.mAnimator.setInterpolator(new DecelerateInterpolator());
        this.mAnimator.addUpdateListener(this);
    }

    // Get the size in relation at the type
    private float getStrokeSize(ScArc object) {
        return object instanceof ScNotchs ?
                ((ScNotchs) object).getNotchsLength() : object.getStrokeSize();
    }

    // Find the maximum stroke size.
    // This method is protected because will be used in the inherited class for reposition
    // the arcs in the space seen this methods is used inside the method to find the components
    // padding.
    protected float findMaxStrokeSize() {
        // Consider all the arcs
        return ScGauge.findMaxValue(
                this.getStrokeSize(this.mArcBase),
                this.getStrokeSize(this.mArcBase),
                this.getStrokeSize(this.mArcNotchs)
        );
    }

    // Fix the arcs padding.
    // This method setting padding automatically of all component inside the gauge centering the
    // the stroke one above the other.
    // Note that if have a custom padding listener linked the procedure will be bypassed and
    // the user should setting. If not the padding rectangle will be empty for all components.
    private void fixArcsPadding() {
        // Define the padding holder
        Rect baseArc = new Rect();
        Rect progressArc = new Rect();
        Rect notchs = new Rect();

        // If have instantiate the customer padding listener the padding will be decided by
        // the final user inside the calling function
        if (this.mOnCustomPaddingListener != null) {
            // Call the method
            this.mOnCustomPaddingListener.onCustomPadding(baseArc, progressArc, notchs);

        } else {
            // Find the middle of max stroke sizes
            float maxSize = this.findMaxStrokeSize();

            // Calc the padding by the case for both arcs
            int basePadding = Math.round((maxSize - this.getStrokeSize(this.mArcBase)) / 2);
            int progressPadding = Math.round((maxSize - this.getStrokeSize(this.mArcProgress)) / 2);
            int notchsPadding = Math.round((maxSize - this.getStrokeSize(this.mArcNotchs)) / 2);

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
        this.mArcNotchs.setPadding(
                notchs.left, notchs.top, notchs.right, notchs.bottom
        );
    }

    // Get the arcs that compose this component.
    private ScArc[] getArcs() {
        return new ScArc[]{this.mArcBase, this.mArcNotchs, this.mArcProgress};
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
        // Check the listener
        if (this.mOnDrawListener != null) {
            // Call the method
            this.mOnDrawListener.onBeforeDraw(
                    this.mArcBase.getPainter(),
                    this.mArcNotchs.getPainter(),
                    this.mArcProgress.getPainter()
            );
        }

        // Cycle all arcs for draw it
        for (ScArc arc : this.getArcs()) {
            // Only if visible
            if (arc.getVisibility() == View.VISIBLE) {
                // Draw
                arc.draw(canvas);
            }
        }
    }

    // On animation update
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // Get the current angle value
        float degrees = (float) animation.getAnimatedValue();
        // Set and refresh
        this.mArcProgress.setAngleDraw(degrees);
        this.invalidate();

        // Manage the listener
        if (this.mOnEventListener != null) {
            this.mOnEventListener.onValueChange(degrees);
        }
    }

    // On before to draw the single notch for each notchs
    @Override
    public void onDrawNotch(ScNotchs.NotchInfo info) {
        // If have a listener linked
        if (this.mOnDrawListener != null) {
            // Forward the event
            this.mOnDrawListener.onDrawNotch(info);
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
        state.putFloat("mAngleStart", this.mAngleStart);
        state.putFloat("mAngleSweep", this.mAngleSweep);
        state.putFloat("mAngleDraw", this.mAngleDraw);
        state.putFloat("mStrokeSize", this.mStrokeSize);
        state.putInt("mStrokeColor", this.mStrokeColor);
        state.putFloat("mProgressSize", this.mProgressSize);
        state.putInt("mProgressColor", this.mProgressColor);
        state.putInt("mNotchsCount", this.mNotchsCount);
        state.putFloat("mNotchsLength", this.mNotchsLength);
        state.putBoolean("mSnapToNotchs", this.mSnapToNotchs);

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
        this.mAngleStart = savedState.getFloat("mAngleStart");
        this.mAngleSweep = savedState.getFloat("mAngleSweep");
        this.mAngleDraw = savedState.getFloat("mAngleDraw");
        this.mStrokeSize = savedState.getFloat("mStrokeSize");
        this.mStrokeColor = savedState.getInt("mStrokeColor");
        this.mProgressSize = savedState.getFloat("mProgressSize");
        this.mProgressColor = savedState.getInt("mProgressColor");
        this.mNotchsCount = savedState.getInt("mNotchsCount");
        this.mNotchsLength = savedState.getFloat("mNotchsLength");
        this.mSnapToNotchs = savedState.getBoolean("mSnapToNotchs");
    }


    /**
     * Public methods
     */

    // Get the base arc.
    @SuppressWarnings("unused")
    public ScArc getBaseArc() {
        return this.getArcs()[0];
    }

    // Get the notchs arc.
    // Note that the notchs will be returned ad a ScArc because in the advanced use of the gauge
    // it could be an ScArc, so you need to cast it for use as ScNotchs.
    @SuppressWarnings("unused")
    public ScArc getNotchsArc() {
        return this.getArcs()[1];
    }

    // Get the progress arc.
    @SuppressWarnings("unused")
    public ScArc getProgressArc() {
        return this.getArcs()[2];
    }

    // Set stroke cap of painter for all components inside the gauge.
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

    // The canvas filling setting for all components inside the gauge.
    @SuppressWarnings("unused")
    public void setCanvasFilling(ScArc.FillingArea area, ScArc.FillingMode mode) {
        // Cycle all arcs and set the filling
        for (ScArc arc : this.getArcs()) {
            arc.setFillingArea(area);
            arc.setFillingMode(mode);
        }
        // Refresh
        this.requestLayout();
    }

    // Set the components visibility.
    // For a correct measure of the component it is better not use GONE.
    @SuppressWarnings("unused")
    public void show(boolean baseArc, boolean notchsArc, boolean progressArc) {
        // Apply the visibility status
        this.mArcBase.setVisibility(baseArc ? View.VISIBLE : View.INVISIBLE);
        this.mArcNotchs.setVisibility(notchsArc ? View.VISIBLE : View.INVISIBLE);
        this.mArcProgress.setVisibility(progressArc ? View.VISIBLE : View.INVISIBLE);

        // Refresh
        this.requestLayout();
    }

    // Translate the angle in a value within the passed range of values.
    @SuppressWarnings("unused")
    public float translateAngleToValue(float angle, float startRange, float endRange) {
        // Limit the value within the range
        angle = ScGauge.valueRangeLimit(angle, 0.0f, this.mArcProgress.getAngleSweep());
        // Check for the division domain
        if (this.mArcProgress.getAngleSweep() != 0.0f) {
            return (angle / this.mArcProgress.getAngleSweep()) * (endRange - startRange);
        } else {
            return 0.0f;
        }
    }

    // Get the value animator.
    // Note that the initial value duration of the animation is zero equal to "no animation".
    @SuppressWarnings("unused")
    public Animator getValueAnimator() {
        return this.mAnimator;
    }

    // Change the components configuration.
    // This method is only for advanced use of ScGauge and use it improperly can be cause of
    // component malfunction. Note that ALWAYS the last component is the progress one.
    // Changing the component type mean create a new one and lost old information like visibility
    // and cap stroke style.
    // So, if you did some change about inner properties before call this
    // method, you must remember to apply again these settings.
    @SuppressWarnings("unused")
    public void changeComponentsConfiguration(
            boolean baseArcToNotchs, boolean notchsArcToArc, boolean progressArcToNotchs) {
        // Transform the base arc to a notchs object
        if (baseArcToNotchs) {
            // Create a new instance of the ScNotchs
            this.mArcBase = new ScNotchs(this.getContext());
            this.arcObjectSetter(this.mArcBase, false);
        }

        // Transform the notchs to an arc object
        if (notchsArcToArc) {
            // Create a new instance of the ScArc
            this.mArcNotchs = new ScArc(this.getContext());
            this.arcObjectSetter(this.mArcNotchs, false);
        }

        // Transform the base arc to a notchs object
        if (baseArcToNotchs) {
            // Create a new instance of the ScNotchs
            this.mArcProgress = new ScNotchs(this.getContext());
            this.arcObjectSetter(this.mArcProgress, true);
        }
    }

    // Set the notchs style for all ScNotchs components inside this component
    @SuppressWarnings("unused")
    public void setNotchsStyle(ScNotchs.NotchsTypes value) {
        // Apply to all notchs object
        for (ScArc arc : this.getArcs()) {
            // Check for ScNotchs class
            if (arc instanceof ScNotchs) {
                // Cast and setting
                ((ScNotchs) arc).setNotchsType(value);
            }
        }
        // Refresh the component
        this.requestLayout();
    }


    /**
     * Public properties
     */

    // Start angle.
    // Use this method change the start angle of all component inside this gauge.
    @SuppressWarnings("unused")
    public float getAngleStart() {
        return this.mAngleStart;
    }

    @SuppressWarnings("unused")
    public void setAngleStart(int value) {
        // Check for changed value
        if (this.mAngleStart != value) {
            // Save the new value
            this.mAngleStart = value;
            // Cycle all arcs and set the start angle
            for (ScArc arc : this.getArcs()) {
                arc.setAngleStart(value);
            }
            // Refresh
            this.requestLayout();
        }
    }

    // Sweep angle.
    // Use this method change the start angle of all component inside this gauge.
    @SuppressWarnings("unused")
    public float getAngleSweep() {
        return this.mAngleSweep;
    }

    @SuppressWarnings("unused")
    public void setAngleSweep(int value) {
        // Check for changed value
        if (this.mAngleSweep != value) {
            // Save the new value
            this.mAngleSweep = value;
            // Cycle all arcs and set the start angle
            for (ScArc arc : this.getArcs()) {
                arc.setAngleSweep(value);
            }
            // Refresh
            this.requestLayout();
        }
    }

    // Stroke size
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mStrokeSize;
    }

    @SuppressWarnings("unused")
    public void setStrokeSize(float value) {
        // Check if value is changed
        if (this.mStrokeSize != value) {
            // Store the new value
            this.mStrokeSize = value;
            this.mArcBase.setStrokeSize(value);
            // Refresh the component
            this.requestLayout();
        }
    }

    // Stroke color
    @SuppressWarnings("unused")
    public int getStrokeColor() {
        return this.mStrokeColor;
    }

    @SuppressWarnings("unused")
    public void setStrokeColor(int value) {
        // Check if value is changed
        if (this.mStrokeColor != value) {
            // Store the new value
            this.mStrokeColor = value;
            this.mArcBase.setStrokeColor(value);
            // Refresh the component
            this.invalidate();
        }
    }

    // Progress size
    @SuppressWarnings("unused")
    public float getProgressSize() {
        return this.mProgressSize;
    }

    @SuppressWarnings("unused")
    public void setProgressSize(float value) {
        // Check if value is changed
        if (this.mProgressSize != value) {
            // Store the new value and refresh the component
            this.mProgressSize = value;
            this.mArcProgress.setStrokeSize(value);
            this.requestLayout();
        }
    }

    // Progress color
    @SuppressWarnings("unused")
    public int getProgressColor() {
        return this.mProgressColor;
    }

    @SuppressWarnings("unused")
    public void setProgressColor(int value) {
        // Check if value is changed
        if (this.mProgressColor != value) {
            // Store the new value and refresh the component
            this.mProgressColor = value;
            this.mArcProgress.setStrokeColor(value);
            this.invalidate();
        }
    }

    // Notchs size
    @SuppressWarnings("unused")
    public float getNotchsSize() {
        return this.mNotchsSize;
    }

    @SuppressWarnings("unused")
    public void setNotchsSize(float value) {
        // Check if value is changed
        if (this.mNotchsSize != value) {
            // Store the new value and refresh the component
            this.mNotchsSize = value;
            this.mArcNotchs.setStrokeSize(value);
            this.requestLayout();
        }
    }

    // Progress color
    @SuppressWarnings("unused")
    public int getNotchsColor() {
        return this.mNotchsColor;
    }

    @SuppressWarnings("unused")
    public void setNotchsColor(int value) {
        // Check if value is changed
        if (this.mNotchsColor != value) {
            // Store the new value and refresh the component
            this.mNotchsColor = value;
            this.mArcNotchs.setStrokeColor(value);
            this.invalidate();
        }
    }

    // Notchs count
    @SuppressWarnings("unused")
    public int getNotchs() {
        return this.mNotchsCount;
    }

    @SuppressWarnings("unused")
    public void setNotchs(int value) {
        // Check if value is changed
        if (this.mNotchsCount != value) {
            // Fix the new value
            this.mNotchsCount = value;
            // Apply to all notchs object
            for (ScArc arc : this.getArcs()) {
                // Check for ScNotchs class
                if (arc instanceof ScNotchs) {
                    // Cast and setting
                    ((ScNotchs) arc).setNotchs(value);
                }
            }
            // Refresh the component
            this.requestLayout();
        }
    }

    // Progress size
    @SuppressWarnings("unused")
    public float getNotchsLength() {
        return this.mNotchsLength;
    }

    @SuppressWarnings("unused")
    public void setNotchsLength(float value) {
        // Check if value is changed
        if (this.mNotchsLength != value) {
            // Fix the new value
            this.mNotchsLength = value;
            // Apply to all notchs object
            for (ScArc arc : this.getArcs()) {
                // Check for ScNotchs class
                if (arc instanceof ScNotchs) {
                    // Cast and setting
                    ((ScNotchs) arc).setNotchsLength(value);
                }
            }
            // Refresh the component
            this.requestLayout();
        }
    }

    // Round the progress value to the near notch degrees
    @SuppressWarnings("unused")
    public boolean getSnapToNotchs() {
        return this.mSnapToNotchs;
    }

    @SuppressWarnings("unused")
    public void setSnapToNotchs(boolean value) {
        // Fix the trigger
        this.mSnapToNotchs = value;
        // Recall the set value method for apply the new setting
        this.setValue(this.getValue());
    }

    // Progress value in degrees
    @SuppressWarnings("unused")
    public float getValue() {
        return this.mAngleDraw;
    }

    @SuppressWarnings("unused")
    public void setValue(float degrees) {
        // Check for snap to notchs the new degrees value
        if (this.mSnapToNotchs) {
            // Round at the closed notchs value
            degrees = this.snapToNotchs(degrees);
        }

        // Check if value is changed
        if (this.mAngleDraw != degrees) {
            // Save the new value
            this.mAngleDraw = degrees;
            // Set and start animation
            this.mAnimator.setFloatValues(
                    this.mAngleDraw,
                    ScGauge.valueRangeLimit(degrees, 0, this.mAngleSweep)
            );
            this.mAnimator.start();
        }
    }

    // Progress value but based on a values range.
    // Translate the reference value to the angle in degrees and call the base methods.
    @SuppressWarnings("unused")
    public float getValue(float startRange, float endRange) {
        // Return the translated value
        return this.translateAngleToValue(
                this.mAngleDraw,
                startRange,
                endRange
        );
    }

    @SuppressWarnings("unused")
    public void setValue(float value, float startRange, float endRange) {
        // Limit the value within the range
        value = ScGauge.valueRangeLimit(value, startRange, endRange);
        // Check for the division domain
        if (endRange == startRange) {
            value = 0;

        } else {
            // Convert the value in the relative angle respect the arc length
            value = ((value - startRange) / (endRange - startRange)) * this.mAngleSweep;
        }
        // Call the base method
        this.setValue(value);
    }


    /**
     * Public listener and interface
     */

    // Draw
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc);

        void onDrawNotch(ScNotchs.NotchInfo info);

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
        this.mOnCustomPaddingListener = listener;
    }

    // Value changing
    @SuppressWarnings("unused")
    public interface OnEventListener {
        void onValueChange(float degrees);
    }

    @SuppressWarnings("unused")
    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

}