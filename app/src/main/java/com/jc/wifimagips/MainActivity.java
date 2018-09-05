package com.jc.wifimagips;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jc.ips.bean.MagTimeSeries;
import com.jc.ips.bean.MagValue;
import com.jc.ips.bean.MatchResult;
import com.jc.ips.util.DTW;
import com.jc.ips.util.FormatUtil;
import com.jc.ips.util.SlideWindow;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Wifi结合地磁进行室内定位
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "MainActivity";

    /**更新传感器采集频率的消息码*/
    public static final int UPDATE_FREQUENCY = 1;

    /**完整的日期格式：年月日 时分秒*/
    private SimpleDateFormat sdfWithAll = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    /**只有后半部分的日期格式：时分秒*/
    private SimpleDateFormat sdfWithLast = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    /**地磁时间序列对象*/
    private MagTimeSeries magTimeSeries;
    /**地磁时间序列值*/
    /*private List<Double> magValues;*/
    private List<MagValue> magValues;
    /**开始采样时间*/
    private long startTime;
    /**采样时间间隔*/
    private int interval = 200;

    /**是否正在采集*/
    private boolean isWork = false;
    /**正在地磁采集，还是地磁定位*/
    private boolean isCollect = true;
    /**是否正在定位*/
    private boolean isLocating = false;

    /**采集的所有地磁时间序列*/
    private List<MagTimeSeries> allCollect = new ArrayList<>();

    /**通过水平方向累计角度变化，识别转角*/
    public static final double MAX_CHANGE_ANGLE = 30;
    /**上一次转向角度：0-360*/
    private double lastDegree = 361;
    /**上一次转向：no、left、right*/
    private String lastTurn = "no";

    // 各种控件View
    private TextView useTime;
    private EditText sxInput;
    private EditText syInput;
    private EditText exInput;
    private EditText eyInput;
    private EditText freqInput;
    private Button startRecord;
    private Button getPosition;
    private TextView magText;
    private TextView dirText;
    private TextView magSeriesText;
    private Button queryCollect;
    private Button deleteCollect;
    private EditText idInput;
    private Button deleteOne;
    private ScrollView scrollView;

    // 传感器管理器
    private SensorManager sensorManager;
    // 常用传感器
    // private Sensor gSensor;// 加速度传感器
    private Sensor mSensor;// 磁场传感器
    // private Sensor pSensor;// 压力传感器
    private Sensor oSensor;// 方向传感器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initSensor();
        Connector.getDatabase();
        queryAllCollect();
    }

    /**
     * 警告已经指名了一条王道：将Handler声明为static并持有其外部类的WeakReference(弱引用)
     */
    private static class MyHandler extends Handler{
        private final WeakReference<MainActivity> mActivity;

        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what){
                    case UPDATE_FREQUENCY:
                        activity.isWork = true;
                        if(activity.isLocating){
                            activity.startTime += activity.interval;
                        }else{
                            long startTime = activity.startTime;
                            long endTime = System.currentTimeMillis();
                            long useTime = endTime-startTime;
                            String hms = activity.sdfWithLast.format(useTime);
                            // 在这里可以更新UI
                            activity.useTime.setText(hms);
                            // LogUtil.d(TAG,"用时："+hms);
                            if(useTime>=60*1000){// 超过1分钟自动停止
                                activity.stopCollectMag();
                                Toast.makeText(activity,"采集超时自动停止",Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
    private final MyHandler handler = new MyHandler(this);

    private Runnable sensorTask = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(UPDATE_FREQUENCY);// 消息(一个整型值)
            handler.postDelayed(this, interval);// 延迟执行task本身,实现循环的效果
        }
    };

    private void queryAllCollect(){
        // true参数表示使用激进查询，关联的数据也会一起查出来
        // MagTimeSeries last = DataSupport.findLast(MagTimeSeries.class,true);
        allCollect =
                // DataSupport.findAll(MagTimeSeries.class,true);
                DataSupport.where("type=?","0").find(MagTimeSeries.class,true);
        StringBuilder builder = new StringBuilder();
        for(MagTimeSeries magTimeSeries : allCollect){
            builder.append(magTimeSeries.toString(false));
        }
        // magSeriesText.setText(builder.toString());
        showScrollView(builder.toString());
    }

    private void showScrollView(String msg){
        magSeriesText.setText(msg);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    /**
     * 初始化传感器管理器和常用的传感器
     */
    private void initSensor(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager != null){
            // gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            // pSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }else{
            Toast.makeText(this,"未获取到传感器服务",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化视图
     */
    private void initView(){
        RadioGroup operateGroup = findViewById(R.id.operate_group);
        /*RadioButton collectButton = findViewById(R.id.collect_button);
        RadioButton locateButton = findViewById(R.id.locate_button);*/
        operateGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                /*// 获取选中的RadioButton的id
                int cid = radioGroup.getCheckedRadioButtonId();
                // 获取选中的RadioButton
                RadioButton radioButton = findViewById(cid);
                // 获取选中的RadioButton的内容
                String cText = radioButton.getText().toString();*/
                switch (i){
                    case R.id.collect_button:
                        isCollect = true;
                        break;
                    case R.id.locate_button:
                        isCollect = false;
                        queryAllCollect();// 预先查询指纹库
                        break;
                    default:
                        break;
                }
                stopCollectMag();
                sxInput.setEnabled(isCollect);
                syInput.setEnabled(isCollect);
                exInput.setEnabled(isCollect);
                eyInput.setEnabled(isCollect);
            }
        });
        useTime = findViewById(R.id.use_time);
        sxInput = findViewById(R.id.sx_input);
        syInput = findViewById(R.id.sy_input);
        exInput = findViewById(R.id.ex_input);
        eyInput = findViewById(R.id.ey_input);
        freqInput = findViewById(R.id.freq_input);
        startRecord = findViewById(R.id.start_record);
        Button stopRecord = findViewById(R.id.stop_record);
        getPosition = findViewById(R.id.get_position);
        magText = findViewById(R.id.mag_text);
        dirText = findViewById(R.id.dir_text);
        magSeriesText = findViewById(R.id.mag_series_text);
        queryCollect = findViewById(R.id.query_collect);
        deleteCollect = findViewById(R.id.delete_collect);
        idInput = findViewById(R.id.id_input);
        deleteOne = findViewById(R.id.delete_one);
        scrollView = findViewById(R.id.scroll_view);
        queryCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryAllCollect();
            }
        });
        deleteCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建一个AlertDialog实例
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                // 设置标题、内容、是否取消等属性
                dialog.setTitle("删除确认");
                dialog.setMessage("是否真的删除？");
                dialog.setCancelable(false);// 不能通过Back键取消
                // 设置确定按钮的点击事件
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        int delRow = DataSupport.deleteAll(MagValue.class);
                        int delRow2 = DataSupport.deleteAll(MagTimeSeries.class);
                        int delRow3 = DataSupport.deleteAll(MatchResult.class);
                        showScrollView("已删除所有数据，包括："
                                +"\n地磁强度："+delRow+"条"
                                +"\n地磁序列："+delRow2+"条"
                                +"\n定位结果："+delRow3+"条");
                    }
                });
                // 设置取消按钮的点击事件
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Toast.makeText(MainActivity.this,"取消删除",Toast.LENGTH_SHORT).show();
                    }
                });
                // 显示对话框
                dialog.show();
            }
        });
        deleteOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String seriesId = idInput.getText().toString().trim();
                if("".equals(seriesId) || seriesId.indexOf("00") == 0){
                    Toast.makeText(MainActivity.this,"",Toast.LENGTH_SHORT).show();
                }else{
                    // 创建一个AlertDialog实例
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    // 设置标题、内容、是否取消等属性
                    dialog.setTitle("删除确认");
                    dialog.setMessage("是否真的删除id为"+seriesId+"的序列？");
                    dialog.setCancelable(false);// 不能通过Back键取消
                    // 设置确定按钮的点击事件
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            int delRow = DataSupport.delete(MagTimeSeries.class,Long.valueOf(seriesId));
                            if(delRow > 0){
                                Toast.makeText(MainActivity.this,"成功删除id为"+seriesId+"的序列",Toast.LENGTH_SHORT).show();
                                queryAllCollect();
                            }else{
                                Toast.makeText(MainActivity.this,"不存在id为"+seriesId+"的序列",Toast.LENGTH_SHORT).show();
                            }
                            idInput.setText("");
                        }
                    });
                    // 设置取消按钮的点击事件
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            Toast.makeText(MainActivity.this,"取消删除",Toast.LENGTH_SHORT).show();
                        }
                    });
                    // 显示对话框
                    dialog.show();
                }
            }
        });
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCollectMag();
            }
        });
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopCollectMag();
            }
        });
        getPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matchSubSeries();
            }
        });
    }

    private void checkFrequency(){
        int frequency;
        String freqStr = freqInput.getText().toString().trim();
        if("".equals(freqStr) || freqStr.indexOf("0") == 0){
            frequency = 5;// 默认5Hz
        }else {
            frequency = Integer.valueOf(freqStr);
            if(frequency < 1){
                frequency = 1;
                freqInput.setText(R.string.freq_min);
            }else if(frequency > 10){
                frequency = 10;
                freqInput.setText(R.string.freq_max);
            }
        }
        interval = 1000/frequency;
    }

    private void startCollectMag(){
        // 重新采集地磁时间序列
        magTimeSeries= new MagTimeSeries();
        if(isCollect){
            String sx = sxInput.getText().toString().trim();
            String sy = syInput.getText().toString().trim();
            String ex = exInput.getText().toString().trim();
            String ey = eyInput.getText().toString().trim();
            if("".equals(sx) || "".equals(sy) || "".equals(ex) || "".equals(ey)){
                Toast.makeText( MainActivity.this,"请输入完整的起始点坐标",Toast.LENGTH_SHORT).show();
                return;
            }else{
                double[] startCoords = new double[]{Double.valueOf(sx),Double.valueOf(sy)};
                double[] endCoords = new double[]{Double.valueOf(ex),Double.valueOf(ey)};
                magTimeSeries.setStartCoords(Arrays.toString(startCoords));
                magTimeSeries.setEndCoords(Arrays.toString(endCoords));
            }
            magTimeSeries.setType(0);
        }else{
            magTimeSeries.setType(1);
            getPosition.setEnabled(true);
        }
        isWork = true;
        checkFrequency();
        startRecord.setEnabled(false);
        deleteCollect.setEnabled(false);
        queryCollect.setEnabled(false);
        idInput.setEnabled(false);
        deleteOne.setEnabled(false);
        magValues = new ArrayList<>();
        magTimeSeries.setMagValues(magValues);
        magTimeSeries.setUserId("1");
        startTime = System.currentTimeMillis();
        useTime.setText(R.string.time_init);
        magTimeSeries.setStartTime(startTime);
        handler.postDelayed(sensorTask,interval);
        Toast.makeText(MainActivity.this,"开始记录地磁信息",Toast.LENGTH_SHORT).show();
    }

    private void stopCollectMag(){
        if(startRecord.isEnabled()){// 当没有开始采集数据时，点击停止按钮无效
            return;
        }
        sxInput.setText("");
        syInput.setText("");
        exInput.setText("");
        eyInput.setText("");
        isWork = false;
        startRecord.setEnabled(true);
        deleteCollect.setEnabled(true);
        queryCollect.setEnabled(true);
        idInput.setEnabled(true);
        deleteOne.setEnabled(true);
        getPosition.setEnabled(false);
        magTimeSeries.setEndTime(System.currentTimeMillis());
        handler.removeCallbacks(sensorTask);
        if(isCollect){
            // 先保存List<MagValue>
            for(MagValue magValue : magValues){
                magValue.save();
            }
            // 再保存地磁时间序列
            boolean isOk = magTimeSeries.save();
            if(isOk){
                Toast.makeText(MainActivity.this,"保存地磁信息成功",Toast.LENGTH_SHORT).show();
                // 显示当前采集信息
                showScrollView(magTimeSeries.toString());
            }else{
                Toast.makeText(MainActivity.this,"保存地磁信息失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public int LIMIT = 14;// 2秒左右
    private void matchSubSeries(){
        if(allCollect == null || allCollect.size() == 0){
            Toast.makeText(this,"请先采集数据",Toast.LENGTH_SHORT).show();
            return;
        }
        int mSize = magTimeSeries.getMagValues().size();
        int start;
        if(mSize < LIMIT){
            Toast.makeText(this,"未预热成功",Toast.LENGTH_SHORT).show();
            return;
        }else{
            start = mSize - LIMIT;
        }
        isLocating = true;
        Double[] seqb = magTimeSeries.getSubSeries(start);
        List<MagTimeSeries> seriesList = allCollect;
        // 遍历采样地磁序列，得到最相似子序列的信息（采样地磁序列，子序列，定位坐标）
        int size = seqb.length;
        SlideWindow slideWindow = new SlideWindow();
        DTW dtw = new DTW();
        MatchResult result = new MatchResult();
        result.setTime(sdfWithAll.format(new Date()));
        for(MagTimeSeries series : seriesList){
            // NPE问题
            /*List<Double> magValues = new ArrayList<>();// series.getMagValues();
            Double[] magArr = new Double[magValues.size()];
            magArr = magValues.toArray(magArr);*/
            Double[] magArr = series.getSubSeries(0);
            // 利用滑动窗口技术，获取每一条采样地磁序列的子序列
            List<Double[]> subArrList = slideWindow.arrsInWindows(magArr, size);
            int num = subArrList.size();
            for(int i=0;i<num;i++){
                // 利用DTW算法计算待匹配地磁序列和采样地磁序列子序列的距离
                Double[] seqa = subArrList.get(i);
                double distance = dtw.getDistance(seqa, seqb);
                distance = FormatUtil.m1(distance);
                if(distance < result.getMinDistance()){
                    result.setMinDistance(distance);
                    result.setMagTimeSeries(series);
                    result.setSimilarSubSeries(Arrays.toString(seqa));
                    result.setSubIndex(i);
                }
            }
        }
        // ----------------------------------------
        MagTimeSeries magTimeSeries = result.getMagTimeSeries();
        String scStr = magTimeSeries.getStartCoords();
        String ecStr = magTimeSeries.getEndCoords();
        // 起始点坐标
        int si = scStr.indexOf(",");
        double sx = Double.valueOf(scStr.substring(1,si));
        double sy = Double.valueOf(scStr.substring(si+1,scStr.length()-1));
        int ei = ecStr.indexOf(",");
        double ex = Double.valueOf(ecStr.substring(1,ei));
        double ey = Double.valueOf(ecStr.substring(ei+1,ecStr.length()-1));
        // 子序列占比
        // 当待匹配序列比采样序列长的时候，rate>1（因为滑动窗口算法在窗口大小比采样序列长度大时，会返回采样序列本身）
        double rate = (result.getSubIndex()+size)*1.0/magTimeSeries.getMagValues().size();
        if(rate > 1 || size > magTimeSeries.getMagValues().size()){
            rate = 1;
        }
        // 定位坐标
        double px = FormatUtil.m1(sx+(ex-sx)*rate);
        double py = FormatUtil.m1(sy+(ey-sy)*rate);
        result.setResultCoords(Arrays.toString(new double[]{px,py}));
        // ----------------------------------------
        showCoordsSetDialog(px,py,result);
        Log.d(TAG, "matchSubSeries: ");
    }

    /**
     * 采样间隔距离
     */
    private static final double INTERVAL_DISTANCE = 1.4;

    private void showCoordsSetDialog(final double px,final double py,final MatchResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        dialog.setTitle("定位结果");
        dialog.setCancelable(false);// 不能通过Back键取消

        View view = View.inflate(this,R.layout.dialog_set_coords,null);
        // dialog.setView(view);// 将自定义布局文件设置给dialog
        // dialog.setView(view,0,0,0,0);// 设置边距为0，保证在2.x版本上能运行
        dialog.setView(view,80,20,80,20);

        TextView resultText = view.findViewById(R.id.result_text);
        resultText.setText(getString(R.string.result_point,result.getResultCoords()));
        final EditText rxInput = view.findViewById(R.id.rx_input);
        final EditText ryInput = view.findViewById(R.id.ry_input);
        final TextView offsetText = view.findViewById(R.id.offset_text);
        Button calcButton = view.findViewById(R.id.calc_button);
        Button okButton = view.findViewById(R.id.ok_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rx = rxInput.getText().toString().trim();
                String ry = ryInput.getText().toString().trim();
                if("".equals(rx) || "".equals(ry)){
                    Toast.makeText(MainActivity.this,"请输入准确的真实坐标",Toast.LENGTH_SHORT).show();
                }else{
                    // 计算定位误差
                    double xDiff = px-Double.valueOf(rx);
                    double yDiff = py-Double.valueOf(ry);
                    double offset = Math.sqrt(xDiff*xDiff+yDiff*yDiff)*INTERVAL_DISTANCE;
                    offset = FormatUtil.m1(offset);
                    result.setRealCoords(Arrays.toString(new String[]{rx,ry}));
                    result.setOffset(offset);
                    offsetText.setText(getString(R.string.result_offset,offset));
                }
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rx = rxInput.getText().toString().trim();
                String ry = ryInput.getText().toString().trim();
                if("".equals(rx) || "".equals(ry)){
                    Toast.makeText(MainActivity.this,"请输入准确的真实坐标",Toast.LENGTH_SHORT).show();
                }else{
                    // 计算定位误差
                    double xDiff = px-Double.valueOf(rx);
                    double yDiff = py-Double.valueOf(ry);
                    double offset = Math.sqrt(xDiff*xDiff+yDiff*yDiff)*INTERVAL_DISTANCE;
                    offset = FormatUtil.m1(offset);
                    result.setRealCoords(Arrays.toString(new String[]{rx,ry}));
                    result.setOffset(offset);
                    offsetText.setText(getString(R.string.result_offset,offset));

                    boolean isOK = result.save();
                    if(isOK){
                        Toast.makeText(MainActivity.this,"保存定位结果成功",Toast.LENGTH_SHORT).show();
                        showScrollView(result.toString());
                    }else{
                        Toast.makeText(MainActivity.this,"保存定位结果失败",Toast.LENGTH_SHORT).show();
                    }
                    isLocating = false;
                    dialog.dismiss();// 隐藏对话框
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLocating = false;
                dialog.dismiss();// 隐藏对话框
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int delay = SensorManager.SENSOR_DELAY_NORMAL;
        // 注册监听
        // sensorManager.registerListener(this,gSensor,delay);
        sensorManager.registerListener(this,mSensor,delay);
        // sensorManager.registerListener(this,pSensor,delay);
        sensorManager.registerListener(this,oSensor,delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 取消注册
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册
        sensorManager.unregisterListener(this);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // 获取传感器变化参数
        float[] values = sensorEvent.values;
        int len = values.length;
        float x = len>0?values[0]:0;
        float y = len>1?values[1]:0;
        float z = len>2?values[2]:0;
        // 根据传感器类型显示
        int type = sensorEvent.sensor.getType();
        switch (type){
            case Sensor.TYPE_MAGNETIC_FIELD:
                double distance = Math.sqrt(x*x+y*y+z*z);
                magText.setText(getString(R.string.mag_text,x,y,z,distance));
                if(isWork && !isLocating){
                    MagValue magValue = new MagValue();
                    magValue.setTime(sdfWithAll.format(new Date()));
                    magValue.setValue(distance);
                    magValues.add(magValue);
                    isWork = false;
                }
                break;
            case Sensor.TYPE_ORIENTATION:
                if(y == 0){
                    break;
                }
                double degree = Math.toDegrees(Math.atan(x/y));
                if(lastDegree > 360){
                    lastDegree = degree;
                }
                double diff = degree - lastDegree;
                if(diff > MAX_CHANGE_ANGLE){
                    lastTurn = "left";
                    lastDegree = degree;
                }else if(diff < -MAX_CHANGE_ANGLE){
                    lastTurn = "right";
                    lastDegree = degree;
                }
                dirText.setText(getString(R.string.dir_text,degree,lastDegree,diff,lastTurn));
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
