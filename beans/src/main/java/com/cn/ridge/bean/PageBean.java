package com.cn.ridge.bean;

import com.cn.ridge.enums.EsSetting;

import java.util.Map;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public class PageBean {
    //分页开始页，包含
    private Integer from;
    //每页条数
    private Integer pageSize;
    //排序字段
    private String orderField;
    //排序方式：1:正序，0：倒序
    private Integer orderType;
    //任务类型：10:单次，20:周期
    private Integer jobType;
    //是否启动,1:启动，0：停止
    private Integer enabled;
    //任务名称
    private String jobName;
    //ci名称
    private String ciName;
    //kpi名称
    private String kpiName;
    //开始时间，包含
    private String startAt;
    //结束时间，包含
    private String endAt;
    //job id
    private String jobId;
    //history job id
    private String historyJobId;
    //这个是用来给报告多字段模糊检索用的。
    private String searchName;
    //instance
    private String instance;
    //kpi的code
    private Long kpiCode;
    //ci的id
    private Long ciId;
    //索引的一部分，用于以后索引扩展，比如月数据更换索引，季度数据更换索引，多个部分索引使用“,”分割
    private String partOfIndexes;
    //查询中，需要的字段，可以将对象中，需要查询的字段，使用“,”分割，放入这个字段用，用于减少查询返回的字段数量
    private String includeField;
    //es setting对象
    private Map<String, EsSetting> settingMap;

    public Map<String, EsSetting> getSettingMap() {
        return settingMap;
    }

    public void setSettingMap(Map<String, EsSetting> settingMap) {
        this.settingMap = settingMap;
    }

    public String getIncludeField() {
        return includeField;
    }

    public void setIncludeField(String includeField) {
        this.includeField = includeField;
    }

    public Long getKpiCode() {
        return kpiCode;
    }

    public void setKpiCode(Long kpiCode) {
        this.kpiCode = kpiCode;
    }

    public Long getCiId() {
        return ciId;
    }

    public void setCiId(Long ciId) {
        this.ciId = ciId;
    }

    public String getPartOfIndexes() {
        return partOfIndexes;
    }

    public void setPartOfIndexes(String partOfIndexes) {
        this.partOfIndexes = partOfIndexes;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getHistoryJobId() {
        return historyJobId;
    }

    public void setHistoryJobId(String historyJobId) {
        this.historyJobId = historyJobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public PageBean() {
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

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCiName() {
        return ciName;
    }

    public void setCiName(String ciName) {
        this.ciName = ciName;
    }

    public String getKpiName() {
        return kpiName;
    }

    public void setKpiName(String kpiName) {
        this.kpiName = kpiName;
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
}
