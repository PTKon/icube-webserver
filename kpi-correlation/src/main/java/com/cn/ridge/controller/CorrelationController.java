package com.cn.ridge.controller;

import com.alibaba.fastjson.JSONObject;
import com.cn.ridge.base.Controller;
import com.cn.ridge.base.Form;
import com.cn.ridge.bean.*;
import com.cn.ridge.enums.EsSetting;
import com.cn.ridge.enums.ReturnCode;
import com.cn.ridge.es.EsClient;
import com.cn.ridge.es.EsInitBean;
import com.cn.ridge.job.KpiCorrelationJob;
import com.cn.ridge.service.EsService;
import com.cn.ridge.service.QuartzService;
import com.cn.ridge.view.CorrelationReportExcelView;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/19
 */
@RestController
public class CorrelationController extends Controller {
    private static final Logger logger = LogManager.getLogger(CorrelationController.class);

    private final EsService esService;
    private final QuartzService quartzService;

    /**
     * 导出相关性报告excel
     *
     * @param pageBean 分页对象
     * @return ModelAndView
     */
    @GetMapping(value = "/excel")
    public ModelAndView toExcel(PageBean pageBean) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, EsSetting> settingMap = new HashMap<>(2);
        settingMap.put("fuzzy", EsSetting.CORRELATION_REPORT_SEARCH_FIELDS);
        settingMap.put("type", EsSetting.CORRELATION_REPORT);
        //模糊检索字段
        pageBean.setSettingMap(settingMap);
        try {
            mapList = esService.getExcelData(pageBean, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("导出excel发生异常！！！", e);
        }
        return new ModelAndView(new CorrelationReportExcelView(), Collections.singletonMap("mapList", mapList));
    }

