package com.jc.wifimagips;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Arrays;

/**
 * G-sensor编码套路
 */
public class SensorActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "SensorActivity";
    
    private final SensorManager sensorManager;// 传感器管理器
    private final Sensor gSensor;// 加速度传感器
    private final Sensor mSensor;// 磁场传感器
    private final Sensor pSensor;// 压力传感器
    private final Sensor oSensor;// 方向传感器

    /**
     * 初始化传感器管理器和常用的传感器
     */
    public SensorActivity(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        pSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int delay = SensorManager.SENSOR_DELAY_NORMAL;
        // 注册监听
        sensorManager.registerListener(this,gSensor,delay);
        sensorManager.registerListener(this,mSensor,delay);
        sensorManager.registerListener(this,pSensor,delay);
        sensorManager.registerListener(this,oSensor,delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 取消注册
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // 获取传感器变化参数
        float[] values = sensorEvent.values;
        Log.d(TAG, "onSensorChanged: "+ Arrays.toString(values));

        // 根据传感器类型显示
        int type = sensorEvent.sensor.getType();
        switch (type){
            case Sensor.TYPE_ACCELEROMETER:

                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // 精度发生改变
    }
}
