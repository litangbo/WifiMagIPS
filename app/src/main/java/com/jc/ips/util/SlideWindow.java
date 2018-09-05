package com.jc.ips.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlideWindow {
	public static void main(String[] args){
		SlideWindow slideWindow = new SlideWindow();
		Double[] arr = {1.0,2.0,3.0,4.0,5.0,6.0};
		List<Double[]> arrs = slideWindow.arrsInWindows(arr,5);
		for(Double[] subArr : arrs){
			System.out.println(Arrays.toString(subArr));
		}
	}

	public List<Double[]> arrsInWindows(Double[] arr, int size) {
		List<Double[]> list = new ArrayList<>();
		int len = arr.length;
		int idx = 0;
		int end = len-1;
		if(size>=len){
			list.add(arr);
		}else{
			for(int i=0;i<len;i++){
				idx = i;
				end = idx+size-1;
				if(end >= len){
					break;
				}else{
					list.add(getSubArr(arr, idx, end));
				}
			}
		}
        return list;
    }
	
	private Double[] getSubArr(Double[] arr, int idx, int end){
    	Double[] arr2 = new Double[end-idx+1];
        for(int i=idx; i<=end; i++){
            arr2[i-idx] = arr[i];
        }
        return arr2;
    }
}
