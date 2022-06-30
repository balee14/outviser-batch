package com.enliple.outviserbatch.outviser.api.send.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.outviser.api.send.service.CrmSendService;
import com.enliple.outviserbatch.outviser.batch.service.BatchApiCallParamsService;
import com.enliple.outviserbatch.outviser.batch.util.BatchContext;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchCrmSendJob {

	@Autowired
	private BatchApiCallParamsService batchApiCallParamsService;

	@Autowired
	private CrmSendService crmService;

	@Autowired
	private BatchExceptionHandler batchExceptionHandler;

	@Bean
	public Job crmSendBatchJob(JobBuilderFactory jobBuilderFactory, Step crmSendBatchStep) throws Exception {

		return jobBuilderFactory.get("crmSendBatchJob")
				.start(crmSendBatchStep)
				.build();
	}

	@Bean
	@JobScope
	public Step crmSendBatchStep(StepBuilderFactory stepBuilderFactory,
			@Value("#{jobParameters[batchApiId]}") String batchApiId) throws Exception {

		return stepBuilderFactory.get("crmSendBatchStep")
				.tasklet((contribution, chunkContext) -> {

					DataMap dataMap = batchApiCallParamsService.getBatchApiCallParams(batchApiId);

					dataMap = crmService.requestSendCrm(dataMap);
					contribution.getStepExecution().getJobExecution().getExecutionContext().put(BatchContext.RESULT_KEY, dataMap);

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(batchExceptionHandler)
				.build();
	}
}
