package com.pengl.williamchart.demo;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pengl.williamchart.animation.Animation;
import com.pengl.williamchart.demo.databinding.FragmentLinechartBinding;
import com.pengl.williamchart.model.LineSet;
import com.pengl.williamchart.renderer.AxisRenderer;
import com.pengl.williamchart.util.Tools;

public class LineChartFragment extends Fragment {

    private FragmentLinechartBinding binding;

    private static final int[] scoreData = new int[]{94, 98, 96, 100, 95, 92, 94, 90, 84, 89, 82, 78, 75, 68, 73, 62, 57, 50, 48};
    // private static final int[] scoreData = new int[]{100, 75, 65, 55, 45, 35, 25, 15, 5, 0, 100, 90, 80, 70, 60, 50, 40, 30, 20, 10};
    private String[] mLabels;
    private float[] mValues;
    private Paint gridPaint, paint90;

    /**
     * 如果不足20条，是否添加至20条显示
     */
    private static final boolean isAddTo20 = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLinechartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 虚线画笔
        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#c1c1c1"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));//虚线设置
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.2f));

        // 阈值线画笔
        paint90 = new Paint();
        paint90.setColor(Color.parseColor("#26c2fd"));
        paint90.setStyle(Paint.Style.STROKE);
        paint90.setAntiAlias(true);
        paint90.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));//虚线设置
        paint90.setStrokeWidth(Tools.fromDpToPx(.8f));

        // 数据
        int size = scoreData.length;
        mLabels = new String[size];
        mValues = new float[size];
        for (int i = 0; i < size; i++) {
            mLabels[size - i - 1] = String.valueOf(size - i);
            mValues[size - i - 1] = scoreData[i];
        }

        showLineCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * 显示
     */
    public void showLineCard() {
        binding.mLineChartView.reset();
        if (null == mLabels || mLabels.length == 0) return;
        if (null == mValues || mValues.length == 0) return;

        LineSet data;
        if (isAddTo20) {
            String[] newLabels = new String[20];
            float[] newValues = new float[20];
            // 显示最近20条记录，不足20条的，在此补0
            if (mLabels.length < 20) {
                int i = 0;
                for (String x : mLabels) {
                    newLabels[i] = x;
                    i++;
                }
                i = 0;
                for (float y : mValues) {
                    newValues[i] = y;
                    i++;
                }
                for (i = mLabels.length; i < 20; i++) {
                    newLabels[i] = String.valueOf(i + 1);
                    newValues[i] = 0;
                }
            }
            data = new LineSet(newLabels, newValues);
        } else {
            data = new LineSet(mLabels, mValues);
        }

        // 数据，不可为空
        data.setColor(Color.parseColor("#66f47635"))// 线条的颜色
                .setGradientFill(new int[]{Color.parseColor("#66f47635"), Color.TRANSPARENT})// 填充渐变色
                .setDotsColor(Color.parseColor("#f47635"))// 描点的颜色
                .setDotsRadius(5)// 圆点的大小
                .setSmooth(false)// 平滑的效果
                .setThickness(3)// 绘制线时的粗细
                .setEnableDrawValue(true)// 显示数值
                .setValueTextSize(Tools.fromDpToPx(10))// 数值的字体大小
                .setValueColor(Color.parseColor("#f47635"))// 数值的颜色
        ;
        binding.mLineChartView.addData(data);

        // Chart
        binding.mLineChartView
                .setXAxis(true)// 是否显示X轴
                .setYAxis(true)// 是否显示Y轴
                .setXLabels(AxisRenderer.LabelPosition.OUTSIDE)// X轴标签显示在轴的外面
                .setYLabels(AxisRenderer.LabelPosition.OUTSIDE)// Y轴标签显示在轴的外面
                .setGrid(10, data.size() - 1, gridPaint)// 填充网络，使用虚线
                .setLabelsColor(Color.parseColor("#aaaaaa"))// 设置标签字体的颜色
                .setFontSize(Tools.fromDpToPx(9))// 轴字体大小
                .setAxisBorderValues(0, 100, 20)// Y轴值
                .setValueThreshold(90, 90, paint90)// 阈值区域
                .setAxisColor(Color.BLUE)// 轴的颜色
                .setAxisThickness(3)// 轴的粗细
        ;

        // 最大值的位置
        binding.mLineChartView.show(new Animation().setInterpolator(new BounceInterpolator()).fromAlpha(0));
    }

}