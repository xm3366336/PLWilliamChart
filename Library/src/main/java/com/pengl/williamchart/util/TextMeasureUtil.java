package com.pengl.williamchart.util;

import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 文字測量类
 */
public class TextMeasureUtil {

    /**
     * 获取文本的宽度
     */
    public static int getTextWidth(Paint mPaint, String text) {
        Rect bounds = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

    /**
     * 获取文本的高度
     */
    public static int getTextHeight(Paint mPaint, String text) {
        Rect bounds = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }
}
