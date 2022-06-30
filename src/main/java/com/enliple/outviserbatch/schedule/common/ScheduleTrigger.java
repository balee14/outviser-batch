package com.enliple.outviserbatch.schedule.common;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;

public interface ScheduleTrigger {

	CronTrigger cronTriggerFactoryBean() throws Exception;

	JobDetail jobDetailFactoryBean();
}
