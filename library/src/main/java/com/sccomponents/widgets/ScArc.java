package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
 * v1.0
 */
public class ScArc extends View {

    /**
     * Private attributes
     */

    private int mStartAngle = 0;
    private int mSweepAngle = 360;

    private int mStrokeSize = 0;
    private int mStrokeColor = Color.BLACK;
    private Stroke mStrokePosition = Stroke.INSIDE;

    private int mMaxWidth = 0;
    private int mMaxHeight = 0;


    /**
     * Private variables
     */

    private Rect mDrawArea = null;
    private Paint mPaint = null;

    private OnCustomPaintListener mOnCustomPaintListener = null;
    private OnArcEventListener mOnArcEventListener = null;


    /**
     * Constructors
     */

    public ScArc(Context context) {
        super(context);
        this.init(context, null, 0);
        this.createPaints();
    }

    public ScArc(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
        this.createPaints();
    }

    public ScArc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        this.createPaints();
    }


    /**
     * Privates methods
     */

    // Check all input values
    private void checkValues() {
        // Size
        if (this.mStrokeSize < 0) this.mStrokeSize = 0;

        // Angle
        if (this.mSweepAngle > 360) this.mSweepAngle = this.mSweepAngle % 360;

        // Dimension
        if (this.mMaxWidth < 0) this.mMaxWidth = 0;
        if (this.mMaxHeight < 0) this.mMaxHeight = 0;
    }

    // Get the display metric
    private DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    // Convert Dip to Pixel
    public int dpToPixel(Context context, int dp) {
        DisplayMetrics metrics = this.getDisplayMetrics(context);
        return (int) (dp * metrics.density);
    }

    // Init the component
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScArc, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mStartAngle = attrArray.getInt(R.styleable.ScArc_sca_start_angle, 0);
        this.mSweepAngle = attrArray.getInt(R.styleable.ScArc_sca_sweep_angle, 360);

        this.mStrokeSize = attrArray.getDimensionPixelSize(R.styleable.ScArc_sca_stroke_size, this.dpToPixel(context, 3));
        this.mStrokeColor = attrArray.getColor(R.styleable.ScArc_sca_stroke_color, Color.BLACK);
        this.mStrokePosition = Stroke.values()[attrArray.getInt(R.styleable.ScArc_sca_stroke_position, 0)];

        this.mMaxWidth = attrArray.getDimensionPixelSize(R.styleable.ScArc_sca_max_width, 0);
        this.mMaxHeight = attrArray.getDimensionPixelSize(R.styleable.ScArc_sca_max_height, 0);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();

        //--------------------------------------------------
        // EVENTS

        // Enable for touch
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
    }

    // Create the paints
    private void createPaints() {
        // Create arc paint
        this.mPaint = new Paint();
        this.mPaint.setColor(this.mStrokeColor);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(this.mStrokeSize);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    // Get the stroke increment by the his position
    float getStrokeIncrement() {
        // Find the middle
        double middle = Math.ceil(this.mStrokeSize / 2);

        // Return the right value
        if (this.mStrokePosition == Stroke.INSIDE) return (float) +middle;
        if (this.mStrokePosition == Stroke.OUTSIDE) return (float) -middle;
        return 0;
    }

    // Calc point position from angle and offset
    private Point getPositionFromAngle(float angle) {
        // Degrees to rad
        double rad = Math.toRadians(angle);
        // Find the stroke position increment
        float increment = this.getStrokeIncrement();

        // Find the arc dimensions
        float width = this.mDrawArea.width() / 2 + increment;
        float height = this.mDrawArea.height() / 2 + increment;

        // Find the coordinates in the space
        int x = Math.round(width * (float) Math.cos(rad));
        int y = Math.round(height * (float) Math.sin(rad));

        // Find the drawable area center
        int xCenter = this.mDrawArea.left + this.mDrawArea.width() / 2;
        int yCenter = this.mDrawArea.top + this.mDrawArea.height() / 2;

        // Create the point and fix center
        return new Point(x + xCenter, y + yCenter);
    }

    // Find the angle from position on screen
    private double getAngleFromPosition(float x, float y) {
        // Calc the center of area
        int xCenter = this.mDrawArea.left + this.mDrawArea.width() / 2;
        int yCenter = this.mDrawArea.top + this.mDrawArea.height() / 2;

        // Get angle from position
        double angle = Math.atan2(
                (y - yCenter) / this.mDrawArea.height(),
                (x - xCenter) / this.mDrawArea.width()
        );
        // Convert to degree and fix over number
        return (Math.toDegrees(angle) + 360) % 360;
    }

    // Check if point is inside a circle (Pitagora)
    @SuppressWarnings("all")
    private boolean pointInsideCircle(float x, float y, float radius) {
        return Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(radius, 2);
    }

    // Check if mouse is on arc
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

    // Calc draw area
    @SuppressWarnings("all")
    private Rect calcDrawArea(int width, int height) {
        // Find layout and create a empty area
        ViewGroup.LayoutParams params = getLayoutParams();

        // Check if width and height is wrapped
        if (params.width == ViewGroup.LayoutParams.WRAP_CONTENT &&
                params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Return empty
            return new Rect();
        }

        // If vertical is WRAP_CONTENT use same circle
        if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = width;
        }

        // If horizontal is WRAP_CONTENT use same circle
        if (params.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = height;
        }

        // Check the dimensions limits
        if (this.mMaxWidth > 0 && width > this.mMaxWidth) width = this.mMaxWidth;
        if (this.mMaxHeight > 0 && height > this.mMaxHeight) height = this.mMaxHeight;

        // Create the area rectangle
        return new Rect(
                this.getPaddingLeft(),
                this.getPaddingTop(),
                width - this.getPaddingRight(),
                height - this.getPaddingBottom()
        );
    }

    // Calc the trimmed area
    private Rect calcTrimmedArea() {
        // Check for sweep angle
        if (this.mSweepAngle == 0) return new Rect();

        // Calc the start and end
        int endAngle = this.mStartAngle + this.mSweepAngle;
        int currAngle = this.mStartAngle;

        // Find the first point
        Point startPoint = this.getPositionFromAngle(this.mStartAngle);
        // Init area rectangle
        Rect area = new Rect(startPoint.x, startPoint.y, startPoint.x, startPoint.y);

        // Cycle all angles
        while (currAngle != endAngle) {
            // Calc current point
            Point current = this.getPositionFromAngle(currAngle);

            // Compare and hold the better value is needed
            if (current.x < area.left) area.left = current.x;
            if (current.x > area.right) area.right = current.x;

            if (current.y < area.top) area.top = current.y;
            if (current.y > area.bottom) area.bottom = current.y;

            // Increment angle
            currAngle += this.mSweepAngle > 0 ? 1 : -1;
        }

        // Return the area
        return area;
    }


    /**
     * Draw methods
     */

    // Draw arc
    private void drawArc(Canvas canvas) {
        // Apply the stroke increment to the area
        float increment = this.getStrokeIncrement();
        RectF fixedArea = new RectF(
                this.mDrawArea.left + increment,
                this.mDrawArea.top + increment,
                this.mDrawArea.right - increment,
                this.mDrawArea.bottom - increment
        );

        // Draw the arc
        canvas.drawArc(
                fixedArea,
                this.mStartAngle,
                this.mSweepAngle,
                false,
                this.mPaint
        );
    }


    /**
     * Overrides
     */

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Call custom paint event
        if (this.mOnCustomPaintListener != null) {
            this.mOnCustomPaintListener.onCustomPaint(this, this.mPaint);
        }

            // Draw an arc
            this.drawArc(canvas);
    }

    // On measure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get suggested dimensions
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        // Check the dimensions limits
        if (this.mMaxWidth > 0 && width > this.mMaxWidth) width = this.mMaxWidth;
        if (this.mMaxHeight > 0 && height > this.mMaxHeight) height = this.mMaxHeight;

        // Calc areas
        this.mDrawArea = this.calcDrawArea(width, height);
        Rect trimmedArea = this.calcTrimmedArea();

        // Trim by layout params
        ViewGroup.LayoutParams params = getLayoutParams();

        // Check horizontal wrap content
        if (params.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Adjust draw area offset
            this.mDrawArea.offset(-trimmedArea.left + this.getPaddingLeft(), 0);
            // Fix global width
            width = this.getPaddingLeft() + trimmedArea.width() + this.getPaddingRight();
        }

        // Check vertical wrap content
        if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Adjust draw area dimension and offset
            this.mDrawArea.offset(0, -trimmedArea.top + this.getPaddingTop());
            // Fix global height
            height = this.getPaddingTop() + trimmedArea.height() + this.getPaddingBottom();
        }

        // Set dimensions
        this.setMeasuredDimension(width, height);
    }

    // Check the touch
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Recalc layout
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
            }
        }

        // Block event propagation
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
        state.putInt("mStartAngle", this.mStartAngle);
        state.putInt("mSweepAngle", this.mSweepAngle);
        state.putInt("mStrokeSize", this.mStrokeSize);
        state.putInt("mStrokeColor", this.mStrokeColor);
        state.putInt("mStrokePosition", this.mStrokePosition.ordinal());
        state.putInt("mMaxWidth", this.mMaxWidth);
        state.putInt("mMaxHeight", this.mMaxHeight);

        return state;
    }

    // Restore
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        this.mStartAngle = savedState.getInt("mStartAngle");
        this.mSweepAngle = savedState.getInt("mSweepAngle");
        this.mStrokeSize = savedState.getInt("mStrokeSize");
        this.mStrokeColor = savedState.getInt("mStrokeColor");
        this.mStrokePosition = Stroke.values()[savedState.getInt("mStrokePosition")];
        this.mMaxWidth = savedState.getInt("mMaxWidth");
        this.mMaxHeight = savedState.getInt("mMaxHeight");
    }


    /**
     * Public listener
     */

    // Custom draw
    public interface OnCustomPaintListener {

        void onCustomPaint(ScArc source, Paint paint);

    }

    @SuppressWarnings("unused")
    public void setOnCustomPaintListener(OnCustomPaintListener listener) {
        this.mOnCustomPaintListener = listener;
    }

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
     * Public enum
     */

    // The stroke position
    public enum Stroke {
        INSIDE,
        CENTER,
        OUTSIDE
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

        this.mPaint.setStrokeWidth(this.mStrokeSize);
        this.requestLayout();
    }

    // Stroke color
    @SuppressWarnings("unused")
    public int getStrokeColor() {
        return this.mStrokeColor;
    }

    @SuppressWarnings("unused")
    public void setStrokeColor(int color) {
        this.mStrokeColor = color;
        this.mPaint.setColor(this.mStrokeColor);
        this.requestLayout();
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

}