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

package com.pengl.chart.listener;

import android.view.View;


/**
 * Interface to define a listener when an chart entry has been clicked
 */
public interface OnTooltipEventListener {

    /**
     * Called once tooltip is entering display.
     *
     * @param view View representing the tooltip which will be added to the display.
     */
    void onEnter(View view);

    /**
     * Called once tooltip is exiting display.
     *
     * @param view View representing tooltip which will be removed from the display.
     */
    void onExit(View view);

}
