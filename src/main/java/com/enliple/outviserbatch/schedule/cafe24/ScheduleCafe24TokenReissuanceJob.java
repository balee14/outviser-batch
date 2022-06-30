package com.enliple.outviserbatch.schedule.cafe24;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.outviser.front.report.crm.mapper.ReportCrmMapper;
import com.enliple.outviserbatch.schedule.cafe24.mapper.ScheduleCafe24TokenReissuanceMapper;
import com.enliple.outviserbatch.schedule.cafe24.service.ScheduleCafe24TokenReissuanceService;
import com.enliple.outviserbatch.schedule.cafe24.vo.ScheduleCafe24TokenReissuanceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Slf4j
@Configuration
public class ScheduleCafe24TokenReissuanceJob {

	private final int size = 1;

	@Autowired
	private ScheduleCafe24TokenReissuanceMapper scheduleCafe24TokenReissuanceMapper;

	@Autowired
	private BatchExceptionHandler handler;

	@Autowired
	private ScheduleCafe24TokenReissuanceService scheduleCafe24TokenReissuanceService;

	@Bean
	public Job cafe24TokenReissuanceScheduleJob(JobBuilderFactory jobFactory, Step cafe24TokenReissuanceScheduleStep) throws Exception {

		return jobFactory.get("cafe24TokenReissuanceScheduleJob")
				.start(cafe24TokenReissuanceScheduleStep)
				.build();
	}

	@Bean
	public Step cafe24TokenReissuanceScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		return stepFactory.get("cafe24TokenReissuanceScheduleStep")
				.tasklet((contribution, chunkContext) -> {

					try {
						List<DataMap> refrashTargetList = scheduleCafe24TokenReissuanceMapper.selectTokenExpirationImminentList();
						if(refrashTargetList.size() > 0){
							scheduleCafe24TokenReissuanceService.getAccessTokenUsingRefreshToken(refrashTargetList);
						}

					} catch (Exception e) {
					}

					return RepeatStatus.FINISHED;
				})
				.exceptionHandler(handler)
				.build();

	}

}
