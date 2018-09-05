/**********************************************************
*
*   Class CubicSplineFast
*
*   Class for performing an interpolation using a cubic spline
*   setTabulatedArrays and interpolate adapted, with modification to
*   an object-oriented approach, from Numerical Recipes in C (http://www.nr.com/)
*   Stripped down version of CubicSpline - all data checks have been removed for faster running
*
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	26 December 2009 (Stripped down version of CubicSpline: May 2002 - 31 October 2009)
*   UPDATE: 14  January 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/CubicSplineFast.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2010  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
*   Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
*   provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
*   and associated documentation or publications.
*
*   Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice,
*   this list of conditions and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
*   Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
*   the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission
*   from the Michael Thomas Flanagan:
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
*   Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
*   or its derivatives.
*
***************************************************************************************/


package com.jc.ips.util;

import java.util.Arrays;

/**
 * 快速进行三样条插值
 * @author litangbo
 *
 */
public class CubicSplineFast{

        /* Exposed fields for testing purposes */
    	public int nPoints = 0;                            // no. of tabulated points
    	public double[] y = null;                          // y=f(x) tabulated function
    	public double[] x = null;                          // x in tabulated function f(x)
    	public double[] d2ydx2 = null;                     // second derivatives of y
        public boolean derivCalculated = false;            // = true when the derivatives have been calculated

    	// Constructors
    	// Constructor with data arrays initialized to arrays x and y
    	public CubicSplineFast(double[] x, double[] y){
        	this.nPoints=x.length;
        	this.x = new double[nPoints];
        	this.y = new double[nPoints];
        	this.d2ydx2 = new double[nPoints];
        	for(int i=0; i<this.nPoints; i++){
            		this.x[i]=x[i];
            		this.y[i]=y[i];
        	}
        	this.calcDeriv();
    	}

    	// Constructor with data arrays initialized to zero
    	// Primarily for use by BiCubicSplineFast
    	public CubicSplineFast(int nPoints){
        	this.nPoints=nPoints;
        	this.x = new double[nPoints];
        	this.y = new double[nPoints];
        	this.d2ydx2 = new double[nPoints];
   	    }

    	// METHODS

    	// Resets the x y data arrays - primarily for use in BiCubicSplineFast
    	public void resetData(double[] x, double[] y){
        	for(int i=0; i<this.nPoints; i++){
            		this.x[i]=x[i];
            		this.y[i]=y[i];
        	}
    	}

    	// Returns a new CubicSplineFast setting array lengths to n and all array values to zero with natural spline default
    	// Primarily for use in BiCubicSplineFast
    	public static CubicSplineFast zero(int n){
        	if(n<3)throw new IllegalArgumentException("A minimum of three data points is needed");
        	CubicSplineFast aa = new CubicSplineFast(n);
        	return aa;
    	}

    	// Create a one dimensional array of cubic spline objects of length n each of array length m
    	// Primarily for use in BiCubicSplineFast
    	public static CubicSplineFast[] oneDarray(int n, int m){
        	CubicSplineFast[] a =new CubicSplineFast[n];
	    	for(int i=0; i<n; i++){
	        	a[i]=CubicSplineFast.zero(m);
        	}
        	return a;
    	}


    	//  Calculates the second derivatives of the tabulated function
    	//  for use by the cubic spline interpolation method (.interpolate)
    	//  This method follows the procedure in Numerical Methods C language procedure for calculating second derivatives
    	public void calcDeriv(){
	    	double	p=0.0D,qn=0.0D,sig=0.0D,un=0.0D;
	    	double[] u = new double[nPoints];

	        d2ydx2[0]=u[0]=0.0;
	    	for(int i=1;i<=this.nPoints-2;i++){
		    	sig=(this.x[i]-this.x[i-1])/(this.x[i+1]-this.x[i-1]);
		    	p=sig*this.d2ydx2[i-1]+2.0;
		    	this.d2ydx2[i]=(sig-1.0)/p;
		    	u[i]=(this.y[i+1]-this.y[i])/(this.x[i+1]-this.x[i]) - (this.y[i]-this.y[i-1])/(this.x[i]-this.x[i-1]);
		    	u[i]=(6.0*u[i]/(this.x[i+1]-this.x[i-1])-sig*u[i-1])/p;
	    	}

		    qn=un=0.0;
	    	this.d2ydx2[this.nPoints-1]=(un-qn*u[this.nPoints-2])/(qn*this.d2ydx2[this.nPoints-2]+1.0);
	    	for(int k=this.nPoints-2;k>=0;k--){
		    	this.d2ydx2[k]=this.d2ydx2[k]*this.d2ydx2[k+1]+u[k];
	    	}
	    	this.derivCalculated = true;
    	}

    	//  INTERPOLATE
    	//  Returns an interpolated value of y for a value of x from a tabulated function y=f(x)
    	//  after the data has been entered via a constructor.
    	//  The derivatives are calculated, bt calcDeriv(), on the first call to this method ands are
    	//  then stored for use on all subsequent calls
    	public double interpolate(double xx){

            double h=0.0D,b=0.0D,a=0.0D, yy=0.0D;
	    	int k=0;
	    	int klo=0;
	    	int khi=this.nPoints-1;
	    	while (khi-klo > 1){
		    	k=(khi+klo) >> 1;
		    	if(this.x[k] > xx){
			    	khi=k;
		    	}
		    	else{
			    	klo=k;
		    	}
	    	}
	    	h=this.x[khi]-this.x[klo];

	    	if (h == 0.0){
	        	throw new IllegalArgumentException("Two values of x are identical: point "+klo+ " ("+this.x[klo]+") and point "+khi+ " ("+this.x[khi]+")" );
	    	}
	    	else{
	        	a=(this.x[khi]-xx)/h;
	        	b=(xx-this.x[klo])/h;
	        	yy=a*this.y[klo]+b*this.y[khi]+((a*a*a-a)*this.d2ydx2[klo]+(b*b*b-b)*this.d2ydx2[khi])*(h*h)/6.0;
	    	}
	    	return yy;
    	}
    	
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
            System.out.println("knownX = "+Arrays.toString(x));
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