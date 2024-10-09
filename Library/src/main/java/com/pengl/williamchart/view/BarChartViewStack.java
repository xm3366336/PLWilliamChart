package com.pengl.williamchart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.pengl.williamchart.model.Bar;
import com.pengl.williamchart.model.BarSet;
import com.pengl.williamchart.model.ChartSet;

import java.util.ArrayList;

/**
 * Implements a StackBar chart extending {@link BarChartView}
 */
public class BarChartViewStack extends BaseStackBarChartView {

    public BarChartViewStack(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(Orientation.VERTICAL);
        setMandatoryBorderSpacing();
    }

    public BarChartViewStack(Context context) {
        super(context);
        setOrientation(Orientation.VERTICAL);
        setMandatoryBorderSpacing();
    }

    @Override
    public void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {
        float verticalOffset;
        float currBottomY;

        float negVerticalOffset;
        float negCurrBottomY;

        float x0;
        float x1;
        float y1;
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
            if (mStyle.hasBarBackground) {
                drawBarBackground(canvas, (int) (data.get(0).getEntry(i).getX() - barWidth / 2),
                        (int) this.getInnerChartTop(),
                        (int) (data.get(0).getEntry(i).getX() + barWidth / 2),
                        (int) this.getInnerChartBottom());
            }

            // Vertical offset to keep drawing bars on top of the others
            verticalOffset = 0;
            negVerticalOffset = 0;

            // Bottom of the next bar to be drawn
            currBottomY = zeroPosition;
            negCurrBottomY = zeroPosition;

            // Unfortunately necessary to discover which set is the bottom and top in case there
            // are entries with value 0. To better understand check one of the methods.
            bottomSetIndex = discoverBottomSet(i, data);
            topSetIndex = discoverTopSet(i, data);

            for (int j = 0; j < dataSize; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);

                barSize = Math.abs(zeroPosition - bar.getY());

                // If:
                // Bar not visible OR
                // Bar value equal to 0 OR
                // Size of bar < 2 (Due to the loss of precision)
                // Then no need to draw
                if (!barSet.isVisible() || bar.getValue() == 0 || barSize < 2) continue;

                applyShadow(mStyle.barPaint, barSet.getAlpha(), bar.getShadowDx(), bar
                        .getShadowDy(), bar.getShadowRadius(), bar.getShadowColor());

                x0 = (bar.getX() - barWidth / 2);
                x1 = (bar.getX() + barWidth / 2);

                if (bar.getValue() > 0) {

                    y1 = zeroPosition - (barSize + verticalOffset);

                    if (!bar.hasGradientColor()) mStyle.barPaint.setColor(bar.getColor());
                    else mStyle.barPaint.setShader(new LinearGradient(x0, y1, x1, currBottomY,
                            bar.getGradientColors(), bar.getGradientPositions(), Shader.TileMode.MIRROR));

                    // Draw bar
                    if (j == bottomSetIndex) {//底部
                        drawBar(canvas, (int) x0, (int) y1, (int) x1, (int) currBottomY);
                        if (bottomSetIndex != topSetIndex && mStyle.cornerRadius != 0) {
                            // Patch top corners of bar
                            cornersPatch = (currBottomY - y1) / 2;
                            canvas.drawRect(new Rect((int) x0, (int) y1, (int) x1, (int) (y1 + cornersPatch)), mStyle.barPaint);
                        }
                    } else if (j == topSetIndex) {//顶部
                        drawBar(canvas, (int) x0, (int) y1, (int) x1, (int) currBottomY);
                        // Patch bottom corners of bar
                        cornersPatch = (currBottomY - y1) / 2;
                        canvas.drawRect(new Rect((int) x0, (int) (currBottomY - cornersPatch), (int) x1, (int) currBottomY), mStyle.barPaint);

                    } else { // if(j != bottomSetIndex && j != topSetIndex) { // Middle sets//中间
                        canvas.drawRect(new Rect((int) x0, (int) y1, (int) x1, (int) currBottomY), mStyle.barPaint);
                    }

                    currBottomY = y1;

                    // Increase the vertical offset to be used by the next bar
                    verticalOffset += barSize + 2;

                } else { // if(bar.getValue() < 0)

                    y1 = zeroPosition + (barSize - negVerticalOffset);

                    if (!bar.hasGradientColor()) mStyle.barPaint.setColor(bar.getColor());
                    else mStyle.barPaint.setShader(new LinearGradient(x0, y1, x1, currBottomY,
                            bar.getGradientColors(), bar.getGradientPositions(), Shader.TileMode.MIRROR));

                    if (j == bottomSetIndex) {
                        drawBar(canvas, (int) x0, (int) negCurrBottomY, (int) x1, (int) y1);
                        if (bottomSetIndex != topSetIndex && mStyle.cornerRadius != 0) {
                            // Patch top corners of bar
                            cornersPatch = (y1 - negCurrBottomY) / 2;
                            canvas.drawRect(new Rect((int) x0, (int) negCurrBottomY, (int) x1,
                                    (int) (negCurrBottomY + cornersPatch)), mStyle.barPaint);
                        }

                    } else if (j == topSetIndex) {
                        drawBar(canvas, (int) x0, (int) negCurrBottomY, (int) x1, (int) y1);
                        // Patch bottom corners of bar
                        cornersPatch = (y1 - negCurrBottomY) / 2;
                        canvas.drawRect(new Rect((int) x0, (int) (y1 - cornersPatch), (int) x1, (int) y1),
                                mStyle.barPaint);

                    } else { // if(j != bottomSetIndex && j != topSetIndex) { // Middle sets
                        canvas.drawRect(new Rect((int) x0, (int) negCurrBottomY, (int) x1, (int) y1),
                                mStyle.barPaint);
                    }

                    negCurrBottomY = y1;

                    // Increase the vertical offset to be used by the next bar
                    negVerticalOffset -= barSize;
                }
            }
        }
    }

    @Override
    public void onPreDrawChart(ArrayList<ChartSet> data) {
        // 在这里进行计算，以避免在动画绘制时进行多次计算
        if (data.get(0).size() == 1)
            barWidth = (this.getInnerChartRight() - this.getInnerChartLeft() - this.getBorderSpacing() * 2);
        else calculateBarsWidth(-1, data.get(0).getEntry(0).getX(), data.get(0).getEntry(1).getX());
    }

    @Override
    void defineRegions(ArrayList<ArrayList<Region>> regions, ArrayList<ChartSet> data) {
        int dataSize = data.size();
        int setSize = data.get(0).size();

        float verticalOffset;
        float currBottomY;

        float negVerticalOffset;
        float negCurrBottomY;

        float y1;
        float barSize;
        BarSet barSet;
        Bar bar;
        float zeroPosition = this.getZeroPosition();

        for (int i = 0; i < setSize; i++) {

            // Vertical offset to keep drawing bars on top of the others
            verticalOffset = 0;
            negVerticalOffset = 0;
            // Bottom of the next bar to be drawn
            currBottomY = zeroPosition;
            negCurrBottomY = zeroPosition;

            for (int j = 0; j < dataSize; j++) {

                barSet = (BarSet) data.get(j);
                bar = (Bar) barSet.getEntry(i);
                barSize = Math.abs(zeroPosition - bar.getY());

                // If:
                // Bar not visible OR
                // Bar value equal to 0 OR
                // Size of bar < 2 (Due to the loss of precision)
                // Then no need to have region
                if (!barSet.isVisible()) continue;

                if (bar.getValue() > 0) {
                    y1 = zeroPosition - (barSize + verticalOffset);
                    regions.get(j)
                            .get(i)
                            .set((int) (bar.getX() - barWidth / 2), (int) y1,
                                    (int) (bar.getX() + barWidth / 2), (int) currBottomY);
                    currBottomY = y1;
                    verticalOffset += barSize + 2;

                } else if (bar.getValue() < 0) {
                    y1 = zeroPosition + (barSize - negVerticalOffset);
                    regions.get(j)
                            .get(i)
                            .set((int) (bar.getX() - barWidth / 2), (int) negCurrBottomY,
                                    (int) (bar.getX() + barWidth / 2), (int) y1);
                    negCurrBottomY = y1;
                    negVerticalOffset -= barSize;

                } else {  // If bar.getValue() == 0, force region to 1 pixel
                    y1 = zeroPosition - (1 + verticalOffset);
                    regions.get(j)
                            .get(i)
                            .set((int) (bar.getX() - barWidth / 2), (int) y1,
                                    (int) (bar.getX() + barWidth / 2), (int) currBottomY);
                }
            }
        }
    }

}