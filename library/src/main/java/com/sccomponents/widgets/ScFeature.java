package com.sccomponents.widgets;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;

import com.sccomponents.utils.ScObserver;

/**
 * Create a feature to draw on a given path.
 * <p>
 * The feature is independent and can be used with any path.
 * Is enough to instantiate it passing the path object and call the draw function passing the
 * canvas where draw.
 * The original design of this class was for link it with the ScDrawer to have a base drawer (the
 * ScDrawer linked) and many features applicable to it.
 * <p>
 * The "feature" base class essentially do nothing.
 * For draw something, hence for specialize the feature, you need to override the onDraw method.
 * The base class provides only a common set of methods to display something on the path as the
 * color manager, visibility, limits, ecc. that is useful to inherit it and create a specialized
 * class.
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public class ScFeature {

    /****************************************************************************************
     * Enumerators
     */

    /**
     * The mode to building the painter shader.
     */
    @SuppressWarnings("unuse")
    public enum ShaderMode {
        SOLID,
        GRADIENT
    }


    /****************************************************************************************
     * Privates and protected variable
     */

    protected Path mPath;
    protected ScPathMeasure mPathMeasure;
    protected float mPathLength;

    protected Paint mPaint;
    protected int[] mColors;

    protected ShaderMode mShaderMode;
    protected Shader mShader;

    protected String mTag;
    protected boolean mVisible;

    protected float mStartPercentage;
    protected float mEndPercentage;

    private ScObserver mPaintObserver;
    private ScObserver mPathObserver;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScFeature(Path path) {
        // Init
        this.mShaderMode = ShaderMode.GRADIENT;
        this.mVisible = true;
        this.mStartPercentage = 0.0f;
        this.mEndPercentage = 100.0f;

        // Path
        this.mPath = path;
        this.mPathMeasure = new ScPathMeasure(this.mPath, false);
        this.mPathLength = this.mPathMeasure.getContoursLength();

        // Create the painter
        this.mPaint = new Paint();
        this.mPaint.setStrokeCap(Paint.Cap.BUTT);
        this.mPaint.setStrokeWidth(0.0f);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(Color.BLACK);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);

        // Shader
        this.mShader = this.createShader();

        // Observers
        this.mPathObserver = new ScObserver(this.mPath);
        this.mPaintObserver = new ScObserver(this.mPaint);
    }


    /****************************************************************************************
     * Shader
     */

    /**
     * Get the current gradient color by a ratio dependently about the distance from the
     * starting of path and the colors array.
     *
     * @param distance from the starting path
     * @return the color
     */
    private int getColor(int distance) {
        // Check the case
        switch (this.mShaderMode) {
            case SOLID:
                return this.mColors[(int) (distance / (this.mPathLength / this.mColors.length))];

            case GRADIENT:
                // Calculation
                float sectorLen = this.mPathLength / (this.mColors.length - 1);
                int sector = (int) (distance / sectorLen);
                int normalized = distance - (int) (sectorLen * sector);
                float ratio = normalized / sectorLen;

                // Get the color to mix
                int startColor = this.mColors[sector];
                int endColor = this.mColors[sector + 1];

                // Calculate the result color
                int red = (int) (Color.red(endColor) * ratio + Color.red(startColor) * (1 - ratio));
                int green = (int) (Color.green(endColor) * ratio + Color.green(startColor) * (1 - ratio));
                int blue = (int) (Color.blue(endColor) * ratio + Color.blue(startColor) * (1 - ratio));

                // Get the color
                return Color.rgb(red, green, blue);

            default:
                return Color.BLACK;
        }
    }

    /**
     * Create the paint shader.
     * This methods need to define what kind of shader filling to coloring the draw path.
     * Essentially there are two different mode of filling: GRADIENT or SOLID.
     * <p>
     * Note that this method was created to be a generic method for work proper on all path but
     * could be slow given the big amount of calculations. So in any case may be better to create
     * a custom shader to attach directly to the painter.
     * <p>
     * In all cases this method will create a series of color that following the path and in some
     * case this could be not the right choice.
     * For example if you build a filled circle might be better to create a radial gradient.
     *
     * @return return the shader
     */
    protected Shader createShader() {
        // Check no values inside the array
        if (this.mColors == null || this.mColors.length < 2)
            return null;

        // Create the bitmap using the path boundaries
        RectF pathBounds = this.mPathMeasure.getBounds();
        Bitmap bitmap = Bitmap.createBitmap(
                (int) pathBounds.right, (int) pathBounds.bottom, Bitmap.Config.ARGB_8888);

        // Retrieve the canvas where draw and create the painter to use
        Canvas canvas = new Canvas(bitmap);
        Paint clonePaint = new Paint(this.mPaint);

        // Hold if a rounded stroke and the point holder
        boolean isRoundedStroke = this.mPaint.getStrokeCap() == Paint.Cap.ROUND;
        float[] point;

        // Cycle all points of the path
        for (int distance = 0; distance < this.mPathLength; distance++) {
            // Get the point and check for null value
            point = this.mPathMeasure.getContoursPosTan(distance);

            // Trigger for index position and get the color
            boolean isFirstOrLast = distance == 0 || distance == this.mPathLength - 1;
            int color = this.getColor(distance);

            // Set the current painter color and stroke
            clonePaint.setColor(color);
            clonePaint.setStrokeCap(
                    isRoundedStroke && isFirstOrLast ? Paint.Cap.ROUND : Paint.Cap.BUTT);

            // Calculate the current angle of tangent in degrees
            float angle = (float) Math.toDegrees(point[3]);

            // If the round stroke is not settled the point have a square shape.
            // This can create a visual issue when the path follow a curve.
            // To avoid this issue the point (square) will be rotate of the tangent angle
            // before to write it on the canvas.
            canvas.save();
            canvas.rotate(angle, point[0], point[1]);
            canvas.drawPoint(point[0], point[1], clonePaint);
            canvas.restore();
        }

        // Return the shader
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }


    /****************************************************************************************
     * Protected methods
     */

    /**
     * The draw method to override in the inherited classes.
     *
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    protected void onDraw(Canvas canvas) {
        // To implement
    }


    /****************************************************************************************
     * Public and static methods
     */

    /**
     * Draw something on the canvas.
     *
     * @param canvas where draw
     */
    @SuppressWarnings("unused")
    public void draw(Canvas canvas) {
        // Check for the visibility
        if (!this.mVisible) return;

        // Changes holder
        boolean pathIsChanged = this.mPathObserver.isChanged();
        boolean paintIsChanged = this.mPaintObserver.isChanged();

        // Refresh the path if needed
        if (pathIsChanged) {
            this.mPathMeasure.setPath(this.mPath, false);
            this.mPathLength = this.mPathMeasure.getContoursLength();
        }

        // Refresh the shader if needed
        if (pathIsChanged || paintIsChanged) {
            this.mShader = this.createShader();
            this.mPaint.setShader(this.mShader);
        }

        // If the have only one color inside the colors array set it directly on the painter
        if (this.mColors != null && this.mColors.length == 1) {
            this.mPaint.setColor(this.mColors[0]);
        }

        // Call the base onDraw method
        this.onDraw(canvas);
    }

    /**
     * Convert a point represented by an array to an modern object.
     * Supposed that the 0 array position correspond to the x coordinate and on the 1 array
     * position correspond the y coordinate.
     *
     * @param point the array
     * @return the point
     */
    @SuppressWarnings("unused")
    public static PointF toPoint(float[] point) {
        // Check the passed point
        if (point == null) return null;
        if (point.length < 2) throw new IndexOutOfBoundsException();

        // Do a conversion
        return new PointF(point[0], point[1]);
    }

    /**
     * Translate a point considering the angle (in radiant) and the offset.
     * Move the pointer on the tangent defined by the angle.
     *
     * @param point  the point to translate
     * @param offset the point offset
     * @param angle  the angle reference in radiant
     */
    @SuppressWarnings("unused")
    public static void translatePoint(PointF point, float offset, float angle) {
        point.x += (float) (Math.cos(angle) * offset);
        point.y += (float) (Math.sin(angle) * offset);
    }

    /**
     * Translate a point considering the angle (in radiant) and the offset (x, y).
     * Move the pointer on the tangent defined by the angle by the x value and move the pointer
     * on the perpendicular defined by the angle by the y value.
     *
     * @param point the point to translate
     * @param offset the point offset
     * @param angle the angle reference
     */
    @SuppressWarnings("unused")
    public static void translatePoint(PointF point, PointF offset, float angle) {
        // Translate on the x
        ScFeature.translatePoint(point, offset.x, angle);
        // Translate on the y
        ScFeature.translatePoint(point, offset.y, angle + (float) Math.PI / 2);
    }

    /**
     * Set the drawing limits (in percentage).
     * The assignment happen only if the value is different from infinity.
     *
     * @param start the start value
     * @param end   the end value
     */
    @SuppressWarnings("unused")
    public void setLimits(float start, float end) {
        // Store the new values
        if (!Float.isInfinite(start)) this.mStartPercentage = start;
        if (!Float.isInfinite(end)) this.mEndPercentage = end;
    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the painter
     *
     * @return the painter
     */
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mPaint;
    }

    /**
     * Set the painter
     *
     * @param value the painter
     */
    @SuppressWarnings("unused")
    public void setPainter(Paint value) {
        this.mPaint = value;
    }

    /**
     * Get the tag
     *
     * @return the tag
     */
    @SuppressWarnings("unused")
    public String getTag() {
        return this.mTag;
    }

    /**
     * Set the tag
     *
     * @param value the tag
     */
    @SuppressWarnings("unused")
    public void setTag(String value) {
        this.mTag = value;
    }

    /**
     * Get the visibility
     *
     * @return the tag
     */
    @SuppressWarnings("unused")
    public boolean getVisible() {
        return this.mVisible;
    }

    /**
     * Set the visibility
     *
     * @param value the tag
     */
    @SuppressWarnings("unused")
    public void setVisible(boolean value) {
        this.mVisible = value;
    }

    /**
     * Return the current stroke colors
     *
     * @return the current stroke colors
     */
    @SuppressWarnings("unused")
    public int[] getColors() {
        return this.mColors;
    }

    /**
     * Set the current stroke colors
     *
     * @param value the new stroke colors
     */
    @SuppressWarnings("unused")
    public void setColors(int... value) {
        // Save the new value and refresh
        this.mColors = value;
    }

    /**
     * Return the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @return the color filling mode
     */
    @SuppressWarnings("unused")
    public ShaderMode getFillingColors() {
        return this.mShaderMode;
    }

    /**
     * Set the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @param value the new color filling mode
     */
    @SuppressWarnings("unused")
    public void setFillingColors(ShaderMode value) {
        // Check if value is changed
        if (this.mShaderMode != value) {
            // Store the new value and refresh the component
            this.mShaderMode = value;
        }
    }

}
