package com.pengl.williamchart.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.FloatRange;

import com.pengl.williamchart.model.ChartSet;
import com.pengl.williamchart.model.LineSet;
import com.pengl.williamchart.model.Point;
import com.pengl.williamchart.util.Tools;
import com.pengl.williamchart.R;

import java.util.ArrayList;

/**
 * 折线图
 */
public class LineChartView extends ChartView {

    private static final float SMOOTH_FACTOR = 0.15f;

    /**
     * Style applied to line chart
     */
    private final Style mStyle;

    /**
     * Radius clickable region
     */
    private float mClickableRadius;

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(Orientation.VERTICAL);
        mStyle = new Style(context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChartAttrs, 0, 0));
        mClickableRadius = context.getResources().getDimension(R.dimen.dot_region_radius);
    }

    public LineChartView(Context context) {
        super(context);
        setOrientation(Orientation.VERTICAL);
        mStyle = new Style();
        mClickableRadius = context.getResources().getDimension(R.dimen.dot_region_radius);
    }

    /**
     * 给定一个点的索引，它将确保返回的索引在数组内。
     */
    private static int si(int setSize, int i) {
        if (i > setSize - 1)
            return setSize - 1;
        else if (i < 0)
            return 0;
        return i;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mStyle.init();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStyle.clean();
    }

    @Override
    public void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {
        LineSet lineSet;
        Path linePath;

        for (ChartSet set : data) {
            lineSet = (LineSet) set;

            if (lineSet.isVisible()) {
                mStyle.mLinePaint.setColor(lineSet.getColor());
                mStyle.mLinePaint.setStrokeWidth(lineSet.getThickness());
                applyShadow(mStyle.mLinePaint,
                        lineSet.getAlpha(),
                        lineSet.getShadowDx(),
                        lineSet.getShadowDy(),
                        lineSet.getShadowRadius(),
                        lineSet.getShadowColor());

                if (lineSet.isDashed())
                    mStyle.mLinePaint.setPathEffect(new DashPathEffect(lineSet.getDashedIntervals(), lineSet.getDashedPhase()));
                else
                    mStyle.mLinePaint.setPathEffect(null);

                if (lineSet.isSmooth())
                    linePath = createSmoothLinePath(lineSet);
                else
                    linePath = createLinePath(lineSet);

                // Draw background
                if (lineSet.hasFill() || lineSet.hasGradientFill())
                    canvas.drawPath(createBackgroundPath(new Path(linePath), lineSet), mStyle.mFillPaint);

                // Draw line
                canvas.drawPath(linePath, mStyle.mLinePaint);

                // Draw points
                drawPoints(canvas, lineSet);

                // draw line value
                if (lineSet.isEnableDrawValue())
                    drawValue(canvas, lineSet);
            }
        }

    }

    /**
     * 绘制文字
     */
    private void drawValue(Canvas canvas, LineSet lineSet) {
        int begin = lineSet.getBegin();
        int end = lineSet.getEnd();
        Point dot;
        float baselineY;
        float baseLinX;

        for (int i = begin; i < end; i++) {
            dot = (Point) lineSet.getEntry(i);
            // Style dot
            mStyle.mValuePaint.setColor(lineSet.getValueColor());
            mStyle.mValuePaint.setTextSize(lineSet.getValueTextSize());
            // Draw value
            String value = style.getLabelsFormat().format(dot.getValue());
            float textX = mStyle.mValuePaint.measureText(value);
            float yOffset = dot.getRadius() == 0 ? Tools.fromDpToPx(4) * 1.75f : dot.getRadius() * 1.75f;
            baselineY = dot.getY() - yOffset;
            baseLinX = dot.getX() - textX / 2;
            canvas.drawText(value, baseLinX, baselineY, mStyle.mValuePaint);
        }
    }

    @Override
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {
        float x, y;
        int dataSize = data.size();
        int setSize;

        for (int i = 0; i < dataSize; i++) {
            setSize = data.get(0).size();
            for (int j = 0; j < setSize; j++) {
                x = data.get(i).getEntry(j).getX();
                y = data.get(i).getEntry(j).getY();
                regions.get(i).get(j).set(
                        (int) (x - mClickableRadius), (int) (y - mClickableRadius),
                        (int) (x + mClickableRadius), (int) (y + mClickableRadius));
            }
        }
    }


    /**
     * Responsible for drawing points
     */
    private void drawPoints(Canvas canvas, LineSet set) {
        int begin = set.getBegin();
        int end = set.getEnd();
        Point dot;

        for (int i = begin; i < end; i++) {
            dot = (Point) set.getEntry(i);

            if (dot.isVisible()) {

                // Style dot
                mStyle.mDotsPaint.setColor(dot.getColor());
                mStyle.mDotsPaint.setAlpha((int) (set.getAlpha() * ChartView.Style.FULL_ALPHA));
                applyShadow(mStyle.mDotsPaint,
                        set.getAlpha(),
                        dot.getShadowDx(),
                        dot.getShadowDy(),
                        dot.getShadowRadius(),
                        dot.getShadowColor());

                // Draw dot
                canvas.drawCircle(dot.getX(), dot.getY(), dot.getRadius(), mStyle.mDotsPaint);

                // Draw dots stroke
                if (dot.hasStroke()) {

                    // Style stroke
                    mStyle.mDotsStrokePaint.setStrokeWidth(dot.getStrokeThickness());
                    mStyle.mDotsStrokePaint.setColor(dot.getStrokeColor());
                    mStyle.mDotsStrokePaint.setAlpha((int) (set.getAlpha() * ChartView.Style.FULL_ALPHA));
                    applyShadow(mStyle.mDotsStrokePaint,
                            set.getAlpha(),
                            dot.getShadowDx(),
                            dot.getShadowDy(),
                            dot.getShadowRadius(),
                            dot.getShadowColor());

                    canvas.drawCircle(dot.getX(), dot.getY(), dot.getRadius(), mStyle.mDotsStrokePaint);
                }

                // Draw drawable
                if (dot.getDrawable() != null) {
                    Bitmap dotsBitmap = Tools.drawableToBitmap(dot.getDrawable());
                    canvas.drawBitmap(dotsBitmap,
                            (float) (dot.getX() - dotsBitmap.getWidth() / 2.0),
                            (float) (dot.getY() - dotsBitmap.getHeight() / 2.0),
                            mStyle.mDotsPaint);
                }
            }
        }

    }


    /**
     * Responsible for drawing a (non smooth) line.
     *
     * @param set {@link LineSet} object
     * @return {@link Path} object containing line
     */
    Path createLinePath(LineSet set) {

        Path res = new Path();
        int begin = set.getBegin();
        int end = set.getEnd();

        for (int i = begin; i < end; i++) {
            if (i == begin)
                res.moveTo(set.getEntry(i).getX(), set.getEntry(i).getY());
            else
                res.lineTo(set.getEntry(i).getX(), set.getEntry(i).getY());
        }

        return res;
    }


    /**
     * 负责使用解析的屏幕点绘制平滑的线。
     *
     * @param set {@link LineSet} object.
     * @return {@link Path} object containing smooth line
     */
    Path createSmoothLinePath(LineSet set) {
        float thisPointX;
        float thisPointY;
        float nextPointX;
        float nextPointY;
        float startDiffX;
        float startDiffY;
        float endDiffX;
        float endDiffY;
        float firstControlX;
        float firstControlY;
        float secondControlX;
        float secondControlY;

        Path res = new Path();
        res.moveTo(set.getEntry(set.getBegin()).getX(), set.getEntry(set.getBegin()).getY());

        int begin = set.getBegin();
        int end = set.getEnd();
        for (int i = begin; i < end - 1; i++) {

            thisPointX = set.getEntry(i).getX();
            thisPointY = set.getEntry(i).getY();

            nextPointX = set.getEntry(i + 1).getX();
            nextPointY = set.getEntry(i + 1).getY();

            startDiffX = (nextPointX - set.getEntry(si(set.size(), i - 1)).getX());
            startDiffY = (nextPointY - set.getEntry(si(set.size(), i - 1)).getY());

            endDiffX = (set.getEntry(si(set.size(), i + 2)).getX() - thisPointX);
            endDiffY = (set.getEntry(si(set.size(), i + 2)).getY() - thisPointY);

            firstControlX = thisPointX + (SMOOTH_FACTOR * startDiffX);
            firstControlY = thisPointY + (SMOOTH_FACTOR * startDiffY);

            secondControlX = nextPointX - (SMOOTH_FACTOR * endDiffX);
            secondControlY = nextPointY - (SMOOTH_FACTOR * endDiffY);

            res.cubicTo(
                    firstControlX, firstControlY,
                    secondControlX, secondControlY,
                    nextPointX, nextPointY);
        }

        return res;
    }


    /**
     * Responsible for drawing line background
     *
     * @param path {@link Path} object containing line path
     * @param set  {@link LineSet} object.
     * @return {@link Path} object containing background
     */
    private Path createBackgroundPath(Path path, LineSet set) {
        mStyle.mFillPaint.setAlpha((int) (set.getAlpha() * ChartView.Style.FULL_ALPHA));

        if (set.hasFill())
            mStyle.mFillPaint.setColor(set.getFillColor());

        if (set.hasGradientFill())
            mStyle.mFillPaint.setShader(
                    new LinearGradient(super.getInnerChartLeft(), super.getInnerChartTop(),
                            super.getInnerChartLeft(), super.getInnerChartBottom(),
                            set.getGradientColors(), set.getGradientPositions(), Shader.TileMode.MIRROR));

        path.lineTo(set.getEntry(set.getEnd() - 1).getX(), super.getInnerChartBottom());
        path.lineTo(set.getEntry(set.getBegin()).getX(), super.getInnerChartBottom());
        path.close();

        return path;
    }

    /**
     * @param radius Point's radius where touch event will be detected
     * @return {@link LineChartView} self-reference.
     */
    public LineChartView setClickablePointRadius(@FloatRange(from = 0.f) float radius) {
        mClickableRadius = radius;
        return this;
    }

    /**
     * Class responsible to mStyle the LineChart!
     * Can be instantiated with or without attributes.
     */
    static class Style {

        static final int FULL_ALPHA = 255;

        /**
         * Paint variables
         */
        private Paint mDotsPaint;

        private Paint mDotsStrokePaint;

        private Paint mLinePaint;

        private Paint mFillPaint;

        private Paint mValuePaint;

        Style() {
        }

        Style(TypedArray attrs) {
        }

        private void init() {
            mDotsPaint = new Paint();
            mDotsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mDotsPaint.setAntiAlias(true);

            mDotsStrokePaint = new Paint();
            mDotsStrokePaint.setStyle(Paint.Style.STROKE);
            mDotsStrokePaint.setAntiAlias(true);

            mLinePaint = new Paint();
            mLinePaint.setStyle(Paint.Style.STROKE);
            mLinePaint.setAntiAlias(true);

            mFillPaint = new Paint();
            mFillPaint.setStyle(Paint.Style.FILL);

            mValuePaint = new Paint();
            mValuePaint.setAntiAlias(true);
        }

        private void clean() {
            mLinePaint = null;
            mFillPaint = null;
            mDotsPaint = null;
            mValuePaint = null;
        }

    }

}
