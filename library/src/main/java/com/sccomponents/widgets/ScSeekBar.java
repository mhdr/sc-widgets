package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * ScCircularSeekBar
 */
public class ScSeekBar extends ScGauge {

    /**
     * Constants
     */

    public static final float DEFAULT_POINTER_RADIUS = 10.0f;
    public static final int DEFAULT_POINTER_COLOR = Color.GRAY;

    public static final float DEFAULT_HALO_SIZE = 5.0f;
    public static final int DEFAULT_HALO_ALPHA = 128;


    /**
     * Private attributes
     */

    private float mPointerRadius;
    private int mPointerColor;
    private float mHaloSize;
    private boolean mSnapToNotchs;


    /**
     * Private variables
     */

    private Paint mPointerPaint;
    private Paint mHaloPaint;

    private boolean mArcPressed;

    private OnDrawListener mOnDrawListener;


    /**
     * Constructors
     */

    public ScSeekBar(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    /**
     * Privates methods
     */

    // Check all input values
    private void checkValues() {
        // Size
        if (this.mPointerRadius < 0) this.mPointerRadius = 0;
        if (this.mHaloSize < 0) this.mHaloSize = 0;
    }

    // Get the base arc.
    // Should be always the first of the components of ScGauge.
    private ScArc getBaseArc() {
        return this.getArcs()[0];
    }

    // Get the notchs arc.
    // Should be always the second of the components of ScGauge.
    private ScNotchs getNotchsArc() {
        return (ScNotchs) this.getArcs()[1];
    }

    // Get the progress arc.
    // Should be always the third of the components of ScGauge.
    private ScArc getProgressArc() {
        return this.getArcs()[2];
    }

    // Round the degree angle to the near notch value
    private float snapToNotchs(float degrees) {
        // Calc the delta angle and the middle stroke
        float deltaAngle = this.getBaseArc().getAngleSweep() / this.getNotchs();
        // Round at notchs value
        return Math.round(degrees / deltaAngle) * deltaAngle;
    }

    // Init the component
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mPointerRadius = attrArray.getDimension(
                R.styleable.ScComponents_scc_pointer_radius, this.dipToPixel(ScSeekBar.DEFAULT_POINTER_RADIUS));
        this.mPointerColor = attrArray.getColor(
                R.styleable.ScComponents_scc_pointer_color, ScSeekBar.DEFAULT_POINTER_COLOR);

        this.mHaloSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_halo_size, this.dipToPixel(ScSeekBar.DEFAULT_HALO_SIZE));

        this.mSnapToNotchs = attrArray.getBoolean(
                R.styleable.ScComponents_scc_snap_to_notchs, false);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.mArcPressed = false;
        this.checkValues();

        // Check for snap to notchs the new degrees value
        if (this.mSnapToNotchs) {
            // Get the current value and round at the closed notchs value
            float drawAngle = this.getProgressArc().getAngleDraw();
            drawAngle = this.snapToNotchs(drawAngle);
            // Apply to the progress arc
            this.getProgressArc().setAngleDraw(drawAngle);
        }

        //--------------------------------------------------
        // PAINTER

        // Create the pointer paint
        this.mPointerPaint = new Paint();
        this.mPointerPaint.setColor(this.mPointerColor);
        this.mPointerPaint.setAntiAlias(true);
        this.mPointerPaint.setStyle(Paint.Style.FILL);
        this.mPointerPaint.setStrokeCap(Paint.Cap.ROUND);

        // Create the halo paint
        this.mHaloPaint = new Paint();
        this.mHaloPaint.setColor(this.mPointerColor);
        this.mHaloPaint.setAntiAlias(true);
        this.mHaloPaint.setStrokeWidth(this.mHaloSize);
        this.mHaloPaint.setStyle(Paint.Style.STROKE);
        this.mHaloPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mHaloPaint.setAlpha(ScSeekBar.DEFAULT_HALO_ALPHA);

        //--------------------------------------------------
        // EVENT

