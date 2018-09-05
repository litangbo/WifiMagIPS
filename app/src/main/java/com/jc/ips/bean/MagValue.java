package com.jc.ips.bean;

import org.litepal.crud.DataSupport;

public class MagValue extends DataSupport{
    private String time;
    private double value;

    @Override
    public String toString() {
        return time+"    "+value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
