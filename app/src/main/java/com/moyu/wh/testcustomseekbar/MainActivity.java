package com.moyu.wh.testcustomseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.moyu.wh.testcustomseekbar.customview.OnRangeRulerChangeListener;
import com.moyu.wh.testcustomseekbar.customview.RangeSeekBar;

public class MainActivity extends AppCompatActivity {
    private RangeSeekBar rangeSeekBar;
    private TextView tv1;
    private RangeSeekBar dayView;
    private TextView tvDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rangeSeekBar = (RangeSeekBar)findViewById(R.id.seekbar_money);
        tv1 = (TextView)findViewById(R.id.tv_money);
        dayView = (RangeSeekBar) findViewById(R.id.seekbar_day);
        tvDay = (TextView) findViewById(R.id.tv_day);

        rangeSeekBar.setOnRangeRulerChangeListener(new OnRangeRulerChangeListener() {
            @Override
            public void onValueChanged(int value) {
                tv1.setText(value+"");
            }
        });
        //设置当前值一定要在设置了监听之后
        rangeSeekBar.setCurrentProgress(500);
        dayView.setOnRangeRulerChangeListener(new OnRangeRulerChangeListener() {
            @Override
            public void onValueChanged(int value) {
                tvDay.setText(value+"");
            }
        });
    }

}
