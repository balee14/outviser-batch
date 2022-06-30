package com.enliple.outviserbatch.schedule.crm;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;
import com.enliple.outviserbatch.schedule.crm.service.ScheduleCrmService;

@Configuration
public class ScheduleCrmJob {

	@Autowired
	private ScheduleCrmService scheduleCrmService;

	@Autowired
	private BatchExceptionHandler handler;

	@Autowired
	private CommonErrorLogService commonErrLogService;

	@Value("${spring.profiles.active}")
	private String activeServer;

	@Bean
	public Job crmScheduleJob(JobBuilderFactory jobFactory, Step crmScheduleStep) throws Exception {

		return jobFactory.get("crmScheduleJob")
				.start(crmScheduleStep)
				.build();
	}

	@Bean
	public Step crmScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		return stepFactory.get("crmScheduleStep")
				.tasklet((contribution, chunkContext) -> {

					try {
						if ("LIVE".equalsIgnoreCase(activeServer)) {
							scheduleCrmService.createCrmReport();
						}
					} catch (Exception e) {
						commonErrLogService.insertErrorLog(e);
					}

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(handler)
				.build();
	}
}
