package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


/**
 * SC Circular Seek Bar
 */
public class ScCircularSeekBar extends View {

    /**
     * Constants
     */

    private static final int HALO_ALPHA = 128;


    /**
     * Private attributes
     */

    private int mStartAngle = 0;
    private int mSweepAngle = 360;

    private int mMinValue = 0;
    private int mMaxValue = 0;
    private int mValue = 0;

    private int mNotchSize = 0;
    private int mPointerRadius = 0;
    private int mHaloSize = 0;
    private int mStrokeSize = 0;

    private int mStrokeColor = Color.BLACK;
    private int mPointerColor = Color.BLACK;

    private boolean mShowProgress = true;

    private int mMaxWidth = 0;
    private int mMaxHeight = 0;

    /**
     * Private variables
     */

    private Rect mDrawArea = null;

    private Paint mArcPaint = null;
    private Paint mNotchPaint = null;
    private Paint mPointerPaint = null;
    private Paint mHaloPaint = null;
    private Paint mProgressPaint = null;

    private int mInternalMaxValue = 0;
    private boolean mDrag = false;

    private OnChangeValueListener mOnChangeListener = null;
    private OnCustomDrawListener mOnCustomDrawListener = null;
    private OnDragPointerListener mOnDragPointerListener = null;


    /**
     * Constructors
     */

    public ScCircularSeekBar(Context context) {
        super(context);
        this.init(context, null, 0);
        this.createPaints();
    }

