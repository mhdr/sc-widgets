package com.sccomponents.widgets;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;

import com.sccomponents.utils.ScObserver;

/**
 * Create a copy of the path.
 * The most important of this feature is that can decide the path segment to draw.
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public class ScCopier extends ScFeature {

    /****************************************************************************************
     * Private and protected variables
     */

    private Path mSegment;
    private Matrix mMatrix;
    private Shader mShader;

    private ScObserver mPaintObserver;
    private ScObserver mPathObserver;

    private OnDrawListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScCopier(Path path) {
        // Super
        super(path);

        // Init
        this.mSegment = new Path();
        this.mMatrix = new Matrix();

        // Observers
        this.mPathObserver = new ScObserver(this.mPath);
        this.mPaintObserver = new ScObserver(this.mPaint);
    }


    /****************************************************************************************
     * Shader
     */

    /**
     * Create the paint shader.
     * This methods need to define what kind of shader filling to coloring the draw path.
     * Essentially there are two different mode of filling: GRADIENT or SOLID.
     * <p/>
     * Note that this method was created to be a generic method for work proper on all path but
     * could be slow given the big amount of calculations. So in any case may be better to create
     * a custom shader to attach directly to the painter.
     * <p/>
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
        RectF bounds = this.mPathMeasure.getBounds();
        Bitmap bitmap = Bitmap.createBitmap(
                (int) bounds.right, (int) bounds.bottom, Bitmap.Config.ARGB_8888);

        // Retrieve the canvas where draw and create the painter to use
        Canvas canvas = new Canvas(bitmap);
        Paint clonePaint = new Paint(this.mPaint);

        // Hold if a rounded stroke and the point holder
        boolean isRoundedStroke = this.mPaint.getStrokeCap() == Paint.Cap.ROUND;
        float[] point;

        // Cycle all points of the path
        for (int distance = 0; distance < this.mPathLength; distance++) {
            // Get the point and check for null value
            point = this.mPathMeasure.getPosTan(distance);

            // Trigger for index position and get the color
            boolean isFirstOrLast = distance == 0 || distance == this.mPathLength - 1;
            int color = this.getGradientColor(distance);

            // Set the current painter color and stroke
            clonePaint.setColor(color);
            clonePaint.setStrokeCap(
                    isRoundedStroke && isFirstOrLast ? Paint.Cap.ROUND : Paint.Cap.BUTT);

            // If the round stroke is not settled the point have a square shape.
            // This can create a visual issue when the path follow a curve.
            // To avoid this issue the point (square) will be rotate of the tangent angle
            // before to write it on the canvas.
            canvas.save();
            canvas.rotate((float) Math.toDegrees(point[3]), point[0], point[1]);
            canvas.drawPoint(point[0], point[1], clonePaint);
            canvas.restore();
        }

        // Return the shader
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }


    /****************************************************************************************
     * Draw methods
     */

    private void drawCopy(Canvas canvas) {
        // Convert the percentage values in distance referred to the current path length.
        float startDistance = (this.mPathLength * this.mStartPercentage) / 100.0f;
        float endDistance = (this.mPathLength * this.mEndPercentage) / 100.0f;

        // Extract the segment to draw
        this.mSegment.reset();
        this.mPathMeasure.getSegment(startDistance, endDistance, this.mSegment, true);

        // Check the listener
        if (this.mOnDrawListener != null) {
            // Define the copy info
            CopyInfo info = new CopyInfo();
            info.source = this;
            info.scale = new PointF(1.0f, 1.0f);
            info.offset = new PointF(0.0f, 0.0f);

            // Call the listener method
            this.mOnDrawListener.onBeforeDrawCopy(info);

            // Define the matrix to transform the path and the shader
            this.mMatrix.postScale(info.scale.x, info.scale.y);
            this.mMatrix.postTranslate(info.offset.x, info.offset.y);
            this.mMatrix.postRotate(
                    info.rotate,
                    this.mPathMeasure.getBounds().centerX(),
                    this.mPathMeasure.getBounds().centerY()
            );

            // Apply
            if (this.mShader != null) this.mShader.setLocalMatrix(this.mMatrix);
            if (this.mSegment != null) this.mSegment.transform(this.mMatrix);
        }

        // Draw only a path segment if the canvas is not null
        if (canvas != null) {
            canvas.drawPath(this.mSegment, this.mPaint);
        }
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * Draw method
     *
     * @param canvas where draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //Check the domain
        if (this.mPath == null || this.mStartPercentage == this.mEndPercentage)
            return;

        // Refresh the shader if needed
        if (this.mPathObserver.isChanged() || this.mPaintObserver.isChanged()) {
            this.mShader = this.createShader();
            this.mPaint.setShader(this.mShader);
        }

        // Draw a copy
        this.drawCopy(canvas);
    }


    /****************************************************************************************
     * Public classes and methods
     */

    /**
     * This is a structure to hold the notch information before draw it.
     * Note that the "rotate" angle is in degrees.
     */
    @SuppressWarnings("unused")
    public class CopyInfo {

        public ScCopier source;
        public PointF scale;
        public PointF offset;
        public float rotate;

    }


    /****************************************************************************************
     * Listeners and Interfaces
     */

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        /**
         * Called before draw the path copy.
         *
         * @param info the copier info
         */
        void onBeforeDrawCopy(CopyInfo info);

    }

    /**
     * Set the draw listener to call.
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}
