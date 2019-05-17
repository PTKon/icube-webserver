package com.cn.ridge.bean;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/28
 */
public class ForExternalPageBean {

    private Integer from;//从多少条开始，Int，这里你默认写死为0，不能空
    private Integer pageSize;//因为你不可能分页请求，但是es最高接受1000万条，要请求多少条合理你要自己判断。Int，不能空
    private String orderField;//排序字段，String，不能空；
    private Integer orderType;//排序方式，int，不能空；1;//正序；0倒序
    private Long ciId;//ci的唯一值，Long，不可空；
    private Long kpiCode;//kpi的唯一值，Long，不可空；
    private String startAt;//开始时间，String，不可空；
    private String endAt;//结束时间，String，不可空；
    private String Instance;//String，可空
    private String includeField;//固定返回那些字段

    public String getIncludeField() {
        return includeField;
    }

    public void setIncludeField(String includeField) {
        this.includeField = includeField;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Long getCiId() {
        return ciId;
    }

    public void setCiId(Long ciId) {
        this.ciId = ciId;
    }

    public Long getKpiCode() {
        return kpiCode;
    }

    public void setKpiCode(Long kpiCode) {
        this.kpiCode = kpiCode;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getInstance() {
        return Instance;
    }

    public void setInstance(String instance) {
        Instance = instance;
    }
}
