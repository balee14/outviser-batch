package com.enliple.outviserbatch.outviser.api.send.batch;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.outviser.api.send.service.CafeSendService;
import com.enliple.outviserbatch.outviser.batch.service.BatchApiCallParamsService;
import com.enliple.outviserbatch.outviser.batch.util.BatchContext;
import lombok.RequiredArgsConstructor;
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

@Configuration
@RequiredArgsConstructor
public class BatchCafeSendJob {

	@Autowired
	private BatchApiCallParamsService batchApiCallParamsService;

	@Autowired
	private CafeSendService cafeService;

	@Autowired
	private BatchExceptionHandler batchExceptionHandler;

	@Bean
	public Job cafeSendBatchJob(JobBuilderFactory jobBuilderFactory, Step cafeSendBatchStep) throws Exception {

		return jobBuilderFactory.get("cafeSendBatchJob")
				.start(cafeSendBatchStep)
				.build();
	}

	@Bean
	@JobScope
	public Step cafeSendBatchStep(StepBuilderFactory stepBuilderFactory,
			@Value("#{jobParameters[batchApiId]}") String batchApiId) throws Exception {

		return stepBuilderFactory.get("cafe24SendBatchStep")
				.tasklet((contribution, chunkContext) -> {

					DataMap dataMap = batchApiCallParamsService.getBatchApiCallParams(batchApiId);

					dataMap = cafeService.requestSendCafe(dataMap);
					contribution.getStepExecution().getJobExecution().getExecutionContext().put(BatchContext.RESULT_KEY, dataMap);

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(batchExceptionHandler)
				.build();
	}
}
