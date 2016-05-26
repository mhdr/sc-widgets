package com.sccomponents.widgets;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.util.Arrays;

/**
 * Draw an arc
 *
 * @author Samuele Carassai
 * @version 2.0.0
 * @since 2016-05-26
 */
public class ScArc extends ScDrawer {

    /****************************************************************************************
     * Constants
     */

    public static final float DEFAULT_ANGLE_MAX = 360.0f;
    public static final float DEFAULT_ANGLE_START = 0.0f;
    public static final float DEFAULT_ANGLE_SWEEP = 360.0f;


    /****************************************************************************************
     * Enumerators
     */

    /**
     * Enum for define what type of draw method calling for render the notch
     */
    @SuppressWarnings("unused")
    public enum ArcTypes {
        LINE,
        CLOSED,
        FILLED
    }


    /****************************************************************************************
     * Private attributes
     */

    protected float mAngleStart;
    protected float mAngleSweep;
    protected float mAngleDraw;
    protected ArcTypes mArcType;


    /****************************************************************************************
     * Private variables
     */

    private Path mPath;
    private Paint mPiePaint;


    /****************************************************************************************
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


    /****************************************************************************************
     * Privates methods
     */

    /**
     * Limit an angle in degrees within a range.
     * When press on the arc space the system return always an positive angle but the ScArc accept
     * also negative value for the start and end angles.
     * So in case of negative setting the normal range limit method not work proper and we must
     * implement a specific method that consider to return all kind of angle value, positive and
     * negative.
     *
     * @param angle      The current angle in degrees
     * @param startAngle The start angle limit in degrees
     * @param endAngle   The end angle limit in degrees
     * @return The fixed angle in degrees
     */
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

