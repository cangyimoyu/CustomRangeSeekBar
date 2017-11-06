package com.moyu.wh.testcustomseekbar.customview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


import com.moyu.wh.testcustomseekbar.R;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Created by admin on 17/10/26.
 */

public class RangeSeekBar extends View {
    private static final float PADDING_LEFT = 44f;
    private static final float PADDING_RIGHT = 44f;
    private float cursorRadius;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mRulerPaint;
    private float progrees = 300f;
    private int maxProgress = 3000;
    private int minProgress = 300;
    //控件和1920*1080屏幕的宽度比
    private float scaleX;
    //控件和1920*1080屏幕的高度比
    private float scaleY;
    private boolean isCanMove;
    //游标的颜色
    private int cursorColor = Color.RED;
    //线的颜色
    private int lineColor = Color.GRAY;
    //字的颜色
    private int textColor = Color.GRAY;
    //一个单元格的单位
    private int oneItemValue = 100;
    //一个刻度的单位
    private int oneGroupValue = 500;
    //一个单元格的宽度
    private float widthOfItem;
    //总共多少格
    private int numberOfItem;
    //标尺宽度
    private float widthOfProgress;
    //线的宽度
    private float widthOfLine = 4f;
    //标尺的位置
    private float xProgress ;
    //能不能滑动到中间
    private boolean scrollable = true;
    //当前的位置
    private int currentProgress;
    private OnRangeRulerChangeListener onRangeRulerChangeListener;
    public void setOnRangeRulerChangeListener(OnRangeRulerChangeListener onRangeRulerChangeListener){
        this.onRangeRulerChangeListener = onRangeRulerChangeListener;
    }
    public RangeSeekBar(Context context) {
        this(context,null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar,0,0);
        cursorColor = types.getColor(R.styleable.RangeSeekBar_cursor_color,cursorColor);
        textColor = types.getColor(R.styleable.RangeSeekBar_text_color,textColor);
        lineColor = types.getColor(R.styleable.RangeSeekBar_line_color,lineColor);
        maxProgress = types.getInteger(R.styleable.RangeSeekBar_max_progress,maxProgress);
        minProgress = types.getInteger(R.styleable.RangeSeekBar_min_progress,minProgress);
        oneItemValue = types.getInteger(R.styleable.RangeSeekBar_oneitem_value,oneItemValue);
        oneGroupValue = types.getInteger(R.styleable.RangeSeekBar_one_group_value,oneGroupValue);
        progrees = types.getFloat(R.styleable.RangeSeekBar_current_progress,progrees);
        widthOfLine = types.getFloat(R.styleable.RangeSeekBar_linewidth,widthOfLine);
        scrollable = types.getBoolean(R.styleable.RangeSeekBar_scrollable,scrollable);


        numberOfItem = (maxProgress-minProgress) / oneItemValue ;
        widthOfItem = (getScreenWidth(getContext().getApplicationContext())-PADDING_LEFT-PADDING_RIGHT)/numberOfItem;
        //总宽减去线的宽度
        widthOfProgress = widthOfItem*numberOfItem+PADDING_LEFT;
        xProgress = PADDING_LEFT + (progrees - minProgress) / oneItemValue * widthOfItem ;
        types.recycle();
        init();

    }

    private void init() {
        //线的画笔
        mLinePaint = new Paint();
        mLinePaint.setColor(lineColor);
        mLinePaint.setAntiAlias(true);//抗锯齿
        mLinePaint.setStyle(Paint.Style.STROKE);//设置画笔样式(Stroke空心,Fill实心)
        mLinePaint.setStrokeWidth(widthOfLine);

        //字的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(36);

        //标尺的画笔
        mRulerPaint = new Paint();
        mRulerPaint.setAntiAlias(true);
        mRulerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRulerPaint.setColor(cursorColor);
        mRulerPaint.setStrokeWidth(widthOfLine);
        Log.i("--++","**************");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取控件的宽高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        scaleX = getScreenWidth(getContext().getApplicationContext()) / 1080f;
        scaleY = heightSize / 1920f;
        Log.i("--++",scaleX+"//////////"+scaleY);
        mTextPaint.setTextSize(36 * scaleX);
        mLinePaint.setStrokeWidth(widthOfLine * scaleX);
        mRulerPaint.setStrokeWidth(widthOfLine * scaleX);
        cursorRadius = 10 * scaleX;
        setMeasuredDimension(setMeasureWidth(widthMeasureSpec), setMeasureHeight(heightMeasureSpec));
    }

    private int setMeasureHeight(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        int result = 300;
        switch (mode) {
            /**
             * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
             * MeasureSpec.EXACTLY 是精确尺寸,控件大小能够确定的设定为xxdp时
             * MeasureSpec.AT_MOST 是最大尺寸,父控件的最大值
             * MeasureSpec.UNSPECIFIED 是未指定尺寸
             */
            case MeasureSpec.AT_MOST:
                size = Math.min(result, size);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                size = result;
                break;
        }
        return size;
    }

