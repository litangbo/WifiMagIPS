package com.jc.ips.bean;

import org.litepal.crud.DataSupport;

/**
 * 定位匹配结果
 */
public class MatchResult extends DataSupport{
    private int id;
    /**
     * 定位时间
     */
    private String time;
    /**
     * 定位坐标
     */
    private String resultCoords;

    /**
     * 采样地磁时间序列
     */
    private MagTimeSeries magTimeSeries;
    /**
     * DTW距离
     */
    private double minDistance = Double.MAX_VALUE;
    /**
     * 最匹配的子序列
     */
    private String similarSubSeries;
    /**
     * 子序列索引
     */
    private int subIndex;

    /**
     * 实际坐标，用户输入
     */
    private String realCoords = "";
    /**
     * 定位误差
     */
    private double offset = 0;

    @Override
    public String toString() {
        return "id:"+id
                +"\n定位时间:"+time
                +"\n,定位坐标:"+realCoords
                +"\n,DTW距离:"+minDistance
                +"\n,实际坐标:"+realCoords
                +"\n,定位误差:"+offset+"米\n";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getResultCoords() {
        return resultCoords;
    }

    public void setResultCoords(String resultCoords) {
        this.resultCoords = resultCoords;
    }

    public MagTimeSeries getMagTimeSeries() {
        return magTimeSeries;
    }

    public void setMagTimeSeries(MagTimeSeries magTimeSeries) {
        this.magTimeSeries = magTimeSeries;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public String getSimilarSubSeries() {
        return similarSubSeries;
    }

    public void setSimilarSubSeries(String similarSubSeries) {
        this.similarSubSeries = similarSubSeries;
    }

    public int getSubIndex() {
        return subIndex;
    }

    public void setSubIndex(int subIndex) {
        this.subIndex = subIndex;
    }

    public String getRealCoords() {
        return realCoords;
    }

    public void setRealCoords(String realCoords) {
        this.realCoords = realCoords;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}
