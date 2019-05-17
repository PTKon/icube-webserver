package com.cn.ridge.bean;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public class CorrelationResult {
    private String type;//数据类型
    private String belongedTo;//所属

    private String resultId;//结果id
    private String mainCiName;//关键资源ci
    private String mainKpiName;//关键资源kpi
    private String mainCiId;//关键资源ci id
    private String mainKpiId;//关键资源 kpi id
    private String mainCiLabel;//主要ci显示名称
    private String mainKpiLabel;//主要kpi显示名称
    private String mainInstanceId;//关键资源instance
    private Long mainSampleCount;//关键资源样本数量
    private String subCiName;//相关资源ci
    private String subKpiName;//相关资源kpi
    private String subCiId;//相关资源ci id
    private String subKpiId;//相关资源kpi id
    private String subCiLabel;//相关ci显示名称
    private String subKpiLabel;//相关Kpi显示名称
    private String subInstanceId;//相关资源 instance
    private Long subSampleCount;//相关资源样本数量
    private Float result;//相关性计算结果，保留4位小数
    private Float absoluteResult;//结果的绝对值

    public String getMainCiLabel() {
        return mainCiLabel;
    }

    public void setMainCiLabel(String mainCiLabel) {
        this.mainCiLabel = mainCiLabel;
    }

    public String getMainKpiLabel() {
        return mainKpiLabel;
    }

    public void setMainKpiLabel(String mainKpiLabel) {
        this.mainKpiLabel = mainKpiLabel;
    }

    public String getSubCiLabel() {
        return subCiLabel;
    }

    public void setSubCiLabel(String subCiLabel) {
        this.subCiLabel = subCiLabel;
    }

    public String getSubKpiLabel() {
        return subKpiLabel;
    }

    public void setSubKpiLabel(String subKpiLabel) {
        this.subKpiLabel = subKpiLabel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBelongedTo() {
        return belongedTo;
    }

    public void setBelongedTo(String belongedTo) {
        this.belongedTo = belongedTo;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getMainCiName() {
        return mainCiName;
    }

    public void setMainCiName(String mainCiName) {
        this.mainCiName = mainCiName;
    }

    public String getMainKpiName() {
        return mainKpiName;
    }

    public void setMainKpiName(String mainKpiName) {
        this.mainKpiName = mainKpiName;
    }

    public String getMainCiId() {
        return mainCiId;
    }

    public void setMainCiId(String mainCiId) {
        this.mainCiId = mainCiId;
    }

    public String getMainKpiId() {
        return mainKpiId;
    }

    public void setMainKpiId(String mainKpiId) {
        this.mainKpiId = mainKpiId;
    }

    public String getMainInstanceId() {
        return mainInstanceId;
    }

    public void setMainInstanceId(String mainInstanceId) {
        this.mainInstanceId = mainInstanceId;
    }

    public Long getMainSampleCount() {
        return mainSampleCount;
    }

    public void setMainSampleCount(Long mainSampleCount) {
        this.mainSampleCount = mainSampleCount;
    }

    public String getSubCiName() {
        return subCiName;
    }

    public void setSubCiName(String subCiName) {
        this.subCiName = subCiName;
    }

    public String getSubKpiName() {
        return subKpiName;
    }

    public void setSubKpiName(String subKpiName) {
        this.subKpiName = subKpiName;
    }

    public String getSubCiId() {
        return subCiId;
    }

    public void setSubCiId(String subCiId) {
        this.subCiId = subCiId;
    }

    public String getSubKpiId() {
        return subKpiId;
    }

    public void setSubKpiId(String subKpiId) {
        this.subKpiId = subKpiId;
    }

    public String getSubInstanceId() {
        return subInstanceId;
    }

    public void setSubInstanceId(String subInstanceId) {
        this.subInstanceId = subInstanceId;
    }

    public Long getSubSampleCount() {
        return subSampleCount;
    }

    public void setSubSampleCount(Long subSampleCount) {
        this.subSampleCount = subSampleCount;
    }

    public Float getResult() {
        return result;
    }

    public void setResult(Float result) {
        this.result = result;
    }

    public Float getAbsoluteResult() {
        return absoluteResult;
    }

    public void setAbsoluteResult(Float absoluteResult) {
        this.absoluteResult = absoluteResult;
    }
}
