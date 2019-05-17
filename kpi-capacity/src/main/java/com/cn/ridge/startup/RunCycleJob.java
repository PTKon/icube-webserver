package com.cn.ridge.startup;

import com.cn.ridge.bean.EsPropertiesBean;
import com.cn.ridge.enums.EsSetting;
import com.cn.ridge.es.EsClient;
import com.cn.ridge.es.EsInitBean;
import com.cn.ridge.job.KpiCorrelationJob;
import com.cn.ridge.service.EsService;
import com.cn.ridge.service.QuartzService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 用于在项目启动的时候，启动已经存在的周期任务
 * Author: create by wang.gf
 * Date: create at 2019/1/16
 */
public class RunCycleJob {
    private static final Logger logger = LogManager.getLogger(RunCycleJob.class);
    private final EsService esService;
    private final QuartzService quartzService;

    /**
     * 启动项目，启动定时
     */
    @PostConstruct
    public void startExistedCycleJob() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return esService.use2Startup(null,
                        new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client),
                        EsSetting.RESTART_CAPACITY_JOB_NEED_FIELD.getKey().split(","));
            } catch (IOException e) {
                logger.error("启动项目，启动周期定时任务，查询es发生异常！！！，", e.getMessage());
                return null;
            }
        }).thenAccept(list -> {
            if (Objects.nonNull(list)) {
                try {
                    quartzService.newCapacityJobs(list, KpiCorrelationJob.class,
                            new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                } catch (Exception e) {
                    logger.error("启动项目，启动周期定时任务，Quartz发生异常！！！", e.getMessage());
                }
            }
        }).join();

    }

    @Autowired
    public RunCycleJob(EsService esService, QuartzService quartzService) {
        this.esService = esService;
        this.quartzService = quartzService;
    }
}
