package com.pengl.williamchart.model;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.pengl.williamchart.view.BaseBarChartView;
import com.pengl.williamchart.util.Preconditions;

/**
 * Data model that represents a bar in {@link BaseBarChartView}
 */
public class Bar extends ChartEntry {

    private boolean mHasGradientColor;
    private int[] mGradientColors;
    private float[] mGradientPositions;

    public Bar(String label, float value) {
        super(label, value);
        isVisible = true;
        mHasGradientColor = false;
    }

    /**
     * If bar has gradient fill color defined.
     *
     * @return true if gradient fill property defined.
     */
    public boolean hasGradientColor() {
        return mHasGradientColor;
    }

    /**
     * Retrieve set of colors defining the gradient of bar's fill.
     * Gradient fill property must have been previously defined.
     *
     * @return Gradient colors array.
     */
    public int[] getGradientColors() {
        return mGradientColors;
    }

    /**
     * Retrieve set of positions to define the gradient of bar's fill.
     * Gradient fill property must have been previously defined.
     *
     * @return Gradient positions.
     */
    public float[] getGradientPositions() {
        return mGradientPositions;
    }

    /**
     * Set gradient colors to the fill of the {@link Bar}.
     *
     * @param colors    The colors to be distributed among gradient
     * @param positions Position/order from which the colors will be place
     * @return {@link Bar} self-reference.
     */
    public Bar setGradientColor(@NonNull @Size(min = 1) int[] colors, float[] positions) {
        if (colors.length == 0)
            throw new IllegalArgumentException("Colors list cannot be empty");

        mHasGradientColor = true;
        mGradientColors = Preconditions.checkNotNull(colors);
        mGradientPositions = positions;
        return this;
    }

}
