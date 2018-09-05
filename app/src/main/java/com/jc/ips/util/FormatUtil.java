package com.jc.ips.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 四舍五入，保留两位小数
 * @author litangbo
 *
 */
public class FormatUtil {
	
    public static double m1(double f) {
        BigDecimal bg = new BigDecimal(f);
        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }
    /**
     * DecimalFormat转换最简便
     * @return 
     */
    public static String m2(double f) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(f);
    }
    /**
     * String.format打印最简便
     */
    public static String m3(double f) {
        return String.format("%.2f", f);
    }
    public static String m4(double f) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        return nf.format(f);
    }
    public static void main(String[] args) {
    	double f = 111231.5585;
    	System.out.println(m1(f));
    	System.out.println(m2(f));
    	System.out.println(m3(f));
    	System.out.println(m4(f));
    }
}