    /**
     * Check all input values if over the limits
     */
    private void checkValues() {
        // Angle
        if (Math.abs(this.mAngleSweep) > ScArc.DEFAULT_ANGLE_MAX)
            this.mAngleSweep = ScArc.normalizeAngle(this.mAngleSweep);
        if (Math.abs(this.mAngleDraw) > ScArc.DEFAULT_ANGLE_MAX)
            this.mAngleDraw = ScArc.normalizeAngle(this.mAngleDraw);

        // Check the draw angle limits
        this.mAngleDraw = ScArc.valueRangeLimit(this.mAngleDraw, 0, this.mAngleSweep);
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
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mAngleStart = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_start, ScArc.DEFAULT_ANGLE_START);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, ScArc.DEFAULT_ANGLE_SWEEP);
        this.mAngleDraw = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_draw, this.mAngleSweep);

        // ArcTypes.LINE
        this.mArcType =
                ArcTypes.values()[attrArray.getInt(R.styleable.ScComponents_scc_stroke_type, 0)];

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();

        //--------------------------------------------------
        // PAINTS

        this.mPiePaint = new Paint();
        this.mPiePaint.setAntiAlias(true);
        this.mPiePaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Create a bitmap shader.
     * If the colors filling mode is SOLID we cannot use a gradient but we must separate colors
     * each other. For do it we will use a trick creating a bitmap and filling it with a colored
     * pies. After that create a bitmap shader that will going to apply to the Painter.
     *
     * @param area The area boundaries reference
     * @return The bitmap shader
     */
    private BitmapShader createBitmapShader(RectF area) {
        // Create a temporary bitmap and get its canvas
        Bitmap bitmap = Bitmap.createBitmap((int) area.width(), (int) area.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Get the delta angle from the colors count.
        float deltaAngle = this.mAngleSweep / this.mStrokeColors.length;

        // Fix a visual filling issue when use a stroke cap type different from BUTT
        if (this.mAngleSweep < 360.0f) {
            // Calc the starting half circle and get the colors
            float startAngle = this.mAngleStart - (360.0f - this.mAngleSweep) / 2;
            int firstColor = this.mStrokeColors[0];
            int lastColor = this.mStrokeColors[this.mStrokeColors.length - 1];

            // Set the painter with the first color and draw half circle
            this.mPiePaint.setColor(deltaAngle < 0 ? lastColor : firstColor);
            canvas.drawArc(area, startAngle, 180.0f, true, this.mPiePaint);

            // Set the painter with the last color and draw the second half circle
            this.mPiePaint.setColor(deltaAngle < 0 ? firstColor : lastColor);
            canvas.drawArc(area, startAngle + 180.0f, 180.0f, true, this.mPiePaint);
        }

        // Draw all pie sector on the circle
        for (int index = 0; index < this.mStrokeColors.length; index++) {
            // Calculate the start and the end angle
            float currentAngle = index * deltaAngle + this.mAngleStart;
            // Set the painter color and draw
            this.mPiePaint.setColor(this.mStrokeColors[index]);
            canvas.drawArc(area, currentAngle, deltaAngle, true, this.mPiePaint);
        }

        // Create the filter from the temporary bitmap
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    /**
     * Create a sweep gradient shader.
     * Since the sweep angle can be minor of 360° we must create an array storing the colors
     * position respect to the arc (sectors).
     *
     * @param area The area boundaries reference
     * @return The gradient shader
     */
    private SweepGradient createSweepGradient(RectF area) {
        // Create a copy of colors because not want lost the original values
        int[] colors = Arrays.copyOf(this.mStrokeColors, this.mStrokeColors.length);
        // Create a positions holder and get the delta angle from the colors count.
        float[] positions = new float[colors.length];
        float deltaAngle = Math.abs(this.mAngleSweep / (colors.length - 1));

        // Fill the positions holder
        for (int index = 0; index < colors.length; index++) {
            positions[index] = index * (deltaAngle / 360.0f);
        }

        // Fix a visual filling issue when use a stroke cap type different from BUTT
        float toClose = 1 - positions[positions.length - 1];
        if (toClose > 0) {
            // Hold the new length
            int len = positions.length + 2;

            // Resize the positions the array and insert the missed values
            positions = Arrays.copyOf(positions, len);
            positions[len - 2] = 1.0f - (toClose / 3) * 2;
            positions[len - 1] = 1.0f - (toClose / 3) * 1;

            // Resize then colors array and insert the last and first color
            colors = Arrays.copyOf(colors, len);
            colors[len - 2] = colors[len - 3];
            colors[len - 1] = colors[0];

            // If the delta angle is negative I must invert the last two colors
            if (deltaAngle < 0) {
                ScArc.swapArrayPosition(colors, len - 2, len - 1);
            }
        }

        // Create the matrix and rotate it
        Matrix matrix = new Matrix();
        matrix.preRotate(this.mAngleStart, area.centerX(), area.centerY());

        // Create the gradient and apply the matrix
        SweepGradient gradient = new SweepGradient(
                area.centerX(), area.centerY(), colors, positions);
        gradient.setLocalMatrix(matrix);

        // Return the gradient
        return gradient;
    }

    /**
     * Calculate the virtual drawing area
     */
    private RectF calculateVirtualDrawingArea() {
        // Calculate the real drawing area measures considering the padding
        int width = this.getMeasuredWidth() - (this.getPaddingLeft() + this.getPaddingRight());
        int height = this.getMeasuredWidth() - (this.getPaddingTop() + this.getPaddingBottom());

        // Scale the dimension
        width *= this.getPathHorizontalScale(width);
        height *= this.getPathVerticalScale(height);

        // Scale the canvas area
        RectF area = new RectF(0, 0, width, height);
        area.offset(this.getPaddingLeft(), this.getPaddingTop());

        // Return the area
        return area;
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * Override the super method adding the style to apply to the painter in relation to the
     * the value settled in the arc type.
     *
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Define the painter style by the current stroke type
        this.getPainter().setStyle(
                this.mArcType == ArcTypes.FILLED ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE
        );

        // Call the super
        super.draw(canvas);
    }

    /**
     * On measure
     *
     * @param widthMeasureSpec  the reference width
     * @param heightMeasureSpec the reference height
     */
    @Override
    @SuppressWarnings("all")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Force to recalculate the path and call the super
        this.mPath = null;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Get the current path
     *
     * @return The path
     */
    @Override
    public Path getPath() {
        // To avoid to calculate the path to every call will use a local variable to store it
        if (this.mPath == null) {
            // The new path and the area
            Path path = new Path();
            RectF area = new RectF(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());

            // If must to be closed move the CP in center of area
            if (this.mArcType == ArcTypes.CLOSED)
                path.moveTo(area.centerX(), area.centerY());

            // Draw the arc
            path.addArc(area, this.mAngleStart, this.mAngleSweep);

            // If the arc is closed
            if (this.mArcType == ArcTypes.CLOSED)
                path.close();

            // Save the new path
            this.mPath = path;
        }

        // Return the path
        return this.mPath;
    }

    /**
     * Return the paint shader.
     * This methods need to define what kind of shader using about coloring the draw path.
     * Is important do a distinction between the filling colors type as the case want have two
     * different type of filling: GRADIENT or SOLID.
     *
     * @return Return the shader
     */
    @Override
    public Shader getPaintShader() {
        // Check no values inside the array
        if (this.mStrokeColors.length == 0)
            return null;

        // If have only one value set directly to the painter and return null
        if (this.mStrokeColors.length == 1) {
            this.getPainter().setColor(this.mStrokeColors[0]);
            return null;
        }

        // Calculate the virtual area
        RectF area = this.calculateVirtualDrawingArea();

        // Select the draw colors method by the case
        switch (this.mFillingColors) {
            // Solid filling
            case SOLID:
                return this.createBitmapShader(area);

            // Gradient filling
            case GRADIENT:
                return this.createSweepGradient(area);

            // Else
            default:
                return null;
        }
    }


    /****************************************************************************************
     * Instance state
     */

    /**
     * Save the current instance state
     *
     * @return The state
     */
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
        state.putInt("mArcType", this.mArcType.ordinal());

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     *
     * @param state The state
     */
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
        this.mArcType = ArcTypes.values()[savedState.getInt("mArcType")];
    }


    /****************************************************************************************
     * Static methods
     */

    /**
     * Normalize a angle in degrees.
     * If the angle is over 360° will be normalized.
     * This method work for negative and positive angle values.
     *
     * @param degrees The start angle in degrees
     * @return The normalized angle in degrees
     */
    @SuppressWarnings("unused")
    public static float normalizeAngle(float degrees) {
        return (degrees + (degrees < 0 ? -360.0f : +360.0f)) % 360.0f;
    }

    /**
     * Check if point is inside a circle (Pitagora).
     * Supposed that the origin of the circle is 0, 0.
     *
     * @param x      The x point
     * @param y      The y point
     * @param radius The radius of the circle
     * @return True if the point is inside the circle
     */
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


    /****************************************************************************************
     * Public methods
     */

    /**
     * Calc point position from relative angle in degrees.
     * Note that the angle must be relative to the start angle defined by the component settings
     * and not intended as a global angle.
     *
     * @param degrees       The passed angle in degrees
     * @param radiusAdjust  The radius adjustment
     * @return              The point on the arc
     */
    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees, float radiusAdjust) {
        // Adjust the area by the passed value and the half stroke size
        RectF adjustedArea = ScArc.inflateRect(
                this.calculateVirtualDrawingArea(),
                radiusAdjust
        );

        // Create the point by the angle relative at the start angle defined in the component
        // settings and return it.
        return ScArc.getPointFromAngle(degrees + this.mAngleStart, adjustedArea);
    }

    /**
     * Calc point position from relative angle in degrees.
     * Note that the angle must be relative to the start angle defined by the component settings
     * and not intended as a global angle.
     *
     * @param degrees       The passed angle in degrees
     * @return              The point on the arc
     */
    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees) {
        return this.getPointFromAngle(degrees, 0.0f);
    }

    /**
     * Find the angle from position on the component.
     * This method consider the angles limits settings and return a relative angle value within
     * this limits.
     *
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @return  The calculated angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleFromPoint(float x, float y) {
        // Get the drawing area
        RectF area = this.calculateVirtualDrawingArea();
        // Get angle from position
        double angle = Math.atan2(
                (y - area.centerY()) / area.height(),
                (x - area.centerX()) / area.width()
        );

        // Normalize the degrees angle by the start angle defined by component settings.
        float degrees = (float) Math.toDegrees(angle) - this.mAngleStart;
        // Check the angle limit and return the checked value
        return this.angleRangeLimit(degrees, 0, this.mAngleSweep);
    }

    /**
     * Check if path contains a point.
     *
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @return  True if the paint contain the passed point
     */
    @SuppressWarnings("unused")
    public boolean contains(float x, float y) {
        return this.contains(x, y, this.mStrokeSize);
    }

    /**
     * Get the distance from center passed an angle or a point.
     * If an angle will passed the method find the relative point on the arc and than will
     * calculate the distance from center.
     *
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @return  The distance from the center
     */
    @SuppressWarnings("unused")
    public float getDistanceFromCenter(float x, float y) {
        // Get the drawing area
        RectF area = this.calculateVirtualDrawingArea();
        // Return the calculated distance
        return (float) Math.sqrt(
                Math.pow(x - area.centerX(), 2) + Math.pow(y - area.centerY(), 2)
        );
    }

    /**
     * Get the distance from center passed an angle or a point.
     * If an angle will passed the method find the relative point on the arc and than will
     * calculate the distance from center.
     *
     * @param degrees   The angle in degrees
     * @return          The distance from the center
     */
    @SuppressWarnings("unused")
    public float getDistanceFromCenter(float degrees) {
        // Find the point on the arc
        Point point = this.getPointFromAngle(degrees);
        // Find the distance
        return this.getDistanceFromCenter(point.x, point.y);
    }

    /**
     * Get the current painter color by the current draw angle
     *
     * @param angle The angle in degrees
     * @return      The calculated color
     */
    @SuppressWarnings("unused")
    public int getPainterColor(float angle) {
        // Check if have colors settled
        if (this.mStrokeColors == null) return Color.TRANSPARENT;

        // Limit the passed angle
        angle = ScArc.valueRangeLimit(angle, 0, this.mAngleSweep);

        // Check the limits
        if (angle == this.mAngleSweep)
            return this.mStrokeColors[this.mStrokeColors.length - 1];
        if (angle == 0)
            return this.mStrokeColors[0];

        // Find the delta angle and the sector
        float deltaAngle = this.mAngleSweep / this.mStrokeColors.length;
        int sector = Math.round(angle / deltaAngle);

        // Choice by the color filling
        switch (this.mFillingColors) {
            case SOLID:
                // Return the color
                return this.mStrokeColors[sector];

            case GRADIENT:
                // Reduce the angle to be relative to the sector and find the fraction
                float sectorAngle = angle - sector * deltaAngle;
                float fraction = sectorAngle / deltaAngle;

                // First color and last color
                int firstColor = this.mStrokeColors[sector];
                int lastColor = this.mStrokeColors[sector + 1];

                // Return the color
                return (int) new ArgbEvaluator().evaluate(fraction, firstColor, lastColor);

            default:
                // Return the default color
                return Color.BLACK;
        }
    }

    /**
     * Get the current painter color by the current draw angle
     *
     * @return  The calculated color
     */
    @SuppressWarnings("unused")
    public int getPainterColor() {
        return this.getPainterColor(this.mAngleDraw);
    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the start angle
     *
     * @return  The start angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleStart() {
        return this.mAngleStart;
    }

    /**
     * Set the start angle
     *
     * @param value The start angle in degrees
     */
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

    /**
     * Return the sweep angle
     *
     * @return The sweep angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleSweep() {
        return this.mAngleSweep;
    }

    /**
     * Set the sweep angle
     *
     * @param value The sweep angle in degrees
     */
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

    /**
     * Return the drawing angle
     *
     * @return The drawing angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleDraw() {
        return this.mAngleDraw;
    }

    /**
     * Set the drawing angle
     *
     * @return The drawing angle in degrees
     */
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

    /**
     * Return the arc type.
     * This define how the arc will be drawed: LINE, CLOSED or FILLED.
     *
     * @return The arc type
     */
    @SuppressWarnings("unused")
    public ArcTypes getArcType() {
        return this.mArcType;
    }

    /**
     * Set the arc type.
     * This define how the arc will be drawed: LINE, CLOSED or FILLED.
     *
     * @return The arc type
     */
    @SuppressWarnings("unused")
    public void setArcType(ArcTypes value) {
        // Check if value is changed
        if (this.mArcType != value) {
            // Store the new value and refresh the component
            this.mArcType = value;
            this.invalidate();
        }
    }

}