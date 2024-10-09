package com.pengl.williamchart.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.pengl.williamchart.animation.Animation;
import com.pengl.williamchart.animation.ChartAnimationListener;
import com.pengl.williamchart.listener.OnEntryClickListener;
import com.pengl.williamchart.model.ChartEntry;
import com.pengl.williamchart.model.ChartSet;
import com.pengl.williamchart.renderer.AxisRenderer;
import com.pengl.williamchart.renderer.XRenderer;
import com.pengl.williamchart.renderer.YRenderer;
import com.pengl.williamchart.tooltip.Tooltip;
import com.pengl.williamchart.util.Preconditions;
import com.pengl.williamchart.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * 抽象类可以扩展来定义任何隐含轴的图表。
 */
public abstract class ChartView extends RelativeLayout {

    private static final String TAG = "chart.view.ChartView";

    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 100;

    final XRenderer xRndr;// 水平位置控制器
    final YRenderer yRndr;// 垂直位置控制器

    final Style style;// 已应用于图表的样式
    ArrayList<ChartSet> data;// 用于显示的图表数据
    private Orientation mOrientation;// 图表的方向

    private int mChartLeft;         // 图表边框（包括填充）
    private int mChartTop;          // 图表边框（包括填充）
    private int mChartRight;        // 图表边框（包括填充）
    private int mChartBottom;       // 图表边框（包括填充）

    private ArrayList<Float> mThresholdStartValues;     // 阈值区域值 - 开始
    private ArrayList<Float> mThresholdEndValues;       // 阈值区域值 - 结束
    private ArrayList<Integer> mThresholdStartLabels;   // 阈值区域标签
    private ArrayList<Integer> mThresholdEndLabels;     // 阈值区域标签

    /**
     * Chart data to be displayed
     */
    private ArrayList<ArrayList<Region>> mRegions;

    /**
     * Gestures detector to trigger listeners callback
     */
    private final GestureDetector mGestureDetector;

    /**
     * Listener callback on entry click
     */
    private OnEntryClickListener mEntryListener;

    /**
     * Listener callback on chart click, no entry intersection
     */
    private OnClickListener mChartListener;

    /**
     * Drawing flag
     */
    private boolean mReadyToDraw;

    /**
     * Drawing flag
     */
    private boolean mIsDrawing;

    /**
     * Chart animation
     */
    private Animation mAnim;

    /**
     * Executed only before the chart is drawn for the first time.
     * . borders are defined
     * . digestData(data), to process the data to be drawn
     * . defineRegions(), if listener has been registered
     * this will define the chart regions to handle by onTouchEvent
     */
    private final OnPreDrawListener drawListener = new OnPreDrawListener() {
        @SuppressLint("NewApi")
        @Override
        public boolean onPreDraw() {

            ChartView.this.getViewTreeObserver().removeOnPreDrawListener(this);

            // Generate Paint object with mStyle attributes
            style.init();

            // Initiate axis labels with data and mStyle
            yRndr.init(data, style);
            xRndr.init(data, style);

            // Set the positioning of the whole chart's frame
            mChartLeft = getPaddingLeft();
            mChartTop = getPaddingTop() + style.fontMaxHeight;// 避免最高点的文字显示不全，加上文字的高度
            mChartRight = getMeasuredWidth() - getPaddingRight();
            mChartBottom = getMeasuredHeight() - getPaddingBottom();

            // Measure space and set the positioning of the inner border.
            // Inner borders will be chart's frame excluding the space needed by axis.
            // They define the actual area where chart's content will be drawn.
            yRndr.measure(mChartLeft, mChartTop, mChartRight, mChartBottom);
            xRndr.measure(mChartLeft, mChartTop, mChartRight, mChartBottom);

            // Negotiate chart inner boundaries.
            // Both renderers may require different space to draw axis stuff.
            final float[] bounds = negotiateInnerChartBounds(yRndr.getInnerChartBounds(), xRndr.getInnerChartBounds());
            yRndr.setInnerChartBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
            xRndr.setInnerChartBounds(bounds[0], bounds[1], bounds[2], bounds[3]);

            // Dispose the various axis elements in their positions
            yRndr.dispose();
            xRndr.dispose();

            // Parse threshold screen coordinates
            if (!mThresholdStartValues.isEmpty()) {
                for (int i = 0; i < mThresholdStartValues.size(); i++) {
                    mThresholdStartValues.set(i, yRndr.parsePos(0, mThresholdStartValues.get(i)));
                    mThresholdEndValues.set(i, yRndr.parsePos(0, mThresholdEndValues.get(i)));
                }
            }

            // Process data to define screen coordinates
            digestData();

            // In case Views extending ChartView need to pre process data before the onDraw
            onPreDrawChart(data);

            // Define entries regions
            if (mRegions.isEmpty()) {
                int dataSize = data.size();
                int setSize;
                mRegions = new ArrayList<>(dataSize);
                ArrayList<Region> regionSet;
                for (int i = 0; i < dataSize; i++) {
                    setSize = data.get(0).size();
                    regionSet = new ArrayList<>(setSize);
                    for (int j = 0; j < setSize; j++)
                        regionSet.add(new Region());
                    mRegions.add(regionSet);
                }
            }
            defineRegions(mRegions, data);

            // Prepare the animation retrieving the first dump of coordinates to be used
            if (mAnim != null)
                data = mAnim.prepareEnterAnimation(ChartView.this);

            ChartView.this.setLayerType(LAYER_TYPE_SOFTWARE, null);
            return mReadyToDraw = true;
        }
    };

