package com.sccomponents.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.List;

/**
 * Manage a generic gauge.
 * <p/>
 * This class is studied to be an "helper class" to facilitate the user to create a gauge.
 * The path is generic and the class start with a standard configuration of features.
 * One base (inherited from the ScDrawer), one notchs manager, one writer manager, one copier to
 * create the progress effect and two pointer for manage the user touch input.
 * <p/>
 * Here are exposed many methods to drive the common feature from the code or directly by the XML.
 * The features are recognized from the class by its tag so changing, for example, the color of
 * notchs you will change the color of all notchs tagged.
 * This is useful when you have a custom features configuration that use one more of feature per
 * type. All the custom added features not tagged should be managed by the user by himself.
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public abstract class ScGauge extends ScDrawer implements
        ValueAnimator.AnimatorUpdateListener,
        ScCopier.OnDrawListener,
        ScPointer.OnDrawListener,
        ScNotchs.OnDrawListener,
        ScWriter.OnDrawListener {

    /****************************************************************************************
     * Constants
     */

    public static final float DEFAULT_STROKE_SIZE = 3.0f;
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;

    public static final float DEFAULT_PROGRESS_SIZE = 0.0f;
    public static final int DEFAULT_PROGRESS_COLOR = Color.GRAY;

    public static final float DEFAULT_TEXT_SIZE = 16.0f;
    public static final float DEFAULT_HALO_SIZE = 10.0f;

    public static final String BASE_IDENTIFIER = "base";
    public static final String NOTCHS_IDENTIFIER = "notchs";
    public static final String WRITER_IDENTIFIER = "writer";
    public static final String PROGRESS_IDENTIFIER = "progress";
    public static final String HIGH_POINTER_IDENTIFIER = "high";
    public static final String LOW_POINTER_IDENTIFIER = "low";


    /****************************************************************************************
     * Privates attribute
     */

    protected float mStrokeSize;
    protected int mStrokeColor;

    protected float mProgressSize;
    protected int mProgressColor;

    protected float mNotchsSize;
    protected int mNotchsColor;
    protected int mNotchsCount;
    protected float mNotchsLength;
    protected boolean mSnapToNotchs;

    protected String[] mTextTokens;
    protected float mTextSize;
    protected int mTextColor;

    protected float mPointerRadius;
    protected int mPointerColor;
    protected float mPointerHaloWidth;


    /****************************************************************************************
     * Privates variable
     */

    private float mHighValue;
    private float mLowValue;

    private ValueAnimator mHighValueAnimator;
    private ValueAnimator mLowValueAnimator;

    private ScPointer mSelectedPointer;

    private OnEventListener mOnEventListener;
    private OnDrawListener mOnDrawListener;


    /****************************************************************************************
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


    /****************************************************************************************
     * Privates methods
     */

    /**
     * Set a feature with the default setting values by its type.
     *
     * @param feature the feature to settle
     */
    private void featureSetter(ScFeature feature) {
        // Check for empty value
        if (feature == null || feature.getTag() == null) return;

        // Hold the tag
        String tag = feature.getTag();

        // Base
        if (tag.equalsIgnoreCase(ScGauge.BASE_IDENTIFIER)) {
            // fill
            feature.getPainter().setColor(this.mStrokeColor);
            feature.getPainter().setStrokeWidth(this.mStrokeSize);
        }

        // Progress
        if (tag.equalsIgnoreCase(ScGauge.PROGRESS_IDENTIFIER)) {
            // fill
            feature.setLimits(this.mLowValue, this.mHighValue);
            feature.getPainter().setColor(this.mProgressColor);
            feature.getPainter().setStrokeWidth(this.mProgressSize);
        }

        // Notchs
        if (feature instanceof ScNotchs &&
                tag.equalsIgnoreCase(ScGauge.NOTCHS_IDENTIFIER)) {
            // Cast and fill
            ScNotchs notchs = (ScNotchs) feature;
            notchs.setLength(this.mNotchsLength);
            notchs.setCount(this.mNotchsCount);
            notchs.getPainter().setColor(this.mNotchsColor);
            notchs.getPainter().setStrokeWidth(this.mNotchsSize);
        }

        // Writer
        if (feature instanceof ScWriter &&
                tag.equalsIgnoreCase(ScGauge.WRITER_IDENTIFIER)) {
            // Cast and fill
            ScWriter writer = (ScWriter) feature;
            writer.getPainter().setColor(this.mTextColor);
            writer.getPainter().setTextSize(this.mTextSize);

            if (this.mTextTokens != null) {
                writer.setTokens(this.mTextTokens);
            }
        }

        // Pointers
        boolean isHigh = tag.equalsIgnoreCase(ScGauge.HIGH_POINTER_IDENTIFIER);
        boolean isLow = tag.equalsIgnoreCase(ScGauge.LOW_POINTER_IDENTIFIER);

        if (feature instanceof ScPointer && (isHigh || isLow)) {
            // Cast and fill
            ScPointer pointer = (ScPointer) feature;
            pointer.setRadius(this.mPointerRadius);
            pointer.setHaloWidth(this.mPointerHaloWidth);
            pointer.getPainter().setColor(this.mPointerColor);

            // Switch the case
            if (isHigh) pointer.setPosition(this.mHighValue);
            if (isLow) pointer.setPosition(this.mLowValue);
        }
    }

    /**
     * Round the value near the closed notch.
     *
     * @param percentage the start percentage value
     * @return the percentage value close the notch
     */
    private float snapToNotchs(float percentage) {
        // Check for empty value
        if (this.mNotchsCount == 0.0f) return percentage;
        if (this.mPathMeasure.getLength() == 0.0f) return 0.0f;

        // Calc the percentage step delta and return the closed value
        float step = 100 / this.mNotchsCount;
        return Math.round(percentage / step) * step;
    }

    /**
     * Define the threshold for the touch on path recognize.
     */
    private void fixTouchOnPathThreshold() {
        // Define the touch threshold
        if (this.mPointerRadius > 0) {
            this.setPathTouchThreshold(mPointerRadius + this.mPointerHaloWidth);
        }
    }

    /**
     * Init the component.
     * Retrieve all attributes with the default values if needed.
     * Check the values for internal use and create the painters.
     *
     * @param context  the owner context
     * @param attrs    the attribute set
     * @param defStyle the style
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context
                .obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Base
        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dipToPixel(ScGauge.DEFAULT_STROKE_SIZE));
        this.mStrokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, ScGauge.DEFAULT_STROKE_COLOR);

        // Progress
        this.mProgressSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_progress_size, this.dipToPixel(ScGauge.DEFAULT_PROGRESS_SIZE));
        this.mProgressColor = attrArray.getColor(
                R.styleable.ScComponents_scc_progress_color, ScGauge.DEFAULT_PROGRESS_COLOR);
        this.mHighValue = attrArray.getFloat(
                R.styleable.ScComponents_scc_value, 0.0f);

        // Notchs
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

        // Text
        this.mTextSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_text_size, this.dipToPixel(ScGauge.DEFAULT_TEXT_SIZE));
        this.mTextColor = attrArray.getColor(
                R.styleable.ScComponents_scc_text_color, ScGauge.DEFAULT_STROKE_COLOR);

        String stringTokens = attrArray.getString(R.styleable.ScComponents_scc_text_tokens);
        this.mTextTokens = stringTokens != null ? stringTokens.split("\\|") : null;

        // Pointer
        this.mPointerRadius = attrArray.getDimension(
                R.styleable.ScComponents_scc_pointer_radius, 0.0f);
        this.mPointerColor = attrArray.getColor(
                R.styleable.ScComponents_scc_pointer_color, ScGauge.DEFAULT_STROKE_COLOR);
        this.mPointerHaloWidth = attrArray.getDimension(
                R.styleable.ScComponents_scc_halo_size, ScGauge.DEFAULT_HALO_SIZE);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        // Disable the hardware acceleration as have problem with the shader
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Check for snap to notchs the new degrees value
        if (this.mSnapToNotchs) {
            // Get the current value and round at the closed notchs value
            this.mHighValue = this.snapToNotchs(this.mHighValue);
            this.mLowValue = this.snapToNotchs(this.mLowValue);
        }

        // Define the touch threshold
        this.fixTouchOnPathThreshold();

        //--------------------------------------------------
        // FEATURES

        ScCopier base = (ScCopier) this.addFeature(ScCopier.class);
        base.setTag(ScGauge.BASE_IDENTIFIER);
        this.featureSetter(base);

        ScNotchs notchs = (ScNotchs) this.addFeature(ScNotchs.class);
        notchs.setTag(ScGauge.NOTCHS_IDENTIFIER);
        this.featureSetter(notchs);

        ScCopier progress = (ScCopier) this.addFeature(ScCopier.class);
        progress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        this.featureSetter(progress);

        ScWriter writer = (ScWriter) this.addFeature(ScWriter.class);
        writer.setTag(ScGauge.WRITER_IDENTIFIER);
        this.featureSetter(writer);

        ScPointer highPointer = (ScPointer) this.addFeature(ScPointer.class);
        highPointer.setTag(ScGauge.HIGH_POINTER_IDENTIFIER);
        this.featureSetter(highPointer);

        ScPointer lowPointer = (ScPointer) this.addFeature(ScPointer.class);
        lowPointer.setTag(ScGauge.LOW_POINTER_IDENTIFIER);
        lowPointer.setOnDrawListener(this);
        this.featureSetter(lowPointer);

        //--------------------------------------------------
        // ANIMATOR

        this.mHighValueAnimator = new ValueAnimator();
        this.mHighValueAnimator.setDuration(0);
        this.mHighValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mHighValueAnimator.addUpdateListener(this);

        this.mLowValueAnimator = new ValueAnimator();
        this.mLowValueAnimator.setDuration(0);
        this.mLowValueAnimator.setInterpolator(new DecelerateInterpolator());
        this.mLowValueAnimator.addUpdateListener(this);
    }

    /**
     * Find the value percentage respect range of values.
     *
     * @param value      the value
     * @param startRange the start range value
     * @param endRange   the end range value
     * @return the percentage
     */
    private float findPercentage(float value, float startRange, float endRange) {
        // Limit the value within the range
        value = ScGauge.valueRangeLimit(value, startRange, endRange);
        // Check the domain
        if (endRange - startRange == 0.0f) {
            // Return zero
            return 0.0f;

        } else {
            // return the calculated percentage
            return ((value - startRange) / (endRange - startRange)) * 100.0f;
        }
    }

    /**
     * Set the current progress value in percentage from the path start.
     *
     * @param value         the new value
     * @param treatLowValue consider the low or the high value
     */
    private void setGenericValue(float value, boolean treatLowValue) {
        // Check the limits
        value = ScGauge.valueRangeLimit(value, 0, 100);

        // Check for snap to notchs the new degrees value.
        if (this.mSnapToNotchs) {
            // Round at the closed notchs value
            value = this.snapToNotchs(value);
        }

        // Choice the value and the animation
        float currValue = treatLowValue ? this.mLowValue : this.mHighValue;
        ValueAnimator animator = treatLowValue ? this.mLowValueAnimator : this.mHighValueAnimator;

        // Limits
        // TODO: not work for inverted values (example negative sweep angle)
        if (treatLowValue && value > this.mHighValue) value = this.mHighValue;
        if (!treatLowValue && value < this.mLowValue) value = this.mLowValue;

        // Check if value is changed
        if (currValue != value) {
            // Set and start animation
            animator.setFloatValues(currValue, value);
            animator.start();
        }
    }

    /**
     * Get the nearest pointer considering the passed distance from the path start.
     *
     * @param distance from the path start
     * @return the nearest pointer
     */
    private ScPointer findNearestPointer(float distance) {
        // Get all pointers
        List<ScFeature> pointers = this.findFeatures(ScPointer.class, null);
        ScPointer nearest = null;

        // Cycle all pointers found
        for (ScFeature pointer : pointers) {
            // Cast to current pointer
            ScPointer current = (ScPointer) pointer;
            // Check if the pointer is visible
            if (!current.getVisible() || current.getRadius() == 0.0f) continue;
            // If the nearest is null assign the first pointer to it
            if (nearest == null ||
                    Math.abs(distance - nearest.getPosition()) > Math.abs(distance - current.getPosition())) {
                nearest = current;
            }
        }
        // Return the nearest pointer if found
        return nearest;
    }

    /**
     * Set the value (high or low) considering the near pointer.
     *
     * @param value   the new value
     * @param pointer the pointer near
     */
    private void setValueByPointer(float value, ScPointer pointer) {
        // Check for the low value
        if (pointer != null &&
                pointer.getTag().equalsIgnoreCase(ScGauge.LOW_POINTER_IDENTIFIER)) {
            // Set and exit
            this.setGenericValue(value, true);
            return;
        }

        // Check for the high value
        if (pointer == null ||
                pointer.getTag().equalsIgnoreCase(ScGauge.HIGH_POINTER_IDENTIFIER)) {
            // Set and exit
            this.setGenericValue(value, false);
            return;
        }

        // If here mean that the pointer is untagged.
        // I will move the pointer to the new position but I will not change no values.
        pointer.setPosition(value);
    }

    /**
     * Attach the feature to the right listener only if the class listener is defined.
     *
     * @param feature the source
     */
    private void attachFeatureToListener(ScFeature feature) {
        // Attach the listener by the class type
        if (this.mOnDrawListener != null) {
            if (feature instanceof ScCopier) ((ScCopier) feature).setOnDrawListener(this);
            if (feature instanceof ScPointer) ((ScPointer) feature).setOnDrawListener(this);
            if (feature instanceof ScNotchs) ((ScNotchs) feature).setOnDrawListener(this);
            if (feature instanceof ScWriter) ((ScWriter) feature).setOnDrawListener(this);
        }
    }


    /****************************************************************************************
     * Instance state
     */

    /**
     * Save the current instance state
     *
     * @return the state
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putFloat("mStrokeSize", this.mStrokeSize);
        state.putInt("mStrokeColor", this.mStrokeColor);
        state.putFloat("mHighValue", this.mHighValue);
        state.putFloat("mLowValue", this.mLowValue);
        state.putFloat("mProgressSize", this.mProgressSize);
        state.putInt("mProgressColor", this.mProgressColor);
        state.putFloat("mNotchsSize", this.mNotchsSize);
        state.putInt("mNotchsColor", this.mNotchsColor);
        state.putInt("mNotchsCount", this.mNotchsCount);
        state.putFloat("mNotchsLength", this.mNotchsLength);
        state.putBoolean("mSnapToNotchs", this.mSnapToNotchs);
        state.putStringArray("mTextTokens", this.mTextTokens);
        state.putFloat("mTextSize", this.mTextSize);
        state.putInt("mTextColor", this.mTextColor);
        state.putFloat("mPointerRadius", this.mPointerRadius);
        state.putInt("mPointerColor", this.mPointerColor);
        state.putFloat("mPointerHaloWidth", this.mPointerHaloWidth);

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     *
     * @param state the state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mStrokeSize = savedState.getFloat("mStrokeSize");
        this.mStrokeColor = savedState.getInt("mStrokeColor");
        this.mHighValue = savedState.getFloat("mHighValue");
        this.mLowValue = savedState.getFloat("mLowValue");
        this.mProgressSize = savedState.getFloat("mProgressSize");
        this.mProgressColor = savedState.getInt("mProgressColor");
        this.mNotchsSize = savedState.getFloat("mNotchsSize");
        this.mNotchsColor = savedState.getInt("mNotchsColor");
        this.mNotchsCount = savedState.getInt("mNotchsCount");
        this.mNotchsLength = savedState.getFloat("mNotchsLength");
        this.mSnapToNotchs = savedState.getBoolean("mSnapToNotchs");
        this.mTextTokens = savedState.getStringArray("mTextTokens");
        this.mTextSize = savedState.getFloat("mTextSize");
        this.mTextColor = savedState.getInt("mTextColor");
        this.mPointerRadius = savedState.getFloat("mPointerRadius");
        this.mPointerColor = savedState.getInt("mPointerColor");
        this.mPointerHaloWidth = savedState.getFloat("mPointerHaloWidth");
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * Setting the features and call the ScDrawer base draw method.
     *
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Cycle all features
        for (ScFeature feature : this.findFeatures(null, null)) {
            // Apply the setting
            this.featureSetter(feature);

            // Check if have a selected pointer
            if (this.mSelectedPointer != null) {
                // Set the current status
                this.mSelectedPointer.setPressed(this.isPressed());
            }
        }

        // Call the base drawing method
        super.onDraw(canvas);
    }

    /**
     * Override the on animation update method
     *
     * @param animation the animator
     */
    @Override
    @SuppressWarnings("unused")
    public void onAnimationUpdate(ValueAnimator animation) {
        // Get the current value
        if (animation.equals(this.mHighValueAnimator))
            this.mHighValue = (float) animation.getAnimatedValue();
        if (animation.equals(this.mLowValueAnimator))
            this.mLowValue = (float) animation.getAnimatedValue();

        // Refresh
        this.invalidate();

        // Manage the listener
        if (this.mOnEventListener != null) {
            this.mOnEventListener.onValueChange(this.mLowValue, this.mHighValue);
        }
    }

    /**
     * Add one feature to this drawer.
     * This particular overload instantiate a new object from the class reference passed.
     * <p/>
     * The passed class reference must implement the ScFeature interface and will be filled
     * with the setting default params of this object by the type.
     * For example if instance a ScNotchs the notchs count will be auto settle to the defined
     * getNotchsCount method.
     * <p/>
     * The new feature instantiate will linked to the gauge on draw listener.
     * If you will create the feature with another method you must manage the on draw listener by
     * yourself or attach it to the gauge at a later time using the proper method.
     *
     * @param classRef the class reference to instantiate
     * @return the new feature object
     */
    @Override
    @SuppressWarnings("unused")
    public ScFeature addFeature(Class<?> classRef) {
        // Instance calling the base method
        ScFeature feature = super.addFeature(classRef);

        // Call the feature setter here is useless but we want to have the right setting from
        // the first creation in case the user looking inside this object.
        this.featureSetter(feature);
        // Attach the feature the listener if needed.
        this.attachFeatureToListener(feature);

        // Return the new feature
        return feature;
    }

    /**
     * Called when the path is touched.
     *
     * @param distance the distance from the path start
     */
    @Override
    protected void onPathTouch(float distance) {
        // Select the nearest pointer and set the value
        float percentage = this.findPercentage(distance, 0, this.mPathMeasure.getLength());
        this.mSelectedPointer = this.findNearestPointer(percentage);
        this.setValueByPointer(percentage, this.mSelectedPointer);

        // Super
        super.onPathTouch(distance);
    }

    /**
     * Called when the user release the touch after than he touched the path.
     */
    @Override
    protected void onPathRelease() {
        // Super and refresh
        super.onPathRelease();
        this.invalidate();
    }

    /**
     * Called when, after a path touch, the user move the finger on the component.
     *
     * @param distance the distance from the path start
     */
    @Override
    protected void onPathSlide(float distance) {
        // Move the pointer and the value
        float percentage = this.findPercentage(distance, 0, this.mPathMeasure.getLength());
        this.setValueByPointer(percentage, this.mSelectedPointer);

        // Super
        super.onPathSlide(distance);
    }

    /**
     * Called before draw the path copy.
     *
     * @param info the copier info
     */
    @Override
    public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawCopy(info);
        }
    }

    /**
     * Called before draw the pointer.
     * If the method set the bitmap inside the info object the default drawing will be bypassed
     * and the new bitmap will be draw on the canvas following the other setting.
     *
     * @param info the pointer info
     */
    @Override
    public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawPointer(info);
        }
    }

    /**
     * Called before draw the single notch.
     *
     * @param info the notch info
     */
    @Override
    public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawNotch(info);
        }
    }

    /**
     * Called before draw the single token
     *
     * @param info the token info
     */
    @Override
    public void onBeforeDrawToken(ScWriter.TokenInfo info) {
        // Forward the calling on local listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawToken(info);
        }
    }


    /****************************************************************************************
     * Public methods
     */

    /**
     * Get the high value animator.
     * Note that the initial value duration of the animation is zero equal to "no animation".
     *
     * @return the animator
     */
    @SuppressWarnings("unused")
    public Animator getHighValueAnimator() {
        return this.mHighValueAnimator;
    }

    /**
     * Get the low value animator.
     * Note that the initial value duration of the animation is zero equal to "no animation".
     *
     * @return the animator
     */
    @SuppressWarnings("unused")
    public Animator getLowValueAnimator() {
        return this.mLowValueAnimator;
    }

    /**
     * Convert a percentage in a value within the passed range of values.
     *
     * @param percentage the percentage
     * @param startValue the range starting value
     * @param endValue   the range ending value
     * @return the value
     */
    @SuppressWarnings("unused")
    public static float percentageToValue(float percentage, float startValue, float endValue) {
        // Calculate the delta range
        float min = Math.min(startValue, endValue);
        float max = Math.max(startValue, endValue);
        float delta = max - min;
        // Return the value
        return (delta * (percentage / 100)) + min;
    }


    /****************************************************************************************
     * Base
     */

    /**
     * Return the stroke size
     *
     * @return the current stroke size in pixel
     */
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mStrokeSize;
    }

    /**
     * Set the stroke size
     *
     * @param value the new stroke size in pixel
     */
    @SuppressWarnings("unused")
    public void setStrokeSize(float value) {
        // Check if value is changed
        if (this.mStrokeSize != value) {
            // Store the new value, check it and refresh the component
            this.mStrokeSize = value;
            this.requestLayout();
        }
    }

    /**
     * Return the current stroke color
     *
     * @return the current stroke color
     */
    @SuppressWarnings("unused")
    public int getStrokesColors() {
        return this.mStrokeColor;
    }

    /**
     * Set the current stroke colors
     *
     * @param value the new stroke colors
     */
    @SuppressWarnings("unused")
    public void setStrokeColors(int value) {
        // Save the new value and refresh
        this.mStrokeColor = value;
        this.requestLayout();
    }


    /****************************************************************************************
     * Progress
     */

    /**
     * Return the progress stroke size
     *
     * @return the size in pixel
     */
    @SuppressWarnings("unused")
    public float getProgressSize() {
        return this.mProgressSize;
    }

    /**
     * Set the progress stroke size
     *
     * @param value the value in pixel
     */
    @SuppressWarnings("unused")
    public void setProgressSize(float value) {
        // Check if value is changed
        if (this.mProgressSize != value) {
            // Store the new value
            this.mProgressSize = value;
            this.invalidate();
        }
    }

    /**
     * Return the progress stroke color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getProgressColor() {
        return this.mProgressColor;
    }

    /**
     * Set the progress color
     *
     * @param value the new color
     */
    @SuppressWarnings("unused")
    public void setProgressColor(int value) {
        // Check if value is changed
        if (this.mProgressColor != value) {
            // Store the new value and refresh the component
            this.mProgressColor = value;
            this.invalidate();
        }
    }

    /**
     * Return the high current progress value in percentage
     *
     * @return the current value in percentage
     */
    @SuppressWarnings("unused")
    public float getHighValue() {
        return this.mHighValue;
    }

    /**
     * Set the current progress high value in percentage from the path start
     *
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setHighValue(float percentage) {
        this.setGenericValue(percentage, false);
    }

    /**
     * Return the high progress value but based on a values range.
     *
     * @param startRange the start value
     * @param endRange   the end value
     * @return the translated value
     */
    @SuppressWarnings("unused")
    public float getHighValue(float startRange, float endRange) {
        // Check the domain
        if (this.mHighValue == 0) {
            return 0.0f;

        } else {
            // Calculate the value relative
            return ((endRange - startRange) * this.mHighValue) / 100.0f;
        }
    }

    /**
     * Set the progress high value but based on a values range.
     *
     * @param value      the value to convert
     * @param startRange the start value
     * @param endRange   the end value
     */
    @SuppressWarnings("unused")
    public void setHighValue(float value, float startRange, float endRange) {
        // Find the relative percentage
        float percentage = this.findPercentage(value, startRange, endRange);
        // Call the base method
        this.setGenericValue(percentage, false);
    }

    /**
     * Return the low current progress value in percentage
     *
     * @return the current value in percentage
     */
    @SuppressWarnings("unused")
    public float getLowValue() {
        return this.mLowValue;
    }

    /**
     * Set the current progress low value in percentage from the path start
     *
     * @param percentage the new value in percentage
     */
    @SuppressWarnings("unused")
    public void setLowValue(float percentage) {
        this.setGenericValue(percentage, true);
    }

    /**
     * Return the low progress value but based on a values range.
     *
     * @param startRange the start value
     * @param endRange   the end value
     * @return the translated value
     */
    @SuppressWarnings("unused")
    public float getLowValue(float startRange, float endRange) {
        // Check the domain
        if (this.mHighValue == 0) {
            return 0.0f;

        } else {
            // Calculate the value relative
            return ((endRange - startRange) * this.mLowValue) / 100.0f;
        }
    }

    /**
     * Set the progress low value but based on a values range.
     *
     * @param value      the value to convert
     * @param startRange the start value
     * @param endRange   the end value
     */
    @SuppressWarnings("unused")
    public void setLowValue(float value, float startRange, float endRange) {
        // Find the relative percentage
        float percentage = this.findPercentage(value, startRange, endRange);
        // Call the base method
        this.setGenericValue(percentage, true);
    }


    /****************************************************************************************
     * Notchs
     */

    /**
     * Return the progress notch size
     *
     * @return the size in pixel
     */
    @SuppressWarnings("unused")
    public float getNotchsSize() {
        return this.mNotchsSize;
    }

    /**
     * Set the progress notch size
     *
     * @param value the new size in pixel
     */
    @SuppressWarnings("unused")
    public void setNotchsSize(float value) {
        // Check if value is changed
        if (this.mNotchsSize != value) {
            // Store the new value and refresh the component
            this.mNotchsSize = value;
            this.invalidate();
        }
    }

    /**
     * Return the notchs color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getNotchsColor() {
        return this.mNotchsColor;
    }

    /**
     * Set the notchs color
     *
     * @param value the new color
     */
    @SuppressWarnings("unused")
    public void setNotchsColor(int value) {
        // Check if value is changed
        if (this.mNotchsColor != value) {
            // Store the new value and refresh the component
            this.mNotchsColor = value;
            this.invalidate();
        }
    }

    /**
     * Return the notchs count
     *
     * @return the count
     */
    @SuppressWarnings("unused")
    public int getNotchs() {
        return this.mNotchsCount;
    }

    /**
     * Set the notchs count
     *
     * @param value the new value
     */
    @SuppressWarnings("unused")
    public void setNotchs(int value) {
        // Check if value is changed
        if (this.mNotchsCount != value) {
            // Fix the new value
            this.mNotchsCount = value;
            this.invalidate();
        }
    }

    /**
     * Return the notchs length
     *
     * @return the length
     */
    @SuppressWarnings("unused")
    public float getNotchsLength() {
        return this.mNotchsLength;
    }

    /**
     * Set the notchs length
     *
     * @param value the new value in pixel
     */
    @SuppressWarnings("unused")
    public void setNotchsLength(float value) {
        // Check if value is changed
        if (this.mNotchsLength != value) {
            // Fix the new value
            this.mNotchsLength = value;
            this.invalidate();
        }
    }

    /**
     * Return if the progress value is rounded to the closed notch.
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public boolean getSnapToNotchs() {
        return this.mSnapToNotchs;
    }

    /**
     * Set if the progress value must rounded to the closed notch.
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setSnapToNotchs(boolean value) {
        // Fix the trigger
        this.mSnapToNotchs = value;

        // Recall the set value method for apply the new setting
        this.setHighValue(this.getHighValue());
        this.setLowValue(this.getLowValue());
    }


    /****************************************************************************************
     * Texts
     */

    /**
     * Return the text tokens to write on the path
     *
     * @return the tokens
     */
    @SuppressWarnings("unused")
    public String[] getTextTokens() {
        return this.mTextTokens;
    }

    /**
     * Set the text token to write on the path
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setTextTokens(String[] value) {
        // Fix the trigger
        this.mTextTokens = value;
        this.invalidate();
    }

    /**
     * Return the text size in pixel
     *
     * @return the size in pixel
     */
    @SuppressWarnings("unused")
    public float getTextSize() {
        return this.mTextSize;
    }

    /**
     * Set the text size in pixel
     *
     * @param value the status
     */
    @SuppressWarnings("unused")
    public void setTextSize(float value) {
        // Check if value is changed
        if (this.mTextSize != value) {
            // Fix the trigger
            this.mTextSize = value;
            this.invalidate();
        }
    }

    /**
     * Return the text color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getTextColor() {
        return this.mTextColor;
    }

    /**
     * Set the text color
     *
     * @param value the color
     */
    @SuppressWarnings("unused")
    public void setTextColor(int value) {
        // Check if value is changed
        if (this.mTextColor != value) {
            // Fix the trigger
            this.mTextColor = value;
            this.invalidate();
        }
    }


    /****************************************************************************************
     * Pointers
     */

    /**
     * Return the pointers radius in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the radius in pixel
     */
    @SuppressWarnings("unused")
    public float getPointerRadius() {
        return this.mPointerRadius;
    }

    /**
     * Set all pointers radius in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the new radius
     */
    @SuppressWarnings("unused")
    public void setPointerRadius(float value) {
        // Check if value is changed
        if (this.mPointerRadius != value) {
            // Fix the trigger
            this.mPointerRadius = value;
            this.fixTouchOnPathThreshold();
            this.invalidate();
        }
    }

    /**
     * Return the pointers color.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getPointersColor() {
        return this.mPointerColor;
    }

    /**
     * Set all pointers color.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the color
     */
    @SuppressWarnings("unused")
    public void setPointersColor(int value) {
        // Check if value is changed
        if (this.mPointerColor != value) {
            // Fix the trigger
            this.mPointerColor = value;
            this.invalidate();
        }
    }

    /**
     * Return the pointers halo width in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @return the halo size
     */
    @SuppressWarnings("unused")
    public float getPointerHaloWidth() {
        return this.mPointerHaloWidth;
    }

    /**
     * Set all pointers halo width in pixel.
     * Note that in the standard configuration the pointers are two: high and low.
     *
     * @param value the new halo size
     */
    @SuppressWarnings("unused")
    public void setPointerHaloWidth(float value) {
        // Check if value is changed
        if (this.mPointerHaloWidth != value) {
            // Fix the trigger
            this.mPointerHaloWidth = value;
            this.fixTouchOnPathThreshold();
            this.invalidate();
        }
    }


    /********************************************************************************************
     * Public listener and interface
     */

    /**
     * Generic event listener
     */
    @SuppressWarnings("unused")
    public interface OnEventListener {

        /**
         * Called when the high or the low value changed.
         *
         * @param lowValue  the current low value
         * @param highValue the current high value
         */
        void onValueChange(float lowValue, float highValue);

    }

    /**
     * Set the generic event listener
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        /**
         * Called before draw the path copy.
         *
         * @param info the copier info
         */
        void onBeforeDrawCopy(ScCopier.CopyInfo info);


        /**
         * Called before draw the single notch.
         *
         * @param info the notch info
         */
        void onBeforeDrawNotch(ScNotchs.NotchInfo info);

        /**
         * Called before draw the pointer.
         * If the method set the bitmap inside the info object the default drawing will be bypassed
         * and the new bitmap will be draw on the canvas following the other setting.
         *
         * @param info the pointer info
         */
        void onBeforeDrawPointer(ScPointer.PointerInfo info);

        /**
         * Called before draw the single token
         *
         * @param info the token info
         */
        void onBeforeDrawToken(ScWriter.TokenInfo info);

    }

    /**
     * Set the draw listener to call.
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        // Hold the reference
        this.mOnDrawListener = listener;

        // Attach the listener to all features
        for (ScFeature feature : this.mFeatures) {
            this.attachFeatureToListener(feature);
        }
    }

}
