package com.cn.ridge.config;

import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Author: create by wang.gf
 * Date: create at 2019/1/3
 */
public class QuartzSchedulerConfig {


    /**
     * 配置JobFactory
     *
     * @param applicationContext 上下文
     * @return JobFactory
     */
    @Bean
    public JobFactory configSchedulerFactoryBean(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory factory = new AutowiringSpringBeanJobFactory();
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    /**
     * SchedulerFactoryBean这个类的真正作用提供了对org.quartz.Scheduler的创建与配置，并且会管理它的生命周期与Spring同步。
     * org.quartz.Scheduler: 调度器。所有的调度都是由它控制。
     * 也就是说：Scheduler的创建，再是通过quartz去创建，而是交由spring来控制，这样在QuartzService可以使用Autowired来注册scheduler
     * 也可以在实现了Job接口的类中，使用Autowired来注册spring管理的bean
     *
     * @param jobFactory 上面配置的JobFactory
     * @return SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setAutoStartup(false); //设置自行启动
        factory.setJobFactory(jobFactory);
        return factory;
    }

    /**
     * 通过继承SpringBeanJobFactory，获得JobFactory
     */
    private final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(final ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }
}
