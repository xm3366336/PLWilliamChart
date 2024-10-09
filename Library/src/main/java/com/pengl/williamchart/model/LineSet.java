package com.pengl.williamchart.model;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.pengl.williamchart.util.Tools;
import com.pengl.williamchart.view.LineChartView;
import com.pengl.williamchart.util.Preconditions;

/**
 * Data model containing a set of {@link Point} to be used by {@link LineChartView}.
 */
public class LineSet extends ChartSet {

    private static final int DEFAULT_COLOR = -16777216;     // 线条的默认颜色
    private static final float LINE_THICKNESS = 4;          // 线条的默认厚度
    private static final float LINE_VALUE_SIZE = 8;         // 数值，默认字体大小

    /**
     * 线条的厚度
     */
    private float mThickness;

    /**
     * 线条的颜色
     */
    private int mColor;

    /**
     * 是否开启绘制每个点的数值
     */
    private boolean isEnableDrawValue = false;

    /**
     * 数值，字体的大小
     */
    private float mValueTextSize;

    /**
     * 数值，字体的颜色
     */
    private int mValueColor;

    /**
     * 是否虚线样式
     */
    private boolean mIsDashed;

    /**
     * 是否平滑样式
     */
    private boolean mIsSmooth;

    /**
     * 背景是否填充颜色
     */
    private boolean mHasFill;

    /**
     * 背景填充的颜色
     */
    private int mFillColor;

    /**
     * 背景是否渐变填充
     */
    private boolean mHasGradientFill;

    /**
     * 背景渐变填充的颜色
     */
    private int[] mGradientColors;

    private float[] mGradientPositions;

    /**
     * Index where set begins/ends
     */
    private int mBegin;

    private int mEnd;

    /**
     * 虚线样式，实线点和虚线点的间隔
     */
    private float[] mDashedIntervals;

    /**
     * 阴影相关的变量
     */
    private float mShadowRadius;
    private float mShadowDx;
    private float mShadowDy;
    private int[] mShadowColor;

    public LineSet() {
        super();
        init();
    }

    public LineSet(@NonNull String[] labels, @NonNull float[] values) {
        super();
        init();

        if (labels.length != values.length)
            throw new IllegalArgumentException("Arrays size doesn't match.");

        Preconditions.checkNotNull(labels);
        Preconditions.checkNotNull(values);

        int nEntries = labels.length;
        for (int i = 0; i < nEntries; i++)
            addPoint(labels[i], values[i]);
    }

    private void init() {
        mThickness = Tools.fromDpToPx(LINE_THICKNESS);
        mValueTextSize = Tools.fromDpToPx(LINE_VALUE_SIZE);
        mColor = DEFAULT_COLOR;
        mValueColor = DEFAULT_COLOR;

        mIsDashed = false;
        mDashedIntervals = null;

        mIsSmooth = false;

        mHasFill = false;
        mFillColor = DEFAULT_COLOR;

        mHasGradientFill = false;
        mGradientColors = null;
        mGradientPositions = null;

        mBegin = 0;
        mEnd = 0;

        mShadowRadius = 0;
        mShadowDx = 0;
        mShadowDy = 0;
        mShadowColor = new int[4];
    }

    /**
     * Add new {@link Point} from a string and a float.
     *
     * @param label new {@link Point}'s label
     * @param value new {@link Point}'s value
     */
    public void addPoint(String label, float value) {
        this.addPoint(new Point(label, value));
    }

    /**
     * Add new {@link Point}.
     *
     * @param point new {@link Point}
     */
    public void addPoint(@NonNull Point point) {
        this.addEntry(Preconditions.checkNotNull(point));
    }

    /**
     * If line dashed.
     *
     * @return true if dashed property defined.
     */
    public boolean isDashed() {
        return mIsDashed;
    }

    /**
     * Define a dashed effect to the line.
     *
     * @param intervals Array of ON and OFF distances
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDashed(@NonNull float[] intervals) {
        mIsDashed = true;
        mDashedIntervals = Preconditions.checkNotNull(intervals);
        return this;
    }

    /**
     * If line smooth.
     *
     * @return true if smooth property defined.
     */
    public boolean isSmooth() {
        return mIsSmooth;
    }

    /**
     * Define a smooth effect to the line.
     *
     * @param bool True if line smooth
     * @return {@link LineSet} self-reference.
     */
    public LineSet setSmooth(boolean bool) {
        mIsSmooth = bool;
        return this;
    }