    /**
     * 根据检索条件，分页检索所有job/historyJob/result
     *
     * @param pageBean 分页检索条件
     * @return json
     */
    @GetMapping(value = "/jobs")
    public Form<Use2ShowPageData> getJobs(PageBean pageBean) {
        //模糊检索字段
        Map<String, EsSetting> settingMap = new HashMap<>(2);
        settingMap.put("fuzzy", EsSetting.CORRELATION_REPORT_SEARCH_FIELDS);
        settingMap.put("type", EsSetting.CORRELATION_REPORT);
        //模糊检索字段
        pageBean.setSettingMap(settingMap);
        Use2ShowPageData result;
        try {
            result = esService.searchByCondition(pageBean,
                    new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("分页检索job发生了异常！！！", e);
            return new Form<>(ReturnCode.error, "分页检索job发生了异常！");
        }
        if (Objects.isNull(result))
            return new Form<>(ReturnCode.notFound, "通过条件没有检索到任何结果！");
        else
            return new Form<>(ReturnCode.success, "成功检索出结果！", result);
    }

    /**
     * 新建job
     *
     * @param job job对象
     * @return json
     */
    @PostMapping(value = "/job")
    public Form createJob(@RequestBody KpiCorrelationJobBean job) {
        Integer enabled = job.getEnabled();
        Assert.isTrue(Objects.nonNull(enabled) && (1 == enabled || 0 == enabled),
                "enabled不能是null，且只能是0,1！");
        String jobId = UUID.randomUUID().toString();
        //job id
        job.setJobId(jobId);
        job.setType(EsSetting.JOB.getKey());
        job.setModified(false);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss"));
        job.setCreateDate(date);
        job.setModifyDate(date);
        final Form[] form = new Form[1];
        form[0] = new Form(ReturnCode.success, "成功的新建了任务！");
        CompletableFuture.runAsync(() -> {
            try {
                esService.insertOrUpdateData(JSONObject.toJSONString(job), jobId, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                form[0] = new Form(ReturnCode.error, "新建任务es发生异常！！");
                logger.error("新建任务，es发生异常！！！！", e);
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            if (1 == enabled) {
                QuartzJobBean jobBean = new QuartzJobBean();
                BeanUtils.copyProperties(job, jobBean);
                try {
                    quartzService.newCorrelationJob(jobBean, KpiCorrelationJob.class, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                } catch (SchedulerException e) {
                    form[0] = new Form(ReturnCode.error, "新建任务quartz发生异常！！");
                    logger.error("新建任务，quartz发生异常！！！！", e);
                }
            }
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，创建了任务！")).whenComplete((i, e) -> {
            if (Objects.nonNull(e)) {
                try {
                    esService.deleteDoc(jobId, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                    quartzService.deleteJobs(new String[]{jobId});
                } catch (Exception e1) {
                    logger.error("任务删除操作发生异常！！", e1);
                }
                logger.info("因为发生错误，在时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]删除了任务！");
            }
        }).join();
        return form[0];
    }

    /**
     * 更新job
     *
     * @param job 任务对象
     * @return json
     */
    @PutMapping(value = "/job")
    public Form updateJob(@RequestBody KpiCorrelationJobBean job) {
        Integer enabled = job.getEnabled();
        Assert.isTrue(Objects.nonNull(enabled) && (1 == enabled || 0 == enabled),
                "enabled不能是null，且只能是0,1！");
        String jobId = job.getJobId();
        final Form[] forms = new Form[1];
        CompletableFuture.runAsync(() -> {
            try {
                if (job.isModified() && Integer.valueOf(1).equals(enabled)) {
                    quartzService.deleteJobs(new String[]{jobId});
                    QuartzJobBean jobBean = new QuartzJobBean();
                    BeanUtils.copyProperties(job, jobBean);
                    quartzService.newCorrelationJob(jobBean, KpiCorrelationJob.class, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                } else if (Integer.valueOf(0).equals(enabled)) {
                    quartzService.deleteJobs(new String[]{jobId});
                }
            } catch (SchedulerException e) {
                logger.error("更新任务定时，quartz发生异常！！！", e);
                forms[0] = new Form(ReturnCode.error, "更新任务定时，quartz发生异常！");
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            job.setModified(false);
            job.setModifyDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
            try {
                esService.insertOrUpdateData(JSONObject.toJSONString(job), jobId, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                logger.error("更新任务，es发生异常！！！", e);
                forms[0] = new Form(ReturnCode.error, "更新任务，es发生异常！");
            }
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，更新了任务！")).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功更新了job！！");
        }).join();
        return forms[0];
    }

    /**
     * 批量删除任务
     *
     * @param docIds 任务的doc id
     * @return json
     */
    @DeleteMapping(value = "/jobs/{docIds}")
    public Form deleteJobs(@PathVariable String[] docIds) {
        final Form[] forms = new Form[1];
        CompletableFuture.runAsync(() -> {
            try {
                esService.bulkDelete(docIds,
                        new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                logger.error("删除多个任务es发生错误！", e);
                forms[0] = new Form(ReturnCode.error, "删除多个任务es发生错误！");
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            try {
                quartzService.deleteJobs(docIds);
            } catch (SchedulerException e) {
                logger.error("删除多个任务，quartz发生错误！！！", e);
                forms[0] = new Form(ReturnCode.error, "删除多个任务，quartz发生错误！！！");
            }
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，删除了多个任务！")).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功的删除了多个任务！");
        }).join();
        return forms[0];
    }

    /**
     * 暂停多个任务
     *
     * @param docIds 任务doc id
     * @return json
     */
    @PatchMapping(value = "/jobs/disable")
    public Form disableJobs(@RequestBody String[] docIds) {
        final Form[] forms = new Form[1];
        CompletableFuture.runAsync(() -> {
            try {
                esService.bulkUpdateOrAddDocField("ctx._source.enabled = params.param1",
                        Collections.singletonMap("param1", 0), docIds, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                logger.error("暂停多个任务，更新es发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "暂停多个任务，更新es发生错误！！");
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            try {
                quartzService.deleteJobs(docIds);
            } catch (SchedulerException e) {
                logger.error("暂停多个任务，quartz发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "暂停多个任务，quartz发生错误！！");
            }
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，暂停了多个任务！")).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功的暂停了多个任务！");
        }).join();
        return forms[0];
    }

    /**
     * 启动多个任务
     *
     * @param docIds 任务doc id
     * @return json
     */
    @PatchMapping(value = "/jobs/enable")
    public Form enableJobs(@RequestBody String[] docIds) {
        final Form[] forms = new Form[1];
        CompletableFuture.runAsync(() -> {
            try {
                esService.bulkUpdateOrAddDocField("ctx._source.enabled = params.param1",
                        Collections.singletonMap("param1", 1), docIds, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                logger.error("启动多个任务，更新es发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "启动多个任务，更新es发生错误！！");
            }
        }).runAfterEitherAsync(CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                //获得所有要启动的job
                List<QuartzJobBean> mapList = esService.searchByKeys(docIds,
                        new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client),
                        1, EsSetting.RESTART_CORRELATION_JOB_NEED_FIELD.getKey().split(","));
                quartzService.newCorrelationJobs(mapList, KpiCorrelationJob.class, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                logger.error("启动多个任务，发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "启动多个任务，发生错误！！");
            }
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，启动了多个任务！")).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功的启动了多个任务！");
        }).join();

        /*EsPropertiesBean esPropertiesBean = new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client);
        CompletableFuture.runAsync(() -> {
            try {
                esService.bulkUpdateOrAddDocField("ctx._source.enabled = params.param1",
                        Collections.singletonMap("param1", 1), docIds, esPropertiesBean);
            } catch (Exception e) {
                logger.error("启动多个任务，更新es发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "启动多个任务，更新es发生错误！！");
            }
        }).thenRun(() -> {
            try {
                //获得所有要启动的job
                List<QuartzJobBean> mapList = esService.searchByKeys(docIds,
                        esPropertiesBean,1, EsSetting.RESTART_CORRELATION_JOB_NEED_FIELD.getKey().split(","));
                quartzService.newCorrelationJobs(mapList, KpiCorrelationJob.class, esPropertiesBean);
            } catch (Exception e) {
                logger.error("启动多个任务，发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "启动多个任务，发生错误！！");
            }
        }).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功的启动了多个任务！");
        }).join();*/
        return forms[0];
    }

    /**
     * 检查job名字是否重复
     *
     * @param jobName 任务名称
     * @return json
     */
    @GetMapping(value = "/job/exists")
    public Form checkJobNameExists(String jobName) {
        if (StringUtils.isBlank(jobName))
            return new Form(ReturnCode.fail, "jobName不能是null、''！！");
        boolean b;
        try {
            b = esService.checkJobNameExists(jobName, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("检查名字是否存在发生异常！！", e);
            return new Form(ReturnCode.error, "检查名字是否存在发生异常！");
        }
        if (b)
            return new Form(ReturnCode.yes, "该名字可以使用！");
        else
            return new Form(ReturnCode.no, "该名字已经存在！");
    }

    /**
     * 通过doc id获得job
     *
     * @param jobId doc id
     * @return json
     */
    @GetMapping(value = "/job/{jobId}")
    public Form<Map<String, Object>> getJob(@PathVariable String jobId) {
        Map<String, Object> result;
        try {
            result = esService.getDataWithId(jobId, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("通过id查询es发生异常！！！", e);
            return new Form<>(ReturnCode.error, "通过id查询es发生异常！");
        }
        if (Objects.isNull(result))
            return new Form<>(ReturnCode.notFound, "通过id没有获得数据！");
        else
            return new Form<>(ReturnCode.success, "成功通过id获得数据！", result);
    }

    @Autowired
    public CorrelationController(EsService esService, QuartzService quartzService) {
        this.esService = esService;
        this.quartzService = quartzService;
    }
}
