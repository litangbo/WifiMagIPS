package com.jc.ips.api;

import com.jc.ips.bean.LimitQueue;
import com.jc.ips.bean.MagTimeSeries;
import com.jc.ips.bean.ResultInfo;
import com.jc.ips.util.DTW;
import com.jc.ips.util.FormatUtil;
import com.jc.ips.util.SlideWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 地磁定位器
 * @author litangbo
 *
 */
public class MagLocator {
	/**
	 * 固定长度的待匹配地磁序列
	 * 固定长度：2s
	 * 采样频率：5Hz
	 */
	public static final int MAG_LIMIT = 2*5;
	/**
	 * 相邻采样坐标的间隔距离
	 */
	public static final double INTERVAL_DISTANCE = 1.4;
	
	public static void main(String[] args) {
		// 模拟定位流程-----------------------------------------------
		
		// 获取待匹配的地磁序列
		MagLocator locator = new MagLocator();
		Double[] seqb = locator.getMatchSeq();
		
		// 获取采样地磁序列
		MagCollector collector = new MagCollector();
		List<MagTimeSeries> seriesList = collector.findAll();
		
		// 地磁匹配
		ResultInfo resultInfo = locator.match(seqb, seriesList);
		System.out.println("定位坐标："+Arrays.toString(resultInfo.scoreCoords));
		
		// 输入实际坐标，计算误差
		double[] realCoords = new double[]{0,0};
		System.out.println("实际坐标："+Arrays.toString(realCoords));
		double offset = resultInfo.calcOffset(realCoords,INTERVAL_DISTANCE);
		System.out.println("定位误差："+offset+"米");
		
		// TODO 存储定位结果
		
	}
	
	/**
	 * 地磁匹配，获取定位结果
	 * @param seqb 待匹配时间序列
	 * @param seriesList 
	 * @return
	 */
	public ResultInfo match(Double[] seqb,List<MagTimeSeries> seriesList){
		// 遍历采样地磁序列，得到最相似子序列的信息（采样地磁序列，子序列，定位坐标）
		int size = seqb.length;
		SlideWindow slideWindow = new SlideWindow();
		DTW dtw = new DTW();
		ResultInfo resultInfo = new ResultInfo();
		/*double minDistance = Double.MAX_VALUE;// 最小DTW距离
		MagTimeSeries scoreSeries = null;// 命中的采样时间序列
		Double[] scoreSubValues = null;// 命中的采样时间序列的子序列
		int subIndex = 0;// 子序列索引*/		
		for(MagTimeSeries series : seriesList){
			List<Double> magValues = new ArrayList<>();// series.getMagValues();
			Double[] magArr = new Double[magValues.size()];
			magArr = magValues.toArray(magArr);
			// 利用滑动窗口技术，获取每一条采样地磁序列的子序列
			List<Double[]> subArrList = slideWindow.arrsInWindows(magArr, size);
			int num = subArrList.size();
			for(int i=0;i<num;i++){
				// 利用DTW算法计算待匹配地磁序列和采样地磁序列子序列的距离
				Double[] seqa = subArrList.get(i);
				double distance = dtw.getDistance(seqa, seqb);
				distance = FormatUtil.m1(distance);
				/*if(distance < minDistance){
					minDistance = distance;
					scoreSeries = series;
					scoreSubValues = seqa;
					subIndex = i;
				}*/
				if(distance < resultInfo.distance){
					resultInfo.distance = distance;
					resultInfo.series = series;
					resultInfo.subVaules = seqa;
					resultInfo.subIndex = i;
				}
			}
		}
		// 计算命中坐标
		resultInfo.calcScoreCoords();
		
		return resultInfo;
	}
	
	/**
	 * 获取【待匹配地磁序列】
	 * @return
	 */
	public Double[] getMatchSeq(){
		// 1、模拟地磁定位序列获取
		LimitQueue<Double> limitQueue = new LimitQueue<>(MAG_LIMIT);
		
		int len = (int)(Math.random()*15+1);
		Double[] seqa = new Double[len];
		for(int i=0;i<len;i++){
			double magValue = Math.random()*20+40;
			magValue = FormatUtil.m1(magValue);
			seqa[i] = magValue;
			// limitQueue.add(magValue);
			limitQueue.offer(magValue);
		}
		System.out.println("seqa:"+Arrays.toString(seqa)+",size="+seqa.length);
		
		Double[] seqb = new Double[limitQueue.getLimit()];
		seqb = limitQueue.getQueue().toArray(seqb);
		System.out.println("limitQueue.size():"+limitQueue.size());
		System.out.println("seqb:"+Arrays.toString(seqb)+",size="+seqb.length);
		return seqb;
	}
	
}
