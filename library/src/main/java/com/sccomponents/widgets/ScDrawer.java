package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Draw a path on a canvas of View
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public abstract class ScDrawer extends ScWidget {

    /****************************************************************************************
     * Constants
     */

    public static final float DEFAULT_STROKE_SIZE = 3.0f;
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;


    /****************************************************************************************
     * Enumerators
     */

    /**
     * The area filling types.
     */
    public enum FillingArea {
        NONE,
        BOTH,
        HORIZONTAL,
        VERTICAL
    }

    /**
     * The area filling mode.
     */
    public enum FillingMode {
        STRETCH,
        DRAW
    }

    /**
     * The colors filling mode.
     */
    public enum FillingColors {
        SOLID,
        GRADIENT
    }


    /****************************************************************************************
     * Private attributes
     */

    protected float mStrokeSize;
    protected int[] mStrokeColors;

    protected FillingArea mFillingArea;
    protected FillingMode mFillingMode;
    protected FillingColors mFillingColors;

    protected int mMaximumWidth;
    protected int mMaximumHeight;


    /****************************************************************************************
     * Private variables
     */

    private Paint mStrokePaint;


    /****************************************************************************************
     * Constructors
     */

    public ScDrawer(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    /****************************************************************************************
     * Privates methods
     */

    /**
     * Check all input values if over the limits
     */
    private void checkValues() {
        // Size
        if (this.mStrokeSize < 0.0f) this.mStrokeSize = 0.0f;

        // Dimensions
        if (this.mMaximumWidth < 0) this.mMaximumWidth = 0;
        if (this.mMaximumHeight < 0) this.mMaximumHeight = 0;
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
        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScComponents_scc_stroke_size, this.dipToPixel(ScDrawer.DEFAULT_STROKE_SIZE));
        this.mStrokeColors = new int[] {
                attrArray.getColor(R.styleable.ScComponents_scc_stroke_color, ScDrawer.DEFAULT_STROKE_COLOR)
        };

        this.mMaximumWidth = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_width, Integer.MAX_VALUE);
        this.mMaximumHeight = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_height, Integer.MAX_VALUE);

        // FillingArea.BOTH
        this.mFillingArea =
                FillingArea.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_area, 1)];
        // FillingMode.DRAW
        this.mFillingMode =
                FillingMode.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_mode, 0)];
        // FillingColors.GRADIENT
        this.mFillingColors =
                FillingColors.values()[attrArray.getInt(R.styleable.ScComponents_scc_fill_colors, 1)];


        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();

        //--------------------------------------------------
        // PAINTS

        // Stroke
        this.mStrokePaint = new Paint();
        this.mStrokePaint.setColor(this.mStrokeColors[0]);
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
        this.mStrokePaint.setStyle(Paint.Style.STROKE);
        this.mStrokePaint.setStrokeCap(Paint.Cap.BUTT);
    }

    /**
     * Get the path bounds
     */
    private RectF getPathBounds() {
        // Holder
        RectF bounds = new RectF();

        // Check fot empty values
        if (this.getPath() != null && !this.getPath().isEmpty()) {
            // Save the path bounds inside the rectangle
            this.getPath().computeBounds(bounds, false);
        }

        // Return
        return bounds;
    }

    /**
     * Get the path horizontal scale factor
     *
     * @param width the width of reference
     * @return the horizontal scale
     */
    protected float getPathHorizontalScale(int width) {
        // If must be stretched in horizontal calculate the factor.
        // Else return "no scale" value.
        if (this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.HORIZONTAL) {
            return (float) width / this.getPathBounds().width();
        } else {
            return 1.0f;
        }
    }

    /**
     * Get the path vertical scale factor
     *
     * @param height the height of reference
     * @return the vertical scale
     */
    protected float getPathVerticalScale(int height) {
        // If must be stretched in vertical calculate the factor.
        // Else return "no scale" value.
        if (this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.VERTICAL) {
            return (float) height / this.getPathBounds().height();
        } else {
            return 1.0f;
        }
    }

    /**
     * Fix the scales of the path and return a new scaled path object
     *
     * @param width  the width of reference
     * @param height the height of reference
     * @return a new scaled path object
     */
    private Path scalePath(int width, int height) {
        // Create a matrix and apply the new scale
        Matrix matrix = new Matrix();
        matrix.postScale(this.getPathHorizontalScale(width), this.getPathVerticalScale(height));

        // Create a new path and apply the scale
        Path scaled = new Path(this.getPath());
        scaled.transform(matrix);

        // Return the scaled path
        return scaled;
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * This method is used to calc the areas and filling it by call/set the right draw plan.
     * Are to consider two type of draw:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it.
     *
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for empty values
        if (this.getPath() == null || this.mStrokeSize == 0)
            return;

        // Check if need to create a gradient
        if (this.mStrokeColors != null) {
            // Create the shader and apply it to the painter
            Shader shader = this.getPaintShader();
            this.mStrokePaint.setShader(shader);
        }

        // Calculate the real measures considering the padding
        int width = canvas.getWidth() - (this.getPaddingLeft() + this.getPaddingRight());
        int height = canvas.getHeight() - (this.getPaddingTop() + this.getPaddingBottom());

        // Select the drawing mode by the case
        switch (this.mFillingMode) {
            // Draw
            case DRAW:
                // Before scale and move the path
                Path fixed = this.scalePath(width, height);
                fixed.offset(this.getPaddingLeft(), this.getPaddingTop());

                // Draw the path
                canvas.drawPath(fixed, this.mStrokePaint);
                break;

            // Stretch
            case STRETCH:
                // Save the current canvas status
                canvas.save();

                // Translate and scale the canvas
                canvas.translate(this.getPaddingLeft(), this.getPaddingTop());
                canvas.scale(this.getPathHorizontalScale(width), this.getPathVerticalScale(height));

                // Draw the path
                canvas.drawPath(this.getPath(), this.mStrokePaint);

                // Restore the last saved canvas status
                canvas.restore();
                break;
        }
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
        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // Layout wrapping
        boolean hWrap = this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean vWrap = this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

        // If have some dimension to wrap will use the path boundaires for have the right
        // dimension summed to the global padding.
        if (hWrap) width = (int) this.getPathBounds().width() +
                (this.getPaddingLeft() + this.getPaddingRight());
        if (vWrap) height = (int) this.getPathBounds().height() +
                (this.getPaddingTop() + this.getPaddingBottom());

        // Fix the component dimensions limits
        width = this.valueRangeLimit(width, this.getMinimumWidth(), this.mMaximumWidth);
        height = this.valueRangeLimit(height, this.getMinimumHeight(), this.mMaximumHeight);

        // Set the calculated dimensions
        this.setMeasuredDimension(width, height);
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
        state.putFloat("mStrokeSize", this.mStrokeSize);
        state.putIntArray("mStrokeColors", this.mStrokeColors);
        state.putInt("mMaximumWidth", this.mMaximumWidth);
        state.putInt("mMaximumHeight", this.mMaximumHeight);
        state.putInt("mFillingArea", this.mFillingArea.ordinal());
        state.putInt("mFillingMode", this.mFillingMode.ordinal());
        state.putInt("mFillingColors", this.mFillingColors.ordinal());

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
        this.mStrokeSize = savedState.getFloat("mStrokeSize");
        this.mStrokeColors = savedState.getIntArray("mStrokeColors");
        this.mMaximumWidth = savedState.getInt("mMaximumWidth");
        this.mMaximumHeight = savedState.getInt("mMaximumHeight");
        this.mFillingArea = FillingArea.values()[savedState.getInt("mFillingArea")];
        this.mFillingMode = FillingMode.values()[savedState.getInt("mFillingMode")];
        this.mFillingColors = FillingColors.values()[savedState.getInt("mFillingColors")];
    }


    /****************************************************************************************
     * Public methods
     */

    /**
     * Return the current path
     *
     * @return the current path
     */
    @SuppressWarnings("unused")
    public abstract Path getPath();

    /**
     * Return the paint shader.
     * This methods need to define what kind of shader using about coloring the draw path.
     * Is important do a distinction between the filling colors type as the case want have two
     * different type of filling: GRADIENT or SOLID.
     *
     * @return Return the shader
     */
    @SuppressWarnings("unused")
    public abstract Shader getPaintShader();

    /**
     * Return the painter
     *
     * @return The painter
     */
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mStrokePaint;
    }

    /**
     * Check if path contains a point.
     *
     * @param x         The x coordinate of the point
     * @param y         The y coordinate of the point
     * @param precision A float value for the precision
     * @return          True if the paint contain the passed point
     */
    @SuppressWarnings("unused")
    public boolean contains(float x, float y, float precision) {
        // IMPORTANT!
        // The calc of the point belonging to the path could make using the Path.op() methods.
        // But this method was introduced since API 19 and, at today, its seem lost around 20% of
        // Android users. So will implemented a custom methods for do this kind of test.

        // Check for empty value
        if (this.getPath() != null && !this.getPath().isEmpty()) {
            // Create a rectangle using the precision around the point and with this create a new
            // path.
            RectF area = new RectF(x - precision, y - precision, x + precision, y + precision);

            // Get the measure of the path.
            PathMeasure measure = new PathMeasure(this.getPath(), false);

            // Find the length of the path
            float len = measure.getLength();
            float distance = 0;

            // Cycle all the point of the path using an arbitrary increment
            while (distance < len) {
                // Define the point holder and get the point
                float[] point = new float[2];
                measure.getPosTan(distance, point, null);

                // Check if the point is contained within the reference rectangle and if true
                // return success and stop the cycle.
                if (area.contains(point[0], point[1])) return true;
            }
        }

        // Else return not found
        return false;
    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the stroke size
     *
     * @return The current stroke size in pixel
     */
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mStrokeSize;
    }

    /**
     * Set the stroke size
     *
     * @param value The new stroke size in pixel
     */
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

    /**
     * Return the current stroke colors
     *
     * @return The current stroke colors
     */
    @SuppressWarnings("unused")
    public int[] getStrokesColors() {
        return this.mStrokeColors;
    }

    /**
     * Set the current stroke colors
     *
     * @param value The new stroke colors
     */
    @SuppressWarnings("unused")
    public void setStrokeColors(int... value) {
            // Save the new value and refresh
            this.mStrokeColors = value;
            this.invalidate();
    }

    /**
     * Return the current area filling type.
     * This setting decide how the path will fit the drawing area.
     * If the setting is different from FillingArea.NONE the path will stretched to fit the
     * dimension specified.
     *
     * @return the filling area type
     */
    @SuppressWarnings("unused")
    public FillingArea getFillingArea() {
        return this.mFillingArea;
    }

    /**
     * Set the current area filling type.
     * This setting decide how the path will fit the drawing area.
     * If the setting is different from FillingArea.NONE the path will stretched to fit the
     * dimension specified.
     *
     * @param value Set filling area type
     */
    @SuppressWarnings("unused")
    public void setFillingArea(FillingArea value) {
        // Check if value is changed
        if (this.mFillingArea != value) {
            // Store the new value and refresh the component
            this.mFillingArea = value;
            this.invalidate();
        }
    }

    /**
     * Get the current area filling mode.
     * This setting tell to the class how drawing the path on canvas:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it.
     *
     * @return get the current filling mode
     */
    @SuppressWarnings("unused")
    public FillingMode getFillingMode() {
        return this.mFillingMode;
    }

    /**
     * Set the current area filling mode.
     * This setting tell to the class how drawing the path on canvas:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it.
     *
     * @param value the new filling mode
     */
    @SuppressWarnings("unused")
    public void setFillingMode(FillingMode value) {
        // Check if value is changed
        if (this.mFillingMode != value) {
            // Store the new value and refresh the component
            this.mFillingMode = value;
            this.invalidate();
        }
    }

    /**
     * Return the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @return The color filling mode
     */
    @SuppressWarnings("unused")
    public FillingColors getFillingColors() {
        return this.mFillingColors;
    }

    /**
     * Set the colors filling mode.
     * You can have to way for draw the colors of the path: SOLID or GRADIENT.
     *
     * @param value The new color filling mode
     */
    @SuppressWarnings("unused")
    public void setFillingColors(FillingColors value) {
        // Check if value is changed
        if (this.mFillingColors != value) {
            // Store the new value and refresh the component
            this.mFillingColors = value;
            this.invalidate();
        }
    }

    /**
     * Return the maximum width of the component
     *
     * @return The actual maximum value
     */
    @SuppressWarnings("unused")
    public int getMaximumWidth() {
        return this.mMaximumWidth;
    }

    /**
     * Set the maximum width of the component
     *
     * @param value The new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public void setMaximumWidth(int value) {
        // Check if value is changed
        if (this.mMaximumWidth != value) {
            // Store the new value
            this.mMaximumWidth = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    /**
     * Return the maximum height of the component
     *
     * @return The new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public int getMaximumHeight() {
        return this.mMaximumHeight;
    }

    /**
     * Set the maximum height of the component
     *
     * @param value The new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public void setMaximumHeight(int value) {
        // Check if value is changed
        if (this.mMaximumHeight != value) {
            // Store the new value
            this.mMaximumHeight = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

}
