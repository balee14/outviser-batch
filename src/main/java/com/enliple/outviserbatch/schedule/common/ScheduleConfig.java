package com.enliple.outviserbatch.schedule.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.enliple.outviserbatch.common.context.ApplicationContextProvider;
import com.enliple.outviserbatch.common.util.BeanUtils;

@Configuration
public class ScheduleConfig {

	@Autowired
	private Environment environment;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobLocator jobLocator;

	/*
	@Autowired
	private DataSource dataSource;
	*/

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws Exception {

		StdSchedulerFactory sf = getStdSchedulerFactory();

		/*
		 * 이전 트리거 정보 삭제
		 * (이중화시 처리는..?)
		 */
		Scheduler sche = sf.getScheduler();
		sche.clear();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setSchedulerFactory(sf);
		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
		schedulerFactoryBean.setTriggers(getTriggers());
		/*
		schedulerFactoryBean.setQuartzProperties(getProperties());
		schedulerFactoryBean.setDataSource(dataSource);
		schedulerFactoryBean.setOverwriteExistingJobs(true);
		*/

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jobLauncher", jobLauncher);
		map.put("jobLocator", jobLocator);
		schedulerFactoryBean.setSchedulerContextAsMap(map);

		return schedulerFactoryBean;
	}

	/**
	 * dataSource 별도 정의
	 * 
	 * @return
	 * @throws Exception
	 */
	private StdSchedulerFactory getStdSchedulerFactory() throws Exception {

		Properties prop = getProperties();

		String datasourcePrefix = StdSchedulerFactory.PROP_DATASOURCE_PREFIX;
		datasourcePrefix += "." + prop.getProperty("org.quartz.jobStore.dataSource");

		prop.setProperty(String.format("%s.provider", datasourcePrefix), "hikaricp");
		prop.setProperty(String.format("%s.driver", datasourcePrefix), environment.getProperty("spring.datasource.driverClassName"));
		prop.setProperty(String.format("%s.URL", datasourcePrefix), environment.getProperty("spring.datasource.url"));
		prop.setProperty(String.format("%s.user", datasourcePrefix), environment.getProperty("spring.datasource.username"));
		prop.setProperty(String.format("%s.password", datasourcePrefix), environment.getProperty("spring.datasource.password"));

		return new StdSchedulerFactory(prop);
	}

	/**
	 * 쿼츠 관련 프로퍼티
	 * 
	 * @return
	 * @throws Exception
	 */
	private Properties getProperties() throws Exception {

		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));

		propertiesFactoryBean.afterPropertiesSet();

		return propertiesFactoryBean.getObject();
	}

	/**
	 * 실행할 스케줄러 트리거 반환
	 * 
	 * @return
	 * @throws Exception
	 */
	private CronTrigger[] getTriggers() throws Exception {

		List<CronTrigger> triggerList = new ArrayList<CronTrigger>();

		ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
		String[] beanNames = ctx.getBeanDefinitionNames();
		for (String bean : beanNames) {
			// 트리거만 빈에서 가져오기 위함
			if (! bean.contains("schedule_trigger")) {
				continue;
			}

			Object obj = BeanUtils.getBean(bean);
			if (obj instanceof ScheduleTrigger) {
				// 해당 객체가 Trigger 구현체인 경우
				ScheduleTrigger trigger = (ScheduleTrigger) obj;
				triggerList.add(trigger.cronTriggerFactoryBean());
			}
		}

		return triggerList.toArray(new CronTrigger[triggerList.size()]);
	}
}
