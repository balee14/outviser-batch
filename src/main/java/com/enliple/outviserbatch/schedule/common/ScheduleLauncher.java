package com.enliple.outviserbatch.schedule.common;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;
import com.enliple.outviserbatch.common.util.BeanUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class ScheduleLauncher extends QuartzJobBean implements org.quartz.StatefulJob {

	private JobLocator jobLocator;

	private JobLauncher jobLauncher;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

		Map<String, Object> jobDataMap = context.getMergedJobDataMap();

		String jobName = objToString(jobDataMap.get("jobName"));

		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("requestDate", System.currentTimeMillis())
				.addString("jobName", jobName)
				.addString("desc", objToString(jobDataMap.get("desc")))
				.toJobParameters();

		try {
			if (StringUtils.isBlank(jobName)) {
				throw new CommonException("Job name is blank");
			}

			Job job = jobLocator.getJob(jobName);
			jobLauncher.run(job, jobParameters);
		} catch (Exception e) {

			try {
				Object obj = BeanUtils.getBean("commonErrorLogService");
				if (obj instanceof CommonErrorLogService) {
					CommonErrorLogService commonErrLogService = (CommonErrorLogService) obj;

					/*
					 * JOB 내부에서 예외를 넘기더라도 해당 catch 문에 접근하지 않음
					 *  - 비즈니스 로직을 호출한 구간에서 별도로 예외 처리(아래 해당하는 로깅 처리)를 해야함
					 */
					commonErrLogService.insertErrorLog(e);
				}
			} catch (Exception notUsed) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private String objToString(Object obj) {

		return obj != null ? obj.toString() : "";
	}
}
