package com.enliple.outviserbatch.outviser.batch.common;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;
import com.enliple.outviserbatch.outviser.batch.param.InitParameter;
import com.enliple.outviserbatch.outviser.batch.util.BatchContext;

@Component
public class BatchLauncher {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobLocator jobLocator;

	@Autowired
	private InitParameter initParameter;

	@Autowired
	private CommonErrorLogService commonErrLogService;

	public DataMap start(String jobName, DataMap dataMap) {

		DataMap result = new DataMap();

		try {
			Job job = jobLocator.getJob(jobName);
			JobParameters jobParameters = initParameter.getJobParam(dataMap);

			JobExecution execution = jobLauncher.run(job, jobParameters);

			Object obj = execution.getExecutionContext().get(BatchContext.RESULT_KEY);
			result.put(BatchContext.MAP_OBJECT_KEY, obj);
		} catch (Exception e) {
			commonErrLogService.insertErrorLog(e);
		}

		return result;
	}

	@Async("threadPoolTaskExecutor")
	public void startAsync(String jobName, DataMap dataMap) {

		try {
			Job job = jobLocator.getJob(jobName);
			JobParameters jobParameters = initParameter.getJobParam(dataMap);

			jobLauncher.run(job, jobParameters);
		} catch (Exception e) {
			commonErrLogService.insertErrorLog(e);
		}
	}
}
