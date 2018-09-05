package com.jc.test;

import com.jc.ips.util.CubicSplineFast;

import java.util.Arrays;

public class CubicSplineFastTest {
    public static void main(String[] args) {

        double[] magSeries = new double[] {46.2268, 45.8292, 47.2509, 46.5127, 46.4261, 47.4008, 46.4661, 44.7416};
        int len = magSeries.length;

        // 采样频率
        int frequence = 5;
        if(!(len > 0 && frequence > 0)){
            throw new RuntimeException();
        }
        // 采样时间间隔
        double step = 1.0/frequence;
        // 采样开始时间和结束时间
        double start = 0;
        double end = start+step*(len-1);

        double[] x = range(start, end, step);
        System.out.println("knownX = "+ Arrays.toString(x));
        System.out.println("knownY = "+Arrays.toString(magSeries));

        double[] testX = range(start, end, 0.1);
        System.out.println("intX = "+Arrays.toString(testX));
        CubicSplineFast spliner = new CubicSplineFast(x, magSeries);
        // interpolate()
        double[] results = new double[testX.length];
        for(int i = 0; i < testX.length; i++)
        {
            results[i] = spliner.interpolate(testX[i]);
        }
        System.out.println("intY = "+Arrays.toString(results));
    }

    public static double[] range(double start, double end, double step) {

        double[] out = new double[(int) Math.floor((end - start) / step) + 1];
        for (int i = 0; i < out.length; i++) {
            out[i] = start + step * i;
        }
        return out;
    }
}
