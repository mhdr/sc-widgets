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
import android.view.View;
import android.view.ViewGroup;

/**
 * Create an arc.
 * v2.0
 */
public class ScArc extends ScWidget {

    /**
     * Constants
     */

    private static final float ANGLE_START = 0.0f;
    private static final float ANGLE_SWEEP = 360.0f;

    private static final float STROKE_SIZE = 3.0f;
    private static final int STROKE_COLOR = Color.BLACK;


    /**
     * Private attributes
     */

    private float mAngleStart;
    private float mAngleSweep;
    private float mAngleDraw;

    private float mStrokeSize;
    private int mStrokeColor;

    private int mMaxWidth;
    private int mMaxHeight;

    private FillingArea mFillingArea;
    private FillingMode mFillingMode;


    /**
     * Private variables
     */

    private RectF mTrimmedArea;
    private Paint mStrokePaint;


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

    // Limit an angle in degrees within a range
    private float angleRangeLimit(float angle, float startAngle, float endAngle) {
        // Find the opposite of the same angle
        float positive = ScArc.normalizeAngle(angle + 360);
        float negative = positive - 360;

        // Try both case of angle is positive and is negative.
        float firstCase = ScArc.valueRangeLimit(positive, startAngle, endAngle);
        float secondCase = ScArc.valueRangeLimit(negative, startAngle, endAngle);

        // If the first case is equal to the positive angle than the correct angle is the
        // positive one
        if (firstCase == positive) {
            return positive;

        } else {
            // If the second case is equal to the negative angle than the correct angle is the
            // negative one
            if (secondCase == negative) {
                return negative;

            } else {
                // The angle if over the limit.
                // Try to find the nearest limit and return it.
                if (Math.abs(firstCase - positive) < Math.abs(secondCase - negative))
                    return firstCase;
                else
                    return secondCase;
            }
        }
    }

