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
 * Draw an arc
 * v1.0
 */
public class ScArc extends ScWidget {

    /**
     * Constants
     */

    public static final float DEFAULT_ANGLE_MAX = 360.0f;
    public static final float DEFAULT_ANGLE_START = 0.0f;
    public static final float DEFAULT_ANGLE_SWEEP = 360.0f;

    public static final float DEFAULT_STROKE_SIZE = 3.0f;
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;


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

    // Limit an angle in degrees within a range.
    // When press on the arc space the system return always an positive angle but the ScArc accept
    // also negative value for the start and end angles.
    // So in case of negative setting the normal range limit method not work proper and we must
    // implement a specific method that consider to return all kind of angle value, positive and
    // negative.
    private float angleRangeLimit(float angle, float startAngle, float endAngle) {
        // Find the opposite of the same angle
        float positive = ScArc.normalizeAngle(angle + ScArc.DEFAULT_ANGLE_MAX);
        float negative = positive - ScArc.DEFAULT_ANGLE_MAX;

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
        if (Math.abs(this.mAngleSweep) > ScArc.DEFAULT_ANGLE_MAX)
            this.mAngleSweep = ScArc.normalizeAngle(this.mAngleSweep);
        if (Math.abs(this.mAngleDraw) > ScArc.DEFAULT_ANGLE_MAX)
            this.mAngleDraw = ScArc.normalizeAngle(this.mAngleDraw);

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
                R.styleable.ScComponents_scc_angle_start, ScArc.DEFAULT_ANGLE_START);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, ScArc.DEFAULT_ANGLE_SWEEP);
        this.mAngleDraw = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_draw, this.mAngleSweep);

        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dipToPixel(ScArc.DEFAULT_STROKE_SIZE));
        this.mStrokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, ScArc.DEFAULT_STROKE_COLOR);

        this.mMaxWidth = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_width, Integer.MAX_VALUE);
        this.mMaxHeight = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_height, Integer.MAX_VALUE);

        // FillingArea.BOTH
        this.mFillingArea =
                FillingArea.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_area, 1)];
        // FillingMode.DRAW
        this.mFillingMode =
                FillingMode.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_mode, 1)];

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

    // Calc starting area from width and height dimensions and apply padding.
    private RectF calcCanvasArea(int width, int height) {
        return new RectF(
                this.getPaddingLeft(),
                this.getPaddingTop(),
                width - this.getPaddingRight(),
                height - this.getPaddingBottom()
        );
    }

    // Calc complete circle drawing area.
    // This methods calc the virtual drawing area not taking into consideration the many adjustments
    // like the stroke size or the area padding.
    private RectF calcDrawingArea(RectF startingArea) {
        // Check for empty values
        if (this.mTrimmedArea == null || this.mTrimmedArea.isEmpty()) return new RectF();

        // Default working area calculated consider the padding and the stroke size
        RectF newArea = new RectF(startingArea);

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
            float hMultiplier = newArea.width() / this.mTrimmedArea.width();
            float left = this.mTrimmedArea.left * hMultiplier;

            // Apply the new values to the area and modify the horizontal offset
            newArea.left = -hMultiplier - left + this.getPaddingLeft();
            newArea.right = hMultiplier - left + this.getPaddingLeft();
        }

        // If fill the area expand the area to have the full filling working space with the arc
        // In the wrapping case the vertical filling it is executed in anyway while the component
        // dimension will be elaborated before inside the component measuring.
        if (vWrap ||
                this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.VERTICAL) {
            // Find the multiplier based on the trimmed area and apply the proportion to the
            // vertical dimensions.
            float vMultiplier = newArea.height() / this.mTrimmedArea.height();
            float top = this.mTrimmedArea.top * vMultiplier;

            // Apply the new values to the area and modify the vertical offset
            newArea.top = -vMultiplier - top + this.getPaddingTop();
            newArea.bottom = vMultiplier - top + this.getPaddingTop();
        }

        // Return the calculated area
        return newArea;
    }


    /**
     * Draw methods
     */

    // Draw arc on the canvas using the passed area reference
    // This is an important method can be override for future inherit class implementation.
    protected void internalDraw(Canvas canvas, RectF area) {
        // Consider the stroke size and draw
        canvas.drawArc(
                ScArc.inflateRect(area, this.mStrokeSize / 2),
                this.mAngleStart,
                this.mAngleDraw,
                false,
                this.mStrokePaint);
    }

    // This method is used to calc the areas and filling it by call/set the right draw plan.
    // Are to consider two type of draw:
    // DRAW ask to render simply on an area.
    // STRETCH before scale and transpose the canvas and after render on it using the default
    // render method.
    // This is an important method can be override for future inherit class implementation.
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

                // Draw the arc on the reset canvas
                canvasArea = ScArc.resetRectToOrigin(canvasArea);
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
        // Calc the trimmed virtual area
        this.mTrimmedArea = this.calcTrimmedArea();

        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // Layout wrapping
        boolean hWrap = this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean vWrap = this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        // Find the horizontal and vertical global padding amount
        float hGlobalPadding = this.getPaddingLeft() + this.getPaddingRight();
        float vGlobalPadding = this.getPaddingTop() + this.getPaddingBottom();

        // If have a horizontal wrap content we want to obtain a perfect circle radius so we must
        // set the new height equal to the current width.
        // For do this I must also consider the horizontal padding to remove before trimming the
        // area and to add after trimmed.
        if (hWrap) {
            width = (int) ((height - hGlobalPadding) * (this.mTrimmedArea.width() / 2));
            width += hGlobalPadding;
        }

        // If have a vertical wrap content we want to obtain a perfect circle radius so we must
        // set the new width equal to the current height.
        // For do this I must also consider the vertical padding to remove before trimming the
        // area and to add after trimmed.
        if (vWrap) {
            height = (int) ((width - vGlobalPadding) * (this.mTrimmedArea.height() / 2));
            height += vGlobalPadding;
        }

        // Check the dimensions limits
        width = this.valueRangeLimit(width, 0, this.mMaxWidth);
        height = this.valueRangeLimit(height, 0, this.mMaxHeight);

        // Set the finded dimensions
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

    // Normalize a angle in degrees.
    // If the angle is over 360° will be normalized.
    // This method work for negative and positive angle values.
    @SuppressWarnings("unused")
    public static float normalizeAngle(float degrees) {
        return (degrees + (degrees < 0 ? -360.0f : +360.0f)) % 360.0f;
    }

    // Check if point is inside a circle (Pitagora).
    // Supposed that the origin of the circle is 0, 0.
    @SuppressWarnings("all")
    public static boolean pointInsideCircle(float x, float y, float radius) {
        return Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(radius, 2);
    }

    // Find a point on the circumference inscribed in the passed area rectangle.
    // This angle is intended to be a global angle and if not subdue to any restriction.
    @SuppressWarnings("unused")
    public static Point getPointFromAngle(float degrees, RectF area) {
        // Find the default arc radius
        float xRadius = area.width() / 2;
        float yRadius = area.height() / 2;

        // Convert the radius in radiant and find the coordinates in the space
        double rad = Math.toRadians(degrees);
        int x = Math.round(xRadius * (float) Math.cos(rad) + area.centerX());
        int y = Math.round(yRadius * (float) Math.sin(rad) + area.centerY());

        // Create the point and return it
        return new Point(x, y);
    }

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


    /**
     * Public methods
     */

    // Get the arc painter
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mStrokePaint;
    }

    // Calc point position from relative angle in degrees.
    // Note that the angle must be relative to the start angle defined by the component settings
    // and not intended as a global angle.
    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees, float radiusAdjust) {
        // Get the drawing area
        RectF canvasArea = this.calcCanvasArea(this.getMeasuredWidth(), this.getMeasuredHeight());
        RectF drawingArea = this.calcDrawingArea(canvasArea);
        // Adjust the area by the passed value and the half stroke size
        RectF adjustedArea = ScArc.inflateRect(drawingArea, radiusAdjust + this.mStrokeSize / 2);

        // Create the point by the angle relative at the start angle defined in the component
        // settings and return it.
        return ScArc.getPointFromAngle(degrees + this.mAngleStart, adjustedArea);
    }

    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees) {
        return this.getPointFromAngle(degrees, 0.0f);
    }

    // Find the angle from position on the component.
    // This method consider the angles limits settings and return a relative angle value within
    // this limits.
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

        // Normalize the degrees angle by the start angle defined by component settings.
        float degrees = (float) Math.toDegrees(angle) - this.mAngleStart;
        // Check the angle limit and return the checked value
        return this.angleRangeLimit(degrees, 0, this.mAngleSweep);
    }

    // Check if a point belongs to the arc
    @SuppressWarnings("unused")
    public boolean belongsToArc(float x, float y, float precision) {
        // Find the angle from the passed point and get the point on the arc
        float angle = this.getAngleFromPoint(x, y);
        Point pointOnArc = this.getPointFromAngle(angle);

        // Find the delta distance between the points and check if is inside a circle build on
        // the precision radius.
        return ScArc.pointInsideCircle(x - pointOnArc.x, y - pointOnArc.y, precision);
    }

    @SuppressWarnings("unused")
    public boolean belongsToArc(float x, float y) {
        return this.belongsToArc(x, y, this.mStrokeSize);
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