    public ScCircularSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
        this.createPaints();
    }

    public ScCircularSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        this.createPaints();
    }


    /**
     * Privates methods
     */

    // Check all input values
    private void checkValues() {
        // Size
        if (this.mNotchSize < 0) this.mNotchSize = 0;
        if (this.mPointerRadius < 0) this.mPointerRadius = 0;
        if (this.mHaloSize < 0) this.mHaloSize = 0;
        if (this.mStrokeSize < 0) this.mStrokeSize = 0;

        // Angle
        if (this.mSweepAngle > 360) this.mSweepAngle = this.mSweepAngle % 360;

        // Dimension
        if (this.mMaxWidth < 0) this.mMaxWidth = 0;
        if (this.mMaxHeight < 0) this.mMaxHeight = 0;

        // Values
        if (this.mMinValue == 0 && this.mMaxValue == 0) {
            if (this.mSweepAngle < 0) {
                this.mMinValue = this.mSweepAngle;
            } else {
                this.mMaxValue = this.mSweepAngle;
            }
        }
    }

    // Calc and fix values
    private void calcValues() {
        // Calc
        this.mInternalMaxValue = this.mMaxValue - this.mMinValue;
        this.mValue = this.mValue + Math.abs(this.mMinValue);

        // Fix
        if (this.mValue < 0) this.mValue = 0;
        if (this.mValue > this.mInternalMaxValue) this.mValue = this.mInternalMaxValue;
    }

    // Get the display metric
    private DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    // Convert Dip to Pixel
    public int dpToPixel(Context context, int dp) {
        DisplayMetrics metrics = this.getDisplayMetrics(context);
        return (int) (dp * metrics.density);
    }

    // Init the component
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScCirclularSeekBar, defStyle, 0);

        // Read all attributes from xml and assign the value to linked variables
        this.mStartAngle = attrArray.getInt(R.styleable.ScCirclularSeekBar_sccsb_start_angle, 0);
        this.mSweepAngle = attrArray.getInt(R.styleable.ScCirclularSeekBar_sccsb_sweep_angle, 360);

        this.mMinValue = attrArray.getInt(R.styleable.ScCirclularSeekBar_sccsb_min, 0);
        this.mMaxValue = attrArray.getInt(R.styleable.ScCirclularSeekBar_sccsb_max, 0);
        this.mValue = attrArray.getInt(R.styleable.ScCirclularSeekBar_sccsb_value, 0);

        this.mNotchSize = attrArray.getDimensionPixelSize(R.styleable.ScCirclularSeekBar_sccsb_notch_size, 0);
        this.mPointerRadius = attrArray.getDimensionPixelSize(R.styleable.ScCirclularSeekBar_sccsb_pointer_radius, this.dpToPixel(context, 10));
        this.mHaloSize = attrArray.getDimensionPixelSize(R.styleable.ScCirclularSeekBar_sccsb_halo_size, this.dpToPixel(context, 5));
        this.mStrokeSize = attrArray.getDimensionPixelSize(R.styleable.ScCirclularSeekBar_sccsb_stroke_size, this.dpToPixel(context, 3));

        this.mStrokeColor = attrArray.getColor(R.styleable.ScCirclularSeekBar_sccsb_stroke_color, Color.BLACK);
        this.mPointerColor = attrArray.getColor(R.styleable.ScCirclularSeekBar_sccsb_pointer_color, Color.parseColor("#3F9BBF"));

        this.mShowProgress = attrArray.getBoolean(R.styleable.ScCirclularSeekBar_sccsb_show_progress, true);

        this.mMaxWidth = attrArray.getDimensionPixelSize(R.styleable.ScCirclularSeekBar_sccsb_max_width, 0);
        this.mMaxHeight = attrArray.getDimensionPixelSize(R.styleable.ScCirclularSeekBar_sccsb_max_height, 0);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();
        this.calcValues();


        //--------------------------------------------------
        // EVENTS

        // Enable for touch
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
    }

    // Create the paints
    private void createPaints() {
        // Create arc paint
        this.mArcPaint = new Paint();
        this.mArcPaint.setColor(this.mStrokeColor);
        this.mArcPaint.setAntiAlias(true);
        this.mArcPaint.setStrokeWidth(this.mStrokeSize);
        this.mArcPaint.setStyle(Paint.Style.STROKE);
        this.mArcPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Create the notch paint
        this.mNotchPaint = new Paint();
        this.mNotchPaint.setColor(this.mStrokeColor);
        this.mNotchPaint.setAntiAlias(true);
        this.mNotchPaint.setStrokeWidth(this.mStrokeSize);
        this.mNotchPaint.setStyle(Paint.Style.STROKE);
        this.mNotchPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mNotchPaint.setStrokeCap(Paint.Cap.ROUND);

        // Create progress paint
        this.mProgressPaint = new Paint();
        this.mProgressPaint.setColor(this.mPointerColor);
        this.mProgressPaint.setAntiAlias(true);
        this.mProgressPaint.setStrokeWidth(this.mStrokeSize);
        this.mProgressPaint.setStyle(Paint.Style.STROKE);
        this.mProgressPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Create the pointer paint
        this.mPointerPaint = new Paint();
        this.mPointerPaint.setColor(this.mPointerColor);
        this.mPointerPaint.setAntiAlias(true);
        this.mPointerPaint.setStyle(Paint.Style.FILL);
        this.mPointerPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPointerPaint.setStrokeCap(Paint.Cap.ROUND);

        // Create the halo paint
        this.mHaloPaint = new Paint();
        this.mHaloPaint.setColor(this.mPointerColor);
        this.mHaloPaint.setAntiAlias(true);
        this.mHaloPaint.setStrokeWidth(this.mHaloSize);
        this.mHaloPaint.setStyle(Paint.Style.STROKE);
        this.mHaloPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mHaloPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mHaloPaint.setAlpha(ScCircularSeekBar.HALO_ALPHA);
    }

    // Calc point position from angle and offset
    private Point getPositionFromAngle(float angle, int offset) {
        // Degrees to rad
        double rad = Math.toRadians(angle);

        // Trova le coordinate nello spazio
        int x = Math.round((this.mDrawArea.width() / 2 + offset) * (float) Math.cos(rad));
        int y = Math.round((this.mDrawArea.height() / 2 + offset) * (float) Math.sin(rad));

        // Find the drawable area center
        int xCenter = this.mDrawArea.left + this.mDrawArea.width() / 2;
        int yCenter = this.mDrawArea.top + this.mDrawArea.height() / 2;

        // Create the point and fix center
        return new Point(x + xCenter, y + yCenter);
    }

    // Find the delta angle
    private float getDeltaAngle() {
        return this.mSweepAngle / (float) this.mInternalMaxValue;
    }

    // Calc point position from value and offset
    private Point getPositionFromValue(int value, int offset) {
        // Current angle
        float angle = this.mStartAngle + value * this.getDeltaAngle();
        // Find the right point on the screen
        return this.getPositionFromAngle(angle, offset);
    }

    // Find the angle from position on screen
    private double getAngleFromPosition(float x, float y) {
        // Calc the center of area
        int xCenter = this.mDrawArea.left + this.mDrawArea.width() / 2;
        int yCenter = this.mDrawArea.top + this.mDrawArea.height() / 2;

        // Get angle from position
        double angle = Math.atan2(
                (y - yCenter) / this.mDrawArea.height(),
                (x - xCenter) / this.mDrawArea.width()
        );
        // Convert to degree and fix over numver
        return (Math.toDegrees(angle) + 360) % 360;
    }

    // Check if point is inside a circle (Pitagora)
    private boolean pointInsideCircle(float x, float y, float radius) {
        return Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(radius, 2);
    }

    // Check if mouse is on pointer
    private boolean mouseOnPointer(float x, float y) {
        // Find the current value position
        Point position = this.getPositionFromValue(this.mValue, 0);
        // Total radius
        float pointerRadius = this.mPointerRadius + this.mHaloSize / 2;
        // Check pitagora
        return this.pointInsideCircle(x - position.x, y - position.y, pointerRadius);
    }

    // Check if mouse is on arc
    private boolean mouseOnArc(float x, float y) {
        // Get the angle from mouse position
        double angle = this.getAngleFromPosition(x, y);
        // Find the point on arc from angle
        Point pointOnArc = this.getPositionFromAngle((float) angle, 0);
        // Find the distance between the points and check it
        return this.pointInsideCircle(x - pointOnArc.x, y - pointOnArc.y, this.mStrokeSize * 2);
    }

    // Check for new value
    private void selectValue(float x, float y) {
        // Find the angle
        double angle = this.getAngleFromPosition(x, y);
        angle = angle + this.getDeltaAngle() / 2;

        // Find the start angle normalized
        double normalized = (this.mStartAngle + 360) % 360;

        // Check the case
        if (normalized < angle) {
            if (this.mSweepAngle > 0) {
                angle -= normalized;
            } else {
                angle = 360 - angle - normalized;
            }
        } else {
            if (this.mSweepAngle > 0) {
                angle = 360 - normalized + angle;
            } else {
                angle = normalized - angle;
            }
        }

        // Find value and check the limits
        int value = (int) (angle / Math.abs(this.getDeltaAngle()));

        // Limits
        if (value < 0 || value > this.mInternalMaxValue) {
            value = this.mValue;
        }

        // Check if value is changed
        if (this.mValue != value) {
            // Save the new value and redraw
            this.mValue = value;

            // Throw a event
            if (this.mOnChangeListener != null) {
                // On change
                this.mOnChangeListener.onChange(this, this.getValue());
            }
        }
    }


    /**
     * Area methods
     */

    // Calc draw area
    private Rect calcDrawArea(int width, int height) {
        // Find layout and create a empty area
        ViewGroup.LayoutParams params = getLayoutParams();

        // Check if width and height is wrapped
        if (params.width == ViewGroup.LayoutParams.WRAP_CONTENT &&
                params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Return empty
            return new Rect();
        }

        // If vertical is WRAP_CONTENT use same circle
        if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = width;
        }

        // If horizontal is WRAP_CONTENT use same circle
        if (params.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = height;
        }

        // Check the dimensions limits
        if (this.mMaxWidth > 0 && width > this.mMaxWidth) width = this.mMaxWidth;
        if (this.mMaxHeight > 0 && height > this.mMaxHeight) height = this.mMaxHeight;

        // Create the area rectangle
        return new Rect(
                this.getPaddingLeft(),
                this.getPaddingTop(),
                width - this.getPaddingRight(),
                height - this.getPaddingBottom()
        );
    }

    // Calc the trimmed area
    private Rect calcTrimmedArea() {
        // Check for sweep angle
        if (this.mSweepAngle == 0) return new Rect();

        // Find the first point
        Point startPoint = this.getPositionFromAngle(this.mStartAngle, 0);
        // Init area rectangle
        Rect area = new Rect(startPoint.x, startPoint.y, startPoint.x, startPoint.y);

        // Calc the start and end
        int endAngle = this.mStartAngle + this.mSweepAngle;
        int currAngle = this.mStartAngle;

        // Cycle all angles
        while (currAngle != endAngle) {
            // Calc current point
            Point current = this.getPositionFromAngle(currAngle, 0);

            // Compare
            if (current.x < area.left) area.left = current.x;
            if (current.x > area.right) area.right = current.x;

            if (current.y < area.top) area.top = current.y;
            if (current.y > area.bottom) area.bottom = current.y;

            // Increment angle
            currAngle += this.mSweepAngle > 0 ? 1 : -1;
        }

        // Return
        return area;
    }


    /**
     * Draw methods
     */

    // Draw arc
    private void drawArc(Canvas canvas) {
        canvas.drawArc(
                new RectF(this.mDrawArea),
                this.mStartAngle,
                this.mSweepAngle,
                false,
                this.mArcPaint
        );
    }

    // Draw progress
    private void drawProgress(Canvas canvas) {
        // Only if show
        if (this.mShowProgress) {
            // Calc the progress angle
            float progressAngle = this.mValue * this.getDeltaAngle();

            // Draw arc
            canvas.drawArc(
                    new RectF(this.mDrawArea),
                    this.mStartAngle,
                    progressAngle,
                    false,
                    this.mProgressPaint
            );
        }
    }

    // Draw notches
    private void drawNotches(Canvas canvas) {
        // Check if need
        if (this.mNotchSize > 0) {
            // If angle is less than 360° add one to max value
            int correction = this.mSweepAngle < 360 ? 1 : 0;

            // Cycle all values
            for (int value = 0; value < this.mInternalMaxValue + correction; value++) {
                // Calc current point
                Point startPoint = this.getPositionFromValue(value, -this.mNotchSize);
                Point endPoint = this.getPositionFromValue(value, this.mNotchSize);

                // Correct the last position
                if (correction == 1 && value == this.mInternalMaxValue) {
                    // Calc the end angle
                    int endAngle = this.mStartAngle + this.mSweepAngle;

                    // Calc current point using the last angle available
                    startPoint = this.getPositionFromAngle(endAngle, -this.mNotchSize);
                    endPoint = this.getPositionFromAngle(endAngle, this.mNotchSize);
                }

                // Draw line
                canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, this.mNotchPaint);
            }
        }
    }

    // Draw pointer
    private void drawPointer(Canvas canvas) {
        // Check for sweep angle
        if (this.mSweepAngle != 0) {
            // The actual pointer position
            Point position = this.getPositionFromValue(this.mValue, 0);

            // Draw
            canvas.drawCircle(position.x, position.y, this.mPointerRadius, this.mPointerPaint);
            canvas.drawCircle(position.x, position.y, this.mPointerRadius + this.mHaloSize / 2, this.mHaloPaint);
        }
    }


    /**
     * Overrides
     */

    // On draw
    @Override
    protected void onDraw(Canvas canvas) {
        // Change paint alpha by drag or not
        if (this.mDrag) {
            this.mPointerPaint.setAlpha(ScCircularSeekBar.HALO_ALPHA);
            this.mHaloPaint.setAlpha(255);

        } else {
            this.mPointerPaint.setAlpha(255);
            this.mHaloPaint.setAlpha(ScCircularSeekBar.HALO_ALPHA);
        }

        // Call custom paint event
        if (this.mOnCustomDrawListener != null) {
            this.mOnCustomDrawListener.onCustomPaint(
                    this,
                    this.mArcPaint,
                    this.mNotchPaint,
                    this.mPointerPaint,
                    this.mHaloPaint,
                    this.mProgressPaint,
                    this.mDrag);
        }

        // Draw all
        this.drawArc(canvas);
        this.drawProgress(canvas);
        this.drawNotches(canvas);
        this.drawPointer(canvas);
    }

    // On measure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get suggested dimensions
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        // Check the dimensions limits
        if (this.mMaxWidth > 0 && width > this.mMaxWidth) width = this.mMaxWidth;
        if (this.mMaxHeight > 0 && height > this.mMaxHeight) height = this.mMaxHeight;

        // Calc areas
        this.mDrawArea = this.calcDrawArea(width, height);
        Rect trimmedArea = this.calcTrimmedArea();

        // Trim by layout params
        ViewGroup.LayoutParams params = getLayoutParams();

        // Check horizontal wrap content
        if (params.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Adjust draw area offset
            this.mDrawArea.offset(-trimmedArea.left + this.getPaddingLeft(), 0);
            // Fix global width
            width = this.getPaddingLeft() + trimmedArea.width() + this.getPaddingRight();
        }

        // Check vertical wrap content
        if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Adjust draw area dimension and offset
            this.mDrawArea.offset(0, -trimmedArea.top + this.getPaddingTop());
            // Fix global height
            height = this.getPaddingTop() + trimmedArea.height() + this.getPaddingBottom();
        }

        // Set dimensions
        this.setMeasuredDimension(width, height);
    }

    // Check the touch
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Recalc layout
        this.requestLayout();

        // Select action
        switch (event.getAction()) {
            // Press
            case MotionEvent.ACTION_DOWN:
                // Check if press on arc
                if (this.mouseOnArc(event.getX(), event.getY())) {
                    // Select a new value
                    this.selectValue(event.getX(), event.getY());
                }

                // Check if must start to drag
                this.mDrag = this.mouseOnPointer(event.getX(), event.getY());
                this.invalidate();

                // Listener
                if (this.mDrag && this.mOnDragPointerListener != null) {
                    this.mOnDragPointerListener.onStart(this);
                }
                break;

            // Release
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Finish drag
                this.mDrag = false;
                this.invalidate();

                // Listener
                if (this.mOnDragPointerListener != null) {
                    this.mOnDragPointerListener.onEnd(this);
                }
                break;

            // Move
            case MotionEvent.ACTION_MOVE:
                // if dragging
                if (this.mDrag) {
                    // Check if must change current value
                    this.selectValue(event.getX(), event.getY());
                    this.invalidate();
                }
                break;
        }

        // Block event propagation
        return true;
    }


    /**
     * Instance state
     */

    // Save
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable("PARENT", superState);
        state.putInt("mStartAngle", this.mStartAngle);
        state.putInt("mSweepAngle", this.mSweepAngle);
        state.putInt("mMinValue", this.mMinValue);
        state.putInt("mMaxValue", this.mMaxValue);
        state.putInt("mNotchSize", this.mNotchSize);
        state.putInt("mPointerRadius", this.mPointerRadius);
        state.putInt("mHaloSize", this.mHaloSize);
        state.putInt("mStrokeSize", this.mStrokeSize);
        state.putInt("mStrokeColor", this.mStrokeColor);
        state.putInt("mPointerColor", this.mPointerColor);
        state.putBoolean("mShowProgress", this.mShowProgress);
        state.putInt("mMaxWidth", this.mMaxWidth);
        state.putInt("mMaxHeight", this.mMaxHeight);
        state.putInt("mValue", this.mValue);
        state.putInt("mInternalMaxValue", this.mInternalMaxValue);

        return state;
    }

    // Restore
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        this.mStartAngle = savedState.getInt("mStartAngle");
        this.mSweepAngle = savedState.getInt("mSweepAngle");
        this.mMinValue = savedState.getInt("mMinValue");
        this.mMaxValue = savedState.getInt("mMaxValue");
        this.mNotchSize = savedState.getInt("mNotchSize");
        this.mPointerRadius = savedState.getInt("mPointerRadius");
        this.mHaloSize = savedState.getInt("mHaloSize");
        this.mStrokeSize = savedState.getInt("mStrokeSize");
        this.mStrokeColor = savedState.getInt("mStrokeColor");
        this.mPointerColor = savedState.getInt("mPointerColor");
        this.mShowProgress = savedState.getBoolean("mShowProgress");
        this.mMaxWidth = savedState.getInt("mMaxWidth");
        this.mMaxHeight = savedState.getInt("mMaxHeight");
        this.mValue = savedState.getInt("mValue");
        this.mInternalMaxValue = savedState.getInt("mInternalMaxValue");
    }


    /**
     * Pubblic listener
     */

    // Listener definition
    public interface OnChangeValueListener {

        void onChange(ScCircularSeekBar source, int value);

    }

    public interface OnCustomDrawListener {

        void onCustomPaint(ScCircularSeekBar source,
                           Paint arc, Paint notch, Paint pointer, Paint halo, Paint progress,
                           boolean drag);
    }

    public interface OnDragPointerListener {

        void onStart(ScCircularSeekBar source);

        void onEnd(ScCircularSeekBar source);

    }

    // Set the listener
    @SuppressWarnings("unused")
    public void setOnChangeListener(OnChangeValueListener listener) {
        this.mOnChangeListener = listener;
    }

    @SuppressWarnings("unused")
    public void setOnCustomDrawListener(OnCustomDrawListener listener) {
        this.mOnCustomDrawListener = listener;
    }

    @SuppressWarnings("unused")
    public void setOnDragPointerListener(OnDragPointerListener listener) {
        this.mOnDragPointerListener = listener;
    }


    /**
     * Public properties
     */

    // Start angle
    @SuppressWarnings("unused")
    public int getStartAngle() {
        return this.mStartAngle;
    }

    @SuppressWarnings("unused")
    public void setStartAngle(int value) {
        this.mStartAngle = value;
        this.checkValues();
        this.requestLayout();
    }

    // Sweep angle
    @SuppressWarnings("unused")
    public int getSweepAngle() {
        return this.mSweepAngle;
    }

    @SuppressWarnings("unused")
    public void setSweepAngle(int value) {
        this.mSweepAngle = value;
        this.checkValues();
        this.calcValues();
        this.requestLayout();
    }

    // Min value
    @SuppressWarnings("unused")
    public int getMinValue() {
        return this.mMinValue;
    }

    @SuppressWarnings("unused")
    public void setMinValue(int value) {
        this.mMinValue = value;
        this.mInternalMaxValue = this.mMaxValue - this.mMinValue;

        this.checkValues();
        this.calcValues();
        this.requestLayout();
    }

    // Min value
    @SuppressWarnings("unused")
    public int getMaxValue() {
        return this.mMaxValue;
    }

    @SuppressWarnings("unused")
    public void setMaxValue(int value) {
        this.mMaxValue = value;
        this.mInternalMaxValue = this.mMaxValue - this.mMinValue;

        this.checkValues();
        this.calcValues();
        this.requestLayout();
    }

    // Notch Size
    @SuppressWarnings("unused")
    public int getNotchSize() {
        return this.mNotchSize;
    }

    @SuppressWarnings("unused")
    public void setNotchSize(int value) {
        this.mNotchSize = value;
        this.checkValues();
        this.requestLayout();
    }

    // Pointer Radius
    @SuppressWarnings("unused")
    public int getPointerRadius() {
        return this.mPointerRadius;
    }

    @SuppressWarnings("unused")
    public void setPointerRadius(int value) {
        this.mPointerRadius = value;
        this.checkValues();
        this.requestLayout();
    }

    // Halo size
    @SuppressWarnings("unused")
    public int getHaloSize() {
        return this.mHaloSize;
    }

    @SuppressWarnings("unused")
    public void setHaloSize(int value) {
        this.mHaloSize = value;
        this.checkValues();
        this.mHaloPaint.setStrokeWidth(this.mHaloSize);
        this.requestLayout();
    }

    // Stroke size
    @SuppressWarnings("unused")
    public int getStrokeSize() {
        return this.mStrokeSize;
    }

    @SuppressWarnings("unused")
    public void setStrokeSize(int value) {
        this.mStrokeSize = value;
        this.checkValues();

        this.mArcPaint.setStrokeWidth(this.mStrokeSize);
        this.mNotchPaint.setStrokeWidth(this.mStrokeSize);
        this.mProgressPaint.setStrokeWidth(this.mStrokeSize);
        this.requestLayout();
    }

    // Stroke color
    @SuppressWarnings("unused")
    public int getStrokeColor() {
        return this.mStrokeColor;
    }

    @SuppressWarnings("unused")
    public void setStrokeColor(int color) {
        this.mStrokeColor = color;
        this.mArcPaint.setColor(this.mStrokeColor);
        this.mNotchPaint.setColor(this.mStrokeColor);
        this.requestLayout();
    }

    // Pointer color
    @SuppressWarnings("unused")
    public int getPointerColor() {
        return this.mPointerColor;
    }

    @SuppressWarnings("unused")
    public void setPointerColor(int color) {
        this.mPointerColor = color;
        this.mPointerPaint.setColor(this.mPointerColor);
        this.mHaloPaint.setColor(this.mPointerColor);
        this.mProgressPaint.setColor(this.mPointerColor);
        this.requestLayout();
    }

    // Show progress
    @SuppressWarnings("unused")
    public boolean getShowProgress() {
        return this.mShowProgress;
    }

    @SuppressWarnings("unused")
    public void setShowProgress(boolean value) {
        this.mShowProgress = value;
        this.requestLayout();
    }

    // Max width
    @SuppressWarnings("unused")
    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    @SuppressWarnings("unused")
    public void setMaxWidth(int value) {
        this.mMaxWidth = value;
        this.checkValues();
        this.requestLayout();
    }

    // Max height
    @SuppressWarnings("unused")
    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    @SuppressWarnings("unused")
    public void setMaxHeight(int value) {
        this.mMaxHeight = value;
        this.checkValues();
        this.requestLayout();
    }

    // Value
    @SuppressWarnings("unused")
    public int getValue() {
        return this.mValue - Math.abs(this.mMinValue);
    }

    @SuppressWarnings("unused")
    public void setValue(int value) {
        this.mValue = value;
        this.checkValues();
        this.calcValues();
        this.requestLayout();
    }

}