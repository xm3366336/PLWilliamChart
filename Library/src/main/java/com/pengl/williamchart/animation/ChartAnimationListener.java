package com.pengl.williamchart.animation;

import com.pengl.williamchart.model.ChartSet;
import com.pengl.williamchart.view.ChartView;

import java.util.ArrayList;

/**
 * Interface used by {@link Animation} to interact with {@link ChartView}
 */
public interface ChartAnimationListener {

    /**
     * Callback to let {@link ChartView} know when to invalidate and present new data.
     *
     * @param data Chart data to be used in the next view invalidation.
     * @return True if {@link ChartView} accepts the call, False otherwise.
     */
    boolean onAnimationUpdate(ArrayList<ChartSet> data);
}
