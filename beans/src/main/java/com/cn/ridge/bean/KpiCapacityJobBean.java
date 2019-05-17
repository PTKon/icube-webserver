package com.cn.ridge.bean;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public class KpiCapacityJobBean {
    private String jobId;//任务id
    private String type;//数据类型
    private String jobNameText;//用于模糊检索
    private String jobNameKeyWord;//用于精准检索
    private Integer enabled;//是否启动
    private String createDate;//创建时间
    private String modifyDate;//修改时间
    private Integer cycleType;//周期类型
    private String executionTime;//执行时间
    private Integer executionStatus;//15：已完成；10：执行中；5：未执行。
    private Cis cis;//ci集合
    private boolean modified;//更改了任务周期类型、任务触发时间
    private Integer predictionCycle;//预测周期；比如周期类型是天、而预测周期可以选小时、天这2种情况，这个字段主要是为了用户定义的时候，让用户更加清晰。
    private Integer cycleCount;//周期数；向前推多少个周期，比如周期是天，这里填3，即以执行时间开始，向前推3天。
    private Integer minute;
    private Integer hour;
    private Integer day;
    private Integer week;
    private Integer month;
    private Integer year;
    private String belongedTo;//所属
    private String algorithmType;//算法类型
    private String algorithm;//算法
    private String confidenceInterval;//置信区间
    private Integer computationAccuracy;//计算精度

    private Integer historyExecutionStatus;//0：失败，1：成功。
    private String historyStartTime;//历史任务开始时间，等同于执行时间executionTime
    private String historyEndTime;//历史任务结束时间
    private Long historyTotalSecond;//总消耗秒数
    private String historyDataStartTime;//数据开始时间
    private String historyDataEndTime;//数据结束时间
    private String historyJobId;//历史任务id
    private String historyJobName;//历史任务名称

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJobNameText() {
        return jobNameText;
    }

    public void setJobNameText(String jobNameText) {
        this.jobNameText = jobNameText;
    }

    public String getJobNameKeyWord() {
        return jobNameKeyWord;
    }

    public void setJobNameKeyWord(String jobNameKeyWord) {
        this.jobNameKeyWord = jobNameKeyWord;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Integer getCycleType() {
        return cycleType;
    }

    public void setCycleType(Integer cycleType) {
        this.cycleType = cycleType;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(Integer executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Cis getCis() {
        return cis;
    }

    public void setCis(Cis cis) {
        this.cis = cis;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Integer getPredictionCycle() {
        return predictionCycle;
    }

    public void setPredictionCycle(Integer predictionCycle) {
        this.predictionCycle = predictionCycle;
    }

    public Integer getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(Integer cycleCount) {
        this.cycleCount = cycleCount;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getBelongedTo() {
        return belongedTo;
    }

    public void setBelongedTo(String belongedTo) {
        this.belongedTo = belongedTo;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getConfidenceInterval() {
        return confidenceInterval;
    }

    public void setConfidenceInterval(String confidenceInterval) {
        this.confidenceInterval = confidenceInterval;
    }

    public Integer getComputationAccuracy() {
        return computationAccuracy;
    }

    public void setComputationAccuracy(Integer computationAccuracy) {
        this.computationAccuracy = computationAccuracy;
    }

    public Integer getHistoryExecutionStatus() {
        return historyExecutionStatus;
    }

    public void setHistoryExecutionStatus(Integer historyExecutionStatus) {
        this.historyExecutionStatus = historyExecutionStatus;
    }

    public String getHistoryStartTime() {
        return historyStartTime;
    }

    public void setHistoryStartTime(String historyStartTime) {
        this.historyStartTime = historyStartTime;
    }

    public String getHistoryEndTime() {
        return historyEndTime;
    }

    public void setHistoryEndTime(String historyEndTime) {
        this.historyEndTime = historyEndTime;
    }

    public Long getHistoryTotalSecond() {
        return historyTotalSecond;
    }

    public void setHistoryTotalSecond(Long historyTotalSecond) {
        this.historyTotalSecond = historyTotalSecond;
    }

    public String getHistoryDataStartTime() {
        return historyDataStartTime;
    }

    public void setHistoryDataStartTime(String historyDataStartTime) {
        this.historyDataStartTime = historyDataStartTime;
    }

    public String getHistoryDataEndTime() {
        return historyDataEndTime;
    }

    public void setHistoryDataEndTime(String historyDataEndTime) {
        this.historyDataEndTime = historyDataEndTime;
    }

    public String getHistoryJobId() {
        return historyJobId;
    }

    public void setHistoryJobId(String historyJobId) {
        this.historyJobId = historyJobId;
    }

    public String getHistoryJobName() {
        return historyJobName;
    }

    public void setHistoryJobName(String historyJobName) {
        this.historyJobName = historyJobName;
    }
}