    /**
     * If line has fill color defined.
     *
     * @return true if fill property defined.
     */
    public boolean hasFill() {
        return mHasFill;
    }

    /**
     * If line has gradient fill color defined.
     *
     * @return true if gradient fill property defined.
     */
    public boolean hasGradientFill() {
        return mHasGradientFill;
    }

    /**
     * If line has shadow.
     *
     * @return True if set has shadow defined, False otherwise.
     */
    public boolean hasShadow() {
        return mShadowRadius != 0;
    }

    /**
     * Retrieve line's thickness.
     *
     * @return Line's thickness.
     */
    public float getThickness() {
        return mThickness;
    }

    /**
     * Define the thickness to be used when drawing the line.
     *
     * @param thickness Line thickness. Can't be equal or less than 0
     * @return {@link LineSet} self-reference.
     */
    public LineSet setThickness(@FloatRange(from = 0.f) float thickness) {
        if (thickness < 0)
            throw new IllegalArgumentException("Line thickness can't be <= 0.");

        mThickness = thickness;
        return this;
    }

    /**
     * Retrieve line's color.
     *
     * @return Line's color.
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Define the color to be used when drawing the line.
     *
     * @param color line color.
     * @return {@link LineSet} self-reference.
     */
    public LineSet setColor(@ColorInt int color) {
        mColor = color;
        return this;
    }

    /**
     * 数值字体颜色
     */
    public int getValueColor() {
        return mValueColor;
    }

    /**
     * 设置数值字体颜色
     */
    public LineSet setValueColor(@ColorInt int color) {
        mValueColor = color;
        return this;
    }

    /**
     * 获取数值字体大小
     */
    public float getValueTextSize() {
        return mValueTextSize;
    }

    /**
     * 设置value值字体大小
     */
    public LineSet setValueTextSize(@FloatRange float valueTextSize) {
        mValueTextSize = valueTextSize;
        return this;
    }

    /**
     * @return is Draw value
     */
    public boolean isEnableDrawValue() {
        return isEnableDrawValue;
    }

    /**
     * @param isDraw is Draw value
     */
    public LineSet setEnableDrawValue(boolean isDraw) {
        isEnableDrawValue = isDraw;
        return this;
    }

    /**
     * Retrieve color defined for line's fill.
     * Fill property must have been previously defined.
     *
     * @return Line's fill color.
     */
    public int getFillColor() {
        return mFillColor;
    }

    /**
     * Retrieve set of colors defining the gradient of line's fill.
     * Gradient fill property must have been previously defined.
     *
     * @return Gradient colors array.
     */
    public int[] getGradientColors() {
        return mGradientColors;
    }

    /**
     * Retrieve set of positions to define the gradient of line's fill.
     * Gradient fill property must have been previously defined.
     *
     * @return Gradient positions.
     */
    public float[] getGradientPositions() {
        return mGradientPositions;
    }

    /**
     * Retrieve first {@link Point} that will be displayed for this set.
     *
     * @return first displayed {@link Point}.
     */
    public int getBegin() {
        return mBegin;
    }

    /**
     * Retrieve last {@link Point} that will be displayed for this set.
     *
     * @return last displayed {@link Point}.
     */
    public int getEnd() {
        if (mEnd == 0)
            return size();
        return mEnd;
    }

    /**
     * Retrieve set of intervals defining line's dash.
     * Dashed property must have been previously defined.
     *
     * @return Line dashed intervals. Dashed property must have been previously defined.
     */
    public float[] getDashedIntervals() {
        return mDashedIntervals;
    }

    /**
     * @return Line dashed phase. Dashed property must have been previously defined.
     */
    public int getDashedPhase() {
        return 0;
    }

    public float getShadowRadius() {
        return mShadowRadius;
    }

    public float getShadowDx() {
        return mShadowDx;
    }

    public float getShadowDy() {
        return mShadowDy;
    }

    public int[] getShadowColor() {
        return mShadowColor;
    }

    /**
     * Define the color to fill up the line area.
     * If no color has been previously defined to the line it will automatically be set to the
     * same color fill color.
     *
     * @param color filling color.
     * @return {@link LineSet} self-reference.
     */
    public LineSet setFill(@ColorInt int color) {
        mHasFill = true;
        mFillColor = color;

        if (mColor == DEFAULT_COLOR)
            mColor = color;

        return this;
    }

