package com.pengl.williamchart.renderer;

import android.graphics.Canvas;
import android.graphics.Paint.Align;

import java.util.Collections;

/**
 * Y轴
 */
public class YRenderer extends AxisRenderer {

    public YRenderer() {
        super();
    }

    /**
     * 重要提示：方法的顺序至关重要。请不要更改
     */
    @Override
    public void dispose() {
        super.dispose();
        defineMandatoryBorderSpacing(mInnerChartTop, mInnerChartBottom);
        defineLabelsPosition(mInnerChartTop, mInnerChartBottom);
    }

    @Override
    protected float defineAxisPosition() {
        float result = mInnerChartLeft;
        if (style.hasYAxis()) result -= style.getAxisThickness() / 2;
        return result;
    }

    @Override
    protected float defineStaticLabelsPosition(float axisCoordinate, int distanceToAxis) {
        float result = axisCoordinate;

        if (style.getYLabelsPositioning() == LabelPosition.INSIDE) {
            result += distanceToAxis;
            if (style.hasYAxis()) result += style.getAxisThickness() / 2;

        } else if (style.getYLabelsPositioning() == LabelPosition.OUTSIDE) {
            result -= distanceToAxis;
            if (style.hasYAxis()) result -= style.getAxisThickness() / 2;
        }
        return result;
    }

    @Override
    public void draw(Canvas canvas) {

        // 绘制坐标轴
        if (style.hasYAxis()) {
            float bottom = mInnerChartBottom;
            if (style.hasXAxis())
                bottom += style.getAxisThickness();
            canvas.drawLine(axisPosition, mInnerChartTop, axisPosition, bottom, style.getChartPaint());
        }

        // 绘制坐轴的坐标
        if (style.getYLabelsPositioning() != LabelPosition.NONE) {
            style.getLabelsPaint().setTextAlign(
                    style.getYLabelsPositioning() == LabelPosition.OUTSIDE
                            ? Align.RIGHT
                            : Align.LEFT);

            int nLabels = labels.size();
            for (int i = 0; i < nLabels; i++) {
                canvas.drawText(labels.get(i), labelsStaticPos,
                        labelsPos.get(i) + style.getLabelHeight(labels.get(i)) / 2f,
                        style.getLabelsPaint());
            }
        }
    }

    @Override
    void defineLabelsPosition(float innerStart, float innerEnd) {
        super.defineLabelsPosition(innerStart, innerEnd);
        Collections.reverse(labelsPos);
    }

    @Override
    public float parsePos(int index, double value) {
        if (handleValues)
            return (float) (mInnerChartBottom
                    - (((value - minLabelValue) * screenStep) / (labelsValues.get(1) - minLabelValue)));
        else
            return labelsPos.get(index);
    }

    @Override
    protected float measureInnerChartLeft(int left) {
        float result = left;
        if (style.hasYAxis())
            result += style.getAxisThickness();

        if (style.getYLabelsPositioning() == LabelPosition.OUTSIDE) {
            float aux;
            float maxLabelLength = 0;
            for (String label : labels) {
                aux = style.getLabelsPaint().measureText(label);
                if (aux > maxLabelLength)
                    maxLabelLength = aux;
            }
            result += maxLabelLength + style.getAxisLabelsSpacing();
        }
        return result;
    }

    @Override
    protected float measureInnerChartTop(int top) {
        return top;
    }

    @Override
    protected float measureInnerChartRight(int right) {
        return right;
    }

    @Override
    protected float measureInnerChartBottom(int bottom) {
        if (style.getYLabelsPositioning() != LabelPosition.NONE
                && style.getAxisBorderSpacing() < style.getFontMaxHeight() / 2)
            return bottom - style.getFontMaxHeight() / 2f;
        return bottom;
    }

}