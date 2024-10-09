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
 * 包含一组 {@link Point} 的数据模型
 * 提供给 {@link LineChartView} 使用。
 */
public class LineSet extends ChartSet {

    private static final int DEFAULT_COLOR = -16777216;     // 线条的默认颜色
    private static final float LINE_THICKNESS = 4;          // 线条的默认粗细
    private static final float LINE_VALUE_SIZE = 8;         // 数值，默认字体大小

    private float mThickness;                   // 线条的粗细
    private int mColor;                         // 线条的颜色
    private boolean isEnableDrawValue = false;  // 是否开启绘制每个点的数值
    private float mValueTextSize;               // 数值，字体的大小
    private int mValueColor;                    // 数值，字体的颜色
    private boolean mIsDashed;                  // 是否虚线样式
    private boolean mIsSmooth;                  // 是否平滑样式
    private boolean mHasFill;                   // 背景是否填充颜色
    private int mFillColor;                     // 背景填充的颜色
    private boolean mHasGradientFill;           // 背景是否渐变填充
    private int[] mGradientColors;              // 背景渐变填充的颜色
    private float[] mGradientPositions;         // 背景渐变填充的颜色对应的相对位置，如果为null，则颜色沿渐变线均匀分布。
    private int mBegin;                         // 定义数据集从哪个点开始
    private int mEnd;                           // 定义数据集从哪个点结束
    private float[] mDashedIntervals;           // 虚线样式，实线点和虚线点的间隔

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
     * 添加一个点 {@link Point}
     *
     * @param label 该点的 {@link Point} 标签
     * @param value 该点的 {@link Point} 浮点值
     */
    public void addPoint(String label, float value) {
        this.addPoint(new Point(label, value));
    }

    /**
     * 添加一个点 {@link Point}.
     *
     * @param point new {@link Point}
     */
    public void addPoint(@NonNull Point point) {
        this.addEntry(Preconditions.checkNotNull(point));
    }

    /**
     * 是否虚线样式
     */
    public boolean isDashed() {
        return mIsDashed;
    }

    /**
     * 为线条定义虚线效果。
     *
     * @param intervals 开启和关闭距离阵列
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDashed(@NonNull float[] intervals) {
        mIsDashed = true;
        mDashedIntervals = Preconditions.checkNotNull(intervals);
        return this;
    }

    /**
     * 线条是否使用平滑的效果
     */
    public boolean isSmooth() {
        return mIsSmooth;
    }

    /**
     * 线条是否使用平滑的效果
     *
     * @param bool true是的
     * @return {@link LineSet} self-reference.
     */
    public LineSet setSmooth(boolean bool) {
        mIsSmooth = bool;
        return this;
    }

    /**
     * 背景是否填充颜色
     */
    public boolean hasFill() {
        return mHasFill;
    }

    /**
     * 背景是否填充渐变颜色
     */
    public boolean hasGradientFill() {
        return mHasGradientFill;
    }

    /**
     * 线条是否使用阴影
     */
    public boolean hasShadow() {
        return mShadowRadius != 0;
    }

    /**
     * 获取线条的粗细
     */
    public float getThickness() {
        return mThickness;
    }

    /**
     * 定义绘制线条时要使用的粗细。
     *
     * @param thickness 线条粗细。不能等于或小于0
     * @return {@link LineSet} self-reference.
     */
    public LineSet setThickness(@FloatRange(from = 0.f) float thickness) {
        if (thickness < 0)
            throw new IllegalArgumentException("Line thickness can't be <= 0.");
        mThickness = thickness;
        return this;
    }

    /**
     * 线条的颜色
     */
    public int getColor() {
        return mColor;
    }

    /**
     * 定义绘制线条时使用的颜色。
     *
     * @param color colorInt 线条颜色
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
     * 是否开启绘制每个点的数值
     */
    public boolean isEnableDrawValue() {
        return isEnableDrawValue;
    }

    /**
     * 是否绘制值
     *
     * @param isDraw true绘制
     */
    public LineSet setEnableDrawValue(boolean isDraw) {
        isEnableDrawValue = isDraw;
        return this;
    }

    /**
     * 检索为线条填充定义的颜色。
     * 填充属性必须事先定义。
     */
    public int getFillColor() {
        return mFillColor;
    }

