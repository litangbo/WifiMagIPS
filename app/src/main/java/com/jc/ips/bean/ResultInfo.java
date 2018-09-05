package com.jc.ips.bean;

import com.jc.ips.util.FormatUtil;

/**
 * 最相似结果信息处理
 * @author litangbo
 *
 */
public class ResultInfo {
	/**
	 * 最终命中坐标
	 */
	public double[] scoreCoords;
	/**
	 * DTW距离
	 */
	public double distance = Double.MAX_VALUE;
	/**
	 * 采样地磁序列
	 */
	public MagTimeSeries series;
	/**
	 * 最匹配的子序列
	 */
	public Double[] subVaules;
	/**
	 * 子序列的索引
	 */
	public int subIndex;
	
	/**
	 * 计算命中坐标
	 */
	public void calcScoreCoords(){
		
	}
	/**
	 * 计算误差
	 * @param realCoords 实际坐标
	 * @param intervalDistance 采样间隔距离
	 * @return
	 */
	public double calcOffset(double[] realCoords,double intervalDistance){
		double xDiff = realCoords[0]-scoreCoords[0];
		double yDiff = realCoords[1]-scoreCoords[1];
		double offset = Math.sqrt(xDiff*xDiff+yDiff*yDiff)*intervalDistance;
		offset = FormatUtil.m1(offset);
		return offset;
	}
}
