/*
 * Copyright 2015 Diogo Bernardino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pengl.chart.model;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.pengl.chart.view.BaseBarChartView;

import static com.pengl.chart.util.Preconditions.checkNotNull;


/**
 * Data model that represents a bar in {@link BaseBarChartView}
 */
public class Bar extends ChartEntry {

    private static final String TAG = "chart.model.Bar";

    private boolean mHasGradientColor;

    private int[] mGradientColors;

    private float[] mGradientPositions;


    public Bar(String label, float value) {

        super(label, value);

        isVisible = true;
        mHasGradientColor = false;
    }


    /*
     * -------------
     * Getters
     * -------------
     */

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


    /*
     * -------------
     * Setters
     * -------------
     */

    /**
     * Set gradient colors to the fill of the {@link Bar}.
     *
     * @param colors    The colors to be distributed among gradient
     * @param positions Position/order from which the colors will be place
     * @return {@link Bar} self-reference.
     */
    public Bar setGradientColor(@NonNull @Size(min = 1) int colors[], float[] positions) {

        if (colors.length == 0)
            throw new IllegalArgumentException("Colors list cannot be empty");

        mHasGradientColor = true;
        mGradientColors = checkNotNull(colors);
        mGradientPositions = positions;
        return this;
    }

}
