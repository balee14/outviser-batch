package com.enliple.outviserbatch.schedule.executional;

import javax.annotation.Resource;
import javax.sql.DataSource;

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
import org.springframework.batch.repeat.support.TaskExecutorRepeatTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.exception.batch.BatchExceptionHandler;
import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;
import com.enliple.outviserbatch.outviser.front.acct.service.AcctService;
import com.enliple.outviserbatch.schedule.executional.serivce.ScheduleExecutionalService;
import com.enliple.outviserbatch.schedule.executional.vo.ScheduleExecutionalVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ScheduleExecutionalJob {

	@Autowired
	private AcctService acctService;

	@Autowired
	private ScheduleExecutionalService scheduleExecutionalService;

	@Autowired
	private BatchExceptionHandler handler;

	@Autowired
	private SqlSession sqlSession;

	@Autowired
	private CommonErrorLogService commonErrLogService;

	@Autowired
	private DataSource dataSource;

	@Resource(name = "threadPoolTaskExecutor")
	private TaskExecutor executor;

	/*
	 * chunk : ???????????? ????????? ???????????? ??????
	 * fetch : read() ?????? pageSize ????????? ???????????? ????????????, ?????? ????????? ???????????? ??????????????? fetchSize ?????? ???????????? write() ??????
	 * 
	 * chunk, fetch ???????????? ???????????? ???
	 * 
	 * [??????]
	 * https://jojoldu.tistory.com/331
	 */
	private final int size = 1;

	@Value("${execution.thread.limit:0}")
	private int threadPoolSize;

	@Bean
	public Job executionalScheduleJob(JobBuilderFactory jobFactory, Step executionalScheduleStep) throws Exception {

		return jobFactory.get("executionalScheduleJob")
				.start(executionalScheduleStep)
				.build();
	}

	@Bean
	public Step executionalScheduleStep(StepBuilderFactory stepFactory) throws Exception {

		if (threadPoolSize <= 0) {
			threadPoolSize = TaskExecutorRepeatTemplate.DEFAULT_THROTTLE_LIMIT;
		}

		return stepFactory.get("executionalScheduleStep")
				.<ScheduleExecutionalVo, DataMap>chunk(size)
				.reader(executionalRead())
				.processor(executionalProcessor())
				.writer(executionalWrite())
				.taskExecutor(executor)
				.throttleLimit(threadPoolSize)
				.exceptionHandler(handler)
				.build();
	}

	private SynchronizedItemStreamReader<ScheduleExecutionalVo> executionalRead() {

		JdbcCursorItemReader<ScheduleExecutionalVo> reader = new JdbcCursorItemReaderBuilder<ScheduleExecutionalVo>()
				.fetchSize(size)
				.dataSource(dataSource)
				.rowMapper(new BeanPropertyRowMapper<>(ScheduleExecutionalVo.class))
				.sql(getSelectExecutionalQuery())
				.name("executionalRead")
				.build();

		return new SynchronizedItemStreamReaderBuilder<ScheduleExecutionalVo>()
				.delegate(reader)
				.build();
	}

	/**
	 * VO -> DataMap ?????? ???????????? ?????? ??????
	 * <br> read ?????? resultSet ??? DataMap ?????? ????????? ??? ????????????...
	 * <br> null ??? ????????? ??????, write ???????????? ??????
	 * 
	 * @return
	 */
	private ItemProcessor<ScheduleExecutionalVo, DataMap> executionalProcessor() {

		return item -> {

			if (item == null || item.getEXE_ROWID() <= 0) {
				throw new CommonException("executional rowid is null");
			}

			DataMap output = new DataMap();
			output.put("EXE_ROWID", item.getEXE_ROWID());
			output.put("CAMP_ROWID", item.getCAMP_ROWID());
			output.put("ACCT_ROWID", item.getACCT_ROWID());
			output.put("CAMP_NAME", item.getCAMP_NAME());
			output.put("TRAN_DATE", item.getTRAN_DATE());
			return output;
		};
		
	}

	/**
	 * Business logic
	 * 
	 * @return
	 */
	private ItemWriter<DataMap> executionalWrite() {

		return list -> {
			// chunk ??? fetch ???????????? 1??? ????????? ?????? list ?????? 1?????? ???????????? ?????????
			for (DataMap item : list) {
				try {
					DataMap userInfo = acctService.selectUserInfo(item);

					if (userInfo == null) {
						log.warn("Executional row -> {}", item);
						continue;
					}

					item.put("sessionUserRowId", userInfo.get("adverId"));
					item.put("sessionUserId", userInfo.get("acctLoginId"));
					item.put("sessionAdverId", userInfo.get("rowid"));

					scheduleExecutionalService.runExecutional(item);
				} catch (Exception e) {
					commonErrLogService.insertErrorLog(e);
				}
			}
		};
	}

	private String getSelectExecutionalQuery() {
		String mapperName = "com.enliple.outviserbatch.quartz.executional.mapper.ScheduleExecutionalMapper.selectExecutional";

		BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(mapperName).getBoundSql(null);
		return boundSql.getSql();
	}
}
