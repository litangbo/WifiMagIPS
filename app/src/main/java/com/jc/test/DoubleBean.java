package com.jc.test;

import org.litepal.crud.DataSupport;

public class DoubleBean extends DataSupport {
    public DoubleBean(){

    }

    public DoubleBean(double num){
        this.num = num;
    }

    private double num;

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }
}
