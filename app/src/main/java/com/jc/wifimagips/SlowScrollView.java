package com.jc.wifimagips;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class SlowScrollView extends ScrollView{
    public SlowScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlowScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlowScrollView(Context context) {
        super(context);
    }

    /**
     * 滑动事件
     */
    @Override
    public void fling(int velocityY) {
        // 重点在"velocityY / 4"，这里意思是滑动速度减慢到原来四分之一的速度，这里大家可以根据自己的需求加快或减慢滑动速度。
        super.fling(velocityY / 4);
    }
}
