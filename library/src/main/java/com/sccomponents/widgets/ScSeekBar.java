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
 * Apply to the gauge an input by touching the arc.
 * v1.0.1
 */
public class ScSeekBar extends ScGauge {

    /**
     * Constants
     */

    public static final float DEFAULT_POINTER_RADIUS = 0.0f;
    public static final int DEFAULT_POINTER_COLOR = Color.GRAY;

    public static final float DEFAULT_HALO_SIZE = 5.0f;
    public static final int DEFAULT_HALO_ALPHA = 128;


    /**
     * Private attributes
     */

    protected float mPointerRadius;
    protected int mPointerColor;
    protected float mHaloSize;


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

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.mArcPressed = false;
        this.checkValues();

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
        // Check for constraints
        if (this.mPointerRadius <= 0) return;
        if (this.mSnapToNotchs && this.mNotchsCount == 0) return ;

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

        // Draw the component from ScGauge
        super.onDraw(canvas);

        // Draw the pointer if needed
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