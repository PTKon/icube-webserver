package com.cn.ridge.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.ridge.bean.CorrelationWebBean;
import com.cn.ridge.bean.EsPropertiesBean;
import com.cn.ridge.bean.KpiCapacityJobBean;
import com.cn.ridge.enums.EsSetting;
import com.cn.ridge.feign.CapacitySparkWeb;
import com.cn.ridge.service.EsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/24
 */
public class KpiCapacityJob implements Job {
    //log
    private static final Logger log = LogManager.getLogger(KpiCapacityJob.class);
    @Autowired
    private EsService esService;
    @Autowired
    private CapacitySparkWeb capacitySparkWeb;

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
            //容量任务
            KpiCapacityJobBean jobBean = JSON.parseObject(json, KpiCapacityJobBean.class);
            String hJobId = UUID.randomUUID().toString();
            Integer cycleCount = jobBean.getCycleCount();
            Assert.isTrue(Objects.nonNull(cycleCount) && cycleCount > 0, "cycleCount不能是null且必须大于0！！");
            //现在时间
            LocalDateTime localDateTime = LocalDateTime.now();
            //format
            String jobStartTime = localDateTime.format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss"));
            //id map
            Map<String, String> docIdMap = new HashMap<>(2);
            docIdMap.put("createDocId", hJobId);
            docIdMap.put("updateDocId", jobId);
            Integer cycleType = jobBean.getCycleType();
            Assert.notNull(cycleType, "cycleType不能是null！！");
            switch (cycleType) {
                case 5://小时周期
                    jobBean.setHistoryDataStartTime(localDateTime.minusHours(cycleCount).format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                    break;
                case 10://天周期
                    jobBean.setHistoryDataStartTime(localDateTime.minusDays(cycleCount).format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                    break;
                case 20://星期周期
                    jobBean.setHistoryDataStartTime(localDateTime.minusWeeks(cycleCount).format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                    break;
                case 30://月周期
                    jobBean.setHistoryDataStartTime(localDateTime.minusMonths(cycleCount).format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                    break;
                case 40://年周期
                    jobBean.setHistoryDataStartTime(localDateTime.minusYears(cycleCount).format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
                    break;
                default:
                    break;
            }
            //set create modify
            jobBean.setCreateDate(jobStartTime);
            jobBean.setModifyDate(jobStartTime);

            jobBean.setHistoryDataEndTime(jobStartTime);
            jobBean.setHistoryJobId(hJobId);
            jobBean.setHistoryJobName(jobBean.getJobNameKeyWord()
                    + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd-HH-mm")) + "]");
            jobBean.setHistoryStartTime(jobStartTime);
            jobBean.setType(EsSetting.JOB_HISTORY.getKey());//type
            jobBean.setBelongedTo(jobBean.getJobId());//belong to
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("p2", 10);
            map.put("p1", jobStartTime);
            //save
            try {
                esService.bulkService(JSONObject.toJSONString(jobBean), docIdMap, client,
                        "ctx._source.executionTime = params.p1;ctx._source.executionStatus = params.p2", map);
                //通知web spark
                capacitySparkWeb.call(new CorrelationWebBean(hJobId));
            } catch (Exception e) {
                log.error("更新job发生错误！！！", e);
            }
        } else {
            log.error("通过id[" + jobId + "]，查询es，并没有获得任何数据！！！");
        }
    }
}