    private int setMeasureWidth(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        int result = getScreenWidth(getContext().getApplicationContext());
        switch (mode) {
            /**
             * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
             * MeasureSpec.EXACTLY 是精确尺寸,控件大小能够确定的设定为xxdp时
             * MeasureSpec.AT_MOST 是最大尺寸,父控件的最大值
             * MeasureSpec.UNSPECIFIED 是未指定尺寸
             */
            case MeasureSpec.AT_MOST:
                size = Math.min(result, size);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                size = result;
                break;
        }
        return size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //画最下面的线
        canvas.drawLine(PADDING_LEFT,getHeight(),widthOfProgress,getHeight(),mLinePaint);
        for (int i = minProgress; i <= maxProgress; i=i+oneItemValue) {
            if (i % oneGroupValue == 0 || i == minProgress) {
                //起点x坐标10像素，画厘米线
                canvas.drawLine(PADDING_LEFT, getHeight(), PADDING_LEFT, getHeight()/2+16*scaleX, mLinePaint);
                //计算刻度数
                String text = i + "";
                Rect rect = new Rect();
                //获取文本宽度
                float txtWidth = mTextPaint.measureText(text);
                mTextPaint.getTextBounds(text, 0, text.length(), rect);
                //画标尺的数字
                canvas.drawText(text, PADDING_LEFT - txtWidth / 2, getHeight()/2+16*scaleX-rect.height()  , mTextPaint);
            } else if (i % oneItemValue == 0) {
                //每隔小单位画间隔线,lineHeight越长，线越短
                double lineHeight = (getHeight()/2)*0.6;
                canvas.drawLine(PADDING_LEFT, getHeight(), PADDING_LEFT, getHeight()/2+(float)lineHeight, mLinePaint);
            }
//            else {
//                //画毫米线
//                double lineHeight = (getHeight()/2)*0.4;
//                canvas.drawLine(44, getHeight(), 44, getHeight()/2+(float)lineHeight, mLinePaint);
//            }
            //每隔一个单位像素移动一次达到划线效果
            canvas.translate(widthOfItem, 0);
        }

        canvas.restore();
        //画红线游标
        canvas.drawLine(xProgress, getHeight()/2, xProgress, getHeight(), mRulerPaint);
        Log.i("--++",cursorRadius+"////////");
        canvas.drawCircle(xProgress, getHeight()/2, cursorRadius, mRulerPaint);
        //测试用显示progress
//        BigDecimal bd = new BigDecimal((progrees - 18) / 180);
//        bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
//        mTextPaint.setTextSize(36);
//        float cursorTextWidth = mTextPaint.measureText(bd.floatValue()+"");
//        canvas.drawText(progrees+"", getWidth()/2-cursorTextWidth/2, cursorTextWidth, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getX() > PADDING_LEFT + widthOfProgress || event.getX() < PADDING_LEFT){
                    isCanMove = false;
                }else{
                    isCanMove = true;

                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isCanMove) {
                    return false;
                }
                float x = event.getX();

                if (x<PADDING_LEFT || x>widthOfProgress){

                    return false;
                }
                if (scrollable){
                    xProgress =  x;
                    progrees = minProgress+Math.round((xProgress-PADDING_LEFT)/widthOfItem)*oneItemValue;
                    if (onRangeRulerChangeListener != null){
                        onRangeRulerChangeListener.onValueChanged((int)progrees);
                    }
                    invalidate();
                }else{
                    if (x < widthOfProgress / 2){
                        xProgress = PADDING_LEFT;
                    }else{
                        xProgress = widthOfProgress;
                    }
                    progrees = minProgress+Math.round((xProgress-PADDING_LEFT)/widthOfItem)*oneItemValue;
                    if (onRangeRulerChangeListener != null){
                        onRangeRulerChangeListener.onValueChanged((int)progrees);
                    }
                    invalidate();

                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                float x1 = event.getX();
                if (scrollable){
                    int number = Math.round((x1 - PADDING_LEFT)/ widthOfItem);
                    if (number < 0){
                        number = 0;

                    }else if (number >= numberOfItem){
                        number = numberOfItem;
                    }
                    xProgress = PADDING_LEFT + number * widthOfItem;
                    invalidate();
                }else{

                    if (x1  >= widthOfProgress/2){
                        xProgress = PADDING_LEFT + numberOfItem * widthOfItem;
                    }else{
                        xProgress = PADDING_LEFT;
                    }
                    invalidate();
                }
                progrees = minProgress+Math.round((xProgress-PADDING_LEFT)/widthOfItem)*oneItemValue;
                if (onRangeRulerChangeListener != null){
                    onRangeRulerChangeListener.onValueChanged((int)progrees);
                }


                break;


        }
        return true;
    }

    private static double mInch = 0;
    /**
     * 获取屏幕尺寸
     * @param context
     * @return
     */
    public static double getScreenInch(Activity context) {
        if (mInch != 0.0d) {
            return mInch;
        }

        try {
            int realWidth = 0, realHeight = 0;
            Display display = context.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                display.getRealSize(size);
                realWidth = size.x;
                realHeight = size.y;
            } else if (android.os.Build.VERSION.SDK_INT < 17
                    && android.os.Build.VERSION.SDK_INT >= 14) {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } else {
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }

            mInch =formatDouble(Math.sqrt((realWidth/metrics.xdpi) * (realWidth /metrics.xdpi) + (realHeight/metrics.ydpi) * (realHeight / metrics.ydpi)),1);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return mInch;
    }
    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    private static double formatDouble(double d,int newScale) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
    public int getCurrentProgress() {
        return Math.round(xProgress/oneItemValue);
    }

    //设置当前位置，传值为标尺上的刻度
    //设置当前值一定要在设置了监听之后
    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        xProgress = (currentProgress/oneItemValue-minProgress/oneItemValue)*widthOfItem+PADDING_LEFT;
        invalidate();
        if (onRangeRulerChangeListener != null){
            onRangeRulerChangeListener.onValueChanged(minProgress+Math.round((xProgress-PADDING_LEFT)/widthOfItem)*oneItemValue);
        }
    }
}
