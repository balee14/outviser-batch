package com.enliple.outviserbatch.schedule.common;

import java.util.HashMap;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * 해당 Trigger 구현체로 즉시 객체화 하지 못하도록 abstract 명시
 * <br>- init() 구현 필요
 * 
 * @author jbnoh
 *
 */
public abstract class ScheduleTriggerImpl implements ScheduleTrigger {

	protected String jobName;

	protected String desc;

	protected String expression;

	/*
	 * 상속받은 클래스에서 jobName, expression 정의하기 위함
	 */
	protected abstract void init();

	@Override
	public CronTrigger cronTriggerFactoryBean() throws Exception {

		this.init();

		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(jobDetailFactoryBean());
		cronTriggerFactoryBean.setCronExpression(expression);

		// QRTZ_TRIGGERS.TRIGGER_NAME
		cronTriggerFactoryBean.setName(desc);

		/* QRTZ_TRIGGERS.TRIGGER_GROUP
		cronTriggerFactoryBean.setGroup("");
		*/
		/* QRTZ_TRIGGERS.DESCRIPTION
		cronTriggerFactoryBean.setDescription("");
		*/

		cronTriggerFactoryBean.afterPropertiesSet();

		return cronTriggerFactoryBean.getObject();
	}

	@Override
	public JobDetail jobDetailFactoryBean() {

		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(ScheduleLauncher.class);
//		jobDetailFactoryBean.setDurability(true);

		// QRTZ_JOB_DETAILS.JOB_NAME
		jobDetailFactoryBean.setName(jobName);

		/* QRTZ_JOB_DETAILS.JOB_GROUP
		jobDetailFactoryBean.setGroup("");
		*/
		/* QRTZ_JOB_DETAILS.DESCRIPTION
		jobDetailFactoryBean.setDescription("");
		*/

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jobName", jobName);
		map.put("desc", desc);
		jobDetailFactoryBean.setJobDataAsMap(map);

		jobDetailFactoryBean.afterPropertiesSet();

		return jobDetailFactoryBean.getObject();
	}

}
