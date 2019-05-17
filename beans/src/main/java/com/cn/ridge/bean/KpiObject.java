package com.cn.ridge.bean;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public class KpiObject {
    private Long kpiId;
    private String kpiName;
    private Integer kpiFrequency;//kpi取值频率
    private String unitOfTime;//kpi的取值频率单位：s、m
    private String kpiValueScope;//kpi的值的取值范围
    private String kpiLabel;

    public String getKpiLabel() {
        return kpiLabel;
    }

    public void setKpiLabel(String kpiLabel) {
        this.kpiLabel = kpiLabel;
    }

    public String getKpiValueScope() {
        return kpiValueScope;
    }

    public void setKpiValueScope(String kpiValueScope) {
        this.kpiValueScope = kpiValueScope;
    }

    public Long getKpiId() {
        return kpiId;
    }

    public void setKpiId(Long kpiId) {
        this.kpiId = kpiId;
    }

    public String getKpiName() {
        return kpiName;
    }

    public void setKpiName(String kpiName) {
        this.kpiName = kpiName;
    }

    public Integer getKpiFrequency() {
        return kpiFrequency;
    }

    public void setKpiFrequency(Integer kpiFrequency) {
        this.kpiFrequency = kpiFrequency;
    }

    public String getUnitOfTime() {
        return unitOfTime;
    }

    public void setUnitOfTime(String unitOfTime) {
        this.unitOfTime = unitOfTime;
    }
}
