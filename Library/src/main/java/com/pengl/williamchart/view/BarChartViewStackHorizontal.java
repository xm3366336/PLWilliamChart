package com.pengl.williamchart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;

import com.pengl.williamchart.model.Bar;
import com.pengl.williamchart.model.BarSet;
import com.pengl.williamchart.model.ChartSet;

import java.util.ArrayList;

/**
 * Implements an HorizontalStackBarChart chart extending {@link BaseStackBarChartView}
 */
public class BarChartViewStackHorizontal extends BaseStackBarChartView {

    public BarChartViewStackHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(Orientation.HORIZONTAL);
        setMandatoryBorderSpacing();
    }

    public BarChartViewStackHorizontal(Context context) {
        super(context);
        setOrientation(Orientation.HORIZONTAL);
        setMandatoryBorderSpacing();
    }

    @Override
    public void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {
        float offset;
        float currBottom;

        float negOffset;
        float negCurrBottom;

        float y0;
        float y1;
        float x1;
        float barSize;
        int bottomSetIndex;
        int topSetIndex;
        float cornersPatch;
        BarSet barSet;
        Bar bar;
        int dataSize = data.size();
        int setSize = data.get(0).size();
        float zeroPosition = this.getZeroPosition();

        for (int i = 0; i < setSize; i++) {

            // If bar needs background
            if (mStyle.hasBarBackground) drawBarBackground(canvas, (int) this.getInnerChartLeft(),
                    (int) (data.get(0).getEntry(i).getY() - barWidth / 2),
                    (int) this.getInnerChartRight(),
                    (int) (data.get(0).getEntry(i).getY() + barWidth / 2));

            // Vertical offset to keep drawing bars on top of the others
            offset = 0;
            negOffset = 0;

            // Bottom of the next bar to be drawn
            currBottom = zeroPosition;
            negCurrBottom = zeroPosition;

            // Unfortunately necessary to discover which set is the bottom and top in case there
            // are entries with value 0. To better understand check one of the methods.
            bottomSetIndex = discoverBottomSet(i, data);
            topSetIndex = discoverTopSet(i, data);

            for (int j = 0; j < dataSize; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                barSize = Math.abs(zeroPosition - bar.getX());

                // If:
                // Bar not visible OR
                // Bar value equal to 0 OR
                // Size of bar < 2 (Due to the loss of precision)
                // Then no need to draw
                if (!barSet.isVisible() || bar.getValue() == 0 || barSize < 2)
                    continue;

                mStyle.barPaint.setColor(bar.getColor());
                applyShadow(mStyle.barPaint, barSet.getAlpha(), bar.getShadowDx(), bar
                        .getShadowDy(), bar.getShadowRadius(), bar.getShadowColor());

                y0 = (bar.getY() - barWidth / 2);
                y1 = (bar.getY() + barWidth / 2);

                if (bar.getValue() > 0) {

                    x1 = zeroPosition + (barSize - offset);

                    // Draw bar
                    if (j == bottomSetIndex) {
                        drawBar(canvas, (int) currBottom, (int) y0, (int) x1, (int) y1);
                        if (bottomSetIndex != topSetIndex && mStyle.cornerRadius != 0) {
                            // Patch top corners of bar
                            cornersPatch = (x1 - currBottom) / 2;
                            canvas.drawRect(
                                    new Rect((int) (x1 - cornersPatch), (int) y0, (int) x1, (int) y1),
                                    mStyle.barPaint);
                        }

                    } else if (j == topSetIndex) {
                        drawBar(canvas, (int) currBottom, (int) y0, (int) x1, (int) y1);
                        // Patch bottom corners of bar
                        cornersPatch = (x1 - currBottom) / 2;
                        canvas.drawRect(
                                new Rect((int) currBottom, (int) y0, (int) (currBottom + cornersPatch),
                                        (int) y1), mStyle.barPaint);

                    } else { // if(j != bottomSetIndex && j != topSetIndex) { // Middle sets
                        canvas.drawRect(new Rect((int) currBottom, (int) y0, (int) x1, (int) y1),
                                mStyle.barPaint);
                    }

                    currBottom = x1;

                    // 增加下一栏使用的垂直偏移
                    offset -= barSize - 0;

                } else { // if(bar.getValue() < 0)

                    x1 = zeroPosition - (barSize + negOffset);

                    if (j == bottomSetIndex) {
                        drawBar(canvas, (int) x1, (int) y0, (int) negCurrBottom, (int) y1);
                        if (bottomSetIndex != topSetIndex && mStyle.cornerRadius != 0) {
                            // Patch top corners of bar
                            cornersPatch = (negCurrBottom - x1) / 2;
                            canvas.drawRect(new Rect((int) (negCurrBottom - cornersPatch), (int) y0,
                                    (int) negCurrBottom, (int) y1), mStyle.barPaint);
                        }

                    } else if (j == topSetIndex) {
                        drawBar(canvas, (int) x1, (int) y0, (int) negCurrBottom, (int) y1);
                        // Patch bottom corners of bar
                        cornersPatch = (negCurrBottom - x1) / 2;
                        canvas.drawRect(new Rect((int) x1, (int) y0, (int) (x1 + cornersPatch), (int) y1),
                                mStyle.barPaint);

                    } else { // if(j != bottomSetIndex && j != topSetIndex) { // Middle sets
                        canvas.drawRect(new Rect((int) x1, (int) y0, (int) negCurrBottom, (int) y1),
                                mStyle.barPaint);
                    }

                    negCurrBottom = x1;

                    // Increase the vertical offset to be used by the next bar
                    negOffset += barSize;
                }
            }
        }
    }

    @Override
    public void onPreDrawChart(ArrayList<ChartSet> data) {
        // Doing calculations here to avoid doing several times while drawing
        // in case of animation
        if (data.get(0).size() == 1)
            barWidth = (this.getInnerChartBottom() - this.getInnerChartTop() - this.getBorderSpacing() * 2);
        else calculateBarsWidth(-1, data.get(0).getEntry(1).getY(), data.get(0).getEntry(0).getY());
    }

    @Override
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {

        int dataSize = data.size();
        int setSize = data.get(0).size();

        float offset;
        float currBottom;

        float negOffset;
        float negCurrBottom;

        float x1;
        float barSize;
        BarSet barSet;
        Bar bar;
        float zeroPosition = this.getZeroPosition();

        for (int i = 0; i < setSize; i++) {

            // Vertical offset to keep drawing bars on top of the others
            offset = 0;
            negOffset = 0;
            // Bottom of the next bar to be drawn
            currBottom = zeroPosition;
            negCurrBottom = zeroPosition;

            for (int j = 0; j < dataSize; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                barSize = Math.abs(zeroPosition - bar.getX());

                // If:
                // Bar not visible OR
                // Then no need to have region
                if (!barSet.isVisible()) continue;

                if (bar.getValue() > 0) {
                    x1 = zeroPosition + (barSize - offset);
                    regions.get(j).get(i).set((int) currBottom, (int) (bar.getY() - barWidth / 2),
                            (int) x1, (int) (bar.getY() + barWidth / 2));
                    currBottom = x1;
                    offset -= barSize - 2;

                } else if (bar.getValue() < 0) {
                    x1 = zeroPosition - (barSize + negOffset);
                    regions.get(j).get(i).set((int) x1, (int) (bar.getY() - barWidth / 2),
                            (int) negCurrBottom, (int) (bar.getY() + barWidth / 2));
                    negCurrBottom = x1;
                    negOffset += barSize;

                } else {  // If bar.getValue() == 0, force region to 1 pixel
                    x1 = zeroPosition + (1 - offset);
                    regions.get(j).get(i).set((int) currBottom, (int) (bar.getY() - barWidth / 2),
                            (int) x1, (int) (bar.getY() + barWidth / 2));
                }
            }
        }
    }

}