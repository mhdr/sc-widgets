package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

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
    @SuppressWarnings("unuse")
    public enum FillingArea {
        NONE,
        BOTH,
        HORIZONTAL,
        VERTICAL
    }

    /**
     * The area filling mode.
     */
    @SuppressWarnings("unuse")
    public enum FillingMode {
        STRETCH,
        DRAW
    }


    /****************************************************************************************
     * Private and protected attributes
     */

    protected float mStrokeSize;
    protected int mStrokeColor;

    protected FillingArea mFillingArea;
    protected FillingMode mFillingMode;

    protected int mMaximumWidth;
    protected int mMaximumHeight;


    /****************************************************************************************
     * Private and protected variables
     */

    protected Path mPath;
    protected ScPathMeasure mPathMeasure;

    protected RectF mDrawArea;
    protected RectF mVirtualArea;
    protected PointF mAreaScale;

    private List<ScFeature> mFeatures;
    private Path mCopyPath;
    private Paint mStrokePaint;
    private Paint mSectorPaint;


    /****************************************************************************************
     * Constructors
     */

    // Constructor
    public ScDrawer(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    // Constructor
    public ScDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    // Constructor
    public ScDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    /****************************************************************************************
     * Abstract methods
     */

    /**
     * Create the path to draw.
     * This method need to draw something on the canvas. Note that the ScDrawer class not expose
     * other methods or public properties to manage the path.
     * To work on path you can use the protected properties: mPath and mPathMeasurer.
     *
     * @param maxWidth  the horizontal boundaries
     * @param maxHeight the vertical boundaries
     * @return return the new path
     */
    @SuppressWarnings("unused")
    protected abstract Path createPath(int maxWidth, int maxHeight);


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
        this.mStrokeColor = attrArray.getColor(
                R.styleable.ScComponents_scc_stroke_color, ScDrawer.DEFAULT_STROKE_COLOR);

        this.mMaximumWidth = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_width, Integer.MAX_VALUE);
        this.mMaximumHeight = attrArray.getDimensionPixelSize(
                R.styleable.ScComponents_scc_max_height, Integer.MAX_VALUE);

        int fillingArea = attrArray.getInt(
                R.styleable.ScComponents_scc_fill_area, FillingArea.BOTH.ordinal());
        this.mFillingArea = FillingArea.values()[fillingArea];

        int fillingMode = attrArray.getInt(
                R.styleable.ScComponents_scc_fill_mode, FillingMode.DRAW.ordinal());
        this.mFillingMode = FillingMode.values()[fillingMode];

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();
        this.mPathMeasure = new ScPathMeasure();
        this.mCopyPath = new Path();

        //--------------------------------------------------
        // PAINTS

        // Stroke
        this.mStrokePaint = new Paint();
        this.mStrokePaint.setColor(this.mStrokeColor);
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setDither(true);
        this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
        this.mStrokePaint.setStyle(Paint.Style.STROKE);
        this.mStrokePaint.setStrokeCap(Paint.Cap.BUTT);

        this.mSectorPaint = new Paint();
        this.mSectorPaint.setAntiAlias(true);
        this.mSectorPaint.setDither(true);
        this.mSectorPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Get the drawable area.
     *
     * @param width  the reference width
     * @param height the reference height
     * @return a rectangle that represent the area
     */
    private RectF getDrawableArea(int width, int height) {
        // Create the area and transpose it by the component padding
        RectF area = new RectF(0, 0, width, height);
        area.offset(this.getPaddingLeft(), this.getPaddingTop());
        // Return the calculated area
        return area;
    }

    /**
     * Calculate the virtual drawing area.
     * This area is calculated starting from the trimmed path area and expanded proportionally
     * by the stretch setting to cover the component drawing area.
     *
     * @param width  the reference width
     * @param height the reference height
     * @return a rectangle that represent the area
     */
    private RectF getVirtualArea(int width, int height) {
        // Check for empty values
        RectF pathBounds = this.mPathMeasure.getBounds();
        if (pathBounds == null || pathBounds.isEmpty()) return new RectF();

        // Create the starting area
        RectF area = new RectF(0, 0, width, height);

        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT ||
                (this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.HORIZONTAL)) {
            // Center the area
            area.offset(-pathBounds.left, 0);

            // Find the horizontal scale and apply it
            float xScale = (float) width / pathBounds.width();
            area.left *= xScale;
            area.right *= xScale;
        }

        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT ||
                (this.mFillingArea == FillingArea.BOTH || this.mFillingArea == FillingArea.VERTICAL)) {
            // Center the area
            area.offset(0, -pathBounds.top);

            // Find the vertical scale and apply it
            float yScale = (float) height / pathBounds.height();
            area.top *= yScale;
            area.bottom *= yScale;
        }

        return area;
    }

    /**
     * Given a source rectangle and a destination one calculate the scale.
     *
     * @param source      the source rectangle
     * @param destination the destination rectangle
     * @return the scale
     */
    private PointF getScale(RectF source, RectF destination) {
        // Check for empty values
        if (source == null || source.isEmpty() ||
                destination == null || destination.isEmpty()) return new PointF();

        // Calculate the scale
        return new PointF(
                source.width() / destination.width(),
                source.height() / destination.height()
        );
    }

    /**
     * Fix the scales of the path by filling mode settings.
     *
     * @param source the source path
     * @param xScale the horizontal scale
     * @param yScale the vertical scale
     */
    private void scalePath(Path source, float xScale, float yScale) {
        // Check for empty value
        if (source == null) return;

        // Create a matrix and apply the new scale
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);

        // Apply the scale
        source.transform(matrix);
    }


    /****************************************************************************************
     * Draw methods
     */

    /**
     * Draw all the features
     *
     * @param canvas the canvas where draw
     */
    private void drawFeatures(Canvas canvas) {
        // Check for empty values
        if (this.mFeatures != null) {
            // Cycle all features
            for (ScFeature feature : this.mFeatures) {
                //Call the draw methods.
                feature.draw(canvas);
            }
        }
    }

    /**
     * Scale and transpose the path and after draw it on canvas
     *
     * @param canvas  the canvas where draw
     * @param xOffset the horizontal offset
     * @param yOffset the vertical offset
     */
    private void draw(Canvas canvas, float xOffset, float yOffset) {
        // Scale and move the path
        this.scalePath(this.mCopyPath, this.mAreaScale.x, this.mAreaScale.y);
        this.mCopyPath.offset(
                xOffset + this.getPaddingLeft(),
                yOffset + this.getPaddingTop()
        );

        // Draw the path and the features
        canvas.drawPath(this.mCopyPath, this.mStrokePaint);
        this.drawFeatures(canvas);
    }

    /**
     * Scale and transpose the canvas and after draw the path on it
     *
     * @param canvas the canvas where draw
     */
    private void stretch(Canvas canvas) {
        // Save the current canvas status
        canvas.save();

        // Translate and scale the canvas
        canvas.translate(this.getPaddingLeft(), this.getPaddingTop());
        canvas.scale(this.mAreaScale.x, this.mAreaScale.y);

        // Translate the path
        this.mCopyPath.offset(
                -this.mPathMeasure.getBounds().left,
                -this.mPathMeasure.getBounds().top
        );

        // Draw the path if needed
        if (this.mStrokeSize > 0.0f || this.mStrokePaint.getStyle() != Paint.Style.STROKE) {
            canvas.drawPath(this.mCopyPath, this.mStrokePaint);
        }

        // Draw all features
        this.drawFeatures(canvas);

        // Restore the last saved canvas status
        canvas.restore();
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * This method is used to calc the areas and filling it by call/set the right draw plan.
     * Are to consider two type of draw:
     * - DRAW: Scale and transpose the path and after draw it on canvas
     * - STRETCH: Scale and transpose the canvas and after draw the path on it
     *
     * @param canvas the view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for empty values
        if (this.mPath == null ||
                (this.mDrawArea == null || this.mDrawArea.isEmpty())) return;

        // Set the painter properties
        this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
        this.mStrokePaint.setColor(this.mStrokeColor);

        // Create a copy of the original path.
        // I need to move the offset or scale the path and not want lost the original one values.
        this.mCopyPath.set(this.mPath);

        // Select the drawing mode by the case
        switch (this.mFillingMode) {
            // Draw
            case DRAW:
                this.draw(canvas, this.mVirtualArea.left, this.mVirtualArea.top);
                break;

            // Stretch
            case STRETCH:
                this.stretch(canvas);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Find the global padding
        int widthGlobalPadding = this.getPaddingLeft() + this.getPaddingRight();
        int heightGlobalPadding = this.getPaddingTop() + this.getPaddingBottom();

        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // Force to re-create the path passing the real dimensions to draw and get the measurer
        this.mPath = this.createPath(width - widthGlobalPadding, height - heightGlobalPadding);
        this.mPathMeasure.setPath(this.mPath, false);

        // If have some dimension to wrap will use the path boundaries for have the right
        // dimension summed to the global padding.
        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT)
            width = (int) this.mPathMeasure.getBounds().width() + widthGlobalPadding;
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
            height = (int) this.mPathMeasure.getBounds().height() + heightGlobalPadding;

        // Get all area info that we need to hold
        this.mDrawArea = this
                .getDrawableArea(width - widthGlobalPadding, height - heightGlobalPadding);
        this.mVirtualArea = this
                .getVirtualArea(width - widthGlobalPadding, height - heightGlobalPadding);
        this.mAreaScale = this.getScale(this.mVirtualArea, this.mDrawArea);

        // Fix the component dimensions limits
        width = ScDrawer.valueRangeLimit(width, 0, this.mMaximumWidth);
        height = ScDrawer.valueRangeLimit(height, 0, this.mMaximumHeight);

        // Set the calculated dimensions
        this.setMeasuredDimension(width, height);
    }


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
        state.putFloat("mStrokeSize", this.mStrokeSize);
        state.putInt("mStrokeColor", this.mStrokeColor);
        state.putInt("mMaximumWidth", this.mMaximumWidth);
        state.putInt("mMaximumHeight", this.mMaximumHeight);
        state.putInt("mFillingArea", this.mFillingArea.ordinal());
        state.putInt("mFillingMode", this.mFillingMode.ordinal());

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
        this.mStrokeSize = savedState.getFloat("mStrokeSize");
        this.mStrokeColor = savedState.getInt("mStrokeColor");
        this.mMaximumWidth = savedState.getInt("mMaximumWidth");
        this.mMaximumHeight = savedState.getInt("mMaximumHeight");
        this.mFillingArea = FillingArea.values()[savedState.getInt("mFillingArea")];
        this.mFillingMode = FillingMode.values()[savedState.getInt("mFillingMode")];
    }


    /****************************************************************************************
     * Features
     */

    /**
     * Add one feature to this drawer.
     *
     * @param feature the new feature to add to the drawer
     */
    @SuppressWarnings("unused")
    public void addFeature(ScFeature feature) {
        // Check for null value
        if (feature == null) return;

        // Check if the holder is null
        if (this.mFeatures == null) {
            // Create an empty list
            this.mFeatures = new ArrayList<>();
        }

        // Check if already in
        if (!this.mFeatures.contains(feature)) {
            // Add the feature and refresh the component
            this.mFeatures.add(feature);
            this.invalidate();
        }
    }

    /**
     * Add one feature to this drawer.
     * This particular overload instantiate a new object from the class reference passed.
     * The passed class reference must implement the ScFeature interface.
     *
     * @param classRef the class reference to instantiate
     * @return the new feature object
     */
    @SuppressWarnings("unused")
    public ScFeature addFeature(Class<?> classRef) {
        // Manage the possible exception
        try {
            // Try to instantiate a new class
            ScFeature feature = (ScFeature) classRef
                    .getDeclaredConstructor(Path.class)
                    .newInstance(this.mCopyPath);

            // Call the base method and return the new object
            this.addFeature(feature);
            return feature;

        } catch (Exception ex) {
            // If the passed class reference not inherit from the ScFeature return null.
            // Maybe better to thrown a exception but this mean manage the exception on the
            // method declaration.
            return null;
        }
    }

    /**
     * Remove a feature from this drawer.
     *
     * @param feature the feature to remove
     * @return true if removed
     */
    @SuppressWarnings("unused")
    public boolean removeFeature(ScFeature feature) {
        // Check if the feature list contain this
        if (this.mFeatures != null && this.mFeatures.contains(feature)) {
            // Remove and return true
            boolean result = this.mFeatures.remove(feature);
            this.invalidate();
            return result;
        }
        // Else return false
        return false;
    }

    /**
     * Remove all feature from this drawer.
     */
    @SuppressWarnings("unused")
    public void removeAllFeatures() {
        // Check if the feature list contain this
        if (this.mFeatures != null) {
            // Remove all and refresh the component
            this.mFeatures.clear();
            this.invalidate();
        }
    }

    /**
     * Find all features that corresponds to a class and tag reference.
     * If the class reference is null the class will be not consider.
     * Same behavior for the tag param.
     *
     * @param classRef the class reference to compare
     * @param tag      the tag reference to compare
     * @return the features found
     */
    @SuppressWarnings("unused")
    public List<ScFeature> findFeatures(Class<?> classRef, String tag) {
        // Holder
        List<ScFeature> founds = new ArrayList<>();

        // Check for empty value
        if (this.mFeatures != null) {
            // Cycle all features
            for (ScFeature feature : this.mFeatures) {
                // Check the instance or add all features if the class reference is null
                if ((classRef == null || feature.getClass().isAssignableFrom(classRef)) &&
                        tag == null || feature.getTag().equalsIgnoreCase(tag)) {
                    // Add the feature to the list
                    founds.add(feature);
                }
            }
        }
        // Return the founds list
        return founds;
    }


    /****************************************************************************************
     * Public methods
     */

    /**
     * Return the painter
     *
     * @return the painter
     */
    @SuppressWarnings("unused")
    public Paint getPainter() {
        return this.mStrokePaint;
    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the stroke size
     *
     * @return the current stroke size in pixel
     */
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mStrokeSize;
    }

    /**
     * Set the stroke size
     *
     * @param value the new stroke size in pixel
     */
    @SuppressWarnings("unused")
    public void setStrokeSize(float value) {
        // Check if value is changed
        if (this.mStrokeSize != value) {
            // Store the new value, check it and refresh the component
            this.mStrokeSize = value;
            this.checkValues();
            this.requestLayout();
        }
    }

    /**
     * Return the current stroke color
     *
     * @return the current stroke color
     */
    @SuppressWarnings("unused")
    public int getStrokesColors() {
        return this.mStrokeColor;
    }

    /**
     * Set the current stroke colors
     *
     * @param value the new stroke colors
     */
    @SuppressWarnings("unused")
    public void setStrokeColors(int value) {
        // Save the new value and refresh
        this.mStrokeColor = value;
        this.requestLayout();
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
     * @param value set filling area type
     */
    @SuppressWarnings("unused")
    public void setFillingArea(FillingArea value) {
        // Check if value is changed
        if (this.mFillingArea != value) {
            // Store the new value and refresh the component
            this.mFillingArea = value;
            this.requestLayout();
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
            this.requestLayout();
        }
    }

    /**
     * Return the maximum width of the component
     *
     * @return the actual maximum value
     */
    @SuppressWarnings("unused")
    public int getMaximumWidth() {
        return this.mMaximumWidth;
    }

    /**
     * Set the maximum width of the component
     *
     * @param value the new maximum value in pixel
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
     * @return the new maximum value in pixel
     */
    @SuppressWarnings("unused")
    public int getMaximumHeight() {
        return this.mMaximumHeight;
    }

    /**
     * Set the maximum height of the component
     *
     * @param value the new maximum value in pixel
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
