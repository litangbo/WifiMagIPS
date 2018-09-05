package com.jc.test;

import org.json.JSONArray;
import org.litepal.crud.DataSupport;

import java.util.List;

public class ListBean extends DataSupport{
    private int id;
    private String name;
    private List<String> strList;
    // private List<Double> numList;
    private List<DoubleBean> numList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getStrList() {
        return strList;
    }

    public void setStrList(List<String> strList) {
        this.strList = strList;
    }

    public List<DoubleBean> getNumList() {
        return numList;
    }

    public void setNumList(List<DoubleBean> numList) {
        this.numList = numList;
    }
}
