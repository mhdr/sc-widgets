package com.sccomponents.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

/**
 * Create a series of notchs that follow a path
 *
 * @author Samuele Carassai
 * @version 2.0.0
 * @since 2016-05-30
 */
public class ScNotchs extends ScFeature {

    /****************************************************************************************
     * Enumerators
     */

    /**
     * Define the types of notchs can be draw
     */
    @SuppressWarnings("unused")
    public enum NotchTypes {
        LINE,
        CIRCLE,
        CIRCLE_FILLED
    }

    /**
     * Define the notchs position respect path
     */
    @SuppressWarnings("unused")
    public enum NotchPositions {
        INSIDE,
        MIDDLE,
        OUTSIDE
    }


    /****************************************************************************************
     * Private and protected variables
     */

    protected int mNotchsCount;
    protected float mNotchsLen;
    protected NotchTypes mNotchType;
    protected NotchPositions mNotchPosition;

    private OnDrawListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScNotchs(Path path) {
        // Super
        super(path);

        // Init
        this.mNotchsCount = 0;
        this.mNotchsLen = 0.0f;
        this.mNotchType = NotchTypes.LINE;
        this.mNotchPosition = NotchPositions.MIDDLE;
    }

    /****************************************************************************************
     * Private methods
     */

    /**
     * Reset the notch info.
     *
     * @param info the notch info
     */
    private void resetNotchInfo(NotchInfo info) {
        info.size = this.mPaint.getStrokeWidth();
        info.color = this.mPaint.getColor();
        info.length = this.mNotchsLen;
        info.offset = 0.0f;
        info.type = this.mNotchType;
        info.align = this.mNotchPosition;
        info.visible = true;
    }


    /****************************************************************************************
     * Draw methods
     *
     * ATTENTION!
     * In these methods I used to instantiate new objects and is preferable NOT do it for improve
     * the performance of the component drawing.
     * In case of low performance the first solution must be to move the new object creation in
     * the global scope for do it once.
     */

    /**
     * Draw a line.
     * I could use the Point class for a better coding but is always not instantiate classes
     * inside the draw method for a speed improvement.
     *
     * @param canvas the canvas to draw
     * @param center the notch center
     * @param angle  the angle of tangent
     * @param info   the notch info
     */
    private void drawLine(Canvas canvas, PointF center, float angle, NotchInfo info) {
        // Global offset
        float globalOffset = info.offset;
        if (info.align == NotchPositions.MIDDLE) globalOffset -= info.length / 2;
        if (info.align == NotchPositions.OUTSIDE) angle += Math.PI;

        // Find the start and end point to draw the line
        PointF first = new PointF(center.x, center.y);
        ScNotchs.translatePoint(first, globalOffset, angle);

        PointF second = new PointF(first.x, first.y);
        ScNotchs.translatePoint(second, info.length, angle);

        // Draw the line if the canvas is not null
        if (canvas != null) {
            canvas.drawLine(first.x, first.y, second.x, second.y, this.mPaint);
        }
    }

    /**
     * Draw a circle.
     * I could use the Point class for a better coding but is always not instantiate classes
     * inside the draw method for a speed improvement.
     *
     * @param canvas the canvas to draw
     * @param center the notch center
     * @param angle  the angle of tangent
     * @param info   the notch info
     */
    private void drawCircle(Canvas canvas, PointF center, float angle, NotchInfo info) {
        // Global offset
        float radius = info.length / 2;
        float globalOffset = info.offset;

        if (info.align == NotchPositions.INSIDE) globalOffset += radius;
        if (info.align == NotchPositions.OUTSIDE) globalOffset -= radius;

        // Apply the point offset
        ScNotchs.translatePoint(center, globalOffset, angle);

        // Draw the circle if the canvas is not null
        if (canvas != null) {
            canvas.drawCircle(center.x, center.y, radius, this.mPaint);
        }
    }