        // Propagate the base class event to this class.
        // Do it because in this class some methods of the listener interface is updated.
        // Noted that the onBeforeDraw will be not forwarded because it also change the calling
        // position respect to code.
        super.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc) {
                // Do nothing
            }

            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Check if have an listener instanced
                if (ScSeekBar.this.mOnDrawListener != null) {
                    // Propagate the methods
                    ScSeekBar.this.mOnDrawListener.onDrawNotch(info);
                }
            }
        });
    }

    // Get the real dimension of the pointer
    private float getPointerSize() {
        return (this.mPointerRadius + this.mHaloSize) * 2;
    }

    // Find the maximum stroke size.
    // This method is protected because will be used in the inherited class for reposition
    // the arcs in the space seen this methods is used inside the method to find the components
    // padding.
    @Override
    protected float findMaxStrokeSize() {
        // Get the super max
        float maxSize = super.findMaxStrokeSize();
        // Compare with the pointer size
        return maxSize > this.getPointerSize() ? maxSize : this.getPointerSize();
    }


    /**
     * Draw methods
     */

    // Draw pointer
    private void drawPointer(Canvas canvas) {
        // The actual pointer position
        Point position = this.getProgressArc()
                .getPointFromAngle(this.getProgressArc().getAngleDraw());
        // Draw the circle and the halo
        canvas.drawCircle(position.x, position.y, this.mPointerRadius, this.mPointerPaint);
        canvas.drawCircle(position.x, position.y, this.mPointerRadius + this.mHaloSize / 2, this.mHaloPaint);
    }


    /**
     * Overrides
     */

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Change pointer paint alpha by pressed status
        this.mPointerPaint.setAlpha(this.mArcPressed ? ScSeekBar.DEFAULT_HALO_ALPHA : 255);
        this.mHaloPaint.setAlpha(this.mArcPressed ? 255 : ScSeekBar.DEFAULT_HALO_ALPHA);

        // Check the listener
        if (this.mOnDrawListener != null) {
            // Call the on before draw methods with the all parameters updated respect the base
            // class ScGauge.
            this.mOnDrawListener.onBeforeDraw(
                    this.getBaseArc().getPainter(),
                    this.getNotchsArc().getPainter(),
                    this.getProgressArc().getPainter(),
                    this.mPointerPaint,
                    this.mHaloPaint,
                    this.mArcPressed
            );
        }

        // Draw all
        super.onDraw(canvas);
        this.drawPointer(canvas);
    }

    // On touch management
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Store the touch position
        float x = event.getX();
        float y = event.getY();

        // Get the angle from touch position and check if the point belong to the arc.
        // Note that the touch precision level is defined by the size of the pointer draw on the
        // the component.
        float angle = this.getBaseArc().getAngleFromPoint(x, y);
        boolean belong = this.getBaseArc().belongsToArc(x, y, this.getPointerSize());

        // Select case by action type
        switch (event.getAction()) {
            // Press
            case MotionEvent.ACTION_DOWN:
                // If the point belong to the arc set the current value and the pressed trigger.
                // The redraw will called inside the setValue method.
                if (belong) {
                    this.mArcPressed = true;
                    this.setValue(angle);
                }
                break;

            // Release
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Trigger is released and refresh the component.
                this.mArcPressed = false;
                this.invalidate();
                break;

            // Move
            case MotionEvent.ACTION_MOVE:
                // If the point belong to the arc and the trigger is pressed set the current value.
                // The component redraw will called inside the setValue method.
                if (belong && this.mArcPressed) {
                    this.setValue(angle);
                }
                break;
        }

        // Event propagation.
        // If return false this method will capture only the press event and bypass all
        // the successive events.
        return true;
    }

    // Progress value in degrees
    @SuppressWarnings("unused")
    @Override
    public void setValue(float degrees) {

        // Check for snap to notchs the new degrees value
        if (this.mSnapToNotchs) {
            // Round at the closed notchs value
            degrees = this.snapToNotchs(degrees);
        }
        // Call the super
        super.setValue(degrees);
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
        state.putFloat("mPointerRadius", this.mPointerRadius);
        state.putInt("mPointerColor", this.mPointerColor);
        state.putFloat("mHaloSize", this.mHaloSize);
        state.putBoolean("mSnapToNotchs", this.mSnapToNotchs);

        return state;
    }

    // Restore
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        this.mPointerRadius = savedState.getFloat("mPointerRadius");
        this.mPointerColor = savedState.getInt("mPointerColor");
        this.mHaloSize = savedState.getFloat("mHaloSize");
        this.mSnapToNotchs = savedState.getBoolean("mSnapToNotchs");
    }


    /**
     * Public properties
     */

    // Halo size
    @SuppressWarnings("unused")
    public float getHaloSize() {
        return this.mHaloSize;
    }

    @SuppressWarnings("unused")
    public void setHaloSize(float value) {
        this.mHaloSize = value;
        this.checkValues();
        this.mHaloPaint.setStrokeWidth(this.mHaloSize);
        this.requestLayout();
    }

    // Pointer Radius
    @SuppressWarnings("unused")
    public float getPointerRadius() {
        return this.mPointerRadius;
    }

    @SuppressWarnings("unused")
    public void setPointerRadius(float value) {
        this.mPointerRadius = value;
        this.checkValues();
        this.requestLayout();
    }

    // Pointer color
    @SuppressWarnings("unused")
    public int getPointerColor() {
        return this.mPointerColor;
    }

    @SuppressWarnings("unused")
    public void setPointerColor(int color) {
        this.mPointerColor = color;
        this.mPointerPaint.setColor(this.mPointerColor);
        this.mHaloPaint.setColor(this.mPointerColor);
        this.requestLayout();
    }

    // Snap the values to the notchs
    @SuppressWarnings("unused")
    public boolean getSnapToNotchs() {
        return this.mSnapToNotchs;
    }

    @SuppressWarnings("unused")
    public void setSnapToNotchs(boolean color) {
        // Fix the trigger
        this.mSnapToNotchs = color;
        // Recall the set value method for apply the new setting
        this.setValue(this.getValue());
    }


    /**
     * Public listener and interface
     */

    // Draw
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        void onBeforeDraw(
                Paint baseArc, Paint notchsArc, Paint progressArc,
                Paint pointer, Paint pointerHalo, boolean pressed
        );

        void onDrawNotch(ScNotchs.NotchInfo info);

    }

    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}