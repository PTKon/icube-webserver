package com.cn.ridge.service;

import com.cn.ridge.bean.EsPropertiesBean;
import com.cn.ridge.bean.QuartzJobBean;
import com.cn.ridge.enums.JobCorns;
import com.cn.ridge.enums.QuartzKey;
import com.cn.ridge.enums.QuartzValue;
import org.elasticsearch.common.Nullable;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
@Service("quartzService")
public class QuartzService {

    private final SchedulerFactoryBean schedulerFactoryBean;

    /**
     * 用于容量定时
     *
     * @param bean   定时相关属性的bean
     * @param clazz  实现job的对象
     * @param client es相关属性
     * @throws SchedulerException 异常
     */
    public void newCapacityJob(QuartzJobBean bean, Class<? extends Job> clazz, EsPropertiesBean client) throws SchedulerException {
        Integer cycleType = bean.getCycleType();
        String jobId = bean.getJobId();
        Integer minute = bean.getMinute();
        Assert.notNull(cycleType, "CycleType周期类型不能是null！！");
        Assert.hasLength(jobId, "任务key不能是null/\"\"！！！");
        Assert.isTrue(Objects.nonNull(minute) && minute >= 0 && minute <= 59, "周期的情况下，分钟不能是null！！");
        //scheduler
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        //job data
        Map<String, Object> keyMap = new HashMap<>(2);
        keyMap.put("jobId", jobId);
        keyMap.put("client", client);

        Trigger trigger;
        JobDetail detail;
        Integer hour;
        Integer week;
        Integer day;
        Integer month;
        //cycle type：5时、10天、20周、30月、40年
        switch (cycleType) {
            case 5://小时周期
                //trigger
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + QuartzKey.trigger_name.getKey()
                                , jobId + QuartzKey.group_name.getKey())
                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                String.format(JobCorns.hour.getCronTemplate(),
                                        minute)
                        )).build();
                //detail
                detail = JobBuilder.newJob(clazz)
                        .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                        .setJobData(new JobDataMap(keyMap))
                        .build();
                //scheduler
                scheduler.scheduleJob(detail, trigger);
                scheduler.start();
                break;
            case 10://天周期
                hour = bean.getHour();
                if (Objects.isNull(hour)) {//如果没有定义小时
                    //trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey()
                                    , jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.hour.getCronTemplate(),
                                            minute)
                            )).build();
                } else {//定义了小时
                    Assert.isTrue(hour >= 0 && hour <= 23,
                            "以月为周期的情况下，小时不能是null，且只能是[0-23]！！");
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.day.getCronTemplate(),
                                            minute,
                                            hour)
                            )).build();
                }
                //detail
                detail = JobBuilder.newJob(clazz)
                        .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                        .setJobData(new JobDataMap(keyMap))
                        .build();
                //scheduler
                scheduler.scheduleJob(detail, trigger);
                scheduler.start();
                break;
            case 20://周周期
                hour = bean.getHour();
                week = bean.getWeek();
                Assert.isTrue(Objects.nonNull(hour) && hour >= 0 && hour <= 23,
                        "以月为周期的情况下，小时不能是null，且只能是[0-23]！！");
                if (Objects.isNull(week)) {//如果没有定义周
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.day.getCronTemplate(),
                                            minute,
                                            hour)
                            )).build();
                } else {//定义了周
                    Assert.isTrue(week >= 1 && week <= 7, "week只能是[1-7]之间是数值！！");
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.week.getCronTemplate(),
                                            minute,
                                            hour,
                                            week)
                            )).build();
                }
                //detail
                detail = JobBuilder.newJob(clazz)
                        .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                        .setJobData(new JobDataMap(keyMap))
                        .build();
                //scheduler
                scheduler.scheduleJob(detail, trigger);
                scheduler.start();
                break;
            case 30://月周期
                hour = bean.getHour();
                day = null == bean.getDay() ? QuartzValue.day_is_null.getValue() : bean.getDay();
                Assert.isTrue(Objects.nonNull(hour) && hour >= 0 && hour <= 23,
                        "以月为周期的情况下，小时不能是null，且只能是[0-23]！！");
                trigger = getTrigger(jobId, minute, hour, day);
                //detail
                detail = JobBuilder.newJob(clazz)
                        .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                        .setJobData(new JobDataMap(keyMap))
                        .build();
                //scheduler
                scheduler.scheduleJob(detail, trigger);
                scheduler.start();
                break;
            case 40://年周期
                hour = bean.getHour();
                day = bean.getDay();
                month = bean.getMonth();
                Assert.notNull(hour, "以月为周期的情况下，小时不能是null！！");
                Assert.notNull(day, "以月为周期的情况下，日期不能是null！！");
                trigger = getTrigger(bean, jobId, minute, hour, day, month);
                //detail
                detail = JobBuilder.newJob(clazz)
                        .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                        .setJobData(new JobDataMap(keyMap))
                        .build();
                //scheduler
                scheduler.scheduleJob(detail, trigger);
                scheduler.start();
                break;
            default:
                break;
        }
    }

    /**
     * 用于容量预警年周期
     *
     * @param bean   job bean
     * @param jobId  doc id
     * @param minute 秒
     * @param hour   小时
     * @param day    天
     * @param month  月
     * @return Trigger
     */
    private Trigger getTrigger(QuartzJobBean bean, String jobId, Integer minute, Integer hour, Integer day, @Nullable Integer month) {
        Trigger trigger;
        if (Objects.isNull(month)) {//如果没有定义月
            switch (day) {
                case 99://最后一天
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.month_last_day.getCronTemplate(),
                                            minute,
                                            hour)
                            )).build();
                    break;
                default:
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.month.getCronTemplate(),
                                            minute,
                                            hour,
                                            day)
                            )).build();
                    break;
            }
        } else {//如果定义了月
            String yearField = LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1000);
            switch (day) {
                case 99://最后一天
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.year_month_last_day.getCronTemplate(),
                                            bean.getMinute(),
                                            bean.getHour(),
                                            bean.getMonth(),
                                            yearField)
                            )).build();
                    break;
                default:
                    //Trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                    jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.year.getCronTemplate(),
                                            bean.getMinute(),
                                            bean.getHour(),
                                            bean.getDay(),
                                            bean.getMonth(),
                                            yearField)
                            )).build();
                    break;
            }
        }
        return trigger;
    }

    /**
     * 用于容量预警月周期
     *
     * @param jobId  doc id
     * @param minute 分
     * @param hour   小时
     * @param day    天
     * @return Trigger
     */
    private Trigger getTrigger(String jobId, Integer minute, Integer hour, Integer day) {
        Trigger trigger;
        switch (day) {
            case 88:
                //Trigger
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                jobId + QuartzKey.group_name.getKey())
                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                String.format(JobCorns.day.getCronTemplate(),
                                        minute,
                                        hour)
                        )).build();
                break;
            case 99:
                //Trigger
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                jobId + QuartzKey.group_name.getKey())
                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                String.format(JobCorns.month_last_day.getCronTemplate(),
                                        minute,
                                        hour)
                        )).build();
                break;
            default:
                //Trigger
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                jobId + QuartzKey.group_name.getKey())
                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                String.format(JobCorns.month.getCronTemplate(),
                                        minute,
                                        hour,
                                        day)
                        )).build();
                break;
        }
        return trigger;
    }

    /**
     * 用于容量定时
     *
     * @param beans  定时相关属性的bean集合
     * @param clazz  实现job的对象
     * @param client es相关属性
     * @throws SchedulerException 异常
     */
    public void newCapacityJobs(List<QuartzJobBean> beans, Class<? extends Job> clazz, EsPropertiesBean client)
            throws SchedulerException {
        //scheduler
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        Map<JobDetail, Set<? extends Trigger>> jobs = new HashMap<>(beans.size());
        beans.forEach(bean -> {
            Integer cycleType = bean.getCycleType();
            String jobId = bean.getJobId();
            Integer minute = bean.getMinute();
            Assert.notNull(cycleType, "CycleType周期类型不能是null！！");
            Assert.hasLength(jobId, "任务key不能是null/\"\"！！！");
            Assert.isTrue(Objects.nonNull(minute) && minute >= 0 && minute <= 59, "周期的情况下，分钟不能是null！！");
            //job data
            Map<String, Object> keyMap = new HashMap<>(2);
            keyMap.put("jobId", jobId);
            keyMap.put("client", client);
            //trigger
            Trigger trigger;
            JobDetail detail;
            //定时时间
            Integer hour;
            Integer week;
            Integer day;
            Integer month;
            //cycle type：5时、10天、20周、30月、40年
            switch (cycleType) {
                case 5://小时周期
                    //trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey()
                                    , jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.hour.getCronTemplate(),
                                            minute)
                            )).build();
                    //detail
                    detail = JobBuilder.newJob(clazz)
                            .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                            .setJobData(new JobDataMap(keyMap))
                            .build();
                    jobs.put(detail, Collections.singleton(trigger));
                    break;
                case 10://天周期
                    hour = bean.getHour();
                    if (Objects.isNull(hour)) {//如果没有定义小时
                        //trigger
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobId + QuartzKey.trigger_name.getKey()
                                        , jobId + QuartzKey.group_name.getKey())
                                .withSchedule(CronScheduleBuilder.cronSchedule(
                                        String.format(JobCorns.hour.getCronTemplate(),
                                                minute)
                                )).build();
                    } else {//定义了小时
                        Assert.isTrue(hour >= 0 && hour <= 23,
                                "以月为周期的情况下，小时不能是null，且只能是[0-23]！！");
                        //Trigger
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                        jobId + QuartzKey.group_name.getKey())
                                .withSchedule(CronScheduleBuilder.cronSchedule(
                                        String.format(JobCorns.day.getCronTemplate(),
                                                minute,
                                                hour)
                                )).build();
                    }
                    //detail
                    detail = JobBuilder.newJob(clazz)
                            .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                            .setJobData(new JobDataMap(keyMap))
                            .build();
                    jobs.put(detail, Collections.singleton(trigger));
                    break;
                case 20://周周期
                    hour = bean.getHour();
                    week = bean.getWeek();
                    Assert.isTrue(Objects.nonNull(hour) && hour >= 0 && hour <= 23,
                            "以月为周期的情况下，小时不能是null，且只能是[0-23]！！");
                    if (Objects.isNull(week)) {//如果没有定义周
                        //Trigger
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                        jobId + QuartzKey.group_name.getKey())
                                .withSchedule(CronScheduleBuilder.cronSchedule(
                                        String.format(JobCorns.day.getCronTemplate(),
                                                minute,
                                                hour)
                                )).build();
                    } else {//定义了周
                        Assert.isTrue(week >= 1 && week <= 7, "week只能是[1-7]之间是数值！！");
                        //Trigger
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                        jobId + QuartzKey.group_name.getKey())
                                .withSchedule(CronScheduleBuilder.cronSchedule(
                                        String.format(JobCorns.week.getCronTemplate(),
                                                minute,
                                                hour,
                                                week)
                                )).build();
                    }
                    //detail
                    detail = JobBuilder.newJob(clazz)
                            .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                            .setJobData(new JobDataMap(keyMap))
                            .build();
                    jobs.put(detail, Collections.singleton(trigger));
                    break;
                case 30://月周期
                    hour = bean.getHour();
                    day = null == bean.getDay() ? QuartzValue.day_is_null.getValue() : bean.getDay();
                    Assert.isTrue(Objects.nonNull(hour) && hour >= 0 && hour <= 23,
                            "以月为周期的情况下，小时不能是null，且只能是[0-23]！！");
                    trigger = getTrigger(jobId, minute, hour, day);
                    //detail
                    detail = JobBuilder.newJob(clazz)
                            .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                            .setJobData(new JobDataMap(keyMap))
                            .build();
                    jobs.put(detail, Collections.singleton(trigger));
                    break;
                case 40://年周期
                    hour = bean.getHour();
                    day = bean.getDay();
                    month = bean.getMonth();
                    Assert.notNull(hour, "以月为周期的情况下，小时不能是null！！");
                    Assert.notNull(day, "以月为周期的情况下，日期不能是null！！");
                    trigger = getTrigger(bean, jobId, minute, hour, day, month);
                    //detail
                    detail = JobBuilder.newJob(clazz)
                            .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                            .setJobData(new JobDataMap(keyMap))
                            .build();
                    jobs.put(detail, Collections.singleton(trigger));
                    break;
                default:
                    break;
            }
        });
        if (!jobs.isEmpty()) {
            scheduler.scheduleJobs(jobs, true);
            scheduler.start();
        }
    }


    /**
     * 用于相关性任务定时
     *
     * @param bean   定时相关属性的bean
     * @param clazz  实现job的对象
     * @param client es相关属性
     * @throws SchedulerException 异常
     */
    public void newCorrelationJob(QuartzJobBean bean, Class<? extends Job> clazz, EsPropertiesBean client) throws SchedulerException {
        Assert.notNull(bean, "new job的时候，JobBean不能为null！！");
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        String jobId = bean.getJobId();
        Trigger trigger;
        JobDetail detail;
        //Assert
        Assert.hasLength(jobId, "任务key不能是null/\"\"！！！");
        Assert.notNull(bean.getMinute(), "minute不能是null！！");
        Assert.notNull(bean.getHour(), "hour不能是null！！");
        //job data
        Map<String, Object> keyMap = new HashMap<>(2);
        keyMap.put("jobId", jobId);
        keyMap.put("client", client);
        //10为单次任务，20为周期任务
        switch (bean.getJobType()) {
            case 10:
                //assert
                Assert.notNull(bean.getDay(), "day不能是null！！");
                Assert.notNull(bean.getMonth(), "month不能是null！！");
                Assert.notNull(bean.getYear(), "year不能是null！！");
                //trigger
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + QuartzKey.trigger_name.getKey()
                                , jobId + QuartzKey.group_name.getKey())
                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                String.format(JobCorns.once.getCronTemplate(),
                                        bean.getMinute(),
                                        bean.getHour(),
                                        bean.getDay(),
                                        bean.getMonth(),
                                        bean.getYear())
                        )).build();
                //detail
                detail = JobBuilder.newJob(clazz)
                        .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                        .setJobData(new JobDataMap(keyMap))
                        .build();
                //scheduler
                scheduler.scheduleJob(detail, trigger);
                scheduler.start();
                break;
            case 20:
                //cycle type：10天、20周、30月、40年
                switch (bean.getCycleType()) {
                    case 10://天
                        //Trigger
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                        jobId + QuartzKey.group_name.getKey())
                                .withSchedule(CronScheduleBuilder.cronSchedule(
                                        String.format(JobCorns.day.getCronTemplate(),
                                                bean.getMinute(),
                                                bean.getHour())
                                )).build();
                        //detail
                        detail = JobBuilder.newJob(clazz)
                                .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                .setJobData(new JobDataMap(keyMap))
                                .build();
                        //scheduler
                        scheduler.scheduleJob(detail, trigger);
                        scheduler.start();
                        break;
                    case 20://周
                        Assert.notNull(bean.getWeek(), "week不能是null！！！");
                        //Trigger
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                        jobId + QuartzKey.group_name.getKey())
                                .withSchedule(CronScheduleBuilder.cronSchedule(
                                        String.format(JobCorns.week.getCronTemplate(),
                                                bean.getMinute(),
                                                bean.getHour(),
                                                bean.getWeek())
                                )).build();
                        //detail
                        detail = JobBuilder.newJob(clazz)
                                .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                .setJobData(new JobDataMap(keyMap))
                                .build();
                        //scheduler
                        scheduler.scheduleJob(detail, trigger);
                        scheduler.start();
                        break;
                    case 30://月
                        Integer day = bean.getDay();
                        Assert.notNull(day, "day不能是null！！");
                        if (QuartzValue.last_day.getValue().equals(day)) {//如果是最后一天
                            //Trigger
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                            jobId + QuartzKey.group_name.getKey())
                                    .withSchedule(CronScheduleBuilder.cronSchedule(
                                            String.format(JobCorns.month_last_day.getCronTemplate(),
                                                    bean.getMinute(),
                                                    bean.getHour())
                                    )).build();
                        } else {
                            Assert.notNull(bean.getDay(), "day不能是null！！");
                            //Trigger
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                            jobId + QuartzKey.group_name.getKey())
                                    .withSchedule(CronScheduleBuilder.cronSchedule(
                                            String.format(JobCorns.month.getCronTemplate(),
                                                    bean.getMinute(),
                                                    bean.getHour(),
                                                    bean.getDay())
                                    )).build();
                        }
                        //detail
                        detail = JobBuilder.newJob(clazz)
                                .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                .setJobData(new JobDataMap(keyMap))
                                .build();
                        //scheduler
                        scheduler.scheduleJob(detail, trigger);
                        scheduler.start();
                        break;
                    case 40://年
                        Integer monthDay = bean.getDay();
                        Assert.notNull(monthDay, "day不能是null！！");
                        Assert.notNull(bean.getMonth(), "month不能是null！！");
                        String yearField = LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1000);
                        if (QuartzValue.last_day.getValue().equals(monthDay)) {//是月最后一天
                            //Trigger
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                            jobId + QuartzKey.group_name.getKey())
                                    .withSchedule(CronScheduleBuilder.cronSchedule(
                                            String.format(JobCorns.year_month_last_day.getCronTemplate(),
                                                    bean.getMinute(),
                                                    bean.getHour(),
                                                    bean.getMonth(),
                                                    yearField)
                                    )).build();
                        } else {//不是月最后一天
                            //Trigger
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                            jobId + QuartzKey.group_name.getKey())
                                    .withSchedule(CronScheduleBuilder.cronSchedule(
                                            String.format(JobCorns.year.getCronTemplate(),
                                                    bean.getMinute(),
                                                    bean.getHour(),
                                                    bean.getDay(),
                                                    bean.getMonth(),
                                                    yearField)
                                    )).build();
                        }
                        //detail
                        detail = JobBuilder.newJob(clazz)
                                .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                .setJobData(new JobDataMap(keyMap))
                                .build();
                        //scheduler
                        scheduler.scheduleJob(detail, trigger);
                        scheduler.start();
                        break;
                }
                break;
            default:
                break;
        }

    }

    /**
     * 用于相关性多个任务定时
     *
     * @param beans  定时相关属性的多个bean
     * @param clazz  实现job的对象
     * @param client es相关属性
     * @throws SchedulerException 异常
     */
    public void newCorrelationJobs(List<QuartzJobBean> beans, Class<? extends Job> clazz, EsPropertiesBean client) throws Exception {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        Map<JobDetail, Set<? extends Trigger>> jobs = new HashMap<>(beans.size());
        beans.forEach(bean -> {
            //trigger
            Trigger trigger;
            //detail
            JobDetail detail;
            String jobId = bean.getJobId();
            //Assert
            Assert.hasLength(jobId, "任务key不能是null/\"\"！！！");
            Assert.notNull(bean.getMinute(), "minute不能是null！！");
            Assert.notNull(bean.getHour(), "hour不能是null！！");
            //job data
            Map<String, Object> keyMap = new HashMap<>(2);
            keyMap.put("jobId", jobId);
            keyMap.put("client", client);
            switch (bean.getJobType()) {
                case 10:
                    //assert
                    Assert.notNull(bean.getDay(), "day不能是null！！");
                    Assert.notNull(bean.getMonth(), "month不能是null！！");
                    Assert.notNull(bean.getYear(), "year不能是null！！");
                    //trigger
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobId + QuartzKey.trigger_name.getKey()
                                    , jobId + QuartzKey.group_name.getKey())
                            .withSchedule(CronScheduleBuilder.cronSchedule(
                                    String.format(JobCorns.once.getCronTemplate(),
                                            bean.getMinute(),
                                            bean.getHour(),
                                            bean.getDay(),
                                            bean.getMonth(),
                                            bean.getYear())
                            )).build();
                    //detail
                    detail = JobBuilder.newJob(clazz)
                            .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                            .setJobData(new JobDataMap(keyMap))
                            .build();
                    jobs.put(detail, Collections.singleton(trigger));
                    break;
                case 20:
                    //cycle type：10天、20周、30月、40年
                    switch (bean.getCycleType()) {
                        case 10://天
                            //Trigger
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                            jobId + QuartzKey.group_name.getKey())
                                    .withSchedule(CronScheduleBuilder.cronSchedule(
                                            String.format(JobCorns.day.getCronTemplate(),
                                                    bean.getMinute(),
                                                    bean.getHour())
                                    )).build();
                            //detail
                            detail = JobBuilder.newJob(clazz)
                                    .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                    .setJobData(new JobDataMap(keyMap))
                                    .build();
                            jobs.put(detail, Collections.singleton(trigger));
                            break;
                        case 20://周
                            Assert.notNull(bean.getWeek(), "week不能是null！！！");
                            //Trigger
                            trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                            jobId + QuartzKey.group_name.getKey())
                                    .withSchedule(CronScheduleBuilder.cronSchedule(
                                            String.format(JobCorns.week.getCronTemplate(),
                                                    bean.getMinute(),
                                                    bean.getHour(),
                                                    bean.getWeek())
                                    )).build();
                            //detail
                            detail = JobBuilder.newJob(clazz)
                                    .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                    .setJobData(new JobDataMap(keyMap))
                                    .build();
                            jobs.put(detail, Collections.singleton(trigger));
                            break;
                        case 30://月
                            Integer day = bean.getDay();
                            Assert.notNull(day, "day不能是null！！");
                            if (QuartzValue.last_day.getValue().equals(day)) {//如果是最后一天
                                //Trigger
                                trigger = TriggerBuilder.newTrigger()
                                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                                jobId + QuartzKey.group_name.getKey())
                                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                                String.format(JobCorns.month_last_day.getCronTemplate(),
                                                        bean.getMinute(),
                                                        bean.getHour())
                                        )).build();
                            } else {
                                Assert.notNull(day, "day不能是null！！");
                                //Trigger
                                trigger = TriggerBuilder.newTrigger()
                                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                                jobId + QuartzKey.group_name.getKey())
                                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                                String.format(JobCorns.month.getCronTemplate(),
                                                        bean.getMinute(),
                                                        bean.getHour(),
                                                        bean.getDay())
                                        )).build();
                            }
                            //detail
                            detail = JobBuilder.newJob(clazz)
                                    .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                    .setJobData(new JobDataMap(keyMap))
                                    .build();
                            jobs.put(detail, Collections.singleton(trigger));
                            break;
                        case 40://年
                            Integer monthDay = bean.getDay();
                            Assert.notNull(monthDay, "day不能是null！！");
                            Assert.notNull(bean.getMonth(), "month不能是null！！");
                            String yearField = LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1000);
                            if (QuartzValue.last_day.getValue().equals(monthDay)) {//是月最后一天
                                //Trigger
                                trigger = TriggerBuilder.newTrigger()
                                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                                jobId + QuartzKey.group_name.getKey())
                                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                                String.format(JobCorns.year_month_last_day.getCronTemplate(),
                                                        bean.getMinute(),
                                                        bean.getHour(),
                                                        bean.getMonth(),
                                                        yearField)
                                        )).build();
                            } else {//不是月最后一天
                                //Trigger
                                trigger = TriggerBuilder.newTrigger()
                                        .withIdentity(jobId + QuartzKey.trigger_name.getKey(),
                                                jobId + QuartzKey.group_name.getKey())
                                        .withSchedule(CronScheduleBuilder.cronSchedule(
                                                String.format(JobCorns.year.getCronTemplate(),
                                                        bean.getMinute(),
                                                        bean.getHour(),
                                                        bean.getDay(),
                                                        bean.getMonth(),
                                                        yearField)
                                        )).build();
                            }
                            //detail
                            detail = JobBuilder.newJob(clazz)
                                    .withIdentity(new JobKey(jobId + QuartzKey.detail_name.getKey()))
                                    .setJobData(new JobDataMap(keyMap))
                                    .build();
                            jobs.put(detail, Collections.singleton(trigger));
                            break;
                    }
                    break;
                default:
                    break;
            }
        });
        if (!jobs.isEmpty()) {
            scheduler.scheduleJobs(jobs, true);
            scheduler.start();
        }
    }

    /**
     * 删除任务
     *
     * @param jobIds job id集合
     * @throws SchedulerException 异常
     */
    public void deleteJobs(String[] jobIds) throws SchedulerException {
        Assert.notEmpty(jobIds, "得到的job key的String数组是一个[]！！！！");
        schedulerFactoryBean
                .getScheduler()
                .deleteJobs(Arrays.stream(jobIds)
                        .map(key -> key + QuartzKey.detail_name.getKey())
                        .map(JobKey::new)
                        .collect(Collectors.toList()));
    }

    /**
     * 暂停任务
     *
     * @param jobIds job id集合
     * @throws SchedulerException 异常
     */
    public void disableJobs(String[] jobIds) throws SchedulerException {
        Assert.notEmpty(jobIds, "得到的job key的String数组是一个[]！！！！");
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (String key : jobIds) {
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals(key + QuartzKey.group_name.getKey()));
        }
    }

    /**
     * 启动任务
     *
     * @param jobIds job id集合
     * @throws SchedulerException 异常
     */
    public void enableJobs(String[] jobIds) throws SchedulerException {
        Assert.notEmpty(jobIds, "得到的job key的String数组是一个[]！！！！");
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (String key : jobIds) {
            scheduler.resumeJobs(GroupMatcher.jobGroupEquals(key + QuartzKey.group_name.getKey()));
        }
    }

    @Autowired
    public QuartzService(SchedulerFactoryBean schedulerFactoryBean) {
        this.schedulerFactoryBean = schedulerFactoryBean;
    }
}