    /**
     * Draw a single notch.
     *
     * @param canvas where to draw
     * @param info   the notch info
     */
    private void drawNotch(Canvas canvas, NotchInfo info) {
        // Find the points info on the path
        float[] point = this.mPathMeasure.getContoursPosTan(info.distanceFromStart);
        if (point == null) return;

        // Find the perpendicular tangent angle in radiant
        float angle = point[3] + (float) Math.PI / 2;

        // Apply the current info settings to the painter
        this.mPaint.setStrokeWidth(info.size);
        this.mPaint.setColor(info.color);
        this.mPaint.setStyle(
                info.type == NotchTypes.CIRCLE_FILLED ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

        // Draw the notchs by the case
        switch (info.type) {
            // Draw a line
            case LINE:
                this.drawLine(canvas, ScNotchs.toPoint(point), angle, info);
                break;

            // Draw a circle
            case CIRCLE:
            case CIRCLE_FILLED:
                this.drawCircle(canvas, ScNotchs.toPoint(point), angle, info);
                break;
        }
    }

    /**
     * Draw all notchs on the path.
     *
     * @param canvas where to draw
     */
    private void drawNotchs(Canvas canvas) {
        // Refresh the measurer and get the path len and the step
        float step = this.mPathLength / this.mNotchsCount;

        // Define the notch info.
        // I use to create the object here for avoid to create they n times after.
        // Always inside the draw method is better not instantiate too much classes.
        NotchInfo info = new NotchInfo();
        info.source = this;

        // Convert the limits from percentages in distances
        float startLimit = (this.mPathLength * this.mStartPercentage) / 100.0f;
        float endLimit = (this.mPathLength * this.mEndPercentage) / 100.0f;

        // If the path is not closed add one notch to the beginning of path.
        int count = this.mNotchsCount +
                (this.mPathMeasure.getContoursCount() == 0 && this.mPathMeasure.isClosed() ? 0 : 1);

        // Cycle all notchs.
        for (int index = 0; index < count; index++) {
            // Define the notch info structure and fill with the local settings
            this.resetNotchInfo(info);
            info.index = index + 1;
            info.distanceFromStart = index * step;
            info.visible =
                    info.distanceFromStart >= startLimit && info.distanceFromStart <= endLimit;

            // Check if have a liked listener
            if (this.mOnDrawListener != null) {
                this.mOnDrawListener.onBeforeDrawNotch(info);
            }

            // Draw the single notch if visible
            if (info.visible) {
                this.drawNotch(canvas, info);
            }
        }
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * Draw method
     *
     * @param canvas where to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for empty value
        if (this.mNotchsCount == 0 || this.mPath == null)
            return;

        // Draw all notchs
        this.drawNotchs(canvas);
    }


    /****************************************************************************************
     * Public methods
     */

    /**
     * Return the point on path by the notch index.
     * Note that this method return always the point on the first contour of the path.
     *
     * @return the point on the path
     */
    @SuppressWarnings("unused")
    public PointF getPointOnPath(int index) {
        // Check the index limit
        if (index < 0 || index > this.mNotchsCount) return new PointF();

        // Get the path len and the step
        float step = this.mPathLength / this.mNotchsCount;

        // Find the points of path
        float[] point = this.mPathMeasure.getContoursPosTan(step * index);
        return point == null ? new PointF() : new PointF(point[0], point[1]);
    }


    /****************************************************************************************
     * Public classes and methods
     */

    /**
     * This is a structure to hold the notch information before draw it
     */
    @SuppressWarnings("unused")
    public class NotchInfo {

        public ScNotchs source;
        public float size;
        public float length;
        public int color;
        public int index;
        public float offset;
        public float distanceFromStart;
        public boolean visible;
        public NotchTypes type;
        public NotchPositions align;

    }

    /**
     * Round the value near the closed notch.
     *
     * @param value the value to round
     * @return a rounded to notch value
     */
    @SuppressWarnings("unused")
    public float snapToNotchs(float value) {
        // Check for empty values
        if (this.mNotchsCount == 0) return value;

        // Calc the delta angle and round at notchs value
        float deltaAngle = this.mPathLength / this.mNotchsCount;
        return Math.round(value / deltaAngle) * deltaAngle;
    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the notchs count.
     *
     * @return the notchs count
     */
    @SuppressWarnings("unused")
    public float getCount() {
        return this.mNotchsCount;
    }

    /**
     * Set the notchs count.
     *
     * @param value the notchs count
     */
    @SuppressWarnings("unused")
    public void setCount(int value) {
        if (value < 0) value = 0;
        this.mNotchsCount = value;
    }

    /**
     * Return the notchs length.
     *
     * @return the notchs count
     */
    @SuppressWarnings("unused")
    public float getLength() {
        return this.mNotchsLen;
    }

    /**
     * Set the notchs length.
     *
     * @param value the notchs count
     */
    @SuppressWarnings("unused")
    public void setLength(float value) {
        if (value < 0) value = 0;
        this.mNotchsLen = value;
    }

    /**
     * Return the notchs type.
     *
     * @return the notchs type
     */
    @SuppressWarnings("unused")
    public NotchTypes getType() {
        return this.mNotchType;
    }

    /**
     * Set the notchs type.
     *
     * @param value the notchs type
     */
    @SuppressWarnings("unused")
    public void setType(NotchTypes value) {
        this.mNotchType = value;
    }

    /**
     * Return the notchs alignment respect the path.
     *
     * @return the notchs alignment
     */
    @SuppressWarnings("unused")
    public NotchPositions getPosition() {
        return this.mNotchPosition;
    }

    /**
     * Set the notchs alignment respect the path.
     *
     * @param value the notchs alignment
     */
    @SuppressWarnings("unused")
    public void setPosition(NotchPositions value) {
        this.mNotchPosition = value;
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
         * Called before draw the single notch.
         *
         * @param info the notch info
         */
        void onBeforeDrawNotch(NotchInfo info);

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
