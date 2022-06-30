package com.enliple.outviserbatch.schedule.cdp;

import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;
import com.enliple.outviserbatch.schedule.cdp.service.ScheduleCdpService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleCdpJob {

	@Autowired
	private ScheduleCdpService scheduleCdpService;

	@Autowired
	private BatchExceptionHandler handler;

	@Autowired
	private CommonErrorLogService commonErrLogService;

	@Value("${spring.profiles.active}")
	private String activeServer;

	@Value("${cdp.backup.enabled}")
	private boolean backup;

	@Bean
	public Job cdpScheduleJob(JobBuilderFactory jobFactory, Step cdpScheduleStep) throws Exception {

		return jobFactory.get("cdpScheduleJob")
				.start(cdpScheduleStep)
				.build();
	}

	@Bean
	public Step cdpScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		return stepFactory.get("cdpScheduleStep")
				.tasklet((contribution, chunkContext) -> {

					try {
						if ("LIVE".equalsIgnoreCase(activeServer)) {
							if(backup) {
								scheduleCdpService.runCdpListBackup();
							}
						}else{
							scheduleCdpService.runCdpListBackup();
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