    /**
     * Define the gradient colors to fill up the line area.
     * If no color has been previously defined to the line it will automatically be set to the
     * first color defined in gradient.
     *
     * @param colors    The colors to be distributed among gradient
     * @param positions Position/order from which the colors will be place
     * @return {@link LineSet} self-reference.
     */
    public LineSet setGradientFill(@NonNull @Size(min = 1) int colors[], float[] positions) {
        if (colors.length == 0)
            throw new IllegalArgumentException("Colors argument can't be null or empty.");

        mHasGradientFill = true;
        mGradientColors = Preconditions.checkNotNull(colors);
        mGradientPositions = positions;

        if (mColor == DEFAULT_COLOR)
            mColor = colors[0];

        return this;
    }

    /**
     * Define at which {@link Point} should the dataset begin.
     *
     * @param index Index where the set begins. Argument mustn't be negative or greater than set's
     *              size.
     * @return {@link LineSet} self-reference.
     */
    public LineSet beginAt(@IntRange(from = 0) int index) {
        mBegin = Preconditions.checkPositionIndex(index, this.size());
        return this;
    }

    /**
     * Define at which {@link Point} should the dataset end.
     *
     * @param index Where the set ends. Argument mustn't be negative, greater than set's size, or
     *              lesser than the first point to be displayed (defined by beginAt() method)..
     * @return {@link LineSet} self-reference.
     */
    public LineSet endAt(@IntRange(from = 0) int index) {
        if (index < mBegin)
            throw new IllegalArgumentException("Index cannot be lesser than the start entry " + "defined in beginAt(index).");

        mEnd = Preconditions.checkPositionIndex(index, this.size());
        return this;
    }

    /**
     * Define the color to be used when drawing the dots.
     * Color will be assigned to all {@link Point}s in the set.
     * Will override previous defined values for any {@link Point}.
     *
     * @param color dots color
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsColor(@ColorInt int color) {
        for (ChartEntry e : getEntries())
            e.setColor(color);

        return this;
    }

    /**
     * Define the radius to be used when drawing the dots.
     * Radius will be assigned to all {@link Point}s in the set.
     * Will override previous defined values for any {@link Point}.
     *
     * @param radius dots radius.
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsRadius(@FloatRange(from = 0.f) float radius) {
        if (radius < 0.f)
            throw new IllegalArgumentException("Dots radius can't be < 0.");

        for (ChartEntry e : getEntries())
            ((Point) e).setRadius(radius);

        return this;
    }

    /**
     * Define the stroke thickness to be used when drawing the dots.
     * Thickness will override previous defined values for any {@link Point}.
     * Will override previous defined values for any {@link Point}s.
     *
     * @param thickness Grid thickness. Can't be equal or less than 0
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsStrokeThickness(@FloatRange(from = 0.f) float thickness) {
        if (thickness < 0.f)
            throw new IllegalArgumentException("Dots thickness can't be < 0.");

        for (ChartEntry e : getEntries())
            ((Point) e).setStrokeThickness(thickness);

        return this;
    }

    /**
     * Define the stroke color to be used when drawing the dots.
     * Color will override previous defined values for any {@link Point}.
     * Will override previous defined values for any {@link Point}s.
     *
     * @param color dots stroke color.
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsStrokeColor(@ColorInt int color) {
        for (ChartEntry e : getEntries())
            ((Point) e).setStrokeColor(color);

        return this;
    }

    /**
     * Define a background drawable to each of the dataset points to be
     * drawn instead of the usual dot.
     *
     * @param drawable dots drawable image.
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsDrawable(@NonNull Drawable drawable) {
        Preconditions.checkNotNull(drawable);

        for (ChartEntry e : getEntries())
            ((Point) e).setDrawable(drawable);

        return this;
    }

    @Override
    public void setShadow(float radius, float dx, float dy, int color) {
        super.setShadow(radius, dx, dy, color);

        mShadowRadius = radius;
        mShadowDx = dx;
        mShadowDy = dy;

        mShadowColor[0] = Color.alpha(color);
        mShadowColor[1] = Color.red(color);
        mShadowColor[2] = Color.blue(color);
        mShadowColor[3] = Color.green(color);
    }

}
