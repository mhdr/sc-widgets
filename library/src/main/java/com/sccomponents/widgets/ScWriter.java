package com.sccomponents.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * Write some token texts on the linked path.
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public class ScWriter extends ScFeature {

    /****************************************************************************************
     * Enumerators
     */

    /**
     * Define the notchs position respect path
     */
    @SuppressWarnings("unused")
    public enum TokenPositions {
        MIDDLE,
        INSIDE,
        OUTSIDE
    }


    /****************************************************************************************
     * Private variables
     */

    private String[] mTokens;
    private TokenPositions mTokenPosition;
    private PointF mTokenOffset;

    private boolean mUnbend;
    private boolean mConsiderFontMetrics;
    private boolean mLastTokenOnEnd;

    private ScPathMeasure mSegmentMeasure;
    private OnDrawListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScWriter(Path path) {
        // Super
        super(path);

        // Init
        this.mConsiderFontMetrics = true;
        this.mTokenPosition = TokenPositions.OUTSIDE;
        this.mTokenOffset = new PointF();
        this.mSegmentMeasure = new ScPathMeasure();

        // Update the painter
        this.mPaint.setStrokeWidth(0.0f);
        this.mPaint.setTextSize(16.0f);
        this.mPaint.setStyle(Paint.Style.FILL);
    }


    /****************************************************************************************
     * Private methods
     */

    /**
     * Reset the token info.
     *
     * @param info the token info
     */
    private void resetTokenInfo(TokenInfo info) {
        info.point = null;
        info.offset = new PointF(this.mTokenOffset.x, this.mTokenOffset.y);
        info.position = TokenPositions.OUTSIDE;
        info.unbend = this.mUnbend;
        info.color = this.mPaint.getColor();
        info.visible = true;
    }

    /**
     * Calculate the extra vertical offset by the text position respect to the path.
     *
     * @param info the token info
     * @return the extra vertical offset
     */
    private float getVerticalOffsetByPosition(TokenInfo info) {
        // Calc the text boundaries
        Rect bounds = new Rect();
        this.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);

        // Return the calculated offset
        switch (info.position) {
            case MIDDLE:
                return bounds.height() / 2;

            case INSIDE:
                return bounds.height();

            default:
                return 0.0f;
        }
    }

    /**
     * Calculate the extra vertical offset by the font metrics dimension.
     *
     * @param info the token info
     * @return the extra vertical offset
     */
    private float getVerticalOffsetByFontMetrics(TokenInfo info) {
        // Check if need to calculate the offset
        if (!this.mConsiderFontMetrics) return 0.0f;

        // Return the calculated offset
        switch (info.position) {
            case OUTSIDE:
                return this.mPaint.getFontMetrics().bottom;

            case INSIDE:
                return this.mPaint.getFontMetrics().top;

            default:
                return 0.0f;
        }
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
     * Draw the single token on canvas.
     *
     * @param canvas  the canvas where draw
     * @param info    the token info
     * @param segment the path segment to follow
     */
    private void drawToken(Canvas canvas, TokenInfo info, Path segment) {
        // Check for empty values
        if (info.point == null || info.offset == null || info.text == null)
            return ;

        // Apply the current settings to the painter
        this.mPaint.setColor(info.color);

        // Fix the vertical offset considering the position of the text on the path and the
        // font metrics offset.
        info.offset.y += this.getVerticalOffsetByPosition(info) -
                this.getVerticalOffsetByFontMetrics(info);

        // Check for null value
        if (canvas == null) return;

        // Save the canvas status and rotate by the calculated tangent angle
        canvas.save();
        canvas.rotate(info.angle, info.point.x, info.point.y);

        // Draw by the case
        if (this.mUnbend) {
            // Draw the straight text
            canvas.drawText(
                    info.text,
                    info.point.x + info.offset.x, info.point.y + info.offset.y,
                    this.mPaint
            );

        } else {
            // Draw the text on the path
            canvas.drawTextOnPath(
                    info.text,
                    segment,
                    info.offset.x, info.offset.y,
                    this.mPaint
            );
        }

        // Restore the canvas status
        canvas.restore();
    }

    /**
     * Find the last setting before draw.
     *
     * @param canvas the canvas where draw
     * @param info   the token info
     * @param step   the step
     */
    private void prepareTokenInfo(Canvas canvas, TokenInfo info, float step) {
        // Get the arc of path
        Path segment = new Path();
        this.mPathMeasure.getSegment(
                info.distance,
                info.distance + step,
                segment,
                true
        );

        // Calculate the distance from start starting consider the text align in the painter.
        // Take as RIGHT the initial value.
        this.mSegmentMeasure.setPath(segment, false);
        float distance = this.mSegmentMeasure.getLength();

        // If must write the last token on the end of path and this is the last token avoid to
        // consider the text align and hold the right position as true.
        if (!this.mLastTokenOnEnd || info.index != this.mTokens.length - 1) {
            // Choose by case
            if (this.getPainter().getTextAlign() == Paint.Align.LEFT) distance = 0.0f;
            if (this.getPainter().getTextAlign() == Paint.Align.LEFT) distance /= 2.0f;
        }

        // Find the point on path segment respect to the alignment type find the tangent angle
        // in degrees.
        float[] point = this.mSegmentMeasure.getPosTan(distance);
        if (point != null) {
            info.angle = (float) Math.toDegrees(this.mUnbend ? point[3] : 0.0f);
            info.point = ScWriter.toPoint(point);
        }

        // Check if have a liked listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawToken(info);
        }

        // Draw the token on the canvas
        if (info.visible) {
            this.drawToken(canvas, info, segment);
        }
    }

    /**
     * Draw all string token on the path.
     *
     * @param canvas where to draw
     */
    private void drawTokens(Canvas canvas) {
        // Check for empty value
        if (this.mTokens == null || this.mPath == null) return;

        // Get the step distance to cover all path
        int count = this.mTokens.length + (this.mLastTokenOnEnd ? -1 : 0);
        float step = this.mPathLength / (count > 0 ? count : 1);

        // Define the token info.
        // I use to create the object here for avoid to create they n times after.
        // Always inside the draw method is better not instantiate too much classes.
        TokenInfo info = new TokenInfo();
        info.source = this;

        // Convert the limits from percentages in distances
        float startLimit = (this.mPathLength * this.mStartPercentage) / 100.0f;
        float endLimit = (this.mPathLength * this.mEndPercentage) / 100.0f;

        // Cycle all token.
        for (int index = 0; index < this.mTokens.length; index++) {
            // Helper for last position
            boolean isLast = index == this.mTokens.length - 1;

            // Define the notch info structure and fill with the local settings
            this.resetTokenInfo(info);
            info.text = this.mTokens[index];
            info.index = index;
            info.distance = (index + (this.mLastTokenOnEnd && isLast ? -1 : 0)) * step;
            info.visible = info.distance >= startLimit && info.distance <= endLimit;
            info.color = this.getGradientColor(info.distance);

            // Draw the single token
            this.prepareTokenInfo(canvas, info, step);
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
    public void onDraw(Canvas canvas) {
        // Internal drawing
        this.drawTokens(canvas);
    }


    /****************************************************************************************
     * Public classes and methods
     */

    // TODO: revise the note

    /**
     * This is a structure to hold the token information before draw it
     * Note that the "point" represent the point from will start to draw.
     * Note that the "distance" is the distance from the oath starting.
     * Note that the "angle" is in degrees.
     */
    public class TokenInfo {

        public ScWriter source;
        public PointF point;
        public int index;
        public String text;
        public float distance;
        public float angle;
        public boolean unbend;
        public int color;
        public boolean visible;
        public PointF offset;
        public TokenPositions position;

    }

    /**
     * Set the global tokens offset.
     *
     * @param horizontal the horizontal offset
     * @param vertical   the vertical offset
     */
    @SuppressWarnings("unused")
    public void setTokenOffset(float horizontal, float vertical) {
        this.mTokenOffset.x = horizontal;
        this.mTokenOffset.y = vertical;
    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the string tokens.
     *
     * @return the tokens list
     */
    @SuppressWarnings("unused")
    public String[] getTokens() {
        return this.mTokens;
    }

    /**
     * Set the string tokens to draw on path.
     *
     * @param value the tokens list
     */
    @SuppressWarnings("unused")
    public void setTokens(String... value) {
        this.mTokens = value;
    }

    /**
     * Return the string tokens alignment respect the path.
     *
     * @return the notchs alignment
     */
    @SuppressWarnings("unused")
    public TokenPositions getPosition() {
        return this.mTokenPosition;
    }

    /**
     * Set the string tokens alignment respect the path.
     *
     * @param value the notchs alignment
     */
    @SuppressWarnings("unused")
    public void setPosition(TokenPositions value) {
        this.mTokenPosition = value;
    }

    /**
     * Return true if the text is unbend.
     *
     * @return the unbend status
     */
    @SuppressWarnings("unused")
    public boolean getUnbend() {
        return this.mUnbend;
    }

    /**
     * Set true to have a unbend text.
     *
     * @param value the unbend status
     */
    @SuppressWarnings("unused")
    public void setUnbend(boolean value) {
        this.mUnbend = value;
    }

    /**
     * Return true if the offset calculation consider the font metrics too.
     *
     * @return the current status
     */
    @SuppressWarnings("unused")
    public boolean getConsiderFontMetrics() {
        return this.mConsiderFontMetrics;
    }

    /**
     * Set true if want that the offset calculation consider the font metrics too.
     *
     * @param value the current status
     */
    @SuppressWarnings("unused")
    public void setConsiderFontMetrics(boolean value) {
        this.mConsiderFontMetrics = value;
    }

    /**
     * Return true if force to draw the last token on the end of the path.
     *
     * @return the current status
     */
    @SuppressWarnings("unused")
    public boolean getLastTokenOnEnd() {
        return this.mLastTokenOnEnd;
    }

    /**
     * Set true if want that the last token is forced to draw to the end of the path.
     *
     * @param value the current status
     */
    @SuppressWarnings("unused")
    public void setLastTokenOnEnd(boolean value) {
        this.mLastTokenOnEnd = value;
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
         * Called before draw the single token
         *
         * @param info the token info
         */
        void onBeforeDrawToken(TokenInfo info);

    }

    /**
     * Set the draw listener to call
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}

