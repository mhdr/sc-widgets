package com.sccomponents.widgets;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;

/**
 * Extend the PathMeasure because the original class not consider the contours in its totality.
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public class ScPathMeasure extends PathMeasure {

    /****************************************************************************************
     * Private variable
     */

    private Path mPath;
    private boolean mForceClosed;

    private RectF mContoursBounds;
    private float mContoursLength;
    private int mContoursCount;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScPathMeasure() {
        super();
    }

    @SuppressWarnings("unused")
    public ScPathMeasure(Path path, boolean forceClosed) {
        // Super
        super(path, forceClosed);

        // Init
        this.mPath = path;
        this.mForceClosed = forceClosed;

        // Get the path info
        this.getContoursInfo();
    }


    /****************************************************************************************
     * Private methods
     */

    /**
     * Internal method to get the path contours info.
     */
    private void getContoursInfo() {
        // Check for empty values
        if (this.mPath == null || this.mPath.isEmpty()) return;

        // Create the starting bounds and other holders
        this.mContoursBounds = new RectF(
                Float.MAX_VALUE, Float.MAX_VALUE,
                Float.MIN_VALUE, Float.MIN_VALUE
        );
        this.mContoursCount = 0;
        this.mContoursLength = 0.0f;

        // Cycle all paths
        do {
            // Find the length of the path
            float len = this.getLength();
            int distance = 0;

            // Increment the contours counter
            if (len > 0.0f) this.mContoursCount++;

            // Add the current length to the global length
            this.mContoursLength += len;

            // Cycle all the point of the path using an arbitrary increment
            while (distance < len) {
                // Define the point holder and get the point
                float[] point = new float[2];
                this.getPosTan(distance, point, null);

                // Check the position of the current point and update the bounds
                if (this.mContoursBounds.left > point[0]) this.mContoursBounds.left = point[0];
                if (this.mContoursBounds.right < point[0]) this.mContoursBounds.right = point[0];

                if (this.mContoursBounds.top > point[1]) this.mContoursBounds.top = point[1];
                if (this.mContoursBounds.bottom < point[1]) this.mContoursBounds.bottom = point[1];

                // Next point
                distance++;
            }

        } while (this.nextContour());

        // Reset the path and return the bounds
        super.setPath(this.mPath, this.mForceClosed);
    }


    /****************************************************************************************
     * Public methods
     */

    /**
     * Set the current path.
     *
     * @param path        the source path
     * @param forceClosed force to close
     */
    @Override
    public void setPath(Path path, boolean forceClosed) {
        // Super
        super.setPath(path, forceClosed);

        // Init
        this.mPath = path;
        this.mForceClosed = forceClosed;

        // Get the path info
        this.getContoursInfo();
    }

    /**
     * Get the contours count.
     * If the path changed you must recall a setPath to update this value.
     *
     * @return the contours count
     */
    @SuppressWarnings("unused")
    public int getContoursCount() {
        return this.mContoursCount;
    }

    /**
     * Get the length of a path considering all the contours.
     * If the path changed you must recall a setPath to update this value.
     *
     * @return the path length
     */
    @SuppressWarnings("unused")
    public float getContoursLength() {
        return this.mContoursLength;
    }

    /**
     * Get the path bounds.
     * Noted that this method consider all contours.
     * If the path changed you must recall a setPath to update this value.
     * <p>
     * As the computeBounds of the path object seem not work proper I must cycle point by point
     * of path for find the right path boundaries.
     *
     * @return the path boundaries
     */
    @SuppressWarnings("unused")
    protected RectF getBounds() {
        return this.mContoursBounds;
    }

    /**
     * Get the point and its tangent on the path considering all the contours.
     *
     * @param distance the point distance from the path start
     * @return the point structure with the coordinates, distance from start and tangent angle
     */
    @SuppressWarnings("unused")
    public float[] getContoursPosTan(float distance) {
        // Holders
        float[] pointStructure = null;
        float[] point = new float[2];
        float[] tangent = new float[2];
        float currentDistance = 0.0f;

        // Check for proper values
        if (distance >= 0)
            // Cycle all contours
            do {
                // add the current contour length to the current distance
                currentDistance += this.getLength();
                // Check if are on the right contour
                if (distance <= currentDistance) {
                    // Get the point and tangent
                    this.getPosTan(distance, point, tangent);

                    // Init the structure and fill it
                    if (pointStructure == null) pointStructure = new float[4];
                    pointStructure[0] = point[0];
                    pointStructure[1] = point[1];
                    pointStructure[2] = distance;
                    pointStructure[3] = (float) Math.atan2(tangent[1], tangent[0]);

                    // Exit from cycle
                    break;
                }

            } while (this.nextContour());

        // Reset the contours and return the result
        super.setPath(this.mPath, this.mForceClosed);
        return pointStructure;
    }

    /**
     * Extract the path segment(s) considering all the contours.
     *
     * @param startD          the start distance
     * @param stopD           the end distance
     * @param dst             the destination path
     * @param startWithMoveTo start with a moveTo calling
     * @return the new path
     */
    @SuppressWarnings("unused")
    public boolean getContoursSegment(float startD, float stopD, Path dst, boolean startWithMoveTo) {
        // Holders
        float currentDistance = 0.0f;

        // Check for proper values
        if (startD <= stopD) {
            // Cycle all contours
            do {
                // The current contour lengths
                float contourLen = this.getLength();
                float contourStart = currentDistance;
                float contourEnd = contourStart + contourLen;

                // Check if are on the right contour
                if (startD <= contourEnd) {
                    // The current start and end segment to take
                    float currStart = 0.0f;
                    float currEnd = 0.0f;

                    // Limit the values
                    if (contourEnd >= startD) {
                        currStart = startD - contourStart;
                        if (currStart < 0.0f) currStart = 0.0f;

                        currEnd = stopD - contourStart;
                        if (currEnd > contourLen) currEnd = contourLen;
                    }

                    // If need take the segment
                    if (currStart < currEnd) {
                        // Extract the segment
                        Path segment = new Path();
                        this.getSegment(currStart, currEnd, segment, startWithMoveTo);

                        // On KITKAT and earlier releases, the resulting path may not display on a
                        // hardware-accelerated Canvas. A simple workaround is to add a single
                        // operation to this path segment.
                        segment.rLineTo(0, 0);

                        // Add the extracted segment to the destination path
                        dst.addPath(segment);
                    }
                }

                // Update the global distance
                currentDistance = contourEnd;

            } while (this.nextContour());
        }

        // Reset the contours and return the result
        super.setPath(this.mPath, this.mForceClosed);
        return !dst.isEmpty();
    }

    /**
     * Find the point nearest to the one passed.
     * Considering only the points inside the area defined by the threshold parameter.
     * Noted that this method consider all contours.
     * <p>
     * Return a structure with the following position values:
     * 0 - x point coordinate
     * 1 - y point coordinate
     * 2 - point distance from the path starting
     * 3 - measure in radiant of the tangent angle
     *
     * @param x         the x of point
     * @param y         the y of point
     * @param threshold the threshold to define the checking area
     * @return the nearest point structure with the coordinates, distance from start and tangent angle
     */
    @SuppressWarnings("unused")
    public float[] findNearestPointOnPath(float x, float y, float threshold) {
        // Find the rectangle around the point
        float left = x - threshold;
        float top = y - threshold;
        float right = x + threshold;
        float bottom = y + threshold;

        // Define the points holder
        float[] point = new float[2];
        float[] tangent = new float[2];

        // Nearest
        float[] nearest = null;
        // Cycle all contours
        do {
            // Find the length of the path
            float len = this.getLength();
            int distance = 0;

            // Cycle all the point of the path using an arbitrary increment
            while (distance < len) {
                // Get the points position on the path
                this.getPosTan(distance, point, tangent);

                // Check if the threshold is infinite or the point is contained within the
                // referenced rectangle
                if (left <= point[0] && right >= point[0] &&
                        top <= point[1] && bottom >= point[1]) {
                    // Trigger to assign the nearest value
                    boolean toAssign = nearest == null;
                    if (!toAssign) {
                        // Calculate the distances from found point
                        float currentPointsDistance = (float) Math
                                .sqrt(Math.pow(x - point[0], 2) + Math.pow(y - point[1], 2));
                        float nearestPointsDistance = (float) Math
                                .sqrt(Math.pow(x - nearest[0], 2) + Math.pow(y - nearest[1], 2));

                        // If the current distance is less than the nearest point distance the
                        // nearest point must be reassigned
                        toAssign = currentPointsDistance < nearestPointsDistance;
                    }

                    // Check if must be assigned
                    if (toAssign) {
                        // If null init
                        if (nearest == null) nearest = new float[4];
                        // Assign the current point to the nearest
                        nearest[0] = point[0];
                        nearest[1] = point[1];
                        nearest[2] = distance;
                        nearest[3] = (float) Math.atan2(tangent[1], tangent[0]);
                    }
                }

                // Next point
                distance++;
            }
        } while (this.nextContour());

        // Reset the path and return
        super.setPath(this.mPath, this.mForceClosed);
        return nearest;
    }

    /**
     * Check if the passed point is on the path.
     * The threshold parameter define the checking tolerance.
     * Noted that this method take valid all contours.
     *
     * @param x         the x of point
     * @param y         the y of point
     * @param threshold the threshold to define the checking area
     * @return true if contain
     */
    @SuppressWarnings("unused")
    public boolean contains(float x, float y, float threshold) {
        // Check if have
        return this.findNearestPointOnPath(x, y, threshold) != null;
    }

    /**
     * Get the point distance from the path start.
     * Noted that this method take valid all contours.
     * Instead using this method you can also use findNearestPointOnPath and control the distance
     * value inserted in the returned array structure.
     *
     * @param x the x of point
     * @param y the y of point
     * @return the calculated distance
     */
    @SuppressWarnings("unused")
    public float getDistance(float x, float y, float threshold) {
        // Searching for point
        float[] details = this.findNearestPointOnPath(x, y, threshold);
        // Return -1 if not find correspondence
        return details == null ? -1.0f : details[2];
    }

    /**
     * Get the point distance from the path start.
     * Noted that this method take valid all contours.
     * Instead using this method you can also use findNearestPointOnPath and control the distance
     * value inserted in the returned array structure.
     *
     * @param x the x of point
     * @param y the y of point
     * @return the calculated distance
     */
    @SuppressWarnings("unused")
    public float getDistance(float x, float y) {
        return this.getDistance(x, y, 0.0f);
    }

    /**
     * Get the first point of the path.
     * Note that this method consider all contours so get the first point of the first contour.
     *
     * @return The first point
     */
    @SuppressWarnings("unused")
    public float[] getFirstPoint() {
        // Reset the contours
        super.setPath(this.mPath, this.mForceClosed);

        // Get the first point
        float[] point = new float[2];
        this.getPosTan(0.0f, point, null);

        // Return the point
        return point;
    }

    /**
     * Get the last point on path.
     * Note that this method consider all contours so get the last point of the last contour.
     *
     * @return The last point
     */
    @SuppressWarnings("unused")
    public float[] getLastPoint() {
        // Get the path length and the last point on path
        float length = this.getContoursLength();
        float[] last = this.getContoursPosTan(length);
        // Return the point converted in a simple point structure
        return new float[]{last[0], last[1]};
    }

}
