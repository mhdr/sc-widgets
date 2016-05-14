package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Create an arc.
 * v2.0
 */
public class ScArc extends View {

    /**
     * Private attributes
     */

    private float mAngleStart = 0.0f;
    private float mAngleSweep = 360.0f;
    private float mAngleDraw = 360.0f;

    private float mStrokeSize = 0.0f;
    private int mStrokeColor = Color.BLACK;

    private int mMaxWidth = 0;
    private int mMaxHeight = 0;

    private AreaFilling mAreaFilling = AreaFilling.BOTH;


    /**
     * Private variables
     */

    private RectF mTrimmedArea = null;
    private Paint mStrokePaint = null;

    private OnArcEventListener mOnArcEventListener = null;


    /**
     * Constructors
     */

    public ScArc(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScArc(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScArc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    /**
     * Privates methods
     */

    // Limit number within a range
    private float valueRangeLimit(float value, float startValue, float endValue) {
        // If is over the limit return the normalized value
        if (value < Math.min(startValue, endValue)) return Math.min(startValue, endValue);
        if (value > Math.max(startValue, endValue)) return Math.max(startValue, endValue);
        // Else return the original value
        return value;
    }

    // Check all input values if over the limits
    private void checkValues() {
        // Size
        if (this.mStrokeSize < 0.0f) this.mStrokeSize = 0.0f;

        // Angle
        if (Math.abs(this.mAngleSweep) > 360.0f) this.mAngleSweep = this.mAngleSweep % 360.0f;

        // Dimension
        if (this.mMaxWidth < 0) this.mMaxWidth = 0;
        if (this.mMaxHeight < 0) this.mMaxHeight = 0;

        // Check the draw angle limits
        this.mAngleDraw = this.valueRangeLimit(this.mAngleDraw, 0, this.mAngleSweep);
    }

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

    // Init the component.
    // Retrieve all attributes with the default values if needed.
    // Check the values for internal use.
    // Create the painter and enable the touch event response.
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mAngleStart = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_start, 0.0f);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, 360.0f);
        this.mAngleDraw = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_draw, 360.0f);

        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dpToPixel(context, 3.0f));
        this.mStrokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, Color.BLACK);

        this.mMaxWidth = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_width, 0);
        this.mMaxHeight = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_height, 0);

        this.mAreaFilling =
                AreaFilling.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_area, 1)];

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();

        //--------------------------------------------------
        // PAINTS

        this.mStrokePaint = new Paint();
        this.mStrokePaint.setColor(this.mStrokeColor);
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
        this.mStrokePaint.setStyle(Paint.Style.STROKE);
        this.mStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        this.mStrokePaint.setStrokeCap(Paint.Cap.BUTT);

        //--------------------------------------------------
        // EVENTS

        // Enable for touch
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
    }

    // Check if point is inside a circle (Pitagora)
    @SuppressWarnings("all")
    private boolean pointInsideCircle(float x, float y, float radius) {
        return Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(radius, 2);
    }

    // Check if user pressed on the arc
    private boolean pressedOnArc(float angle, float x, float y) {
        // Find the point on arc from angle
        Point pointOnArc = this.getPointFromAngle(angle);

        // Find the distance between the points and check it
        return this.pointInsideCircle(
                x - pointOnArc.x,
                y - pointOnArc.y,
                this.mStrokeSize
        );
    }


    /**
     * Area methods
     */

    // Calc the trimmed area.
    // This is only an image of the arc dimensions inside the space, not contains the real arc
    // dimensions but only a proportional representation.
    // This method essentially hold the left/top padding and the arc width/height.
    private RectF calcTrimmedArea() {
        // Check for sweep angle.
        // If 0 return and empty rectangle
        if (this.mAngleSweep == 0.0f) return new RectF();

        // Init the area rectangle with the inverted values that will be replaced with the real
        // values.
        RectF area = new RectF(1.0f, 1.0f, -1.0f, -1.0f);

        // Calc the start and end angles in radians.
        double startAngle = Math.toRadians(this.mAngleStart);
        double endAngle = startAngle + Math.toRadians(this.mAngleSweep);

        // Sort the angles to find the min and the max
        double minAngle = startAngle < endAngle ? startAngle : endAngle;
        double maxAngle = startAngle > endAngle ? startAngle : endAngle;

        // Cycle all angles and compare the found sin and cos values for find the bounds of the
        // area.
        while (minAngle <= maxAngle) {
            // Convert the current angle in radiant and find the sin and cos values
            float sin = (float) Math.sin(minAngle);
            float cos = (float) Math.cos(minAngle);

            // Check the the precedents limits and update they if needed
            if (cos < area.left) area.left = cos;
            if (cos > area.right) area.right = cos;

            if (sin < area.top) area.top = sin;
            if (sin > area.bottom) area.bottom = sin;

            // Increment the current angle
            minAngle += 0.01;
        }

        // Return the area.
        // Inside this could have an image of the trimmed area used to draw this arc.
        return area;
    }

    // Calc complete circle drawing area.
    // This methods calc the virtual drawing area not taking into consideration the many adjustments
    // like the stroke size or the area padding.
    private RectF calcVirtualArea(int width, int height) {
        // Check for empty values
        if (this.mTrimmedArea == null || this.mTrimmedArea.isEmpty()) return new RectF();

        // Default working area
        RectF area = new RectF(0, 0, width, height);

        // Layout wrapping
        boolean hWrap = this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean vWrap = this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        // If fill the area expand the area to have the full filling working space with the arc.
        // In the wrapping case the horizontal filling it is executed in anyway while the component
        // dimension will be elaborated before inside the component measuring.
        if (hWrap ||
                this.mAreaFilling == AreaFilling.BOTH || this.mAreaFilling == AreaFilling.HORIZONTAL) {
            // Find the multiplier based on the trimmed area and apply the proportion to the
            // horizontal dimensions.
            float hMultiplier = width / this.mTrimmedArea.width();
            float left = this.mTrimmedArea.left * hMultiplier;

            // Apply the new values to the area and modify the horizontal offset
            area.left = -hMultiplier - left;
            area.right = hMultiplier - left;
        }

        // If fill the area expand the area to have the full filling working space with the arc
        // In the wrapping case the vertical filling it is executed in anyway while the component
        // dimension will be elaborated before inside the component measuring.
        if (vWrap ||
                this.mAreaFilling == AreaFilling.BOTH || this.mAreaFilling == AreaFilling.VERTICAL) {
            // Find the multiplier based on the trimmed area and apply the proportion to the
            // vertical dimensions.
            float vMultiplier = height / this.mTrimmedArea.height();
            float top = this.mTrimmedArea.top * vMultiplier;

            // Apply the new values to the area and modify the vertical offset
            area.top = -vMultiplier - top;
            area.bottom = vMultiplier - top;
        }

        // Return the calc area
        return area;
    }

    // Calc the real drawing area.
    // This methods can be use to find the real area for drawing the arc.
    private RectF calcDrawingArea(int width, int height) {
        // Get the virtual area and the half stroke size
        RectF area = this.calcVirtualArea(width, height);
        float halfStroke = this.mStrokeSize / 2;

        // Check for area empty values
        if (!area.isEmpty()) {
            // Adjust the padding and the stroke size
            area.left += this.getPaddingLeft() + halfStroke;
            area.top += this.getPaddingTop() + halfStroke;
            area.right -= this.getPaddingRight() + halfStroke;
            area.bottom -= this.getPaddingBottom() + halfStroke;
        }
        // Return the area
        return area;
    }


    /**
     * Draw methods
     */

    // Draw arc on the canvas.
    // In this method will be calc the drawing area for reuse it in other methods.
    private void drawArc(Canvas canvas) {
        // Calc the drawing area
        RectF drawingArea = this.calcDrawingArea(canvas.getWidth(), canvas.getHeight());
        // Check for area empty value
        if (!drawingArea.isEmpty()) {
            // Draw the arc
            canvas.drawArc(
                    drawingArea,
                    this.mAngleStart,
                    this.mAngleDraw,
                    false,
                    this.mStrokePaint);
        }
    }


    /**
     * Overrides
     */

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw an arc
        this.drawArc(canvas);
    }

    // On measure
    @Override
    @SuppressWarnings("all")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // Layout wrapping
        boolean hWrap = this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean vWrap = this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        // If have some wrap content create a perfected square before trim it.
        if (hWrap) width = height;
        if (vWrap) height = width;

        // Check the dimensions limits
        if (this.mMaxWidth > 0 && width > this.mMaxWidth) width = this.mMaxWidth;
        if (this.mMaxHeight > 0 && height > this.mMaxHeight) height = this.mMaxHeight;

        // Calc the areas
        this.mTrimmedArea = this.calcTrimmedArea();

        // Check for wrapping the dimensions
        if (hWrap) width = (int) (width * this.mTrimmedArea.width() / 2);
        if (vWrap) height = (int) (height * this.mTrimmedArea.height() / 2);

        // Set dimensions
        this.setMeasuredDimension(width, height);
    }

    // Check the touch
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Calc the layout
        this.requestLayout();

        // Store the touch position
        float x = event.getX();
        float y = event.getY();

        // Check if press on arc
        if (this.mOnArcEventListener != null) {
            // Get the angle from touch position
            float angle = this.getAngleFromPoint(x, y);

            // Check if the pressure is on the arc
            if (this.pressedOnArc(angle, x, y)) {
                // Select action
                switch (event.getAction()) {
                    // Press
                    case MotionEvent.ACTION_DOWN:
                        this.mOnArcEventListener.onPress(angle);
                        break;

                    // Release
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        this.mOnArcEventListener.onRelease();
                        break;

                    // Move
                    case MotionEvent.ACTION_MOVE:
                        this.mOnArcEventListener.onSlide(angle);
                        break;
                }

                // Block event propagation
                return true;
            }
        }

        // Not block the event propagation
        return false;
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
        state.putInt("mMaxWidth", this.mMaxWidth);
        state.putInt("mMaxHeight", this.mMaxHeight);
        state.putInt("mAreaFilling", this.mAreaFilling.ordinal());

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
        this.mMaxWidth = savedState.getInt("mMaxWidth");
        this.mMaxHeight = savedState.getInt("mMaxHeight");
        this.mAreaFilling = AreaFilling.values()[savedState.getInt("mAreaFilling")];
    }


    /**
     * Public methods
     */

    // The area filling types
    @SuppressWarnings("unused")
    public enum AreaFilling {
        NONE,
        BOTH,
        HORIZONTAL,
        VERTICAL
    }

    // Calc point position from angle in degrees.
    // Given an angle this method calc the relative point on the arc.
    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees) {
        return this.getPointFromAngle(degrees, 0.0f);
    }

    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees, float radiusAdjust) {
        // Get the drawing area
        RectF area = this.calcDrawingArea(this.getMeasuredWidth(), this.getMeasuredHeight());

        // Find the default arc radius
        float xRadius = area.width() / 2  + radiusAdjust;
        float yRadius = area.height() / 2 +  + radiusAdjust;

        // Convert the radius in radiant and find the coordinates in the space
        double rad = Math.toRadians(degrees);
        int x = Math.round(xRadius * (float) Math.cos(rad) + area.centerX());
        int y = Math.round(yRadius * (float) Math.sin(rad) + area.centerY());

        // Create the point and return it
        return new Point(x, y);
    }

    // Find the angle from position on screen.
    @SuppressWarnings("unused")
    public float getAngleFromPoint(float x, float y) {
        // Get the drawing area
        RectF area = this.calcDrawingArea(this.getMeasuredWidth(), this.getMeasuredHeight());

        // Get angle from position
        double angle = Math.atan2(
                (y - area.centerY()) / area.height(),
                (x - area.centerX()) / area.width()
        );
        // Convert to degree and fix over number
        return ((float) Math.toDegrees(angle) + 360.0f) % 360.0f;
    }

    // Get the arc painter
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mStrokePaint;
    }


    /**
     * Public properties
     */

    // Start angle
    @SuppressWarnings("unused")
    public float getAngleStart() {
        return this.mAngleStart;
    }

    @SuppressWarnings("unused")
    public void setAngleStart(float value) {
        // Check if value is changed
        if (this.mAngleStart != value) {
            // Store the new value
            this.mAngleStart = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    // Sweep angle
    @SuppressWarnings("unused")
    public float getAngleSweep() {
        return this.mAngleSweep;
    }

    @SuppressWarnings("unused")
    public void setAngleSweep(float value) {
        // Check if value is changed
        if (this.mAngleSweep != value) {
            // Store the new value
            this.mAngleDraw = this.mAngleSweep == this.mAngleDraw ? value : this.mAngleDraw;
            this.mAngleSweep = value;
            // Check and refresh
            this.checkValues();
            this.requestLayout();
        }
    }

    // Draw angle
    @SuppressWarnings("unused")
    public float getAngleDraw() {
        return this.mAngleDraw;
    }

    @SuppressWarnings("unused")
    public void setAngleDraw(float value) {
        // Check if value is changed
        if (this.mAngleDraw != value) {
            // Store the new value
            this.mAngleDraw = value;
            // Check and refresh
            this.checkValues();
            this.invalidate();
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
            // Store the new value and check it
            this.mStrokeSize = value;
            this.checkValues();
            // Fix the painter and refresh the component
            this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
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
            // Fix the painter and refresh the component
            this.mStrokePaint.setColor(this.mStrokeColor);
            this.invalidate();
        }
    }

    // Max width
    @SuppressWarnings("unused")
    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    @SuppressWarnings("unused")
    public void setMaxWidth(int value) {
        // Check if value is changed
        if (this.mMaxWidth != value) {
            // Store the new value
            this.mMaxWidth = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    // Max height
    @SuppressWarnings("unused")
    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    @SuppressWarnings("unused")
    public void setMaxHeight(int value) {
        // Check if value is changed
        if (this.mMaxHeight != value) {
            // Store the new value
            this.mMaxHeight = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    // Area filling
    @SuppressWarnings("unused")
    public AreaFilling getAreaFilling() {
        return this.mAreaFilling;
    }

    @SuppressWarnings("unused")
    public void setAreaFilling(AreaFilling value) {
        // Check if value is changed
        if (this.mAreaFilling != value) {
            // Store the new value and refresh the component
            this.mAreaFilling = value;
            this.invalidate();
        }
    }


    /**
     * Public listener
     */

    // Events on arc
    public interface OnArcEventListener {

        void onPress(double angle);

        void onRelease();

        void onSlide(double angle);

    }

    @SuppressWarnings("unused")
    public void setOnArcEventListener(OnArcEventListener listener) {
        this.mOnArcEventListener = listener;
    }

}