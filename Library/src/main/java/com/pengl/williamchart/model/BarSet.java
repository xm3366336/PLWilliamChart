package com.pengl.williamchart.model;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.pengl.williamchart.view.BaseBarChartView;
import com.pengl.williamchart.util.Preconditions;

/**
 * Data model containing a set of {@link Bar} to be used by {@link BaseBarChartView}.
 */
public class BarSet extends ChartSet {

    public BarSet() {
        super();
    }

    public BarSet(@NonNull String[] labels, @NonNull float[] values) {
        super();

        if (labels.length != values.length)
            throw new IllegalArgumentException("Arrays size doesn't match.");

        Preconditions.checkNotNull(labels);
        Preconditions.checkNotNull(values);

        int nEntries = labels.length;
        for (int i = 0; i < nEntries; i++)
            addBar(labels[i], values[i]);
    }

    /**
     * Add new {@link Bar} from a string and a float.
     *
     * @param label new {@link Bar}'s label
     * @param value new {@link Bar}'s value
     */
    public void addBar(String label, float value) {
        this.addBar(new Bar(label, value));
    }

    /**
     * Add new {@link Bar}.
     *
     * @param bar new nonnull {@link Bar}
     */
    public void addBar(@NonNull Bar bar) {
        this.addEntry(Preconditions.checkNotNull(bar));
    }

    /**
     * Retrieve line's color.
     *
     * @return {@link BarSet} color.
     */
    public int getColor() {
        return this.getEntry(0).getColor();
    }

    /**
     * Define the color of bars. Previously defined colors will be overridden.
     *
     * @param color Color to be assigned to every bar in this set.
     * @return {@link BarSet} self-reference.
     */
    public BarSet setColor(@ColorInt int color) {
        for (ChartEntry e : getEntries())
            e.setColor(color);
        return this;
    }

    /**
     * Define a gradient color to the bars. Previously defined colors will be overridden.
     *
     * @param colors    The colors to be distributed among gradient
     * @param positions Position/order from which the colors will be place
     * @return {@link BarSet} self-reference.
     */
    public BarSet setGradientColor(@NonNull @Size(min = 1) int[] colors, float[] positions) {
        if (colors.length == 0)
            throw new IllegalArgumentException("Colors argument can't be null or empty.");
        Preconditions.checkNotNull(colors);

        for (ChartEntry e : getEntries())
            ((Bar) e).setGradientColor(colors, positions);
        return this;
    }

}
