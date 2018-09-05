package com.jc.ips.bean;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 地磁时间序列
 * @author litangbo
 *
 */
public class MagTimeSeries extends DataSupport{
	private static final String TAG = "MagTimeSeries";
	/**
	 * type为0表示采集地磁时间序列，为1表示定位地磁时间序列
	 */
	private int type = 0;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Double[] getSubSeries(int start){
		return getSubSeries(start,-1);
	}

	public Double[] getSubSeries(int start,int end){
		int size = magValues.size();
		if(start < 0 || start > size-1){
			throw new RuntimeException("start IndexOutOfBounds(0,"+(size-1)+"):"+start);
		}
		if(end > size){
			throw new RuntimeException("end IndexOutOfBounds(0,"+size+"):"+end);
		}
		if(end < 0){
			end = magValues.size();
		}
		Double[] subSeries = new Double[end-start];
		for(int i=start;i<end;i++){
			subSeries[i-start] = magValues.get(i).getValue();
		}
		return subSeries;
	}

	/**
	 * 自增长id
	 */
	private long id;
	/**
	 * 采样人员id
	 */
	private String userId;
	/**
	 * 起点时间戳
	 */
	private long startTime;
	/**
	 * 终点时间戳
	 */
	private long endTime;
	/**
	 * 起点坐标
	 */
	private String startCoords;
	/**
	 * 终点坐标
	 */
	private String endCoords;
	/**
	 * 地磁强度组成的时间序列数组
	 */
	/*private String magValues;*/

	private List<MagValue> magValues = new ArrayList<>();

	public List<MagValue> getMagValues() {
		return magValues;
	}

	public void setMagValues(List<MagValue> magValues) {
		this.magValues = magValues;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean isShowValues) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder builder = new StringBuilder();
		if(isShowValues){
			for(MagValue magValue : magValues){
				builder.append("\n"+magValue.toString());
			}
		}
		builder.append("\n-------------------------------------------\n");

		return "id:"+id
				+",userId:"+userId
				+",开始采集时间:"+sdf.format(new Date(startTime))
				+",结束采集时间:"+sdf.format(new Date(endTime))
				+",起点坐标:"+startCoords
				+",终点坐标:"+endCoords
				/*+",magValues:"+magValues*/
				+builder.toString();
	}
	
	public String getUserId() {
		return userId;
	}
	public long getStartTime() {
		return startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public String getStartCoords() {
		return startCoords;
	}
	public String getEndCoords() {
		return endCoords;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public void setStartCoords(String startCoords) {
		this.startCoords = startCoords;
	}
	public void setEndCoords(String endCoords) {
		this.endCoords = endCoords;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
}