    // Check all input values if over the limits
    private void checkValues() {
        // Size
        if (this.mStrokeSize < 0.0f) this.mStrokeSize = 0.0f;

        // Angle
        if (Math.abs(this.mAngleSweep) > 360.0f) this.mAngleSweep = this.mAngleSweep % 360.0f;
        if (Math.abs(this.mAngleDraw) > 360.0f) this.mAngleDraw = this.mAngleDraw % 360.0f;

        // Dimension
        if (this.mMaxWidth < 0) this.mMaxWidth = 0;
        if (this.mMaxHeight < 0) this.mMaxHeight = 0;

        // Check the draw angle limits
        this.mAngleDraw = ScArc.valueRangeLimit(this.mAngleDraw, 0, this.mAngleSweep);
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
                R.styleable.ScComponents_scc_angle_start, ScArc.ANGLE_START);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, ScArc.ANGLE_SWEEP);
        this.mAngleDraw = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_draw, this.mAngleSweep);

        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dipToPixel(ScArc.STROKE_SIZE));
        this.mStrokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, ScArc.STROKE_COLOR);

        this.mMaxWidth = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_width, 0);
        this.mMaxHeight = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_height, 0);

        // FillingArea.BOTH
        this.mFillingArea =
                FillingArea.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_area, 1)];
        // FillingMode.STRETCH
        this.mFillingMode =
                FillingMode.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_mode, 0)];

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

    // Calc starting area from a width and height and apply padding and stroke size.
    // The origin of the rectangle is always 0,0 you must remember to apply an offset for
    // balance the area.
    private RectF calcCanvasArea(int width, int height) {
        width -= this.getPaddingLeft() + this.getPaddingRight() + this.mStrokeSize;
        height -= this.getPaddingTop() + this.getPaddingBottom() + this.mStrokeSize;
        return new RectF(0, 0, width, height);
    }

    // Calc complete circle drawing area.
    // This methods calc the virtual drawing area not taking into consideration the many adjustments
    // like the stroke size or the area padding.
    private RectF calcDrawingArea(RectF startingArea) {
        // Check for empty values
        if (this.mTrimmedArea == null || this.mTrimmedArea.isEmpty()) return new RectF();

        // Default working area calculated consider the padding and the stroke size
        RectF area = new RectF(startingArea);

        // Layout wrapping
        boolean hWrap = this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean vWrap = this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        // If fill the area expand the area to have the full filling working space with the arc.
        // In the wrapping case the horizontal filling it is executed in anyway while the component
        // dimension will be elaborated before inside the component measuring.
        if (hWrap ||
                this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.HORIZONTAL) {
            // Find the multiplier based on the trimmed area and apply the proportion to the
            // horizontal dimensions.
            float hMultiplier = area.width() / this.mTrimmedArea.width();
            float left = this.mTrimmedArea.left * hMultiplier;

            // Apply the new values to the area and modify the horizontal offset
            area.left = -hMultiplier - left;
            area.right = hMultiplier - left;
        }

        // If fill the area expand the area to have the full filling working space with the arc
        // In the wrapping case the vertical filling it is executed in anyway while the component
        // dimension will be elaborated before inside the component measuring.
        if (vWrap ||
                this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.VERTICAL) {
            // Find the multiplier based on the trimmed area and apply the proportion to the
            // vertical dimensions.
            float vMultiplier = area.height() / this.mTrimmedArea.height();
            float top = this.mTrimmedArea.top * vMultiplier;

            // Apply the new values to the area and modify the vertical offset
            area.top = -vMultiplier - top;
            area.bottom = vMultiplier - top;
        }

        // Adjust the offset
        area.offset(
                this.getPaddingLeft() + this.mStrokeSize / 2,
                this.getPaddingTop() + this.mStrokeSize / 2
        );

        // Return the calc area
        return area;
    }


    /**
     * Draw methods
     */

    // Draw arc on the canvas using the passed area reference
    protected void internalDraw(Canvas canvas, RectF area) {
        canvas.drawArc(
                area,
                this.mAngleStart,
                this.mAngleDraw,
                false,
                this.mStrokePaint);
    }

    // This method is used to calc the area by the filling mode case and call/set the right
    // draw method.
    protected void internalDraw(Canvas canvas) {
        // Find the canvas and drawing area
        RectF canvasArea = this.calcCanvasArea(canvas.getWidth(), canvas.getHeight());
        RectF drawingArea = this.calcDrawingArea(canvasArea);

        // Select the drawing mode by the case
        switch (this.mFillingMode) {
            // Draw
            case DRAW:
                // Draw the arc on the calculated drawing area
                this.internalDraw(canvas, drawingArea);
                break;

            // Stretch
            case STRETCH:
                // Save the current canvas status
                canvas.save();

                // Translate and scale the canvas
                canvas.translate(drawingArea.left, drawingArea.top);
                canvas.scale(
                        drawingArea.width() / canvasArea.width(),
                        drawingArea.height() / canvasArea.height()
                );

                // Draw the arc on the canvas
                this.internalDraw(canvas, canvasArea);
                // Restore the last saved canvas status
                canvas.restore();
                break;
        }
    }


    /**
     * Overrides
     */

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw an arc
        this.internalDraw(canvas);
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
        state.putInt("mFillingArea", this.mFillingArea.ordinal());
        state.putInt("mFillingMode", this.mFillingMode.ordinal());

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
        this.mFillingArea = FillingArea.values()[savedState.getInt("mFillingArea")];
        this.mFillingMode = FillingMode.values()[savedState.getInt("mFillingMode")];
    }


    /**
     * Static methods
     */

    // Normalize a angle in degrees
    @SuppressWarnings("unused")
    public static float normalizeAngle(float degrees) {
        return (degrees + (degrees < 0 ? -360.0f : +360.0f)) % 360.0f;
    }

    // Check if point is inside a circle (Pitagora)
    @SuppressWarnings("all")
    public static boolean pointInsideCircle(float x, float y, float radius) {
        return Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(radius, 2);
    }


    /**
     * Public methods
     */

    // The area filling types.
    // Decide what filling in drawing area.
    @SuppressWarnings("unused")
    public enum FillingArea {
        NONE,
        BOTH,
        HORIZONTAL,
        VERTICAL
    }

    // The area filling mode.
    // STRETCH action on the canvas scale instead DRAW draw all the points respect the
    // the drawing area.
    @SuppressWarnings("unused")
    public enum FillingMode {
        STRETCH,
        DRAW
    }

    // Calc point position from angle in degrees.
    // Given an angle this method calc the relative point on the arc.
    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees, float radiusAdjust) {
        // Normalize the angle
        degrees = ScArc.normalizeAngle(degrees);

        // Get the drawing area
        RectF canvasArea = this.calcCanvasArea(this.getMeasuredWidth(), this.getMeasuredHeight());
        RectF drawingArea = this.calcDrawingArea(canvasArea);

        // Find the default arc radius
        float xRadius = drawingArea.width() / 2 + radiusAdjust;
        float yRadius = drawingArea.height() / 2 + +radiusAdjust;

        // Convert the radius in radiant and find the coordinates in the space
        double rad = Math.toRadians(degrees);
        int x = Math.round(xRadius * (float) Math.cos(rad) + drawingArea.centerX());
        int y = Math.round(yRadius * (float) Math.sin(rad) + drawingArea.centerY());

        // Create the point and return it
        return new Point(x, y);
    }

    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees) {
        return this.getPointFromAngle(degrees, 0.0f);
    }

    // Find the angle from position on screen.
    @SuppressWarnings("unused")
    public float getAngleFromPoint(float x, float y) {
        // Get the drawing area
        RectF canvasArea = this.calcCanvasArea(this.getMeasuredWidth(), this.getMeasuredHeight());
        RectF drawingArea = this.calcDrawingArea(canvasArea);

        // Get angle from position
        double angle = Math.atan2(
                (y - drawingArea.centerY()) / drawingArea.height(),
                (x - drawingArea.centerX()) / drawingArea.width()
        );

        // Limit the value within the component angle range and return it
        return this.angleRangeLimit(
                (float) Math.toDegrees(angle),
                this.mAngleStart,
                this.mAngleStart + this.mAngleSweep
        );
    }

    // Check if a point belongs to the arc
    @SuppressWarnings("unused")
    public boolean belongsToArc(float x, float y, float precision) {
        // Find the angle from the passed point
        float angle = this.getAngleFromPoint(x, y);
        // Find the point on arc from angle
        Point pointOnArc = this.getPointFromAngle(angle);

        // Find the distance between the points and check it
        return ScArc.pointInsideCircle(x - pointOnArc.x, y - pointOnArc.y, precision);
    }

    @SuppressWarnings("unused")
    public boolean belongsToArc(float x, float y) {
        return this.belongsToArc(x, y, this.mStrokeSize);
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

    // Area filling type
    @SuppressWarnings("unused")
    public FillingArea getFillingArea() {
        return this.mFillingArea;
    }

    @SuppressWarnings("unused")
    public void setFillingArea(FillingArea value) {
        // Check if value is changed
        if (this.mFillingArea != value) {
            // Store the new value and refresh the component
            this.mFillingArea = value;
            this.invalidate();
        }
    }

    // Area filling mode
    @SuppressWarnings("unused")
    public FillingMode getFillingMode() {
        return this.mFillingMode;
    }

    @SuppressWarnings("unused")
    public void setAreaFilling(FillingMode value) {
        // Check if value is changed
        if (this.mFillingMode != value) {
            // Store the new value and refresh the component
            this.mFillingMode = value;
            this.invalidate();
        }
    }

}