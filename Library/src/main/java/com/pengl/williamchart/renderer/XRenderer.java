package com.pengl.williamchart.renderer;

import android.graphics.Canvas;
import android.graphics.Paint.Align;

/**
 * X轴
 */
public class XRenderer extends AxisRenderer {

    public XRenderer() {
        super();
    }

    /**
     * 重要提示：方法的顺序至关重要。请不要更改
     */
    @Override
    public void dispose() {
        super.dispose();

        defineMandatoryBorderSpacing(mInnerChartLeft, mInnerChartRight);
        defineLabelsPosition(mInnerChartLeft, mInnerChartRight);
    }

    @Override
    protected float defineAxisPosition() {
        float result = mInnerChartBottom;
        if (style.hasXAxis()) result += style.getAxisThickness() / 2;
        return result;
    }

    @Override
    protected float defineStaticLabelsPosition(float axisCoordinate, int distanceToAxis) {
        float result = axisCoordinate;

        if (style.getXLabelsPositioning() == LabelPosition.INSIDE) { // Labels sit inside of chart
            result -= distanceToAxis;
            result -= style.getLabelsPaint().descent();
            if (style.hasXAxis()) result -= style.getAxisThickness() / 2;

        } else if (style.getXLabelsPositioning() == LabelPosition.OUTSIDE) { // Labels sit outside of chart
            result += distanceToAxis;
            result += style.getFontMaxHeight() - style.getLabelsPaint().descent();
            if (style.hasXAxis()) result += style.getAxisThickness() / 2;
        }
        return result;
    }

    @Override
    public void draw(Canvas canvas) {

        // 绘制坐标轴
        if (style.hasXAxis())
            canvas.drawLine(mInnerChartLeft, axisPosition, mInnerChartRight, axisPosition,
                    style.getChartPaint());

        // 绘制坐轴的坐标
        if (style.getXLabelsPositioning() != LabelPosition.NONE) {
            style.getLabelsPaint().setTextAlign(Align.CENTER);

            int nLabels = labels.size();
            for (int i = 0; i < nLabels; i++) {
                canvas.drawText(labels.get(i), labelsPos.get(i), labelsStaticPos,
                        style.getLabelsPaint());

            }
        }
    }

    @Override
    public float parsePos(int index, double value) {
        if (handleValues)
            return (float) (mInnerChartLeft
                    + (((value - minLabelValue) * screenStep) / (labelsValues.get(1) - minLabelValue)));
        else return labelsPos.get(index);
    }

    @Override
    protected float measureInnerChartLeft(int left) {
        return (style.getXLabelsPositioning() != LabelPosition.NONE)
                ? style.getLabelsPaint().measureText(labels.get(0)) / 2
                : left;
    }

    @Override
    protected float measureInnerChartTop(int top) {
        return top;
    }

    @Override
    protected float measureInnerChartRight(int right) {

        // 管理最后一个轴标签的水平宽度
        float lastLabelWidth = 0;
        // 修复尝试通过索引-1访问标签时可能发生的崩溃。
        if (!labels.isEmpty())
            lastLabelWidth = style.getLabelsPaint().measureText(labels.get(labels.size() - 1));

        float rightBorder = 0;
        if (style.getXLabelsPositioning() != LabelPosition.NONE
                && style.getAxisBorderSpacing() + mandatoryBorderSpacing < lastLabelWidth / 2)
            rightBorder = lastLabelWidth / 2 - (style.getAxisBorderSpacing() + mandatoryBorderSpacing);

        return right - rightBorder;
    }

    @Override
    protected float measureInnerChartBottom(int bottom) {
        float result = bottom;
        if (style.hasXAxis())
            result -= style.getAxisThickness();

        if (style.getXLabelsPositioning() == LabelPosition.OUTSIDE)
            result -= style.getFontMaxHeight() + style.getAxisLabelsSpacing();

        return result;
    }

}
