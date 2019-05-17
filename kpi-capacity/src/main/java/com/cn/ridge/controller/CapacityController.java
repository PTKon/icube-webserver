package com.cn.ridge.controller;

import com.alibaba.fastjson.JSONObject;
import com.cn.ridge.base.Form;
import com.cn.ridge.bean.*;
import com.cn.ridge.enums.EsSetting;
import com.cn.ridge.enums.ReturnCode;
import com.cn.ridge.es.BaseDataEsClient;
import com.cn.ridge.es.BaseDataEsInitBean;
import com.cn.ridge.es.EsClient;
import com.cn.ridge.es.EsInitBean;
import com.cn.ridge.job.KpiCapacityJob;
import com.cn.ridge.service.EsService;
import com.cn.ridge.service.ForExternalEsService;
import com.cn.ridge.service.QuartzService;
import com.cn.ridge.view.CapacityReportExcelView;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Author: create by wang.gf
 * Date: create at 2018/12/24
 */
@RestController
public class CapacityController {

    private static final Logger logger = LogManager.getLogger(CapacityController.class);
    private final EsService esService;
    private final QuartzService quartzService;
    private final ForExternalEsService forExternalEsService;

    @GetMapping(value = "/kpiIds")
    public Form<String> getAllCi$KpiId() {
        String s;
        try {
            s = esService.getAllJobCi$KpiId(new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (IOException e) {
            logger.error("查询所有的kpiId发生异常！异常是：" + e.getMessage(), e);
            return new Form<>(ReturnCode.error, "查询所有的kpiId发生异常！");
        }
        if (Objects.isNull(s)) {
            return new Form<>(ReturnCode.notFound, "查询所有的kpiId，但是没有检索到任何数据！");
        } else {
            return new Form<>(ReturnCode.success, "成功检索数据！", s);
        }
    }

    /**
     * 为容量做线图展示提供数据
     *
     * @param pageBean 查询对象
     * @return 保存了基础数据以及容量的预测，置信区间上下界数据的对象
     */
    @GetMapping(value = "/toLine")
    public Form<UnionSource> getCapacityLineData(PageBean pageBean) {
        UnionSource unionSource;
        Map<String, EsPropertiesBean> map = new HashMap<>(2);
        map.put("capacity", new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        map.put("base", new EsPropertiesBean(BaseDataEsInitBean.getIndex(), BaseDataEsInitBean.getType(), BaseDataEsClient.client));
        try {
            unionSource = esService.getUnionSource(pageBean, map);
        } catch (Exception e) {
            logger.error("查询基础数据和容量预警数据发生异常！！", e);
            return new Form<>(ReturnCode.error, "查询基础数据和容量预警数据发生异常！！");
        }
        if (Objects.isNull(unionSource)) {
            logger.warn("通过条件没有查询到任何数据！！");
            return new Form<>(ReturnCode.notFound, "通过条件没有查询到任何数据！！");
        } else {
            return new Form<>(ReturnCode.success, "成功的检索到了数据！", unionSource);
        }
    }

    /**
     * 为vic展示线图提供数据
     *
     * @param pageBean 查询条件对象
     * @return 容量预测数据
     */
    @GetMapping(value = "/results")
    public Form<JSONObject> getCapacityResult(ForExternalPageBean pageBean) {
        JSONObject result;
        try {
            result = forExternalEsService.getCapacitySource(pageBean, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("给vic端提供的查询接口发生异常！！", e);
            return new Form<>(ReturnCode.error, "查询发生异常!");
        }
        if (Objects.isNull(result) || "{}".equals(result.toJSONString())) {
            logger.warn("通过给到的条件，没有获得任何数据！！");
            return new Form<>(ReturnCode.notFound, "通过给到的条件，没有获得任何数据！！");
        } else {
            return new Form<>(ReturnCode.success, "成功查询到了数据！", result);
        }
    }

    /**
     * 导出相关性报告excel
     *
     * @param bean 分页对象
     * @return ModelAndView
     */
    @GetMapping(value = "/excel")
    public ModelAndView toExcel(PageBean bean) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, EsSetting> settingMap = new HashMap<>(2);
        settingMap.put("fuzzy", EsSetting.CAPACITY_REPORT_SEARCH_FIELDS);
        settingMap.put("type", EsSetting.CAPACITY_REPORT);
        //模糊检索字段
        bean.setSettingMap(settingMap);
        //控制返回字段
        bean.setIncludeField(EsSetting.CAPACITY_VIC_NEED_FIELD.getKey());
        try {
            mapList = esService.getExcelData(bean, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("导出excel发生异常！！！", e);
        }
        return new ModelAndView(new CapacityReportExcelView(), Collections.singletonMap("mapList", mapList));
    }

    /**
     * 删除多个任务
     *
     * @param docIds 主键数组
     * @return Form
     */
    @DeleteMapping(value = "/jobs/{id}")
    public Form deleteJobs(@PathVariable("id") String[] docIds) {
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
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) +
                "]，调用了删除了多个任务！")).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功的删除了多个任务！");
        }).join();
        return forms[0];
    }

    /**
     * 分页检索
     *
     * @param bean 分页对象
     * @return Form
     */
    @GetMapping(value = "/jobs")
    public Form<Use2ShowPageData> getJobs(PageBean bean) {
        //set 模糊查询字段
        Map<String, EsSetting> settingMap = new HashMap<>(2);
        settingMap.put("fuzzy", EsSetting.CAPACITY_REPORT_SEARCH_FIELDS);
        settingMap.put("type", EsSetting.CAPACITY_REPORT);
        //模糊检索字段
        bean.setSettingMap(settingMap);
        Use2ShowPageData jsonObject;
        try {
            jsonObject = esService.searchByCondition(bean,
                    new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
        } catch (Exception e) {
            logger.error("容量-根据条件分页获取数据发生错误！！", e);
            return new Form<>(ReturnCode.error, "容量-根据条件分页获取数据发生错误！！");
        }
        if (Objects.isNull(jsonObject)) {
            return new Form<>(ReturnCode.notFound, "通过查询条件，没有获得任何数据！！");
        } else {
            return new Form<>(ReturnCode.success, "成功获得数据！", jsonObject);
        }
    }

    /**
     * 新建
     *
     * @param bean 容量对象
     * @return Form
     */
    @PostMapping(value = "/job")
    public Form create(@RequestBody KpiCapacityJobBean bean) {
        Integer enabled = bean.getEnabled();
        Assert.isTrue(Objects.nonNull(enabled) && (1 == enabled || 0 == enabled),
                "enabled不能是null，且只能是0,1！");
        String docId = UUID.randomUUID().toString();
        bean.setJobId(docId);
        bean.setType(EsSetting.JOB.getKey());
        bean.setModified(false);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss"));
        bean.setCreateDate(date);
        bean.setModifyDate(date);

        final Form[] forms = new Form[1];
        CompletableFuture.runAsync(() -> {
            try {
                esService.insertOrUpdateData(JSONObject.toJSONString(bean), docId,
                        new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                forms[0] = new Form(ReturnCode.error, "新建容量任务，向es中插入数据失败！！");
                logger.error("新建容量任务，向es中插入数据失败！！", e);
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            if (1 == enabled) {
                QuartzJobBean jobBean = new QuartzJobBean();
                BeanUtils.copyProperties(bean, jobBean);
                try {
                    quartzService.newCapacityJob(jobBean, KpiCapacityJob.class, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                } catch (SchedulerException e) {
                    forms[0] = new Form(ReturnCode.error, "新建容量任务，定时发生错误！！");
                    logger.error("新建容量任务，定时发生错误！！", e);
                }
            }
        }), () -> logger.info("在时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，创建了任务！！")).whenComplete((i, e) -> {
            if (Objects.nonNull(e)) {
                try {
                    quartzService.deleteJobs(new String[]{docId});
                    esService.deleteDoc(docId, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                } catch (Exception e1) {
                    logger.error("删除定时、任务数据发生错误！！", e1);
                }
                logger.warn("因为发生了错误，在[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]，删除了任务！！");
            } else {
                forms[0] = new Form(ReturnCode.success, "成功的新建了任务！");
            }
        }).join();
        return forms[0];
    }

    /**
     * 更新
     *
     * @param bean 容量对象
     * @return Form
     */
    @PutMapping(value = "/job")
    public Form update(@RequestBody KpiCapacityJobBean bean) {
        Integer enabled = bean.getEnabled();
        Assert.isTrue(Objects.nonNull(enabled) && (1 == enabled || 0 == enabled),
                "enabled不能是null，且只能是0,1！");
        String jobId = bean.getJobId();
        final Form[] forms = new Form[1];
        CompletableFuture.runAsync(() -> {
            try {
                if (bean.isModified() && Integer.valueOf(1).equals(enabled)) {
                    quartzService.deleteJobs(new String[]{jobId});
                    QuartzJobBean jobBean = new QuartzJobBean();
                    BeanUtils.copyProperties(bean, jobBean);
                    quartzService.newCapacityJob(jobBean, KpiCapacityJob.class, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
                } else if (Integer.valueOf(0).equals(enabled)) {
                    quartzService.deleteJobs(new String[]{jobId});
                }
            } catch (SchedulerException e) {
                forms[0] = new Form(ReturnCode.error, "更新容量预测，quartz发生异常！！");
                logger.error("更新容量预测，quartz发生异常！！", e);
            }
        }).runAfterBoth(CompletableFuture.runAsync(() -> {
            bean.setModified(false);
            bean.setModifyDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")));
            try {
                esService.insertOrUpdateData(JSONObject.toJSONString(bean), jobId, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                forms[0] = new Form(ReturnCode.error, "更新容量预测，Es发生异常！！");
                logger.error("更新容量预测，Es发生异常！！", e);
            }
        }), () -> logger.info("在[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) + "]更新容量预测")).whenComplete((i, e) -> {
            if (Objects.isNull(e)) {
                forms[0] = new Form(ReturnCode.success, "成功的更新了容量预测！！");
            }
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
                        Collections.singletonMap("param1", 0), docIds,
                        new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
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
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) +
                "]，调用了暂停了多个任务！")).whenComplete((i, e) -> {
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
                        Collections.singletonMap("param1", 1), docIds,
                        new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
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
                        1, EsSetting.RESTART_CAPACITY_JOB_NEED_FIELD.getKey().split(","));
                quartzService.newCapacityJobs(mapList, KpiCapacityJob.class, new EsPropertiesBean(EsInitBean.getIndex(), EsInitBean.getType(), EsClient.client));
            } catch (Exception e) {
                logger.error("启动多个任务，发生错误！！", e);
                forms[0] = new Form(ReturnCode.error, "启动多个任务，发生错误！！");
            }
        }), () -> logger.info("时间[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss")) +
                "]，调用了启动了多个任务！")).whenComplete((i, e) -> {
            if (Objects.isNull(e))
                forms[0] = new Form(ReturnCode.success, "成功的启动了多个任务！");
        }).join();
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
    public CapacityController(EsService esService, QuartzService quartzService, ForExternalEsService forExternalEsService) {
        this.esService = esService;
        this.quartzService = quartzService;
        this.forExternalEsService = forExternalEsService;
    }
}
