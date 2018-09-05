package com.jc.ips.api;

import com.jc.ips.bean.MagTimeSeries;
import com.jc.ips.bean.MagValue;
import com.jc.ips.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 地磁采集器
 * @author litangbo
 *
 */
public class MagCollector {
	public static void main(String[] args) {
		MagCollector collector = new MagCollector();
		// 模拟采集流程-----------------------------------------------
		MagTimeSeries timeSeries = new MagTimeSeries();
		// 在文本框中输入起始网格坐标
		timeSeries.setStartCoords("0,0");
		timeSeries.setEndCoords("0,5");
		// 点击开始按钮，进行地磁采集
		timeSeries.setUserId("ltb");
		timeSeries.setStartTime(System.currentTimeMillis());
		// 采集中...（注意采集频率）
		List<Double> magValues = new ArrayList<>();
		// -14.169312,-27.799988,-38.497925
		double magValue = Math.sqrt(Math.pow(-14.169312, 2)+Math.pow(-27.799988, 2)+Math.pow(-38.497925, 2));
		// TODO 四舍五入
		magValue = FormatUtil.m1(magValue);
		magValues.add(magValue);
		magValues.add(magValue);
		magValues.add(magValue);
		try {
			long time = 2000L;
			Thread.sleep(time);
			System.out.println("采集用时："+time/1000+"s");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 点击结束按钮，得到采集的地磁时间序列
		timeSeries.setEndTime(System.currentTimeMillis());
		// timeSeries.setMagValues(magValues.toString());
		timeSeries.setMagValues(new ArrayList<MagValue>());
		// 确认后，保存采集信息
		collector.save(timeSeries);
		
		// 模拟地磁采集校正-----------------------------------------------
		// TODO
	}
	/**
	 * 保存一条地磁时间序列
	 * @param timeSeries
	 */
	public void save(MagTimeSeries timeSeries){
		System.out.println(timeSeries);
		// TODO 保存到数据库或txt文本里
	}
	
	/**
	 * 查询所有已采集的地磁时间序列（方便采集工作的断续开展）
	 * @return
	 */
	public List<MagTimeSeries> findAll(){
		List<MagTimeSeries> all = new ArrayList<>();
		// TODO
		
		return all;
	}
	
	/**
	 * 重新采集某条路径
	 * @param id
	 * @param timeSeries
	 */
	public boolean update(int id,MagTimeSeries timeSeries){
		boolean isOk = false;
		// TODO
		
		return isOk;
	}
}
