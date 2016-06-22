package com.sccomponents.widgets;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;

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
        this.mPathMeasure.getContoursSegment(startDistance, endDistance, this.mSegment, true);

        // Check the listener
        if (this.mOnDrawListener != null) {
            // Define the copy info
            CopyInfo info = new CopyInfo();
            info.source = this;
            info.scale = new PointF(1.0f, 1.0f);
            info.offset = new PointF(0.0f, 0.0f);

            // Call the listener method
            this.mOnDrawListener.onBeforeDrawCopy(info);

            // Apply the copy info changes to the segment
            this.mMatrix.postScale(info.scale.x, info.scale.y);
            this.mMatrix.postTranslate(info.offset.x, info.offset.y);
            this.mSegment.transform(this.mMatrix);
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
     * @param canvas where draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //Check the domain
        if (this.mPath == null || this.mStartPercentage == this.mEndPercentage)
            return;

        // Draw a copy
        this.drawCopy(canvas);
    }


    /****************************************************************************************
     * Public classes and methods
     */

    /**
     * This is a structure to hold the notch information before draw it
     */
    @SuppressWarnings("unused")
    public class CopyInfo {

        public ScCopier source;
        public PointF scale;
        public PointF offset;

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
