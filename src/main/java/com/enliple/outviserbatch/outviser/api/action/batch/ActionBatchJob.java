package com.enliple.outviserbatch.outviser.api.action.batch;

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
import com.enliple.outviserbatch.outviser.api.action.service.ActionService;
import com.enliple.outviserbatch.outviser.batch.service.BatchApiCallParamsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ActionBatchJob {

	@Autowired
	private BatchApiCallParamsService batchApiCallParamsService;

	@Autowired
	private ActionService actionService;

	@Autowired
	private BatchExceptionHandler batchExceptionHandler;

	@Bean
	public Job actionBatchJobStart(JobBuilderFactory jobBuilderFactory, Step actionBatchStep) throws Exception {

		return jobBuilderFactory.get("actionBatchJobStart")
				.start(actionBatchStep)
				.build();
	}

	@Bean
	@JobScope
	public Step actionBatchStep(StepBuilderFactory stepBuilderFactory,
			@Value("#{jobParameters[batchApiId]}") String batchApiId) throws Exception {

		return stepBuilderFactory.get("actionTaskletStep")
				.tasklet((contribution, chunkContext) -> {

					DataMap dataMap = batchApiCallParamsService.getBatchApiCallParams(batchApiId);
					actionService.insertRequestAction(dataMap);

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(batchExceptionHandler)
				.build();
	}
}
