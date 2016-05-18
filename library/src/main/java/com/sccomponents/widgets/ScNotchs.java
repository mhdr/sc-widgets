package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

/**
 *
 * Create a series of notchs that follow an arc path
 * v1.0
 *
 * */
class ScNotchs extends ScArc {

    /**
     * Private attributes
     */

    private int mNotchsCount;
    private float mNotchsLength;


    /**
     * Private variables
     */

    private OnDrawListener mOnDrawListener = null;


    /**
     * Constructors
     */

    public ScNotchs(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScNotchs(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScNotchs(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    /**
     * Privates methods
     */

    // Check the values limits
    private void checkValues() {
        // Notchs
        if (this.mNotchsCount < 0) this.mNotchsCount = 0;
        if (this.mNotchsLength < 0) this.mNotchsLength = 0;
    }

    // Init the component.
    // Retrieve all attributes with the default values if needed and create the internal using
    // objects.
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        this.mNotchsCount = attrArray.getInt(
                R.styleable.ScComponents_scc_notchs, 0);
        this.mNotchsLength = attrArray.getDimension(
                R.styleable.ScComponents_scc_notchs_length, this.getStrokeSize() * 2);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();
    }


    /**
     * Overrides
     */

    // Draw the notchs on the canvas
    @Override
    protected void internalDraw(Canvas canvas, RectF area) {
        // Draw only if the notch length and count is more of zero.
        if (this.mNotchsLength <= 0 || this.mNotchsCount <= 0) return;

        // Calc the delta angle and the middle stroke
        float deltaAngle = this.getAngleSweep() / this.mNotchsCount;
        float middleStroke = this.getStrokeSize() / 2;

        // Cycle all notchs
        for (int index = 0; index < this.mNotchsCount + 1; index++) {
            // Find current the angle and length
            float currentAngle = index * deltaAngle;
            float length = this.mNotchsLength;

            // If the current angle is outside the draw limit stop to draw
            if ((deltaAngle < 0 && currentAngle < this.getAngleDraw()) ||
                    (deltaAngle > 0 && currentAngle > this.getAngleDraw())) break;

            // Event
            if (this.mOnDrawListener != null) {
                length = this.mOnDrawListener.onDrawNotch(this.getPainter(), currentAngle, index);
            }

            // Find the start and the end points on the canvas in reference to the arc
            // TODO: when scaled have notchs direction issue
            RectF startArea = ScNotchs.inflateRect(area, length - middleStroke);
            Point startPoint = this.getPointFromAngle(currentAngle, startArea);

            RectF endArea = ScNotchs.inflateRect(area, -middleStroke);
            Point endPoint = this.getPointFromAngle(currentAngle, endArea);

            // Draw the line
            canvas.drawLine(
                    startPoint.x, startPoint.y,
                    endPoint.x, endPoint.y,
                    this.getPainter()
            );
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
        state.putInt("mNotchsCount", this.mNotchsCount);
        state.putFloat("mNotchsLength", this.mNotchsLength);

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
        this.mNotchsCount = savedState.getInt("mNotchsCount");
        this.mNotchsLength = savedState.getFloat("mNotchsLength");
    }


    /**
     * Public properties
     */

    // Notchs count
    @SuppressWarnings("unused")
    public int getNotchs() {
        return this.mNotchsCount;
    }

    @SuppressWarnings("unused")
    public void setNotchs(int value) {
        // Check if value is changed
        if (this.mNotchsCount != value) {
            // Store the new value
            this.mNotchsCount = value;
            // Check and refresh the component
            this.checkValues();
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
            // Store the new value
            this.mNotchsLength = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }


    /**
     * Public listener and interface
     */

    // Before draw
    @SuppressWarnings("unused")
    public interface OnDrawListener {
        // Return the new length of notch
        float onDrawNotch(Paint painter, float angle, int count);
    }

    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}