    /**
     * 填充的渐变色
     */
    public int[] getGradientColors() {
        return mGradientColors;
    }

    /**
     * 颜色数组中每个对应颜色的相对位置[0..1]。
     * 如果为null，则颜色沿渐变线均匀分布。
     * <p>
     * 渐变填充属性必须事先定义。
     */
    public float[] getGradientPositions() {
        return mGradientPositions;
    }

    /**
     * 显示的第一个点{@link Point}。
     */
    public int getBegin() {
        return mBegin;
    }

    /**
     * 显示的最后一个点{@link Point}。
     */
    public int getEnd() {
        if (mEnd == 0)
            return size();
        return mEnd;
    }

    /**
     * 虚线样式，实线点和虚线点的间隔
     *
     * @return 线虚线间隔。虚线属性必须事先定义。
     */
    public float[] getDashedIntervals() {
        return mDashedIntervals;
    }

    /**
     * 间隔数组的偏移量
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
     * 定义填充线条区域的颜色。
     * 如果之前没有为线条定义颜色，它将自动设置为相同的颜色填充颜色。
     *
     * @param color colorInt 填充色
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
     * 定义渐变颜色以填充线条区域。
     * 如果之前未为线条定义颜色，则会自动将其设置为渐变中定义的第一个颜色。
     *
     * @param colors    渐变色的颜色
     * @param positions 颜色放置的位置/顺序
     * @return {@link LineSet} self-reference.
     */
    public LineSet setGradientFill(@NonNull @Size(min = 1) int[] colors, float[] positions) {
        if (colors.length == 0)
            throw new IllegalArgumentException("Colors argument can't be null or empty.");

        mHasGradientFill = true;
        mGradientColors = Preconditions.checkNotNull(colors);
        mGradientPositions = positions;

        if (mColor == DEFAULT_COLOR)
            mColor = colors[0];

        return this;
    }

    public LineSet setGradientFill(@NonNull @Size(min = 1) int[] colors) {
        return setGradientFill(colors, null);
    }

    /**
     * 定义数据集从哪个{@link Point} 开始。
     *
     * @param index 集合开始的索引。参数不得为负数或大于集合的大小。
     * @return {@link LineSet} self-reference.
     */
    public LineSet beginAt(@IntRange(from = 0) int index) {
        mBegin = Preconditions.checkPositionIndex(index, this.size());
        return this;
    }

    /**
     * 定义数据集从哪个{@link Point} 结束。
     *
     * @param index 集合结束的位置。参数不得为负数、大于集合的大小或小于要显示的第一个点（由 beginAt() 方法定义）。
     * @return {@link LineSet} self-reference.
     */
    public LineSet endAt(@IntRange(from = 0) int index) {
        if (index < mBegin)
            throw new IllegalArgumentException("Index cannot be lesser than the start entry " + "defined in beginAt(index).");
        mEnd = Preconditions.checkPositionIndex(index, this.size());
        return this;
    }

    /**
     * 定义绘制点时要使用的颜色。
     * 颜色将分配给集合中的所有 {@link Point}。
     * 将覆盖任何 {@link Point} 的先前定义值。
     *
     * @param color 描点的颜色
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsColor(@ColorInt int color) {
        for (ChartEntry e : getEntries())
            e.setColor(color);
        return this;
    }

    /**
     * 定义绘制点的大小
     * 半径将分配给集合中的所有{@link Point}。
     * 将覆盖任何{@link Point}的先前定义值。
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
     * 定义绘制点时要使用的描边粗细。
     * 粗细将覆盖任何 {@link Point} 的先前定义值。
     * 将覆盖任何 {@link Point} 的先前定义值。
     *
     * @param thickness 描边粗细。不能等于或小于0
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
     * 定义绘制点时要使用的描边颜色。
     * 颜色将覆盖任何 {@link Point} 的先前定义值。
     * 将覆盖任何 {@link Point} 的先前定义值。
     *
     * @param color 圆点的描边颜色
     * @return {@link LineSet} self-reference.
     */
    public LineSet setDotsStrokeColor(@ColorInt int color) {
        for (ChartEntry e : getEntries())
            ((Point) e).setStrokeColor(color);
        return this;
    }

    /**
     * 为每个要绘制的数据集点定义一个背景可绘制对象，而不是通常的点。
     *
     * @param drawable 点的图像
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
