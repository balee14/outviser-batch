package com.enliple.outviserbatch.outviser.api.send.batch;

import org.json.simple.JSONObject;
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
import com.enliple.outviserbatch.outviser.api.send.service.SweetTrackerSendService;
import com.enliple.outviserbatch.outviser.batch.service.BatchApiCallParamsService;
import com.enliple.outviserbatch.outviser.batch.util.BatchContext;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchSweetTrackerSendJob {

	@Autowired
	private BatchApiCallParamsService batchApiCallParamsService;

	@Autowired
	private SweetTrackerSendService sweetTrackerService;

	@Autowired
	private BatchExceptionHandler batchExceptionHandler;

	@Bean
	public Job sweetTrackerSendBatchJob(JobBuilderFactory jobBuilderFactory, Step sweetTrackerSendBatchStep) throws Exception {

		return jobBuilderFactory.get("sweetTrackerSendBatchJob")
				.start(sweetTrackerSendBatchStep)
				.build();
	}

	@SuppressWarnings("unchecked")
	@Bean
	@JobScope
	public Step sweetTrackerSendBatchStep(StepBuilderFactory stepBuilderFactory,
			@Value("#{jobParameters[batchApiId]}") String batchApiId) throws Exception {

		return stepBuilderFactory.get("sweetTrackerSendBatchStep")
				.tasklet((contribution, chunkContext) -> {

					Throwable throwable = null;

					DataMap param = batchApiCallParamsService.getBatchApiCallParams(batchApiId);

					JSONObject jsonObj = new JSONObject();
					jsonObj.put("code", true);
					jsonObj.put("message", "success");

					try {
						// OV_LINK_DELIVERY_MST 테이블 업데이트 필요
						// OV_LINK_DELIVERY_LOG 테이블로 적재 필요
						sweetTrackerService.updateSweetTrackerLog(param);

						// 배송단계 메시지 발송
						// 배송완료시 CRM 배송완료 API 호출
						sweetTrackerService.insertSweetTrackerSand(param);
					} catch (Exception e) {
						jsonObj.put("code", false);
						jsonObj.put("message", "failure");
						throwable = e;
					}

					contribution.getStepExecution().getJobExecution().getExecutionContext().put(BatchContext.RESULT_KEY, jsonObj);

					if (throwable instanceof Exception) {
						throw (Exception) throwable;
					}

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(batchExceptionHandler)
				.build();
	}

	@Bean
	public Job deliveryTrackingSendBatchJob(JobBuilderFactory jobBuilderFactory, Step deliveryTrackingSendBatchStep) throws Exception {

		return jobBuilderFactory.get("deliveryTrackingSendBatchJob")
				.start(deliveryTrackingSendBatchStep)
				.build();
	}

	@Bean
	@JobScope
	public Step deliveryTrackingSendBatchStep(StepBuilderFactory stepBuilderFactory,
			@Value("#{jobParameters[batchApiId]}") String batchApiId) throws Exception {

		return stepBuilderFactory.get("deliveryTrackingSendBatchStep")
				.tasklet((contribution, chunkContext) -> {

					DataMap dataMap = batchApiCallParamsService.getBatchApiCallParams(batchApiId);

					dataMap = sweetTrackerService.insertNonCrmCampDeliveyTracking(dataMap);
					contribution.getStepExecution().getJobExecution().getExecutionContext().put(BatchContext.RESULT_KEY, dataMap);

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(batchExceptionHandler)
				.build();
	}
}
