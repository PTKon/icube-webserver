package com.cn.ridge.bean;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/29
 */
public class CorrelationWebBean {
    private String historyJobId;

    public String getHistoryJobId() {
        return historyJobId;
    }

    public void setHistoryJobId(String historyJobId) {
        this.historyJobId = historyJobId;
    }

    public CorrelationWebBean() {
    }

    public CorrelationWebBean(String historyJobId) {
        this.historyJobId = historyJobId;
    }
}