    private ChartAnimationListener mAnimListener;

    private Tooltip mTooltip;

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mGestureDetector = new GestureDetector(context, new GestureListener());
        xRndr = new XRenderer();
        yRndr = new YRenderer();
        style = new Style(context, attrs);
    }

    public ChartView(Context context) {
        super(context);
        init();
        mGestureDetector = new GestureDetector(context, new GestureListener());
        xRndr = new XRenderer();
        yRndr = new YRenderer();
        style = new Style(context);
    }

    private void init() {
        mReadyToDraw = false;
        mThresholdStartValues = new ArrayList<>();
        mThresholdEndValues = new ArrayList<>();
        mThresholdStartLabels = new ArrayList<>();
        mThresholdEndLabels = new ArrayList<>();
        mIsDrawing = false;
        data = new ArrayList<>();
        mRegions = new ArrayList<>();
        mAnimListener = data -> {
            if (!mIsDrawing) {
                addData(data);
                postInvalidate();
                return true;
            }
            return false;
        };
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setWillNotDraw(false);
        style.init();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        style.clean();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int tmpWidth = widthMeasureSpec;
        int tmpHeight = heightMeasureSpec;

        if (widthMode == MeasureSpec.AT_MOST) tmpWidth = DEFAULT_WIDTH;
        if (heightMode == MeasureSpec.AT_MOST) tmpHeight = DEFAULT_HEIGHT;

        setMeasuredDimension(tmpWidth, tmpHeight);
    }

    /**
     * The method listens for chart clicks and checks whether it intercepts
     * a known Region. It will then use the registered Listener.onClick
     * to return the region's index.
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        return !(mAnim != null && mAnim.isPlaying()
                || mEntryListener == null && mChartListener == null && mTooltip == null)
                && mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        mIsDrawing = true;
        super.onDraw(canvas);

        if (mReadyToDraw) {
            // long time = System.currentTimeMillis();

            // Draw grid
            if (style.hasVerticalGrid()) drawVerticalGrid(canvas);
            if (style.hasHorizontalGrid()) drawHorizontalGrid(canvas);

            // Draw threshold
            if (!mThresholdStartValues.isEmpty())
                for (int i = 0; i < mThresholdStartValues.size(); i++)
                    drawThreshold(canvas, getInnerChartLeft(), mThresholdStartValues.get(i),
                            getInnerChartRight(), mThresholdEndValues.get(i), style.valueThresPaint);
            if (!mThresholdStartLabels.isEmpty())
                for (int i = 0; i < mThresholdStartLabels.size(); i++)
                    drawThreshold(canvas, data.get(0).getEntry(mThresholdStartLabels.get(i)).getX(),
                            getInnerChartTop(), data.get(0).getEntry(mThresholdEndLabels.get(i)).getX(),
                            getInnerChartBottom(), style.labelThresPaint);

            // Draw data
            if (!data.isEmpty()) onDrawChart(canvas, data);

            // Draw Axis Y
            yRndr.draw(canvas);

            // Draw axis X
            xRndr.draw(canvas);

            //System.out.println("Time drawing "+(System.currentTimeMillis() - time));
        }

        mIsDrawing = false;
    }

    /**
     * Convert {@link ChartEntry} values into screen points.
     */
    private void digestData() {
        int nEntries = data.get(0).size();
        for (ChartSet set : data) {
            for (int i = 0; i < nEntries; i++) {
                set.getEntry(i).setCoordinates(
                        xRndr.parsePos(i, set.getValue(i)),
                        yRndr.parsePos(i, set.getValue(i)));
            }
        }
    }

    /**
     * (Optional) To be overridden in case the view needs to execute some code before
     * starting the drawing.
     *
     * @param data Array of {@link ChartSet} to do the necessary preparation just before onDraw
     */
    void onPreDrawChart(ArrayList<ChartSet> data) {
    }

    /**
     * (Optional) To be overridden in order for each chart to define its own clickable regions.
     * This way, classes extending ChartView will only define their clickable regions.
     * <p>
     * Important: the returned vector must match the order of the data passed
     * by the user. This ensures that onTouchEvent will return the correct index.
     *
     * @param regions Empty list of regions where result of this method must be assigned
     * @param data    {@link java.util.ArrayList} of {@link ChartSet}
     *                to use while defining each region of a {@link ChartView}
     */
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {
    }

    /**
     * Method responsible to draw bars with the parsed screen points.
     *
     * @param canvas The canvas to draw on
     * @param data   {@link java.util.ArrayList} of {@link ChartSet}
     *               to use while drawing the Chart
     */
    protected abstract void onDrawChart(Canvas canvas, ArrayList<ChartSet> data);

    /**
     * Set new data to the chart and invalidates the view to be then drawn.
     *
     * @param set {@link ChartSet} object.
     */
    public void addData(@NonNull ChartSet set) {
        Preconditions.checkNotNull(set);

        if (!data.isEmpty() && set.size() != data.get(0).size())
            throw new IllegalArgumentException("The number of entries between sets doesn't match.");

        data.add(set);
    }

    /**
     * Add full chart data.
     *
     * @param data An array of {@link ChartSet}
     */
    public void addData(ArrayList<ChartSet> data) {
        this.data = data;
    }

    /**
     * Base method when a show chart occurs
     */
    private void display() {
        this.getViewTreeObserver().addOnPreDrawListener(drawListener);
        postInvalidate();
    }

    /**
     * Show chart data
     */
    public void show() {
        for (ChartSet set : data)
            set.setVisible(true);
        display();
    }

    /**
     * Show only a specific chart dataset.
     *
     * @param setIndex Dataset index to be displayed
     */
    public void show(int setIndex) {
        data.get(Preconditions.checkPositionIndex(setIndex, data.size())).setVisible(true);
        display();
    }

    /**
     * Starts the animation given as parameter.
     *
     * @param anim Animation used while showing and updating sets
     */
    public void show(@NonNull Animation anim) {
        mAnim = Preconditions.checkNotNull(anim);
        mAnim.setAnimationListener(mAnimListener);
        show();
    }

    /**
     * Dismiss chart data.
     */
    public void dismiss() {
        dismiss(mAnim);
    }

    /**
     * Dismiss a specific chart dataset.
     *
     * @param setIndex Dataset index to be dismissed
     */
    public void dismiss(int setIndex) {
        data.get(Preconditions.checkPositionIndex(setIndex, data.size())).setVisible(false);
        invalidate();
    }

    /**
     * Dismiss chart data with animation.
     *
     * @param anim Animation used to exit
     */
    public void dismiss(@NonNull Animation anim) {
        mAnim = Preconditions.checkNotNull(anim);
        mAnim.setAnimationListener(mAnimListener);

        final Runnable endAction = mAnim.getEndAction();
        mAnim.withEndAction(() -> {
            if (endAction != null) endAction.run();
            data.clear();
            invalidate();
        });

        data = mAnim.prepareExitAnimation(this);
        invalidate();
    }

    /**
     * Method not expected to be used often. More for testing.
     * Resets chart state to insert new configuration.
     */
    public void reset() {
        if (mAnim != null && mAnim.isPlaying())
            mAnim.cancel();

        init();
        xRndr.reset();
        yRndr.reset();
        setOrientation(mOrientation);

        style.labelThresPaint = null;
        style.valueThresPaint = null;
        style.gridPaint = null;
    }

    /**
     * Update set values. Animation support in case previously added.
     *
     * @param setIndex Index of set to be updated
     * @param values   Array of new values. Array length must match current data
     * @return {@link ChartView} self-reference.
     */
    public ChartView updateValues(int setIndex, float[] values) {
        data.get(Preconditions.checkPositionIndex(setIndex, data.size())).updateValues(values);
        return this;
    }

    /**
     * Notify {@link ChartView} about updated values. {@link ChartView} will be validated.
     */
    public void notifyDataUpdate() {

        // Ignore update if chart is not even ready to draw or if it is still animating
        if (mAnim != null && !mAnim.isPlaying() && mReadyToDraw || mAnim == null && mReadyToDraw) {

            ArrayList<float[][]> oldCoords = new ArrayList<>(data.size());
            ArrayList<float[][]> newCoords = new ArrayList<>(data.size());

            for (ChartSet set : data)
                oldCoords.add(set.getScreenPoints());

            digestData();
            for (ChartSet set : data)
                newCoords.add(set.getScreenPoints());

            defineRegions(mRegions, data);
            if (mAnim != null) mAnim.prepareUpdateAnimation(oldCoords, newCoords);
            else invalidate();

        } else {
            Log.w(TAG, "Unexpected data update notification. "
                    + "Chart is still not displayed or still displaying.");
        }
    }

    /**
     * Toggles {@link Tooltip} between show and dismiss.
     *
     * @param rect  {@link Rect} containing the bounds of last clicked entry
     * @param value Value of the last entry clicked
     */
    private void toggleTooltip(@NonNull Rect rect, float value) {
        Preconditions.checkNotNull(rect);
        if (!mTooltip.on()) {
            mTooltip.prepare(rect, value);
            showTooltip(mTooltip, true);
        } else {
            dismissTooltip(mTooltip, rect, value);
        }
    }

    /**
     * Adds a tooltip to {@link ChartView}.
     * If is not the case already, the whole tooltip is forced to be inside {@link ChartView}
     * bounds. The area used to apply the correction exclude any padding applied, the whole view
     * size in the layout is take into account.
     *
     * @param tooltip    {@link Tooltip} view to be added
     * @param correctPos False if tooltip should not be forced to be inside ChartView.
     *                   You may want to take care of it.
     */
    public void showTooltip(@NonNull Tooltip tooltip, boolean correctPos) {
        Preconditions.checkNotNull(tooltip);
        if (correctPos) tooltip.correctPosition(mChartLeft, mChartTop, mChartRight, mChartBottom);
        if (tooltip.hasEnterAnimation()) tooltip.animateEnter();
        this.addTooltip(tooltip);
    }

    /**
     * Add {@link Tooltip}/{@link View}. to chart/parent view.
     *
     * @param tooltip tooltip to be added to chart
     */
    private void addTooltip(@NonNull Tooltip tooltip) {
        Preconditions.checkNotNull(tooltip);
        this.addView(tooltip);
        tooltip.setOn(true);
    }

    /**
     * Remove {@link Tooltip}/{@link View} to chart/parent view.
     *
     * @param tooltip tooltip to be removed to chart
     */
    private void removeTooltip(@NonNull Tooltip tooltip) {
        Preconditions.checkNotNull(tooltip);
        this.removeView(tooltip);
        tooltip.setOn(false);
    }

    /**
     * Dismiss tooltip from {@link ChartView}.
     *
     * @param tooltip View to be dismissed
     */
    private void dismissTooltip(@NonNull Tooltip tooltip) {
        dismissTooltip(Preconditions.checkNotNull(tooltip), null, 0);
    }

    /**
     * Dismiss tooltip from {@link ChartView}.
     *
     * @param tooltip View to be dismissed
     */
    private void dismissTooltip(@NonNull final Tooltip tooltip, final Rect rect, final float value) {
        Preconditions.checkNotNull(tooltip);

        if (tooltip.hasExitAnimation()) {
            tooltip.animateExit(() -> {
                removeTooltip(tooltip);
                if (rect != null) toggleTooltip(rect, value);
            });
        } else {
            this.removeTooltip(tooltip);
            if (rect != null) this.toggleTooltip(rect, value);
        }
    }

    /**
     * Removes all tooltips currently presented in the chart.
     */
    public void dismissAllTooltips() {
        this.removeAllViews();
        if (mTooltip != null) mTooltip.setOn(false);
    }

    /**
     * Asks the view if it is able to draw now.
     *
     * @return {@link ChartView} self-reference.
     */
    public boolean canIPleaseAskYouToDraw() {
        return !mIsDrawing;
    }

    /**
     * Negotiates the inner bounds required by renderers.
     *
     * @param innersA Inner bounds require by element A
     * @param innersB Inned bound required by element B
     * @return float vector with size equal to 4 containing agreed
     * inner bounds (left, top, right, bottom).
     */
    float[] negotiateInnerChartBounds(float[] innersA, float[] innersB) {
        return new float[]{Math.max(innersA[0], innersB[0]),
                Math.max(innersA[1], innersB[1]),
                Math.min(innersA[2], innersB[2]),
                Math.min(innersA[3], innersB[3])};
    }

    /**
     * Draw a threshold line or band on the labels or values axis. If same values or same label
     * index have been given then a line will be drawn rather than a band.
     *
     * @param canvas Canvas to draw line/band on.
     * @param left   The left side of the line/band to be drawn
     * @param top    The top side of the line/band to be drawn
     * @param right  The right side of the line/band to be drawn
     * @param bottom The bottom side of the line/band to be drawn
     */
    private void drawThreshold(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        if (left == right || top == bottom)
            canvas.drawLine(left, top, right, bottom, paint);
        else canvas.drawRect(left, top, right, bottom, paint);
    }

    /**
     * 绘制网格的垂直线
     *
     * @param canvas Canvas to draw on.
     */
    private void drawVerticalGrid(Canvas canvas) {
        final float offset = (getInnerChartRight() - getInnerChartLeft()) / style.gridColumns;
        float marker = getInnerChartLeft();

        if (style.hasYAxis) marker += offset;

        while (marker < getInnerChartRight()) {
            canvas.drawLine(marker, getInnerChartTop(), marker, getInnerChartBottom(), style.gridPaint);
            marker += offset;
        }

        canvas.drawLine(getInnerChartRight(), getInnerChartTop(), getInnerChartRight(),
                getInnerChartBottom(), style.gridPaint);
    }

    /**
     * 绘制网格的水平线
     *
     * @param canvas Canvas to draw on.
     */
    private void drawHorizontalGrid(Canvas canvas) {
        final float offset = (getInnerChartBottom() - getInnerChartTop()) / style.gridRows;
        float marker = getInnerChartTop();
        while (marker < getInnerChartBottom()) {
            canvas.drawLine(getInnerChartLeft(), marker, getInnerChartRight(), marker, style.gridPaint);
            marker += offset;
        }

        if (!style.hasXAxis)
            canvas.drawLine(getInnerChartLeft(), getInnerChartBottom(), getInnerChartRight(),
                    getInnerChartBottom(), style.gridPaint);
    }

    /**
     * Get orientation of chart.
     *
     * @return Object of type {@link ChartView.Orientation}
     * defining an horizontal or vertical orientation.
     * Orientation.HORIZONTAL | Orientation.VERTICAL
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    /**
     * Sets the chart's orientation.
     *
     * @param orient Orientation.HORIZONTAL | Orientation.VERTICAL
     */
    void setOrientation(@NonNull Orientation orient) {
        mOrientation = Preconditions.checkNotNull(orient);
        if (mOrientation == Orientation.VERTICAL) {
            yRndr.setHandleValues(true);
        } else {
            xRndr.setHandleValues(true);
        }
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner bottom side of the chart
     */
    public float getInnerChartBottom() {
        return yRndr.getInnerChartBottom();
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner left side of the chart
     */
    public float getInnerChartLeft() {
        return xRndr.getInnerChartLeft();
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner right side of the chart
     */
    public float getInnerChartRight() {
        return xRndr.getInnerChartRight();
    }

    /**
     * Inner Chart refers only to the area where chart data will be draw,
     * excluding labels, axis, etc.
     *
     * @return Position of the inner top side of the chart
     */
    public float getInnerChartTop() {
        return yRndr.getInnerChartTop();
    }

    /**
     * Returns the position of 0 value on chart.
     *
     * @return Position of 0 value on chart
     */
    public float getZeroPosition() {
        AxisRenderer rndr = mOrientation == Orientation.VERTICAL ? yRndr : xRndr;

        if (rndr.getBorderMinimumValue() > 0)
            return rndr.parsePos(0, rndr.getBorderMinimumValue());
        else if (rndr.getBorderMaximumValue() < 0)
            return rndr.parsePos(0, rndr.getBorderMaximumValue());

        return rndr.parsePos(0, 0);
    }

    /**
     * Get the step used between Y values.
     *
     * @return step
     */
    float getStep() {
        if (mOrientation == Orientation.VERTICAL)
            return yRndr.getStep();
        else
            return xRndr.getStep();
    }

    /**
     * A step is seen as the step to be defined between 2 labels.
     * As an example a step of 2 with a max label value of 6 will end
     * up with {0, 2, 4, 6} as labels.
     *
     * @param step (real) value distance from every label
     * @return {@link ChartView} self-reference.
     */
    public ChartView setStep(int step) {
        if (step <= 0)
            throw new IllegalArgumentException("Step can't be lower or equal to 0");

        if (mOrientation == Orientation.VERTICAL)
            yRndr.setStep(step);
        else
            xRndr.setStep(step);

        return this;
    }

    /**
     * Get chart's border spacing.
     *
     * @return spacing
     */
    float getBorderSpacing() {
        return style.axisBorderSpacing;
    }

    /**
     * 图表左边轴 与 第1个 标签之间的间距
     * 图片右边轴 与 最后1个 标签之间的间距
     *
     * @param spacing 间距，默认0
     * @return {@link ChartView} self-reference.
     */
    public ChartView setBorderSpacing(int spacing) {
        style.axisBorderSpacing = spacing;
        return this;
    }

    /**
     * Get the whole data owned by the chart.
     *
     * @return List of {@link ChartSet} owned by the chart
     */
    public ArrayList<ChartSet> getData() {
        return data;
    }

    /**
     * Get the list of {@link android.graphics.Rect} associated to each entry of a ChartSet.
     *
     * @param index {@link ChartSet} index
     * @return The list of {@link android.graphics.Rect} for the specified dataset
     */
    public ArrayList<Rect> getEntriesArea(int index) {
        Preconditions.checkPositionIndex(index, mRegions.size());
        ArrayList<Rect> result = new ArrayList<>(mRegions.get(index).size());
        for (Region r : mRegions.get(index))
            result.add(getEntryRect(r));

        return result;
    }

    /**
     * Get the area, {@link android.graphics.Rect}, of an entry from the entry's {@link
     * android.graphics.Region}
     *
     * @param region Region covering {@link ChartEntry} area
     * @return {@link android.graphics.Rect} specifying the area of an {@link ChartEntry}
     */
    Rect getEntryRect(@NonNull Region region) {
        Preconditions.checkNotNull(region);
        // Subtract the view left/top padding to correct position
        return new Rect(region.getBounds().left - getPaddingLeft(),
                region.getBounds().top - getPaddingTop(), region.getBounds().right - getPaddingLeft(),
                region.getBounds().bottom - getPaddingTop());
    }

    /**
     * Get the current {@link Animation}
     * held by {@link ChartView}.
     * Useful, for instance, to define another endAction.
     *
     * @return Current {@link Animation}
     */
    public Animation getChartAnimation() {
        return mAnim;
    }

    /**
     * 显示/隐藏Y标签和相应的轴。
     *
     * @param position 无 - 无标签
     *                 外部 - 标签将位于图表外部
     *                 内部 - 标签将位于图表内部
     * @return {@link ChartView} self-reference.
     */
    public ChartView setYLabels(@NonNull YRenderer.LabelPosition position) {
        style.yLabelsPositioning = Preconditions.checkNotNull(position);
        return this;
    }

    /**
     * 显示/隐藏X标签和相应的轴。
     *
     * @param position 无 - 无标签
     *                 外部 - 标签将位于图表外部
     *                 内部 - 标签将位于图表内部
     * @return {@link ChartView} self-reference.
     */
    public ChartView setXLabels(@NonNull XRenderer.LabelPosition position) {
        style.xLabelsPositioning = Preconditions.checkNotNull(position);
        return this;
    }

    /**
     * Set the format to be added to Y labels.
     *
     * @param format Format to be applied
     * @return {@link ChartView} self-reference.
     */
    public ChartView setLabelsFormat(@NonNull DecimalFormat format) {
        style.labelsFormat = Preconditions.checkNotNull(format);
        return this;
    }

    /**
     * 设置标签字体的颜色
     *
     * @param color colorInt
     * @return {@link ChartView} self-reference.
     */
    public ChartView setLabelsColor(@ColorInt int color) {
        style.labelsColor = color;
        return this;
    }

    /**
     * 设置标签字体的大小
     *
     * @param size px
     * @return {@link ChartView} self-reference.
     */
    public ChartView setFontSize(@FloatRange(from = 0) float size) {
        style.fontSize = size;
        return this;
    }

    /**
     * Set typeface to be used in labels.
     *
     * @param typeface To be applied to labels
     * @return {@link ChartView} self-reference.
     */
    public ChartView setTypeface(@NonNull Typeface typeface) {
        style.typeface = Preconditions.checkNotNull(typeface);
        return this;
    }

    /**
     * Show/Hide X axis.
     *
     * @param bool If true axis won't be visible
     * @return {@link ChartView} self-reference.
     */
    public ChartView setXAxis(boolean bool) {
        style.hasXAxis = bool;
        return this;
    }

    /**
     * Show/Hide Y axis.
     *
     * @param bool If true axis won't be visible
     * @return {@link ChartView} self-reference.
     */
    public ChartView setYAxis(boolean bool) {
        style.hasYAxis = bool;
        return this;
    }

    /**
     * 步长被视为两个标签之间定义的步长。
     * 例如，步长为 2 且 maxAxisValue 为 6 的标签最终将为 {0, 2, 4, 6}。
     *
     * @param minValue Y轴最小值的标签
     * @param maxValue Y轴最大值的标签
     * @param step     标签的距离步长
     * @return {@link ChartView} self-reference.
     */
    public ChartView setAxisBorderValues(float minValue, float maxValue, float step) {
        if (mOrientation == Orientation.VERTICAL)
            yRndr.setBorderValues(minValue, maxValue, step);
        else
            xRndr.setBorderValues(minValue, maxValue, step);
        return this;
    }

    /**
     * 步长默认按  (max - min) / 3 计算
     *
     * @param minValue Y轴最小值的标签
     * @param maxValue Y轴最大值的标签
     * @return {@link ChartView} self-reference.
     */
    public ChartView setAxisBorderValues(float minValue, float maxValue) {
        if (mOrientation == Orientation.VERTICAL)
            yRndr.setBorderValues(minValue, maxValue);
        else
            xRndr.setBorderValues(minValue, maxValue);
        return this;
    }

    /**
     * 轴的粗细
     *
     * @param thickness px
     * @return {@link ChartView} self-reference.
     */
    public ChartView setAxisThickness(@FloatRange(from = 0.f) float thickness) {
        style.axisThickness = thickness;
        return this;
    }

    /**
     * 轴的颜色
     *
     * @param color colorInt
     * @return {@link ChartView} self-reference.
     */
    public ChartView setAxisColor(@ColorInt int color) {
        style.axisColor = color;
        return this;
    }

    /**
     * Register a listener to be called when an {@link ChartEntry} is clicked.
     *
     * @param listener Listener to be used for callback.
     */
    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.mEntryListener = listener;
    }

    /**
     * Register a listener to be called when the {@link ChartView} is clicked.
     *
     * @param listener Listener to be used for callback.
     */
    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.mChartListener = listener;
    }

    /**
     * 图表顶部和第一个标签之间的间距
     *
     * @param spacing 间距，默认0
     * @return {@link ChartView} self-reference.
     */
    public ChartView setTopSpacing(int spacing) {
        style.axisTopSpacing = spacing;
        return this;
    }

    /**
     * 使用网格来填充图表
     *
     * @param rows    网络的行数
     * @param columns 网络的列数
     * @param paint   用于绘制网格的Paint实例，如果为null，则不会绘制网格
     * @return {@link ChartView} self-reference.
     */
    public ChartView setGrid(@IntRange(from = 0) int rows, @IntRange(from = 0) int columns, @NonNull Paint paint) {
        if (rows < 0 || columns < 0)
            throw new IllegalArgumentException("Number of rows/columns can't be smaller than 0.");
        style.gridRows = rows;
        style.gridColumns = columns;
        style.gridPaint = Preconditions.checkNotNull(paint);
        return this;
    }

    /**
     * 绘制阈值区域
     * 如果要绘制一条直线，起始值和结束值相等即可
     *
     * @param startValue 起始值
     * @param endValue   结束值
     * @param paint      用于绘制阈值线的Paint实例，如果为null，则不会绘制
     * @return {@link ChartView} self-reference.
     */
    public ChartView setValueThreshold(float startValue, float endValue, @NonNull Paint paint) {
        mThresholdStartValues.add(startValue);
        mThresholdEndValues.add(endValue);
        style.valueThresPaint = Preconditions.checkNotNull(paint);
        return this;
    }

    /**
     * 绘制阈值区域
     *
     * @param startValues 起始值
     * @param endValues   结束值
     * @param paint       用于绘制阈值线的Paint实例，如果为null，则不会绘制
     * @return {@link ChartView} self-reference.
     */
    public ChartView setValueThreshold(@NonNull float[] startValues, @NonNull float[] endValues, @NonNull Paint paint) {
        Preconditions.checkNotNull(startValues);
        Preconditions.checkNotNull(endValues);

        mThresholdStartValues.clear();
        mThresholdEndValues.clear();
        for (int i = 0; i < startValues.length; i++) {
            mThresholdStartValues.add(startValues[i]);
            mThresholdEndValues.add(endValues[i]);
        }
        style.valueThresPaint = Preconditions.checkNotNull(paint);
        return this;
    }

    /**
     * 绘制阈值线的label
     *
     * @param startLabel 开始的label
     * @param endLabel   结束的label
     * @param paint      用于绘制阈值线label的Paint实例，如果为null，则不会绘制
     * @return {@link ChartView} self-reference.
     */
    public ChartView setLabelThreshold(int startLabel, int endLabel, @NonNull Paint paint) {
        mThresholdStartLabels.add(startLabel);
        mThresholdEndLabels.add(endLabel);
        style.labelThresPaint = Preconditions.checkNotNull(paint);
        return this;
    }

    /**
     * 绘制阈值线的label，适用于多段折线
     *
     * @param startLabels 开始的label
     * @param endLabels   结束的label
     * @param paint       用于绘制阈值线label的Paint实例，如果为null，则不会绘制
     * @return {@link ChartView} self-reference.
     */
    public ChartView setLabelThreshold(@NonNull int[] startLabels, @NonNull int[] endLabels, @NonNull Paint paint) {
        Preconditions.checkNotNull(startLabels);
        Preconditions.checkNotNull(endLabels);

        mThresholdStartLabels.clear();
        mThresholdEndLabels.clear();
        for (int i = 0; i < startLabels.length; i++) {
            mThresholdStartLabels.add(startLabels[i]);
            mThresholdEndLabels.add(endLabels[i]);
        }
        style.labelThresPaint = Preconditions.checkNotNull(paint);
        return this;
    }

    /**
     * 设置标签和轴之间的间距。同时适用于X和Y轴
     *
     * @param spacing 间距，默认5dip
     * @return {@link ChartView} self-reference.
     */
    public ChartView setAxisLabelsSpacing(int spacing) {
        style.axisLabelsSpacing = spacing;
        return this;
    }

    /**
     * 必要时强制水平边框（例如：条形图）
     * 根据图表的方向设置属性。
     * 例如，如果方向为垂直，则意味着此属性必须由水平轴而不是垂直轴处理。
     */
    void setMandatoryBorderSpacing() {
        if (mOrientation == Orientation.VERTICAL)
            xRndr.setMandatoryBorderSpacing(true);
        else
            yRndr.setMandatoryBorderSpacing(true);
    }

    /**
     * Set the {@link Tooltip} object which will be used to create chart tooltips.
     *
     * @param tooltip {@link Tooltip} object in order to produce chart tooltips
     * @return {@link ChartView} self-reference.
     */
    public ChartView setTooltips(Tooltip tooltip) {
        mTooltip = tooltip;
        return this;
    }

    /**
     * 手动设置图表可点击区域。
     * 通常系统会设置与屏幕上条目位置相匹配的区域。
     * 有关更多信息，请参阅方法 defineRegions。
     *
     * @param regions 可点击区域，用于检测触摸事件。
     */
    void setClickableRegions(ArrayList<ArrayList<Region>> regions) {
        mRegions = regions;
    }

    /**
     * Applies an alpha to the paint object.
     *
     * @param paint  {@link android.graphics.Paint} object to apply alpha
     * @param alpha  Alpha value (opacity)
     * @param dx     Dx
     * @param dy     Dy
     * @param radius Radius
     * @param color  Color
     */
    protected void applyShadow(Paint paint, float alpha, float dx, float dy, float radius, int[] color) {
        paint.setAlpha((int) (alpha * Style.FULL_ALPHA));
        paint.setShadowLayer(radius, dx, dy,
                Color.argb(Math.min((int) (alpha * Style.FULL_ALPHA), color[0]), color[1], color[2], color[3]));
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    /**
     * 负责对 Graph 进行样式化的类！
     * 可以实例化，也可以不实例化属性。
     */
    public class Style {

        static final int FULL_ALPHA = 255;
        private static final int DEFAULT_COLOR = Color.BLACK;
        private static final int DEFAULT_GRID_OFF = 0;

        /**
         * Chart
         */
        private Paint chartPaint;

        /**
         * Axis
         */
        private boolean hasXAxis;
        private boolean hasYAxis;
        private float axisThickness;
        private int axisColor;

        private int axisLabelsSpacing;// 轴和标签之间的距离
        private int axisBorderSpacing;// 轴标签和图表边之间的间距
        private int axisTopSpacing;// 图表顶部和轴标签之间的间距

        private Paint gridPaint;// 背景网络的画笔
        private Paint labelThresPaint;// 阈值区域 - 标签的画笔
        private Paint valueThresPaint;// 阈值区域 - 数值的画笔

        private AxisRenderer.LabelPosition xLabelsPositioning;
        private AxisRenderer.LabelPosition yLabelsPositioning;

        private Paint labelsPaint;
        private int labelsColor;
        private float fontSize;
        private Typeface typeface;

        private int fontMaxHeight;// 基于定义的字体样式，取文本的高度
        private int gridRows;// 网络线的条数
        private int gridColumns;// 网络线的列数

        private DecimalFormat labelsFormat;// 标签的格式化显示

        Style(Context context) {
            axisColor = DEFAULT_COLOR;
            axisThickness = context.getResources().getDimension(R.dimen.grid_thickness);
            hasXAxis = true;
            hasYAxis = true;

            xLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
            yLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
            labelsColor = DEFAULT_COLOR;
            fontSize = context.getResources().getDimension(R.dimen.font_size);

            axisLabelsSpacing = context.getResources().getDimensionPixelSize(R.dimen.axis_labels_spacing);
            axisBorderSpacing = context.getResources().getDimensionPixelSize(R.dimen.axis_border_spacing);
            axisTopSpacing = context.getResources().getDimensionPixelSize(R.dimen.axis_top_spacing);

            gridRows = DEFAULT_GRID_OFF;
            gridColumns = DEFAULT_GRID_OFF;

            labelsFormat = new DecimalFormat();
        }

        Style(Context context, AttributeSet attrs) {
            TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChartAttrs, 0, 0);

            hasXAxis = arr.getBoolean(R.styleable.ChartAttrs_chart_axis, true);
            hasYAxis = arr.getBoolean(R.styleable.ChartAttrs_chart_axis, true);
            axisColor = arr.getColor(R.styleable.ChartAttrs_chart_axisColor, DEFAULT_COLOR);
            axisThickness = arr.getDimension(R.styleable.ChartAttrs_chart_axisThickness, context.getResources().getDimension(R.dimen.axis_thickness));

            switch (arr.getInt(R.styleable.ChartAttrs_chart_labels, 0)) {
                case 1:
                    xLabelsPositioning = AxisRenderer.LabelPosition.INSIDE;
                    yLabelsPositioning = AxisRenderer.LabelPosition.INSIDE;
                    break;
                case 2:
                    xLabelsPositioning = AxisRenderer.LabelPosition.NONE;
                    yLabelsPositioning = AxisRenderer.LabelPosition.NONE;
                    break;
                default:
                    xLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
                    yLabelsPositioning = AxisRenderer.LabelPosition.OUTSIDE;
                    break;
            }

            labelsColor = arr.getColor(R.styleable.ChartAttrs_chart_labelColor, DEFAULT_COLOR);
            fontSize = arr.getDimension(R.styleable.ChartAttrs_chart_fontSize, context.getResources().getDimension(R.dimen.font_size));

            String typefaceName = arr.getString(R.styleable.ChartAttrs_chart_typeface);
            if (typefaceName != null)
                typeface = Typeface.createFromAsset(getResources().getAssets(), typefaceName);

            axisLabelsSpacing = arr.getDimensionPixelSize(R.styleable.ChartAttrs_chart_axisLabelsSpacing, context.getResources().getDimensionPixelSize(R.dimen.axis_labels_spacing));
            axisBorderSpacing = arr.getDimensionPixelSize(R.styleable.ChartAttrs_chart_axisBorderSpacing, context.getResources().getDimensionPixelSize(R.dimen.axis_border_spacing));
            axisTopSpacing = arr.getDimensionPixelSize(R.styleable.ChartAttrs_chart_axisTopSpacing, context.getResources().getDimensionPixelSize(R.dimen.axis_top_spacing));

            gridRows = DEFAULT_GRID_OFF;
            gridColumns = DEFAULT_GRID_OFF;

            labelsFormat = new DecimalFormat();
            arr.recycle();
        }

        private void init() {
            chartPaint = new Paint();
            chartPaint.setColor(axisColor);
            chartPaint.setStyle(Paint.Style.STROKE);
            chartPaint.setStrokeWidth(axisThickness);
            chartPaint.setAntiAlias(true);

            labelsPaint = new Paint();
            labelsPaint.setColor(labelsColor);
            labelsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            labelsPaint.setAntiAlias(true);
            labelsPaint.setTextSize(fontSize);
            labelsPaint.setTypeface(typeface);

            fontMaxHeight = (int) (style.labelsPaint.descent() - style.labelsPaint.ascent());
        }

        private void clean() {
            chartPaint = null;
            labelsPaint = null;
        }

        /**
         * Get label's height.
         *
         * @param text Label to measure
         * @return Height of label
         */
        public int getLabelHeight(String text) {
            final Rect rect = new Rect();
            style.labelsPaint.getTextBounds(text, 0, text.length(), rect);
            return rect.height();
        }

        public Paint getChartPaint() {
            return chartPaint;
        }

        public float getAxisThickness() {
            return axisThickness;
        }

        /**
         * If axis x (not the labels) should be displayed.
         *
         * @return True if axis x is displayed
         */
        public boolean hasXAxis() {
            return hasXAxis;
        }

        /**
         * If axis y (not the labels) should be displayed.
         *
         * @return True if axis y is displayed
         */
        public boolean hasYAxis() {
            return hasYAxis;
        }

        public Paint getLabelsPaint() {
            return labelsPaint;
        }

        public int getFontMaxHeight() {
            return fontMaxHeight;
        }

        public AxisRenderer.LabelPosition getXLabelsPositioning() {
            return xLabelsPositioning;
        }

        public AxisRenderer.LabelPosition getYLabelsPositioning() {
            return yLabelsPositioning;
        }

        public int getAxisLabelsSpacing() {
            return axisLabelsSpacing;
        }

        public int getAxisBorderSpacing() {
            return axisBorderSpacing;
        }

        public int getAxisTopSpacing() {
            return axisTopSpacing;
        }

        public DecimalFormat getLabelsFormat() {
            return labelsFormat;
        }

        private boolean hasHorizontalGrid() {
            return gridRows > 0;
        }

        private boolean hasVerticalGrid() {
            return gridColumns > 0;
        }

    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent ev) {

            if (mEntryListener != null || mTooltip != null) { // Check if tap on any entry
                int nSets = mRegions.size();
                int nEntries = mRegions.get(0).size();
                for (int i = 0; i < nSets; i++)
                    for (int j = 0; j < nEntries; j++)
                        if (mRegions.get(i).get(j).contains((int) ev.getX(), (int) ev.getY())) {
                            if (mEntryListener != null)  // Trigger entry callback
                                mEntryListener.onClick(i, j, getEntryRect(mRegions.get(i).get(j)));
                            if (mTooltip != null)  // Toggle tooltip
                                toggleTooltip(getEntryRect(mRegions.get(i).get(j)), data.get(i).getValue(j));
                            return true;
                        }
            }

            if (mChartListener != null) mChartListener.onClick(ChartView.this);
            if (mTooltip != null && mTooltip.on()) dismissTooltip(mTooltip);
            return true;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

    }

}
