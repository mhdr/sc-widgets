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

    private int mStartAngle = 0;
    private int mSweepAngle = 360;

    private int mStrokeSize = 0;
    private int mStrokeColor = Color.BLACK;

    private int mMaxWidth = 0;
    private int mMaxHeight = 0;

    private AreaFilling mAreaFilling = AreaFilling.BOTH;


    /**
     * Private variables
     */

    private RectF mTrimmedArea = null;
    private RectF mDrawingArea = null;
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

    // Check all input values if over the limits
    protected void checkValues() {
        // Size
        if (this.mStrokeSize < 0) this.mStrokeSize = 0;

        // Angle
        if (Math.abs(this.mSweepAngle) > 360) this.mSweepAngle = this.mSweepAngle % 360;

        // Dimension
        if (this.mMaxWidth < 0) this.mMaxWidth = 0;
        if (this.mMaxHeight < 0) this.mMaxHeight = 0;
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
    private int dpToPixel(Context context, int dp) {
        // Get the display metrics
        DisplayMetrics metrics = this.getDisplayMetrics(context);
        // Calc the conversion by the screen density
        return (int) (dp * metrics.density);
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
        this.mStartAngle = attrArray.getInt(R.styleable.ScComponents_scc_start_angle, 0);
        this.mSweepAngle = attrArray.getInt(R.styleable.ScComponents_scc_sweep_angle, 360);

        this.mStrokeSize = attrArray.getDimensionPixelSize(R.styleable.ScComponents_scc_stroke_size, this.dpToPixel(context, 3));
        this.mStrokeColor = attrArray.getColor(R.styleable.ScComponents_scc_stroke_color, Color.BLACK);

        this.mMaxWidth = attrArray.getDimensionPixelSize(R.styleable.ScComponents_scc_max_width, 0);
        this.mMaxHeight = attrArray.getDimensionPixelSize(R.styleable.ScComponents_scc_max_height, 0);

        this.mAreaFilling = AreaFilling.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_area, 1)];

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
        this.mStrokePaint.setStrokeCap(Paint.Cap.ROUND);

        //--------------------------------------------------
        // EVENTS

        // Enable for touch
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
    }

    // Calc point position from angle.
    // Gived an angle this method calc the relative point on the arc.
    private Point getPositionFromAngle(float angle) {
        // Find the arc radius
        float xRadius = this.mDrawingArea.width() / 2;
        float yRadius = this.mDrawingArea.height() / 2;

        // Convert the radius in radiant and find the coordinates in the space
        double rad = Math.toRadians(angle);
        int x = Math.round(xRadius * (float) Math.cos(rad));
        int y = Math.round(yRadius * (float) Math.sin(rad));

        // Create the point and return it
        return new Point(x, y);
    }

    // Find the angle from position on screen.
    private double getAngleFromPosition(float x, float y) {
        // Get angle from position
        double angle = Math.atan2(
                (y - this.mDrawingArea.centerY()) / this.mDrawingArea.height(),
                (x - this.mDrawingArea.centerX()) / this.mDrawingArea.width()
        );
        // Convert to degree and fix over number
        return (Math.toDegrees(angle) + 360) % 360;
    }

    // Check if point is inside a circle (Pitagora)
    @SuppressWarnings("all")
    private boolean pointInsideCircle(float x, float y, float radius) {
        return Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(radius, 2);
    }

    // Check if user pressed on the arc
    private boolean mouseOnArc(double angle, float x, float y) {
        // Find the point on arc from angle
        Point pointOnArc = this.getPositionFromAngle((float) angle);

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
        if (this.mSweepAngle == 0) return new RectF();

        // Init the area rectangle with the inverted values that will be replaced with the real
        // values.
        RectF area = new RectF(1, 1, 0, 0);

        // Calc the start and end angles in degrees.
        // We still use the degrees for cycle all the angles because the range could be positive
        // or negative and using the double values we could be have some comparing issue for
        // determinate when the cycle is finished.
        int endAngle = this.mStartAngle + this.mSweepAngle;
        int currAngle = this.mStartAngle;

        // Cycle all angles and compare the found sin and cos values for find the bounds of the
        // area.
        while (currAngle != endAngle) {
            // Convert the current angle in radiant and find the sin and cos values
            double radAngle = Math.toRadians(currAngle);
            float sin = (float) Math.sin(radAngle);
            float cos = (float) Math.cos(radAngle);

            // Check the the precedents limits and update they if needed
            if (cos < area.left) area.left = cos;
            if (cos > area.right) area.right = cos;

            if (sin < area.top) area.top = sin;
            if (sin > area.bottom) area.bottom = sin;

            // Increment angle by the sweep angle sign
            currAngle += this.mSweepAngle > 0 ? 1 : -1;
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
    // NB: In this methods will be calc the drawing area for reuse it in other methods.
    private void drawArc(Canvas canvas) {
        // Calc the drawing area
        this.mDrawingArea = this.calcDrawingArea(canvas.getWidth(), canvas.getHeight());
        // Check for area empty value
        if (!this.mDrawingArea.isEmpty()) {
            // Draw the arc
            canvas.drawArc(
                    this.mDrawingArea,
                    this.mStartAngle,
                    this.mSweepAngle,
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
            double angle = this.getAngleFromPosition(x, y);

            // Check if the pressure is on the arc
            if (this.mouseOnArc(angle, x, y)) {
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
        state.putInt("mStartAngle", this.mStartAngle);
        state.putInt("mSweepAngle", this.mSweepAngle);
        state.putInt("mStrokeSize", this.mStrokeSize);
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
        this.mStartAngle = savedState.getInt("mStartAngle");
        this.mSweepAngle = savedState.getInt("mSweepAngle");
        this.mStrokeSize = savedState.getInt("mStrokeSize");
        this.mStrokeColor = savedState.getInt("mStrokeColor");
        this.mMaxWidth = savedState.getInt("mMaxWidth");
        this.mMaxHeight = savedState.getInt("mMaxHeight");
        this.mAreaFilling = AreaFilling.values()[savedState.getInt("mAreaFilling")];
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


    /**
     * Public methods
     */

    // Get the arc painter
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mStrokePaint;
    }


    /**
     * Public enum
     */

    // The area filling types
    @SuppressWarnings("unused")
    public enum AreaFilling {
        NONE,
        BOTH,
        HORIZONTAL,
        VERTICAL
    }


    /**
     * Public properties
     */

    // Start angle
    @SuppressWarnings("unused")
    public int getStartAngle() {
        return this.mStartAngle;
    }

    @SuppressWarnings("unused")
    public void setStartAngle(int value) {
        this.mStartAngle = value;
        this.checkValues();
        this.requestLayout();
    }

    // Sweep angle
    @SuppressWarnings("unused")
    public int getSweepAngle() {
        return this.mSweepAngle;
    }

    @SuppressWarnings("unused")
    public void setSweepAngle(int value) {
        this.mSweepAngle = value;
        this.checkValues();
        this.requestLayout();
    }

    // Stroke size
    @SuppressWarnings("unused")
    public int getStrokeSize() {
        return this.mStrokeSize;
    }

    @SuppressWarnings("unused")
    public void setStrokeSize(int value) {
        this.mStrokeSize = value;
        this.checkValues();

        this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
        this.invalidate();
    }

    // Stroke color
    @SuppressWarnings("unused")
    public int getStrokeColor() {
        return this.mStrokeColor;
    }

    @SuppressWarnings("unused")
    public void setStrokeColor(int color) {
        this.mStrokeColor = color;
        this.mStrokePaint.setColor(this.mStrokeColor);
        this.invalidate();
    }

    // Max width
    @SuppressWarnings("unused")
    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    @SuppressWarnings("unused")
    public void setMaxWidth(int value) {
        this.mMaxWidth = value;
        this.checkValues();
        this.requestLayout();
    }

    // Max height
    @SuppressWarnings("unused")
    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    @SuppressWarnings("unused")
    public void setMaxHeight(int value) {
        this.mMaxHeight = value;
        this.checkValues();
        this.requestLayout();
    }

    // Area filling
    @SuppressWarnings("unused")
    public AreaFilling getAreaFilling() {
        return this.mAreaFilling;
    }

    @SuppressWarnings("unused")
    public void setAreaFilling(AreaFilling value) {
        this.mAreaFilling = value;
        this.invalidate();
    }

}