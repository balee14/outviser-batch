package com.enliple.outviserbatch.schedule.report;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.schedule.report.service.ScheduleReportService;

@Configuration
public class ScheduleReportJob {

	@Autowired
	private ScheduleReportService scheduleReportService;

	@Autowired
	private BatchExceptionHandler handler;

	@Bean
	public Job reportScheduleJob(JobBuilderFactory jobFactory, Step reportScheduleStep) throws Exception {

		return jobFactory.get("reportScheduleJob")
				.start(reportScheduleStep)
				.build();
	}

	@Bean
	public Step reportScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		return stepFactory.get("reportScheduleStep")
				.tasklet((contribution, chunkContext) -> {

					scheduleReportService.reportMts();

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(handler)
				.build();
	}
}
