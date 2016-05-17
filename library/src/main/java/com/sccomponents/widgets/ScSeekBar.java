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
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;


/**
 * ScCircularSeekBar
 */
public class ScSeekBar extends ScGauge {

    /**
     * Constants
     */

    private static final float POINTER_RADIUS = 10.0f;
    private static final int POINTER_COLOR = Color.GRAY;

    private static final float HALO_SIZE = 5.0f;
    private static final int HALO_ALPHA = 128;


    /**
     * Private attributes
     */

    private float mPointerRadius;
    private int mPointerColor;

    private float mHaloSize;


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

    // Init the component
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScCirclularSeekBar, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mPointerRadius = attrArray.getDimension(
                R.styleable.ScCirclularSeekBar_sccsb_pointer_radius, this.dpToPixel(context, ScSeekBar.POINTER_RADIUS));
        this.mPointerColor = attrArray.getColor(
                R.styleable.ScCirclularSeekBar_sccsb_pointer_color, ScSeekBar.POINTER_COLOR);

        this.mHaloSize = attrArray.getDimension(
                R.styleable.ScCirclularSeekBar_sccsb_halo_size, this.dpToPixel(context, ScSeekBar.HALO_SIZE));

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

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
        this.mHaloPaint.setAlpha(ScSeekBar.HALO_ALPHA);

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
            public float onDrawNotch(Paint painter, float angle, int count) {
                // Check if have an listener instanced
                if (ScSeekBar.this.mOnDrawListener != null) {
                    // Propagate the methods
                    return ScSeekBar.this.mOnDrawListener.onDrawNotch(painter, angle, count);
                }
                // Else return the default value retrieved from the notch object
                return ScSeekBar.this.getNotchsArc().getNotchsLength();
            }
        });
    }

    // Find the maximum stroke size.
    // This method is protected because will be used in the inherited class for reposition
    // the arcs in the space seen this methods is used inside the method to find the components
    // padding.
    @Override
    protected float findMaxStrokeSize() {
        // Get the super max and the pointer global size
        float maxSize = super.findMaxStrokeSize();
        float pointerSize = (this.mPointerRadius + this.mHaloSize) * 2;
        // Compare with the pointer size
        return maxSize > pointerSize ? maxSize : pointerSize;
    }


    /**
     * Draw methods
     */

    // Draw pointer
    private void drawPointer(Canvas canvas) {
        // The actual pointer position
        Point position = this.getProgressArc().getPointFromAngle(this.getProgressArc().getAngleDraw());
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
        // Change paint alpha by drag or not
        if (this.mArcPressed) {
            this.mPointerPaint.setAlpha(ScSeekBar.HALO_ALPHA);
            this.mHaloPaint.setAlpha(255);

        } else {
            this.mPointerPaint.setAlpha(255);
            this.mHaloPaint.setAlpha(ScSeekBar.HALO_ALPHA);
        }

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

        // Get the angle from touch position
        float angle = this.getBaseArc().getAngleFromPoint(x, y);

        // Select case by action
        switch (event.getAction()) {
            // Press
            case MotionEvent.ACTION_DOWN:
                // Trigger is pressed and set the current value only if the pressure happened
                // on the arc space.
                // The redraw will called inside the setValue method.
                if (this.getBaseArc().belongsToArc(x, y)) {
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
                // If the trigger is pressed set the current value.
                // The redraw will called inside the setValue method.
                if (this.mArcPressed) {
                    this.setValue(angle);
                }
                break;
        }

        // Event propagation
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
     * Public methods
     */

    // Convert Dip to Pixel
    public float dpToPixel(Context context, float dp) {
        // Get the display metrics
        DisplayMetrics metrics = this.getDisplayMetrics(context);
        // Calc the conversion by the screen density
        return dp * metrics.density;
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

    float onDrawNotch(Paint painter, float angle, int count);

}

    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}