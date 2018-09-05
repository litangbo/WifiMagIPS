package com.jc.ips.util;

/**
 * DTW(Dynamic Time Wrapping,动态时间规整)算法，求两个长度不一致的时间序列的距离
 * @author litangbo
 *
 */
public class DTW {
	public static void main(String[] args) {
		DTW dtw = new DTW();
		double[] seqa = {1,2,3,2,1};
		double[] seqb = {1,2,3,3,3,4,2,1};
		double distance = dtw.getDistance(seqa, seqb);
		System.out.println("distance:"+distance);
	}
	
	private double getMin(double a, double b, double c) {
		double min = a;
		if (b > a)
			min = a;
		else if (c > b) {
			min = b;
		} else {
			min = c;
		}
		return min;
	}
	
	public double getDistance(Double[] seqa, Double[] seqb) {
		double distance = 0;
		int lena = seqa.length;
		int lenb = seqb.length;
		double[][] c = new double[lena][lenb];
		for (int i = 0; i < lena; i++) {
			for (int j = 0; j < lenb; j++) {
				c[i][j] = 1;
			}
		}
		for (int i = 0; i < lena; i++) {
			for (int j = 0; j < lenb; j++) {
				double tmp = (seqa[i] - seqb[j]) * (seqa[i] - seqb[j]);
				// 局部路径约束
				if (j == 0 && i == 0)
					c[i][j] = tmp;
				else if (j > 0)
					c[i][j] = c[i][j - 1] + tmp;
				if (i > 0) {
					if (j == 0)
						c[i][j] = tmp + c[i - 1][j];
					else
						c[i][j] = tmp + getMin(c[i][j - 1], c[i - 1][j - 1], c[i - 1][j]);
				}
			}
		}
		distance = c[lena - 1][lenb - 1];
		
		// ----
		/*Matrix cMatrix = new Matrix(c);
		System.out.println("cMatrix:");
		cMatrix.print(8, 4);*/
		
		return distance;
	}

	public double getDistance(double[] seqa, double[] seqb) {
		double distance = 0;
		int lena = seqa.length;
		int lenb = seqb.length;
		double[][] c = new double[lena][lenb];
		for (int i = 0; i < lena; i++) {
			for (int j = 0; j < lenb; j++) {
				c[i][j] = 1;
			}
		}
		for (int i = 0; i < lena; i++) {
			for (int j = 0; j < lenb; j++) {
				double tmp = (seqa[i] - seqb[j]) * (seqa[i] - seqb[j]);
				// 局部路径约束
				if (j == 0 && i == 0)
					c[i][j] = tmp;
				else if (j > 0)
					c[i][j] = c[i][j - 1] + tmp;
				if (i > 0) {
					if (j == 0)
						c[i][j] = tmp + c[i - 1][j];
					else
						c[i][j] = tmp + getMin(c[i][j - 1], c[i - 1][j - 1], c[i - 1][j]);
				}
			}
		}
		distance = c[lena - 1][lenb - 1];
		
		// ----
		/*Matrix cMatrix = new Matrix(c);
		System.out.println("cMatrix:");
		cMatrix.print(8, 4);*/
		
		return distance;
	}
}
