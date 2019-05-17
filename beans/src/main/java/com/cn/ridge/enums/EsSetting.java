package com.cn.ridge.enums;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public enum EsSetting {
    JOB("job"),//任务数据类型
    JOB_HISTORY("jobHistory"),//任务历史数据类型
    CORRELATION_REPORT("correlationReport"),//相关性任务报告数据类型
    CORRELATION_REPORT_SEARCH_FIELDS("mainCiLabel,mainKpiLabel,subCiLabel,subKpiLabel"),//用于相关性报告迷糊检索的字段
    CAPACITY_REPORT_SEARCH_FIELDS("mainCiLabel,mainKpiLabel"),//用于容量报告迷糊检索的字段
    CAPACITY_REPORT("capacityReport"),//容量报告lowerBoundaryLine
    CAPACITY_UPPER_BOUNDARY_LINE("upperBoundaryLine"),
    CAPACITY_LOWER_BOUNDARY_LINE("lowerBoundaryLine"),
    CAPACITY_RESULT("capacityResult"),
    CAPACITY_VIC_NEED_FIELD("ciName,kpiName,ciId,kpiId,instanceId,forecastResult,forecastDateTime,type,sampleCount,kpiFrequency,unitOfTime,alpha,beta,gamma,RMSError"),//给vic接口返回的字段
    BASE_DATA_NEED_FIELD("value,kpiCode,arisingTime,instance,nodeId"),//基础数据返回的字段
    RESTART_CORRELATION_JOB_NEED_FIELD("jobId,jobType,minute,hour,day,week,month,year,cycleType"),//相关性基础数据返回的字段
    RESTART_CAPACITY_JOB_NEED_FIELD("jobId,minute,hour,day,week,month,year,cycleType"),//容量基础数据返回的字段
    JOB_INCLUDE_FIELD("jobId,jobNameKeyWord,jobType,cis.ciLabels,cis.kpiLabels,enabled,modifyDate,executionTime,executionStatus,historyExecutionStatus,historyJobId,historyDataStartTime,historyDataEndTime,historyJobName,historyStartTime,historyTotalSecond,historyExecutionStatus"),//任务、历史任务检索返回字段
    WILDCARD_STR("*%s*");

    private String key;

    EsSetting(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
