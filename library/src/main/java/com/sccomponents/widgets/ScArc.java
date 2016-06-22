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
import android.view.ViewGroup;

import java.util.Arrays;

/**
 * Draw an arc
 *
 * @author Samuele Carassai
 * @version 2.0.0
 * @since 2016-05-26
 */
public class ScArc extends ScGauge {

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
     * Enum for define what type of draw method calling for render the arc
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
    protected ArcTypes mArcType;


    /****************************************************************************************
     * Private variables
     */

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
        final TypedArray attrArray = context
                .obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mAngleStart = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_start, ScArc.DEFAULT_ANGLE_START);
        this.mAngleSweep = attrArray.getFloat(
                R.styleable.ScComponents_scc_angle_sweep, ScArc.DEFAULT_ANGLE_SWEEP);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.mArcType = ArcTypes.LINE;

        //--------------------------------------------------
        // PAINTS

        this.mPiePaint = new Paint();
        this.mPiePaint.setAntiAlias(true);
        this.mPiePaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Limit an angle in degrees within a range.
     * When press on the arc space the system return always an positive angle but the ScArc accept
     * also negative value for the start and end angles.
     * So in case of negative setting the normal range limit method not work proper and we must
     * implement a specific method that consider to return all kind of angle value, positive and
     * negative.
     *
     * @param angle      the current angle in degrees
     * @param startAngle the start angle limit in degrees
     * @param endAngle   the end angle limit in degrees
     * @return the fixed angle in degrees
     */
    private float angleRangeLimit(float angle, float startAngle, float endAngle) {
        // Define the holders
        float min = Math.min(startAngle, endAngle);
        float max = Math.max(startAngle, endAngle);
        float current = angle;
        float increment = ScArc.DEFAULT_ANGLE_MAX * (current < min ? 1 : -1);

        // Infinite cycle
        while (true) {
            // Check if the current angle is within the range
            if (current >= min && current <= max) return current;
            // Check if finished to check
            if ((increment > 0 && current > max) || (increment < 0 && current < min)) break;

            // Next step
            current += increment;
        }

        // In this case the angle is outside the range so I can try to snap the value to the
        // closed limit.
        if (increment > 0)
            return (min - angle) < (current - max) ? min : max;
        else
            return (angle - max) < (min - current) ? max : min;
    }

    /**
     * Create a bitmap shader.
     * If the colors filling mode is SOLID we cannot use a gradient but we must separate colors
     * each other. For do it we will use a trick creating a bitmap and filling it with a colored
     * pies. After that create a bitmap shader that will going to apply to the Painter.
     *
     * @param area the area boundaries reference
     * @return the bitmap shader
     */
    // TODO: check
    /*
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
    */

    /**
     * Create a sweep gradient shader.
     * Since the sweep angle can be minor of 360° we must create an array storing the colors
     * position respect to the arc (sectors).
     *
     * @param area the area boundaries reference
     * @return the gradient shader
     */
    // TODO: check
    /*
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
    */


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
        super.onDraw(canvas);
    }

    /**
     * Create the path to draw.
     * This is fundamental to draw something on the canvas.
     *
     * @return the path
     */
    @Override
    @SuppressWarnings("all")
    protected Path createPath(int maxWidth, int maxHeight) {
        // If have any wrap dimensions to apply to the content we want to have a perfect circle
        // so we'll update the dimensions for have equal.
        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) maxWidth = maxHeight;
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) maxHeight = maxWidth;

        // the new path and the area
        Path path = new Path();
        RectF area = new RectF(0, 0, maxWidth, maxHeight);

        // If must to be closed move the CP in center of area
        if (this.mArcType == ArcTypes.CLOSED)
            path.moveTo(area.centerX(), area.centerY());

        // Draw the arc
        path.addArc(area, this.mAngleStart, this.mAngleSweep);

        // If the arc is closed
        if (this.mArcType == ArcTypes.CLOSED)
            path.close();

        // Return the path
        return path;
    }

    /**
     * Create the paint shader.
     * This methods need to define what kind of shader using about coloring the draw path.
     * Is important do a distinction between the filling colors type as the case want have two
     * different type of filling: GRADIENT or SOLID.
     *
     * @return Return the shader
     */
    // TODO: check
    /*
    protected Shader createShader(int width, int height) {
        // Check no values inside the array
        if (this.mStrokeColors.length == 0)
            return null;

        // If have only one value set directly to the painter and return null
        if (this.mStrokeColors.length == 1) {
            this.getPainter().setColor(this.mStrokeColors[0]);
            return null;
        }

        // Select the draw colors method by the case
        switch (this.mFillingColors) {
            // Solid filling
            case SOLID:
                return this.createBitmapShader(this.mVirtualArea);

            // Gradient filling
            case GRADIENT:
                return this.createSweepGradient(this.mVirtualArea);

            // Else
            default:
                return null;
        }
    }
    */


    /****************************************************************************************
     * Instance state
     */

    /**
     * Save the current instance state
     *
     * @return the state
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
        state.putInt("mArcType", this.mArcType.ordinal());

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     *
     * @param state the state
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
     * @param degrees the start angle in degrees
     * @return the normalized angle in degrees
     */
    @SuppressWarnings("unused")
    public static float normalizeAngle(float degrees) {
        return (degrees + (degrees < 0 ? -360.0f : +360.0f)) % 360.0f;
    }

    /**
     * Check if point is inside a circle (Pitagora).
     * Supposed that the origin of the circle is 0, 0.
     *
     * @param x      the x point
     * @param y      the y point
     * @param radius the radius of the circle
     * @return true if the point is inside the circle
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
     * @param degrees      the passed angle in degrees
     * @param radiusAdjust the radius adjustment
     * @return the point on the arc
     */
    @SuppressWarnings("unused")
    public Point getPointFromAngle(float degrees, float radiusAdjust) {
        // Adjust the area by the passed value and the half stroke size
        RectF adjustedArea = ScArc.inflateRect(this.mVirtualArea, radiusAdjust);
        // Create the point by the angle relative at the start angle defined in the component
        // settings and return it.
        return ScArc.getPointFromAngle(degrees + this.mAngleStart, adjustedArea);
    }

    /**
     * Find the angle from position on the component.
     * This method consider the angles limits settings and return a relative angle value within
     * this limits.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return the calculated angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleFromPoint(float x, float y) {
        // Get angle from position
        double angle = Math.atan2(
                (y - this.mVirtualArea.centerY()) / this.mVirtualArea.height(),
                (x - this.mVirtualArea.centerX()) / this.mVirtualArea.width()
        );

        // Normalize the degrees angle by the start angle defined by component settings.
        float degrees = (float) Math.toDegrees(angle) - this.mAngleStart;
        // Check the angle limit and return the checked value
        return this.angleRangeLimit(degrees, 0, this.mAngleSweep);
    }

    /**
     * Get the distance from center passed an angle or a point.
     * If an angle will passed the method find the relative point on the arc and than will
     * calculate the distance from center.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return the distance from the center
     */
    @SuppressWarnings("unused")
    public float getDistanceFromCenter(float x, float y) {
        // Return the calculated distance
        return (float) Math.sqrt(
                Math.pow(x - this.mVirtualArea.centerX(), 2) + Math.pow(y - this.mVirtualArea.centerY(), 2)
        );
    }

    /**
     * Get the distance from center passed an angle or a point.
     * If an angle will passed the method find the relative point on the arc and than will
     * calculate the distance from center.
     *
     * @param degrees the angle in degrees
     * @return the distance from the center
     */
    @SuppressWarnings("unused")
    public float getDistanceFromCenter(float degrees) {
        // Find the point on the arc
        Point point = this.getPointFromAngle(degrees, 0.0f);
        // Find the distance
        return this.getDistanceFromCenter(point.x, point.y);
    }

    /**
     * Get the current painter color by the current draw angle
     *
     * @param angle the angle in degrees
     * @return the calculated color
     */
    // TODO: check
    /*
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
    */


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the start angle
     *
     * @return the start angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleStart() {
        return this.mAngleStart;
    }

    /**
     * Set the start angle
     *
     * @param value the start angle in degrees
     */
    @SuppressWarnings("unused")
    public void setAngleStart(float value) {
        // Check if value is changed
        if (this.mAngleStart != value) {
            // Store the new value
            this.mAngleStart = value;
            this.requestLayout();
        }
    }

    /**
     * Return the sweep angle
     *
     * @return the sweep angle in degrees
     */
    @SuppressWarnings("unused")
    public float getAngleSweep() {
        return this.mAngleSweep;
    }

    /**
     * Set the sweep angle
     *
     * @param value the sweep angle in degrees
     */
    @SuppressWarnings("unused")
    public void setAngleSweep(float value) {
        // Check if value is changed
        if (this.mAngleSweep != value) {
            // Store the new value
            this.mAngleSweep = value;
            this.requestLayout();
        }
    }

    /**
     * Return the arc type.
     * This define how the arc will be draw: LINE, CLOSED or FILLED.
     *
     * @return the arc type
     */
    @SuppressWarnings("unused")
    public ArcTypes getArcType() {
        return this.mArcType;
    }

    /**
     * Set the arc type.
     * This define how the arc will be draw: LINE, CLOSED or FILLED.
     *
     * @param value the arc type
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