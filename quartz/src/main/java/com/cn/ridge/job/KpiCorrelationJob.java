package com.cn.ridge.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.ridge.bean.CorrelationWebBean;
import com.cn.ridge.bean.EsPropertiesBean;
import com.cn.ridge.bean.KpiCorrelationJobBean;
import com.cn.ridge.enums.EsSetting;
import com.cn.ridge.enums.QuartzValue;
import com.cn.ridge.feign.CorrelationSparkWeb;
import com.cn.ridge.service.EsService;
import com.cn.ridge.util.LocalDateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
@Component
public class KpiCorrelationJob implements Job {
    //log
    private static final Logger log = LogManager.getLogger(KpiCorrelationJob.class);

    @Autowired
    private EsService esService;
    @Autowired
    private CorrelationSparkWeb correlationSparkWeb;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobId = context.getMergedJobDataMap().getString("jobId");
        EsPropertiesBean client = (EsPropertiesBean) context.getMergedJobDataMap().get("client");
        Assert.hasText(jobId, "jobId必须有值！！！！");
        Assert.notNull(client, "client必须有值！！！！");
        String json;
        try {
            json = esService.getDataWithId2(jobId, client);
        } catch (Exception e) {
            log.error("根据id[" + jobId + "]，在es中查询数据发生错误！！", e);
            return;
        }
        if (StringUtils.isNotBlank(json) && !"{}".equals(json)) {
            KpiCorrelationJobBean jobBean = JSON.parseObject(
                    json,
                    KpiCorrelationJobBean.class);
            String hJobId = UUID.randomUUID().toString();
            Assert.hasText(jobBean.getDataEndAt(), "数据结束时间不能是null/''！！");
            Assert.hasText(jobBean.getDataStartAt(), "数据开始时间不能是null/''！！");

            String jobStartTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss"));
            //set create modify
            jobBean.setCreateDate(jobStartTime);
            jobBean.setModifyDate(jobStartTime);

            LocalDate localDate;//日期
            LocalTime startTime;//开始时间
            LocalTime endTime;//结束时间
            LocalDateTime startDateTime;//开始日期+时间
            LocalDateTime endDateTime;//结束日期+时间
            //id map
            Map<String, String> docIdMap = new HashMap<>(2);
            docIdMap.put("createDocId", hJobId);
            docIdMap.put("updateDocId", jobId);
            //实际数据结束日期
            int endDay;
            //实际数据开始日期
            int startDay;
            //月份
            Month month;
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("p2", 10);
            //10为单次任务，20为周期任务
            switch (jobBean.getJobType()) {
                case 10:
                    jobBean.setHistoryDataEndTime(jobBean.getDataEndAt());
                    jobBean.setHistoryDataStartTime(jobBean.getDataStartAt());
                    jobBean.setHistoryJobId(hJobId);
                    jobBean.setHistoryJobName(jobBean.getJobNameKeyWord()
                            + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd-HH-mm")) + "]");
                    jobBean.setHistoryStartTime(jobStartTime);
                    jobBean.setType(EsSetting.JOB_HISTORY.getKey());//type
                    jobBean.setBelongedTo(jobBean.getJobId());//belong to
                    //save
                    operateESAndCallWebServer(client, jobBean, hJobId, jobStartTime, docIdMap, map);
                    break;
                case 20:
                    //任务数据开始
                    Integer[] start = Arrays.stream(jobBean.getDataStartAt().split("-"))
                            .map(Integer::valueOf)
                            .collect(Collectors.toList())
                            .toArray(new Integer[]{});
                    //任务数据结束
                    Integer[] end = Arrays.stream(jobBean.getDataEndAt().split("-"))
                            .map(Integer::valueOf)
                            .collect(Collectors.toList())
                            .toArray(new Integer[]{});
                    switch (jobBean.getCycleType()) {
                        case 10://天周期
                            Assert.isTrue(start.length == 2 && end.length == 2,
                                    "天为周期的时候，任务数据的开始结束时间，必须是[HH-mm]");
                            //转换成具体时间
                            startTime = LocalTime.of(start[0], start[1], 0);
                            endTime = LocalTime.of(end[0], end[1], 0);
                            if (jobBean.isThisCycle()) {
                                //本天
                                localDate = LocalDate.now();
                            } else {
                                //前一天
                                localDate = LocalDate.now().minusDays(1);
                            }
                            startDateTime = LocalDateTime.of(localDate, startTime);
                            endDateTime = LocalDateTime.of(localDate, endTime);
                            jobBean.setHistoryDataEndTime(endDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryDataStartTime(startDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryJobId(hJobId);
                            jobBean.setHistoryJobName(jobBean.getJobNameKeyWord()
                                    + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd-HH-mm")) + "]");
                            jobBean.setHistoryStartTime(jobStartTime);
                            jobBean.setType(EsSetting.JOB_HISTORY.getKey());//type
                            jobBean.setBelongedTo(jobBean.getJobId());//belong to
                            //save
                            operateESAndCallWebServer(client, jobBean, hJobId, jobStartTime, docIdMap, map);
                            break;
                        case 20://周周期
                            Assert.isTrue(start.length == 3 && end.length == 3,
                                    "周为周期的时候，任务数据的开始结束时间，必须是[week-HH-mm]");
                            startTime = LocalTime.of(start[1], start[2], 0);
                            endTime = LocalTime.of(end[1], end[2], 0);
                            if (jobBean.isThisCycle()) {
                                //本周
                                localDate = LocalDate.now();
                            } else {
                                //前一周
                                localDate = LocalDate.now().minusWeeks(1);
                            }
                            startDateTime = LocalDateTime.of(LocalDateTimeUtil.week2Date(localDate, start[0]), startTime);
                            endDateTime = LocalDateTime.of(LocalDateTimeUtil.week2Date(localDate, end[0]), endTime);
                            jobBean.setHistoryDataEndTime(endDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryDataStartTime(startDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryJobId(hJobId);
                            jobBean.setHistoryJobName(jobBean.getJobNameKeyWord()
                                    + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd-HH-mm")) + "]");
                            jobBean.setHistoryStartTime(jobStartTime);
                            jobBean.setType(EsSetting.JOB_HISTORY.getKey());//type
                            jobBean.setBelongedTo(jobBean.getJobId());//belong to
                            //save
                            operateESAndCallWebServer(client, jobBean, hJobId, jobStartTime, docIdMap, map);
                            break;
                        case 30://月周期
                            Assert.isTrue(start.length == 3 && end.length == 3,
                                    "月为周期的时候，任务数据的开始结束时间，必须是[dd-HH-mm]");
                            if (jobBean.isThisCycle()) {//本月
                                localDate = LocalDate.now();
                            } else {//上个月
                                localDate = LocalDate.now().minusMonths(1);
                            }
                            month = localDate.getMonth();//月对象
                            if (QuartzValue.last_day.getValue().equals(jobBean.getDay())) {//最后一天
                                endDay = month.length(LocalDateTimeUtil.isLeapYear(localDate.getYear()));//最后一天
                            } else {//不是最后一天
                                endDay = end[0] > month.length(LocalDateTimeUtil.isLeapYear(localDate.getYear()))
                                        ? month.length(LocalDateTimeUtil.isLeapYear(localDate.getYear())) : end[0];
                            }
                            int thisMonth = month.getValue();//月数值
                            startDateTime = LocalDateTime.of(LocalDate.of(localDate.getYear(), thisMonth, start[0]),
                                    LocalTime.of(start[1], start[2], 0));
                            endDateTime = LocalDateTime.of(LocalDate.of(localDate.getYear(), thisMonth, endDay),
                                    LocalTime.of(end[1], end[2], 0));
                            jobBean.setHistoryDataEndTime(endDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryDataStartTime(startDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryJobId(hJobId);
                            jobBean.setHistoryJobName(jobBean.getJobNameKeyWord()
                                    + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd-HH-mm")) + "]");
                            jobBean.setHistoryStartTime(jobStartTime);
                            jobBean.setType(EsSetting.JOB_HISTORY.getKey());//type
                            jobBean.setBelongedTo(jobBean.getJobId());//belong to
                            //save
                            operateESAndCallWebServer(client, jobBean, hJobId, jobStartTime, docIdMap, map);
                            break;
                        case 40://年周期
                            Assert.isTrue(start.length == 4 && end.length == 4,
                                    "年为周期的时候，任务数据的开始结束时间，必须是[MM-dd-HH-mm]");
                            if (jobBean.isThisCycle()) {
                                localDate = LocalDate.now();
                            } else {
                                localDate = LocalDate.now().minusYears(1);
                            }
                            //start day
                            LocalDate startLocalDate = localDate.minusMonths(end[0] - start[0]);
                            month = startLocalDate.getMonth();
                            if (QuartzValue.last_day.getValue().equals(start[1])) {
                                startDay = month.length(startLocalDate.isLeapYear());
                            } else {
                                startDay = start[1] > month.length(startLocalDate.isLeapYear())
                                        ? month.length(startLocalDate.isLeapYear()) : start[1];
                            }
                            //end day
                            month = localDate.getMonth();
                            if (QuartzValue.last_day.getValue().equals(end[1])) {
                                endDay = month.length(localDate.isLeapYear());
                            } else {
                                endDay = end[1] > month.length(localDate.isLeapYear())
                                        ? month.length(localDate.isLeapYear()) : end[1];
                            }
                            startDateTime = LocalDateTime.of(LocalDate.of(localDate.getYear(), start[0], startDay),
                                    LocalTime.of(start[2], start[3]));
                            endDateTime = LocalDateTime.of(LocalDate.of(localDate.getYear(), end[0], endDay),
                                    LocalTime.of(end[2], end[3]));
                            jobBean.setHistoryDataEndTime(endDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryDataStartTime(startDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                            jobBean.setHistoryJobId(hJobId);
                            jobBean.setHistoryJobName(jobBean.getJobNameKeyWord()
                                    + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd-HH-mm")) + "]");
                            jobBean.setHistoryStartTime(jobStartTime);
                            jobBean.setType(EsSetting.JOB_HISTORY.getKey());//type
                            jobBean.setBelongedTo(jobBean.getJobId());//belong to
                            //save
                            operateESAndCallWebServer(client, jobBean, hJobId, jobStartTime, docIdMap, map);
                            break;
                        default:
                            log.warn("不存在的周期类型！！[" + jobBean.getCycleType() + "]");
                            break;
                    }
                    break;
                default:
                    log.warn("不存在的任务类型！！[" + jobBean.getJobType() + "]");
                    break;
            }
        } else {
            log.error("通过id[" + jobId + "]，查询es，并没有获得任何数据！！！");
        }
    }

    private void operateESAndCallWebServer(EsPropertiesBean client, KpiCorrelationJobBean jobBean, String hJobId, String jobStartTime, Map<String, String> docIdMap, HashMap<String, Object> map) {
        try {
            map.put("p1", jobStartTime);
            esService.bulkService(JSONObject.toJSONString(jobBean), docIdMap, client,
                    "ctx._source.executionTime = params.p1;ctx._source.executionStatus = params.p2", map);
            //通知web spark
            correlationSparkWeb.call(new CorrelationWebBean(hJobId));
        } catch (Exception e) {
            log.error("更新job发生错误！！！", e);
        }
    }
}
